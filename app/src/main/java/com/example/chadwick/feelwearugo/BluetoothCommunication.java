package com.example.chadwick.feelwearugo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Created by Photonovation on 08/08/2016.
 */
public class BluetoothCommunication {

    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    private static final UUID MY_UUID = UUID.fromString("b0014657-2da3-4933-a4cf-039409988135");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    private boolean mAllowInsecureConnections;

    // Constants that indicate the current connection state
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote device

    // Name for the SDP record when creating server socket
    private static final String NAME = "StreetConnect";


    public BluetoothCommunication(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;

    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
//
//        // Give the new state to the Handler so the UI Activity can update
//        mHandler.obtainMessage(BlueTerm.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectedThread.start();
        ;
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        if (D)
            Log.d(TAG, "connected, Socket Type: " + socketType);

        // Cancel any thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectedThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the acceptThread to connect to only one
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("Connected", device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "Stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        // Create Temp Object
        ConnectedThread r;
        // Synchronize a copy of fthe ConnectThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("Toast", "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    private void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("Toast", "Device connection lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void sendMessge(String message){
     // Check if connected before trying anything
        if(this.getState() != BluetoothCommunication.STATE_CONNECTED){
            Log.w(TAG,"Bluetooth is not connected");

            //check if there is something to say
            if(message.length()>0){
                char EOT = (char) 3;
                byte[] send = (message + EOT).getBytes();
                this.write(send);
            }

        }
    }

    public void  connectDevice(String deviceName){
        String address = null;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        for(BluetoothDevice device: adapter.getBondedDevices()){
            if(device.getName().equals(deviceName))
                address = device.getAddress();
        }

        try{
            BluetoothDevice device = adapter.getRemoteDevice(address); // get the bluetooth device object
            this.connect(device); // attempt to connect to the device
        }catch(Exception e){
            Log.e("Unable to connect to "+ address, e.getMessage());

        }
    }



//-------------------------------------- SUB-CLASSES ----------------------------------------------

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mmSocketType;

        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        public AcceptThread() {
            BluetoothServerSocket tmpSocket = null;
            try {
                tmpSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "SocketType: " + mmSocketType + " Listen() failed ", e);
            }
            mmServerSocket = tmpSocket;
        }

        public void run() {
            if (D)
                Log.d(TAG, "SocketType: " + mmSocketType + " Begin mAcceptedThread " + this);
            setName("AcceptThread" + mmSocketType);

            BluetoothSocket socket = null;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "SocketType: " + mmSocketType + " accept() failed ", e);
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothCommunication.this) {
                        switch (mState) {
                            case STATE_LISTEN: {
                                break;
                            }
                            case STATE_CONNECTING:
                                break;
                            case STATE_NONE:
                                break;
                            case STATE_CONNECTED: {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;

                            }
                        }
                    }
                }
            }
            if (D)
                Log.i(TAG, "END mAcceptThread, socket Type: " + mmSocketType);

        }

        public void cancel() {
            if (D)
                Log.d(TAG, "Socket Type" + mmSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mmSocketType + "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmpSocket = null;
            try {
                if (mAllowInsecureConnections) {
                    Method method;

                    method = device.getClass().getMethod("createRfCommSocket", new Class[]{int.class});
                    tmpSocket = (BluetoothSocket) method.invoke(device, 1);
                } else {
                    tmpSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

                }


            } catch (Exception e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmpSocket;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" +mSocketType);
            mAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG,"Unable to connect socket ",e);

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType
                            + " socket during connection failure", e2);

                }
                connectionFailed();
                return;
            }
            synchronized (BluetoothCommunication.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice,mSocketType);

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String mSocketType;

        private ConnectedThread(BluetoothSocket socket, String socketType) {
            mmSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            Log.d(TAG, "create ConnectedThread: " + socketType);
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tempIn;
            mmOutStream = tempOut;
        }

        public void run() {

            Log.i(TAG, "BEGIN mConnectedThread SocketType");
            setName("ConnectThread" + mSocketType);


            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "message bytes " + bytes);
                    Log.d(TAG, "message string bytes " + String.valueOf(bytes));
                    Log.d(TAG, "message buffer " + new String(buffer));
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    BluetoothCommunication.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
