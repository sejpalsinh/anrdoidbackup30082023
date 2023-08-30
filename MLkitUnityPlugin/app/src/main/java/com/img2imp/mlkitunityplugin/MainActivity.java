package com.img2imp.mlkitunityplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.img2imp.mlkithelper.MlkitInstance;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    TextView statusTv,countTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTv = findViewById(R.id.status_tv);
        countTv = findViewById(R.id.counter_tv);
        MlkitInstance.ActivatePlugin();
    }

    public byte[] imageFileToByte(File file){
        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        return baos.toByteArray();
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 1001);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1001 ) {
                Uri selectedImageUri = data.getData();
                System.out.println("PPP "+selectedImageUri.getPath());
                MlkitInstance.DetectPose(imageFileToByte(new File(selectedImageUri.getPath())));
            }
        }
    }

    public void DetectPose(View view) {
        //imageChooser();
        MlkitInstance.DetectPose(imageFileToByte(new File("/storage/emulated/0/DCIM/Camera/IMG_20211215_154324.jpg")));
    }

    @SuppressLint("SetTextI18n")
    public void CheckStatus(View view) {
        statusTv.setText(MlkitInstance.GetPoseCounter()+" "+MlkitInstance.GetErrorMsg());
    }
}