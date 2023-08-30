package com.i2i.img2impclock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //dottask();
        startActivity(new Intent(getApplicationContext(),Alltask.class));
    }

    @SuppressLint("ShowToast")
    void dottask()
    {
        Btfiles allBtFunctions = new Btfiles("Img2imp Clock");
        if(allBtFunctions.isPaired())
        {
            if(allBtFunctions.ConnectBt())
            {
                allBtFunctions.sendString("Thanks For Connecting");
                startActivity(new Intent(getApplicationContext(),Alltask.class));
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Please Be near to Clock and make sure Clock is on.",Toast.LENGTH_SHORT);
                System.out.println("no connection");
            }
        }
        else {
            Toast.makeText(getApplicationContext(),"Please Pair Bluetooth with Clock",Toast.LENGTH_SHORT);
            System.out.println("Plese Connect Bluetooth device");
            startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            finish();
        }
    }
}

