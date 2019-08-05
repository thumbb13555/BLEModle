package com.example.bleconnected01;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDisplayActivity extends Activity {
    private final static String TAG = DataDisplayActivity.class.getSimpleName();

    private String PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK;
    private String Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9;

    String DeviceName, DeviceAddress;
    private boolean mConnected = true;
    private BluetoothLeService mBluetoothLeService;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        getData();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        getActionBar().setTitle(DeviceName);


    }//onCreate

    private void getData() {
        Intent intent = this.getIntent();
        DeviceName = intent.getStringExtra("DeviceName");
        DeviceAddress = intent.getStringExtra("DeviceAddress");
        Name1 = intent.getStringExtra("PV1");
        PV1 = intent.getStringExtra("PV1_Value");
        Name2 = intent.getStringExtra("PV2");
        PV2 = intent.getStringExtra("PV2_Value");
        Name3 = intent.getStringExtra("EH1");
        EH1 = intent.getStringExtra("EH1_Value");
        Name4 = intent.getStringExtra("EH2");
        EH2 = intent.getStringExtra("EH2_Value");
        Name5 = intent.getStringExtra("EL1");
        EL1 = intent.getStringExtra("EL1_Value");
        Name6 = intent.getStringExtra("EL2");
        EL2 = intent.getStringExtra("EL2_Value");
        Name7 = intent.getStringExtra("CR1");
        CR1 = intent.getStringExtra("CR1_Value");
        Name8 = intent.getStringExtra("CR2");
        CR2 = intent.getStringExtra("CR2_Value");
        Name9 = intent.getStringExtra("SPK");
        SPK = intent.getStringExtra("SPK_Value");
        String[][] data = {
                {"裝置名稱", DeviceName},
                {Name1, PV1},
                {Name2, PV2},
                {Name3, EH1},
                {Name4, EH2},
                {Name5, EL1},
                {Name6, EL2},
                {Name7, CR1},
                {Name8, CR2},
                {Name9, SPK},
        };
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < data.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("level", data[i][0]);
            item.put("name", data[i][1]);
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                items,
                R.layout.style_listview,
                new String[]{"level", "name"},
                new int[]{R.id.TitleName, R.id.ResultValue}
        );
        ListView listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {

            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;

    }//OpMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(DeviceAddress);
                Toast.makeText(getBaseContext(), "連接裝置", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                Toast.makeText(getBaseContext(), "斷開裝置，請重新連接裝置 ", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }//onOptionsSelected

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mConnected = true;//這邊表示訊息正在傳遞中，有傳遞就表示連線中
                invalidateOptionsMenu();
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(DeviceAddress);
            Log.v("BT", "連接情況: " + result);
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(DeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService.connect(DeviceAddress);
        }
    };//ServiceConnection

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);//連接一個GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);//從GATT服務中斷開連接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);//查找GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收?)數據
        return intentFilter;
    }
}
