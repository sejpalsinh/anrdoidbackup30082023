package com.img2imp.mlkithelper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

public class MlkitInstance {

    private static int poseCounter = 0;
    private static String detectionError = "This is Error msg";

    public static int GetPoseCounter(){ return poseCounter; }
    public static String GetErrorMsg(){ return detectionError; }

    private static PoseDetector poseDetector;
    public static void ActivatePlugin(){
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);
    }

    public static int Add(int i, int j){
        detectionError = "in Add";
        return i+j;
    }

    private static Activity unityActivity;
    public static void SetActivity(Activity uA){
        unityActivity = uA;
    }

    public static void DetectPose(byte[] byteArray) {
        detectionError = "in DetectPose";
        detectionError = byteArray.length+"";
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapOptions);
        detectionError = "bmp done";
        try{
            InputImage image = InputImage.fromBitmap(bmp,0);
            detectionError = "image done";
            Task<Pose> result = poseDetector.process(image).addOnSuccessListener( pose -> {
                poseCounter = pose.getAllPoseLandmarks().size();
                System.out.println("MlkitInstance MlkitInstance "+poseCounter);
            }).addOnFailureListener( e -> {
                detectionError = e.getMessage();
            });
        }
        catch (Exception e){
            detectionError = e.getMessage();
        }
    }

}
