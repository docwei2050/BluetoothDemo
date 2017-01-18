package com.example.tobo.bluetoothp2pdemo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tobo.bluetoothp2pdemo.R;
import com.example.tobo.bluetoothp2pdemo.adapter.DeviceListAdapter;

import java.util.Set;

/**
 * Created by tobo on 17/1/17.
 */
public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mLv_device;
    private MenuItem mTv_search;
    private DeviceListAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicelist);
        initData();
    }

    private void initData() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLv_device = (ListView) findViewById(R.id.lv_device);
        mDeviceListAdapter = new DeviceListAdapter(this);
        mLv_device.setAdapter(mDeviceListAdapter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //先把已经配对的放入到集合中显示


        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices != null && devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                mDeviceListAdapter.add(device);
            }
        }

        //开始搜索
        mBluetoothAdapter.startDiscovery();


        //广播监听==搜索
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);

        mLv_device.setOnItemClickListener(this);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    //开始搜索---标题修改为取消
                    if(mTv_search!=null){
                        mTv_search.setTitle("取消");
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //搜索完成
                    if(mTv_search!=null){
                        mTv_search.setTitle("重新搜索");
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //搜索到可以连接的设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && !mDeviceListAdapter.getBluetoothDeviceList().contains(device)) {
                        mDeviceListAdapter.add(device);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        mTv_search = menu.findItem(R.id.search);
        mTv_search.setTitle("取消");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            //默认就是取消----正在搜索
            //如果是搜索中---取消搜索
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            } else {
                mDeviceListAdapter.clear();

                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                if (devices != null && devices.size() > 0) {
                    for (BluetoothDevice device : devices) {
                        mDeviceListAdapter.add(device);
                    }
                }

                mBluetoothAdapter.startDiscovery();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("device_address",mDeviceListAdapter.getAddress(position));
        setResult(RESULT_OK,intent);
        finish();
    }
}
