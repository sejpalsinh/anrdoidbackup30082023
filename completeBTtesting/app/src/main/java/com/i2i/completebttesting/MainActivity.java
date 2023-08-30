package com.i2i.completebttesting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    String btname = "Img2imp Clock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dottask();
    }

    void dottask()
    {
        AllBtFunctions allBtFunctions = new AllBtFunctions(btname);
        if(allBtFunctions.isPaired())
        {
            if(allBtFunctions.ConnectBt())
            {
                allBtFunctions.sendString("Thanks For Connecting");
            }
        }
        else {
            System.out.println("Plese Connect Bluetooth device");
            startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
        }
    }

}
