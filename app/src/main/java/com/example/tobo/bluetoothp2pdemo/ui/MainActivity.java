package com.example.tobo.bluetoothp2pdemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tobo.bluetoothp2pdemo.R;
import com.example.tobo.bluetoothp2pdemo.adapter.ChatListAdapter;
import com.example.tobo.bluetoothp2pdemo.common.Constants;
import com.example.tobo.bluetoothp2pdemo.model.Message;



public class MainActivity extends AppCompatActivity  {

    private static final int REQUEST_ENABLE = 100;
    private static final int REQUEST_DISCOVERABLE = 200;
    private static final int GET_DEVICE = 300;
    private ListView mLv_chat;
    private EditText mEt_content;
    private Button mBtn_send;
    private ChatListAdapter mMsgAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
              switch (msg.what){
                  case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1){
                            case ConnectManager.STATE_NONE:
                            case ConnectManager.STATE_LISTEN:
                                //启动连接是代表主动连接
                                if(mTv_connect!=null) {
                                    mTv_connect.setTitle("启动连接");
                                }
                                break;
                            case ConnectManager.STATE_CONNECTING:
                                if(mTv_connect!=null) {
                                    mTv_connect.setTitle("取消连接");
                                }
                                break;
                            case ConnectManager.STATE_CONNECTED:
                                mEt_content.setEnabled(true);
                                mBtn_send.setEnabled(true);

                                if(mTv_connect!=null) {
                                    mTv_connect.setTitle("断开连接");
                                }
                                break;
                        }
                      break;
                  case Constants.MESSAGE_WRITE:
                      byte[] bys= (byte[]) msg.obj;
                      Message sendStr=new Message(new String(bys),1);
                      mMsgAdapter.add(sendStr);
                      mLv_chat.setSelection(mMsgAdapter.getCount()-1);
                      break;
                  case Constants.MESSAGE_READ:
                      byte[] buffer= (byte[]) msg.obj;
                      Message receiveStr=new Message(new String(buffer),2);
                      mMsgAdapter.add(receiveStr);
                      mLv_chat.setSelection(mMsgAdapter.getCount()-1);
                      break;
                  case Constants.MESSAGE_TOAST:
                      Toast.makeText(MainActivity.this,(String)(msg.obj),Toast.LENGTH_SHORT).show();
                      break;

              }

        }
    };
    private ConnectManager mConnectManager;
    private MenuItem mTv_connect;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
          Toast.makeText(this, "没有蓝牙模块啊", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            //开启蓝牙先
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //是为了防止用户点击拒绝开启蓝牙，拒绝开启就退出这个app
            startActivityForResult(intent,REQUEST_ENABLE);
        }

        if(mBluetoothAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //这里使用0表示一直可以被发现
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
        }


        mLv_chat = (ListView) findViewById(R.id.lv_chat);
        mEt_content = (EditText) findViewById(R.id.et_content);
        mBtn_send = (Button) findViewById(R.id.btn_send);
        mMsgAdapter = new ChatListAdapter(this);
        mLv_chat.setAdapter(mMsgAdapter);


        mConnectManager = new ConnectManager(mHandler);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mEt_content.setEnabled(false);
        mBtn_send.setEnabled(false);
        mEt_content.setFocusable(false);
        mEt_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);//需要手动才能获得焦点
                view.requestFocus();
            }
        });

        mBtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String sendStr=mEt_content.getText().toString();
                mConnectManager.write(sendStr.getBytes());
                mEt_content.setText("");

                if(mInputMethodManager.isActive()){
                    mInputMethodManager.hideSoftInputFromWindow(mBtn_send.getWindowToken(),0);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mConnectManager!=null&&mConnectManager.getState()==ConnectManager.STATE_NONE){
            mConnectManager.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect,menu);
        mTv_connect = menu.findItem(R.id.connect);
        mTv_connect.setTitle("启动连接");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.connect:
                //如果title是启动连接，那么就页面跳转---未连接
                switch (mConnectManager.getState()){
                    case ConnectManager.STATE_NONE:
                    case ConnectManager.STATE_LISTEN:
                        mMsgAdapter.clear();
                        Intent intent = new Intent(this, DeviceListActivity.class);
                        startActivityForResult(intent, GET_DEVICE);
                        break;
                    case ConnectManager.STATE_CONNECTING:
                        mConnectManager.cancelConnectting();

                        break;
                    case ConnectManager.STATE_CONNECTED:
                       mConnectManager.stop();
                        break;
                }
                return true;
            case R.id.about:
                Toast.makeText(this,"第一个版本",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE:
                if(resultCode!=RESULT_OK){
                    Toast.makeText(this,"必须开启蓝牙才能玩此app",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            case REQUEST_DISCOVERABLE:
                if(resultCode!=RESULT_OK){
                    Toast.makeText(this,"必须开启蓝牙才能玩此app",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            case GET_DEVICE:
                if(resultCode==RESULT_OK){
                    String device_address=data.getStringExtra("device_address");
                    BluetoothDevice device= mBluetoothAdapter.getRemoteDevice(device_address);
                     mConnectManager.connectting(device);
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectManager.stop();
    }
}
