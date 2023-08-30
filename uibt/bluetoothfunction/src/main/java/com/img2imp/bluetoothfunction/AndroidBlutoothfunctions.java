package com.img2imp.bluetoothfunction;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class AndroidBlutoothfunctions {
    public static final AndroidBlutoothfunctions btInstance = new AndroidBlutoothfunctions();
    private static final String LOGTAG = "ANDROIDBLUETOOTH";
    public static AndroidBlutoothfunctions getInstance() {
        return btInstance;
    }
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;
    private byte[] buffer = new byte[1024];
    private int bytes;
    public boolean isSetUpDone;
    public boolean isConnected;
    public boolean isNewMsg;
    public String newDataString = " ";

    public void setUpAndroidBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isSetUpDone = true;
        if(!mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.enable();
        }
    }

    public String getPairedDevice() {
        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        String pairedDeviceNames = "";
        if (pairedDevices.size() > 0) {
            for (Object devic : pairedDevices) {
                BluetoothDevice device = (BluetoothDevice) devic;
                pairedDeviceNames = pairedDeviceNames + "," + device.getName();
            }
        }
        return pairedDeviceNames;
    }

    public boolean makeBluetoothConnectio(String bt_name) // function for setup the bluetooth
    {
        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (Object devic : pairedDevices) {
                BluetoothDevice device = (BluetoothDevice) devic;
                if (device.getName().equals(bt_name)) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = device;
                    break;
                }
            }
        } else {
            Log.i(LOGTAG, "Not in Paired List makeBluetoothConnectio");
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        BluetoothSocket mmSocket = null;
        try {
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);  //createRfcommSocketToServiceRecord
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
        } catch (Exception e) {
            Log.i(LOGTAG, "Error while making connection makeBluetoothConnectio E : " + e);
            isConnected = false;
            return false;
        }
        isConnected = true;
        return true;
    }

    public boolean sendData(String data) {
        if (isConnected) {
            try {
                mmOutputStream.write(data.getBytes());
            } catch (IOException e) {
                Log.i(LOGTAG, "Error while Sending Data E : " + e);
                return false;
            }
            return true;
        }
        return false;
    }


    public boolean isDevicePaired(String btname)
    {
        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (Object devic : pairedDevices) {
                BluetoothDevice device = (BluetoothDevice) devic;
                if(device.getName().equals(btname))
                {
                    return true;
                }
            }
        }
        return false;
    }
    public void startBluetoothReading()
    {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                while (true)
                {
                    try {
                        if(mmInputStream.available()>0)
                        {
                            while(newDataString.charAt(newDataString.length()-1)!='\n')
                            {
                                bytes = mmInputStream.read(buffer);
                                if(bytes>0)
                                {
                                    newDataString = newDataString +new String(buffer, 0, bytes);
                                }
                            }
                            Log.i(LOGTAG, "Send data is : "+newDataString);
                            isNewMsg = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

}
