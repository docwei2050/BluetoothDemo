package com.example.tobo.bluetoothp2pdemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.tobo.bluetoothp2pdemo.common.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by tobo on 17/1/17.
 */

public class ConnectManager {
    private static final UUID uuid=UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private BluetoothAdapter mAdapter;
    private int mState;
    private Handler mHandler;
    
    public static final int STATE_NONE=0;  //未做任何处理
    public static final int STATE_LISTEN=1;  //正在监听
    public static final int STATE_CONNECTING=2;//正在连接
    public static final int STATE_CONNECTED=3;  //已经连接

    public ConnectManager(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }
    public synchronized void setState(int state){
        mState=state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE,state,-1).sendToTarget();
    }
    public  synchronized int getState(){
        return mState;
    }
    private AcceptThread mAcceptThread;
    private ConnecttingThread mConnecttingThread;
    private ConnectedThread mConnectedThread;
    public synchronized void start(){

        //已经连接的线程或者正在连接的线程应该取消掉
        if(mConnecttingThread!=null){
            mConnecttingThread.cancel();
            mConnecttingThread=null;
        }
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }

        //设置状态---为监听
        setState(STATE_LISTEN);

        //监听的线程开始
        if(mAcceptThread==null){
            mAcceptThread=new AcceptThread();
            mAcceptThread.start();
        }
    }
    //默认就用安全连接，不用不安全的，，谷歌demo很全可以参考
    public synchronized void connectting(BluetoothDevice device){
        //正在连接的线程取消掉
        //已经连接的线程也取消掉
        //重新开启连接线程
        //已经连接的线程或者正在连接的线程应该取消掉
        if(mConnecttingThread!=null){
            mConnecttingThread.cancel();
            mConnecttingThread=null;
        }
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
        if(mConnecttingThread==null){
            mConnecttingThread=new ConnecttingThread(device);
            mConnecttingThread.start();
        }

        //设置状态为正在连接
        setState(STATE_CONNECTING);
    }
    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device){
        //正在连接的线程取消
        //已经连接的线程取消
        //监听的线程取消
        if(mConnecttingThread!=null){
            mConnecttingThread.cancel();
            mConnecttingThread=null;
        }
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
        if(mAcceptThread!=null){
            mAcceptThread.cancel();
            mAcceptThread=null;
        }


        //已经连接的线程开启
        if(mConnectedThread==null){
            mConnectedThread=new ConnectedThread(socket);
            mConnectedThread.start();
        }
        
        //设置状态为已经连接
        setState(STATE_CONNECTED);
    }
    public synchronized void stop(){
        //所有的线程都取消
        if(mConnecttingThread!=null){
            mConnecttingThread.cancel();
            mConnecttingThread=null;
        }
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
        if(mAcceptThread!=null){
            mAcceptThread.cancel();
            mAcceptThread=null;
        }


        //置空状态为最初
        setState(STATE_NONE);
    }
    public void write(byte[] out){
        //创建临时对象执行写的操作-----为什么呢
       ConnectedThread r;
        synchronized (this){
            if(mState!=STATE_CONNECTED) return;
            r=mConnectedThread;
        }
        r.write(out);
    }
    public void cancelConnectting(){
        if(mConnecttingThread!=null){
            mConnecttingThread.cancel();
            mConnecttingThread=null;
        }
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
    }



    private void connectionFailed(){
        android.os.Message msg=mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        String tip="不能连接设备";
        msg.obj=tip;
        mHandler.sendMessage(msg);
        start();
    }
    private void connctionLost(){
        android.os.Message msg=mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        String tip="设备连接丢失";
        msg.obj=tip;
        mHandler.sendMessage(msg);
        start();
    }
    private class AcceptThread extends Thread{
        private BluetoothServerSocket mBluetoothServerSocket;
        public AcceptThread(){
            try {
                mBluetoothServerSocket=mAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat",uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            //假如没有连接上，就监听
            while(mState!=STATE_CONNECTED){
                //阻塞的方法
                BluetoothSocket socket=null;
                try {
                    socket=mBluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //如果获取到一个连接
                if(socket!=null){
                    synchronized (ConnectManager.this){
                        switch (mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //走的是被动连接，不需要socket.connect()方法了
                                connected(socket,socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //已经在连接了，就不需要新的连接,那就关闭吧
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            }
        }
        public void cancel(){
            try {
                mBluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ConnecttingThread extends Thread{
        private BluetoothSocket mBluetoothSocket;
        private BluetoothDevice mBluetoothDevice;
        public ConnecttingThread(BluetoothDevice device){
            mBluetoothDevice=device;
            try {
                mBluetoothSocket=device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            //正在连接的话，停止搜索，因为搜索会减慢连接的速度
            mAdapter.cancelDiscovery();
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;
            }
            //重置正在连接的线程，因为我们已经做完了------好习惯
            synchronized (ConnectManager.this){
                mConnecttingThread=null;
            }
            //开启连接的线程
            connected(mBluetoothSocket,mBluetoothDevice);
        }
        public void cancel(){
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ConnectedThread extends Thread{
        private BluetoothSocket mBluetoothSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        public ConnectedThread(BluetoothSocket socket) {
            mBluetoothSocket = socket;

            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            public void run(){
                byte[] bys=new byte[1024];
                int len;
                while(mState==STATE_CONNECTED){
                    try {
                        len=mInputStream.read(bys);
                        mHandler.obtainMessage(Constants.MESSAGE_READ, len, -1, bys).sendToTarget();

                    } catch (IOException e) {
                        e.printStackTrace();
                        connctionLost();
                    }
                   
                }
            }
        public void write(byte[] bys){
            try {
                mOutputStream.write(bys);
                mHandler.obtainMessage(Constants.MESSAGE_WRITE,-1,-1,bys).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
    }

