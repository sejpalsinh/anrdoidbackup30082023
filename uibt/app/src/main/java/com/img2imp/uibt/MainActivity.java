package com.img2imp.uibt;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.img2imp.bluetoothfunction.AndroidBlutoothfunctions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText btName,btData;
    TextView recivedData;
    AndroidBlutoothfunctions myBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myBt = AndroidBlutoothfunctions.getInstance();
        btName = findViewById(R.id.btName);
        btData  = findViewById(R.id.sendMsg);
        recivedData = findViewById(R.id.recivedData);
        myBt.setUpAndroidBluetooth();
        System.out.println(myBt.getPairedDevice());
        Log.i("MainScreen", "MainScreen");
        startBtStrean();
        myBt.newDataString = " ";
    }

    public void connectToBluetooth(View view) {
        if(myBt.isDevicePaired("tony"))
        {
            if(myBt.makeBluetoothConnectio("tony"))
            {
                recivedData.setText("Connected");
                myBt.startBluetoothReading();
            }
            else
            {
                recivedData.setText("Error in Connection");
            }
        }
        else
        {
            recivedData.setText("Please Paire your bluetooth device");
        }
    }

    public void sendData(View view) {
        if(myBt.isConnected)
        {
            if(myBt.sendData(btData.getText().toString()))
            {
                recivedData.setText("Data sent");
            }
            else
            {
                recivedData.setText("Error in data sending");
            }
        }
        else
        {
            recivedData.setText("device is not connected");
        }
    }

    public void startBtStrean()
    {
        Log.i("LOGTAG", "thread started");
        final String[] myData = {" "};
        final Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        sleep(100);
                        if(myBt.isNewMsg)
                        {
                            recivedData.setText(myBt.newDataString.substring(0,myBt.newDataString.length()-1));
                            myBt.newDataString = " ";
                            myBt.isNewMsg = false;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }
}
