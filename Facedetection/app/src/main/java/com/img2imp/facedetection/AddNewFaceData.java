package com.img2imp.facedetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddNewFaceData extends AppCompatActivity {

    PreviewView previewView;
    EditText facename_edt;
    TextView read_text_tv;

    TextToSpeech mtts;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    private static final int CAMERA_REQUEST = 1888;
    int cam_face = 2; //Default Back Camera
    FaceDetector detector;
    boolean start = true, flipX = false;
    TextRecognizer recognizer;

    Interpreter tfLite;
    int[] intValues;
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model

    boolean isRead = false, isAdd = false, isRecognition = false;
    String readedText = "";

    String modelFile = "mobile_face_net.tflite"; //model name

    // Temp saved
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_face_data);
        registered = readFromSP();
        // Init view components
        previewView = findViewById(R.id.camera_view);
        facename_edt = findViewById(R.id.facename_edt);
        read_text_tv = findViewById(R.id.read_text_tv);
        flipX = true;
        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        mtts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mtts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    read_text_tv.setText("Language is not Supported");
                }
            } else {
                read_text_tv.setText("mtts Initialization failed");
            }
        });
        // Functions on screen load
        CameraManager manager = (CameraManager)getSystemService(CAMERA_SERVICE);
        try {
            StringBuilder tempCamList = new StringBuilder();
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
                // Do something with the characteristics
                int deviceLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                tempCamList.append("\n").append(cameraId).append(" level:").append(deviceLevel);
            }
            tempCamList.append(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL));
            read_text_tv.setText(tempCamList);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
        cameraBind();
    }

    // Bind camera with view to set in preview
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        // With out operations
        //cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        // ImageAnalysis use for Analyse the frame from camera
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();
        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            InputImage image = null;
            @SuppressLint("UnsafeExperimentalUsageError")
            // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            }
            //Process acquired image to detect faces
            if (isRecognition || isAdd) {
                Task<List<Face>> result = detector.process(image).addOnSuccessListener(faces -> {
                    System.out.println("isRecognition faces.size() " + faces.size());
                    if (faces.size() != 0) {
                        System.out.println("isRecognition faces.size() != 0");
                        Face face = faces.get(0); //Get first face from detected faces
                        System.out.println(face);
                        //mediaImage to Bitmap
                        Bitmap frame_bmp = ImageHelper.toBitmap(mediaImage);
                        int rot = imageProxy.getImageInfo().getRotationDegrees();
                        System.out.println("RRRR "+rot);
                        //Adjust orientation of Face
                        Bitmap frame_bmp1 = ImageHelper.rotateBitmap(frame_bmp, rot, false, false);
                        //Get bounding box of face
                        RectF boundingBox = new RectF(face.getBoundingBox());
                        //Crop out bounding box from whole Bitmap(image)
                        Bitmap cropped_face = ImageHelper.getCropBitmapByCPU(frame_bmp1, boundingBox);
                        if (flipX) {
                            cropped_face = ImageHelper.rotateBitmap(cropped_face, 0, flipX, false);
                        }
                        //Scale the acquired Face to 112*112 which is required input for model
                        Bitmap scaled = ImageHelper.getResizedBitmap(cropped_face, 112, 112);
                        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);
                        imgData.order(ByteOrder.nativeOrder());
                        intValues = new int[inputSize * inputSize];
                        scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, scaled.getWidth(), scaled.getHeight());
                        imgData.rewind();
                        for (int i = 0; i < inputSize; ++i) {
                            for (int j = 0; j < inputSize; ++j) {
                                int pixelValue = intValues[i * inputSize + j];
                                if (isModelQuantized) {
                                    // Quantized model
                                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                                    imgData.put((byte) (pixelValue & 0xFF));
                                } else { // Float model
                                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                }
                            }
                        }
                        //imgData is input to our model
                        Object[] inputArray = {imgData};
                        Map<Integer, Object> outputMap = new HashMap<>();
                        //output of model will be stored in this variable
                        embeedings = new float[1][OUTPUT_SIZE];
                        outputMap.put(0, embeedings);
                        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
                        System.out.println("isRecognition DoOperations called");
                        DoOperations();
                        try {
                            Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        read_text_tv.setText("No face detected");
                        isAdd = false;
                        isRecognition = false;
                    }
                }).addOnFailureListener(e -> {
                    // Task failed with an exception
                    // ...
                    System.out.println("onFailure onFailure" + e.toString());
                    isAdd = false;
                    isRecognition = false;
                }).addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Face>> task) {
                        imageProxy.close(); //v.important to acquire next frame for analysis
                    }
                });
            } else if (isRead) {
                Task<Text> result = recognizer.process(image).addOnSuccessListener(visionText -> {
                    // Task completed successfully
                    readedText = visionText.getText();
                    DoOperations();
                }).addOnFailureListener(e -> {
                    read_text_tv.setText("Error while reading text");
                    isRead = false;
                    // Task failed with an exception
                }).addOnCompleteListener(task -> {
                    imageProxy.close();
                });
            } else {
                try {
                    Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("imageProxy close");
                imageProxy.close();
            }

        });
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

    }

    public void DoOperations() {
        System.out.println("isRecognition DoOperations DoOperations " + isRecognition + " isAdd " + isAdd + " isRead " + isRead);
        if (isAdd) {
            SimilarityClassifier.Recognition outputRec = new SimilarityClassifier.Recognition("0", "", -1f);
            outputRec.setExtra(embeedings);
            registered.put(facename_edt.getText().toString(), outputRec);
            mtts.speak(facename_edt.getText().toString() + " Image added", TextToSpeech.QUEUE_FLUSH, null);
            facename_edt.setText("");
            read_text_tv.setText("Added");
            isAdd = false;
        } else if (isRecognition) {
            float distance = Float.MAX_VALUE;
            String id = "0";
            String label = "?";
            //Compare new face with saved Faces.
            if (registered.size() > 0) {
                final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face
                if (nearest != null) {
                    final String name = nearest.first;
                    label = name;
                    distance = nearest.second;
                    if (distance < 1.000f) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    {
                        mtts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
                        read_text_tv.setText(name);
                    } else {
                        mtts.speak("Not able to Recognition", TextToSpeech.QUEUE_FLUSH, null);
                        read_text_tv.setText("Not able to Recognition");
                    }
                    System.out.println("nearest: " + name + " - distance: " + distance);
                }
            }
            isRecognition = false;
        } else if (isRead) {
            read_text_tv.setText(readedText);
            mtts.speak(readedText, TextToSpeech.QUEUE_FLUSH, null);
            isRead = false;
        }
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP() {
        SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json = sharedPreferences.getString("map", defValue);
        // System.out.println("Output json"+json.toString());
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {
        };
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());
        // System.out.println("Output map"+retrievedMap.toString());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {
            float[][] output = new float[1][OUTPUT_SIZE];
            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            arrayList = (ArrayList) arrayList.get(0);
            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
            }
            entry.getValue().setExtra(output);

            //System.out.println("Entry output "+entry.getKey()+" "+entry.getValue().getExtra() );

        }
//        System.out.println("OUTPUT"+ Arrays.deepToString(outut));
        Toast.makeText(this, "Recognitions Loaded", Toast.LENGTH_SHORT).show();
        return retrievedMap;
    }

    //Compare Faces by distance between face embeddings
    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {
            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }
        return ret;
    }

    public void AddFace(View view) {
        isAdd = true;
    }

    public void Recognize(View view) {
        isRecognition = true;
    }

    public void ReadText(View view) {
        isRead = true;
    }

    public void GetImage(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            System.out.println("hehehehe on onActivityResult");
            Bitmap rowBitMap = (Bitmap) data.getExtras().get("data");
            InputImage image = InputImage.fromBitmap(rowBitMap, 90);
            Task<List<Face>> result = detector.process(image).addOnSuccessListener(faces -> {
                if (faces.size() != 0) {
                    Face face = faces.get(0); //Get first face from detected faces
                    System.out.println("hehehehe " + face);
                    //mediaImage to Bitmap
                    //Adjust orientation of Face
                    Bitmap frame_bmp1 = ImageHelper.rotateBitmap(rowBitMap, 90, false, false);
                    //Get bounding box of face
                    RectF boundingBox = new RectF(face.getBoundingBox());
                    //Crop out bounding box from whole Bitmap(image)
                    Bitmap cropped_face = ImageHelper.getCropBitmapByCPU(frame_bmp1, boundingBox);
                    if (flipX) {
                        cropped_face = ImageHelper.rotateBitmap(cropped_face, 0, flipX, false);
                    }
                    //Scale the acquired Face to 112*112 which is required input for model
                    Bitmap scaled = ImageHelper.getResizedBitmap(cropped_face, 112, 112);
                    ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);
                    imgData.order(ByteOrder.nativeOrder());
                    intValues = new int[inputSize * inputSize];
                    scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, scaled.getWidth(), scaled.getHeight());
                    imgData.rewind();
                    for (int i = 0; i < inputSize; ++i) {
                        for (int j = 0; j < inputSize; ++j) {
                            int pixelValue = intValues[i * inputSize + j];
                            if (isModelQuantized) {
                                // Quantized model
                                imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                                imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                                imgData.put((byte) (pixelValue & 0xFF));
                            } else { // Float model
                                imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                            }
                        }
                    }
                    //imgData is input to our model
                    Object[] inputArray = {imgData};
                    Map<Integer, Object> outputMap = new HashMap<>();
                    //output of model will be stored in this variable
                    embeedings = new float[1][OUTPUT_SIZE];
                    outputMap.put(0, embeedings);
                    tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
                    System.out.println("hehehehe" + faces.size());
                } else {
                    System.out.println("else else else");
                    if (registered.isEmpty()) {
                        System.out.println("isEmpty isEmpty isEmpty");
                    } else {
                        System.out.println("locha  locha locha");
                    }
                }
            });
        }
    }
}