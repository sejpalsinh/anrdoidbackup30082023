package com.i2i.completebttesting;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

class AllBtFunctions {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;
    private Activity activity;
    private String btname;

    // Constructor For Class Activity need for brodcaster and string btname for bt connection
    AllBtFunctions(String btname)
    {
        this.btname = btname;
    }

    // If Bluetooth is off make it on
    private void makeBtON()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.enable();
        }
    }

    // Check if bluetooth is already paired with device or not
    boolean isPaired()
    {
        makeBtON();
        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(Object devic : pairedDevices)
            {
                BluetoothDevice device = (BluetoothDevice)devic;
                if(device.getName().equals(btname)) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = device;
                    System.out.println("BTBTBT : Device in Paired List ");
                    return true;
                }
                System.out.println("BTBTBT : device not in list ");
            }
        }
        else
        {
            System.out.println("BTBTBT : Not device ");
        }
        return false;
    }


    //Make connection with Bluetooth device with mmDevice witch is output of isPaired
    boolean ConnectBt()
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        BluetoothSocket mmSocket;
        try {
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);  //createRfcommSocketToServiceRecord
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            System.out.println("BTBTBT : Connection Done");
        }
        catch (Exception e)
        {
            System.out.println("BTBTBT : Error while making connection : "+e.getMessage());
            return false;
        }
        return true;
    }



    // Function For sending data
    void sendString(String btdata)
    {
        try {
            mmOutputStream.write(btdata.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("BTBTBT : Error while sending data : "+e.getMessage());
        }
    }
}
