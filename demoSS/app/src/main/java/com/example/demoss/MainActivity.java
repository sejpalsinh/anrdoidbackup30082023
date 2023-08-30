package com.example.demoss;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SCREENSHOT=59706;
    private MediaProjectionManager mgr;
    public static  boolean tak=false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        assert mgr != null;
        startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("outout");
        super.onActivityResult(requestCode, resultCode, data);
        Intent i= new Intent(this, ScreenshotService.class).putExtra(ScreenshotService.EXTRA_RESULT_CODE, resultCode).putExtra(ScreenshotService.EXTRA_RESULT_INTENT, data);
        startService(i);
    }

    public void clicked(View view) {
        tak = !tak;
    }
}
