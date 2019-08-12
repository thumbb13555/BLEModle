package com.example.bleconnected01;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDisplayActivity extends Activity {
    private final static String TAG = DataDisplayActivity.class.getSimpleName();

    private String PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK,IH1,IL1,IH2,IL2,DP1,DP2;
    private String Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9,Name10,
            Name11,Name12,Name13,Name14,Name15;
    public static String FromDataDisplaySendValue;
    ListView SimpleListView;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public String returnData;

    String DeviceName, DeviceAddress;
    private boolean mConnected = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);
        DeviceControlActivity.closefromDD.finish();
        DeviceName = DeviceScanActivity.Devicename;
        DeviceAddress = DeviceScanActivity.DeviceAddress;

        /**決定往哪裡跑！！！！！！！！！！！！！！！！！*/
        if(DeviceControlActivity.DeviceType.contains("BT-2-TH"))
        {
            Device_BT_2_TH();
        }else if(DeviceControlActivity.DeviceType.contains("BT-2-II")){
            Log.v("BT","DeviceDisplay待寫");
            Device_BT_2_II();
        }
        /**決定往哪裡跑！！！！！！！！！！！！！！！！！*/

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        getActionBar().setTitle(DeviceScanActivity.Devicename);


    }//onCreate
    private void Device_BT_2_II(){
        Intent intent = this.getIntent();
        Name10  = intent.getStringExtra("IH1");
        Name10  = "第一排最大量程";
        IH1     = intent.getStringExtra("IH1_Value");

        Name11  = intent.getStringExtra("IL1");
        Name11  = "第一排最小量程";
        IL1     = intent.getStringExtra("IL1_Value");

        Name12  = intent.getStringExtra("IH2");
        Name12  = "第二排最大量程";
        IH2     = intent.getStringExtra("IH2_Value");

        Name13  = intent.getStringExtra("IL2");
        Name13  = "第二排最小量程";
        IL2     = intent.getStringExtra("IL2_Value");

        Name1   = intent.getStringExtra("PV1");
        Name1   = "第一排補正";
        PV1     = intent.getStringExtra("PV1_Value");

        Name2   = intent.getStringExtra("PV2");
        Name2   = "第二排補正";
        PV2     = intent.getStringExtra("PV2_Value");

        Name3   = intent.getStringExtra("EH1");
        Name3   = "第一排上限警報";
        EH1     = intent.getStringExtra("EH1_Value");

        Name5   = intent.getStringExtra("EL1");
        Name5   = "第一排下限警報";
        EL1     = intent.getStringExtra("EL1_Value");

        Name4   = intent.getStringExtra("EH2");
        Name4   = "第二排上限警報";
        EH2     = intent.getStringExtra("EH2_Value");

        Name6   = intent.getStringExtra("EL2");
        Name6   = "第二排下限警報";
        EL2     = intent.getStringExtra("EL2_Value");

        Name7   = intent.getStringExtra("CR1");
        Name7   = "第一排顏色轉換";
        CR1     = intent.getStringExtra("CR1_Value");

        Name8   = intent.getStringExtra("CR2");
        Name8   = "第二排顏色轉換";
        CR2     = intent.getStringExtra("CR2_Value");

        Name9   = intent.getStringExtra("SPK");
        Name9   = "警報聲";
        SPK     = intent.getStringExtra("SPK_Value");

        Name14  = intent.getStringExtra("DP1");
        Name14  = "第一排小數點";
        DP1     = intent.getStringExtra("DP1_Value");
        if (DP1.contains("0000.0")){
            DP1 = "off";
        }else {
            DP1 = "on";
        }

        Name15  = intent.getStringExtra("DP2");
        Name15  = "第二排小數點";
        DP2     = intent.getStringExtra("DP2_Value");
        if (DP2.contains("0000.0")){
            DP2 = "off";
        }else {
            DP2 = "on";
        }

        final String[] nameItems   =  {"裝置名稱",Name10,Name11,Name12,Name13,Name1,Name2,Name3,Name5,Name4
                                        ,Name6,Name7,Name8,Name9,Name14,Name15};
        final String[] valuesItems =  {DeviceName,IH1,IL1,IH2,IL2,PV1,PV2,EH1,EH2,EL1,EL2,CR1,CR2,SPK
                                        ,DP1,DP2};

        SimpleListView = findViewById(R.id.listView);

        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for(int i = 0;i<nameItems.length; i++){
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("name",nameItems[i]);
            hashMap.put("values",valuesItems[i]);
            arrayList.add(hashMap);
        }
        String[] from = {"name","values"};
        int[] to = {R.id.TitleName,R.id.ResultValue};
        final SimpleAdapter simpleAdapter =
                new SimpleAdapter(this,arrayList,R.layout.style_listview,from,to);
        SimpleListView.setAdapter(simpleAdapter);


        SimpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String GetName = nameItems[position];
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
                View v = getLayoutInflater().inflate(R.layout.alertdialog_use, null);
                final EditText edInput = (EditText) v.findViewById(R.id.editText1);
                final Switch swInput = (Switch) v.findViewById(R.id.theSwitch);


            if (DP1.contains("on")) {
                if (GetName.contains("第一排補正")) {
                    edInput.setHint("-99.9~99.9");
                } else {
                    edInput.setHint("-199.9~999.9");
                }
            }else{
                if (GetName.contains("第一排補正")) {
                    edInput.setHint("-999~999");
                } else {
                    edInput.setHint("-9999~9999" );
                }
            }

                switch (GetName){
                    case "第一排最大量程":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog = mBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                double toDoubleForHint = Double.parseDouble(HintText);
                                if (toDoubleForHint > -199 && toDoubleForHint <999.9){

                                }else{

                                }

                            }
                        });

                        break;
                    case "第一排最小量程":

                        break;
                    case "第二排最大量程":

                        break;
                    case "第二排最小量程":

                        break;
                    case "第一排補正":

                        break;
                    case "第二排補正":

                        break;
                    case "第一排上限警報":

                        break;
                    case "第一排下限警報":

                        break;
                    case "第二排上限警報":

                        break;
                    case "第二排下限警報":

                        break;
                    case "第一排顏色轉換":

                        break;
                    case "第二排顏色轉換":

                        break;
                    case "警報聲":

                        break;
                    case "第一排小數點":


                        break;
                    case "第二排小數點":

                        break;



                }


            }
        });
    }


    private void Device_BT_2_TH(){
        Intent intent = this.getIntent();
        Name1   = intent.getStringExtra("PV1");
        Name1   = "溫度補正";
        PV1     = intent.getStringExtra("PV1_Value");

        Name2   = intent.getStringExtra("PV2");
        Name2   = "濕度補正";
        PV2     = intent.getStringExtra("PV2_Value");

        Name3   = intent.getStringExtra("EH1");
        Name3   = "溫度上限警報";
        EH1     = intent.getStringExtra("EH1_Value");

        Name4   = intent.getStringExtra("EH2");
        Name4   = "溫度下限警報";
        EH2     = intent.getStringExtra("EH2_Value");

        Name5   = intent.getStringExtra("EL1");
        Name5   = "濕度上限警報";
        EL1     = intent.getStringExtra("EL1_Value");

        Name6   = intent.getStringExtra("EL2");
        Name6   = "濕度下限警報";
        EL2     = intent.getStringExtra("EL2_Value");

        Name7   = intent.getStringExtra("CR1");
        Name7   = "溫度顏色轉換";
        CR1     = intent.getStringExtra("CR1_Value");

        Name8   = intent.getStringExtra("CR2");
        Name8   = "濕度顏色轉換";
        CR2     = intent.getStringExtra("CR2_Value");

        Name9   = intent.getStringExtra("SPK");
        Name9   = "警報聲";
        SPK     = intent.getStringExtra("SPK_Value");

        final String[] nameItems={"裝置名稱",Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9};
        final String[] valuesItems = {DeviceName,PV1,PV2,EH1,EH2,EL1,EL2,CR1,CR2,SPK};

        SimpleListView = findViewById(R.id.listView);

        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for(int i = 0;i<nameItems.length; i++){
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("name",nameItems[i]);
            hashMap.put("values",valuesItems[i]);
            arrayList.add(hashMap);
        }
        String[] from = {"name","values"};
        int[] to = {R.id.TitleName,R.id.ResultValue};
        final SimpleAdapter simpleAdapter =
                new SimpleAdapter(this,arrayList,R.layout.style_listview,from,to);
        SimpleListView.setAdapter(simpleAdapter);

        SimpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String GetName = nameItems[position];
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
                View v = getLayoutInflater().inflate(R.layout.alertdialog_use, null);
                final EditText edInput = (EditText) v.findViewById(R.id.editText1);
                final Switch swInput = (Switch) v.findViewById(R.id.theSwitch);

                switch (GetName) {
                    case "溫度補正":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog = mBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble>5){
                                    edInput.setText("5");
                                }else if (toDouble<-5){
                                    edInput.setText("-5");
                                }else{
                                    if(toDouble >= 0){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "PV1+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog.dismiss();
                                    }else{
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-000" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "PV1-000" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog.dismiss();
                                    }
                                }


                            }
                        });

                        break;
                    case "濕度補正":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog1 = mBuilder.create();
                        dialog1.setCanceledOnTouchOutside(false);
                        dialog1.setCancelable(false);
                        dialog1.show();
                        dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble>5){
                                    edInput.setText("5");
                                }else if (toDouble<-5){
                                    edInput.setText("-5");
                                }else{
                                    if(toDouble >= 0){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "PV2+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog1.dismiss();
                                    }else{
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-000" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "PV2-000" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog1.dismiss();
                                    }
                                }


                            }
                        });

                        break;
                    case "溫度上限警報":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog2 = mBuilder.create();
                        dialog2.setCanceledOnTouchOutside(false);
                        dialog2.setCancelable(false);
                        dialog2.show();
                        dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble>65){
                                    edInput.setText("65");
                                }else if (toDouble<-10){
                                    edInput.setText("-10");
                                }else{
                                    if(toDouble >= 0 && toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH1+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog2.dismiss();
                                    }else if(toDouble >9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH1+00" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog2.dismiss();
                                    }else if(toDouble == -10){
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-00" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH1-00" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog2.dismiss();
                                    }
                                    else{
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-000" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH1-000" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog2.dismiss();
                                    }
                                }


                            }
                        });

                        break;
                    case "溫度下限警報":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog3 = mBuilder.create();
                        dialog3.setCanceledOnTouchOutside(false);
                        dialog3.setCancelable(false);
                        dialog3.show();
                        dialog3.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble>65){
                                    edInput.setText("65");
                                }else if (toDouble<-10){
                                    edInput.setText("-10");
                                }else{
                                    if(toDouble >= 0 && toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL1+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog3.dismiss();
                                    }else if(toDouble >9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL1+00" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog3.dismiss();
                                    }else if(toDouble == -10){
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-00" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL1-00" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog3.dismiss();
                                    }
                                    else{
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-000" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL1-000" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog3.dismiss();
                                    }
                                }



                            }
                        });

                        break;
                    case "濕度上限警報":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog4 = mBuilder.create();
                        dialog4.setCanceledOnTouchOutside(false);
                        dialog4.setCancelable(false);
                        dialog4.show();
                        dialog4.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble<0){
                                    edInput.setText("0");
                                }else if(toDouble>100){
                                    edInput.setText("100");
                                }else{
                                    if(toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH2+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog4.dismiss();
                                    }else if(toDouble>10&&toDouble<=99){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH2+00"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog4.dismiss();
                                    }else{
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+0"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EH2+0"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog4.dismiss();
                                    }
                                }


                            }
                        });

                        break;
                    case "濕度下限警報":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog5 = mBuilder.create();
                        dialog5.setCanceledOnTouchOutside(false);
                        dialog5.setCancelable(false);
                        dialog5.show();
                        dialog5.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble<0){
                                    edInput.setText("0");
                                }else if(toDouble>100){
                                    edInput.setText("100");
                                }else{
                                    if(toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL2+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog5.dismiss();
                                    }else if(toDouble>10&&toDouble<=99){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL2+00"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog5.dismiss();
                                    }else{
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+0"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "EL2+0"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog5.dismiss();
                                    }
                                }

                            }
                        });

                        break;
                    case "溫度顏色轉換":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog6 = mBuilder.create();
                        dialog6.setCanceledOnTouchOutside(false);
                        dialog6.setCancelable(false);
                        dialog6.show();
                        dialog6.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble>65){
                                    edInput.setText("65");
                                }else if (toDouble<-10){
                                    edInput.setText("-10");
                                }else{
                                    if(toDouble >= 0 && toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR1+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog6.dismiss();
                                    }else if(toDouble >9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR1+00" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog6.dismiss();
                                    }else if(toDouble == -10){
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-00" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR1-00" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog6.dismiss();
                                    }
                                    else{
                                        toDouble = Math.abs(toDouble);
                                        String InputMiner = String.valueOf(toDouble);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","-000" + InputMiner);
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR1-000" + InputMiner ;
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog6.dismiss();
                                    }
                                }


                            }
                        });

                        break;
                    case "濕度顏色轉換":
                        swInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog7 = mBuilder.create();
                        dialog7.setCanceledOnTouchOutside(false);
                        dialog7.setCancelable(false);
                        dialog7.show();
                        dialog7.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                double toDouble = Double.parseDouble(Input);
                                if (toDouble<0){
                                    edInput.setText("0");
                                }else if(toDouble>100){
                                    edInput.setText("100");
                                }else{
                                    if(toDouble <=9){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+000" + Input + ".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR2+000" + Input + ".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                        dialog7.dismiss();
                                    }else if(toDouble>10&&toDouble<=99){
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+00"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR2+00"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog7.dismiss();
                                    }else{
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("name",nameItems[position]);
                                        hashMap.put("values","+0"+Input+".0");
                                        arrayList.set(position,hashMap);
                                        simpleAdapter.notifyDataSetChanged();
                                        FromDataDisplaySendValue = "CR2+0"+Input+".0";
                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                        dialog7.dismiss();
                                    }
                                }

                            }
                        });

                        break;
                    case "警報聲":
                        if(SPK.contains("on")){
                            swInput.setChecked(true);
                        }else {
                            swInput.setChecked(false);
                        }
                        edInput.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);

                        swInput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked){
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("name",nameItems[position]);
                                    hashMap.put("values","on");
                                    arrayList.set(position,hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "SPK+0001.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                }else{
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("name",nameItems[position]);
                                    hashMap.put("values","off");
                                    arrayList.set(position,hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "SPK+0000.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData,true);
                                }
                            }
                        });
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog8 = mBuilder.create();
                        dialog8.setCanceledOnTouchOutside(false);
                        dialog8.setCancelable(false);
                        dialog8.show();


                        break;
                }

            }
        });
    }//Device_BT_2_TH


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(DeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
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

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                /**接收來自Service的訊息*/
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }

        }
    };//onReceive

    private void displayData(String data) {
        Log.v("BT", "DD 回傳" + data);
        if (data.contains("SPK+0001.0")){
            SPK = "on";
        }else{
            SPK = "off";
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

