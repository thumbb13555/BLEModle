package com.example.bleconnected01;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.example.bleconnected01.SQL.CustomDBOpenHelper;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataDisplayActivity extends Activity {
    private final static String TAG = DataDisplayActivity.class.getSimpleName();

    private static final String DB_Name = "LeCustomDB.db";
    private int GET_ITEM_POSITION = 100;
    private String DB_TABLE;
    private SQLiteDatabase mCustomDb;
    private String PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK, IH1, IL1, IH2, IL2, DP1, DP2;
    private String Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9, Name10,
            Name11, Name12, Name13, Name14, Name15;

    public static String FromDataDisplaySendValue;
    private JSONArray jsonArray;
    ListView SimpleListView;
    private DrawerLayout drawerLayout;
    private SimpleAdapter simpleAdapter;
    private BluetoothLeService mBluetoothLeService;
//    private BluetoothGattCharacteristic mNotifyCharacteristic;
//    public String returnData;

    String DeviceName, DeviceAddress, getSimpleListViewItem;
    private boolean mConnected = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);
        DeviceControlActivity.closefromDD.finish();
        DeviceName = DeviceScanActivity.Devicename;
        DeviceAddress = DeviceScanActivity.DeviceAddress;
        drawerLayout = findViewById(R.id.drawerLayout);
        Button btnCloseMenu = (Button) findViewById(R.id.Go_saveData);
        Button btnLoadData = (Button) findViewById(R.id.Go_LoadData);
        btnCloseMenu.setOnClickListener(SaveDataToSQLite);
        btnLoadData.setOnClickListener(LoadDataFromSQLite);
        //===SQLite
        DB_TABLE = DeviceControlActivity.GetMySQL;
        CustomDBOpenHelper customDBOpenHelper =
                new CustomDBOpenHelper(getBaseContext(), DB_Name, null, 1);
        mCustomDb = customDBOpenHelper.getWritableDatabase();
        Stetho.initializeWithDefaults(this);


        /**決定往哪裡跑！！！！！！！！！！！！！！！！！*/
        if (DeviceControlActivity.DeviceType.contains("BT-2-TH")) {
            Device_BT_2_TH();
        } else if (DeviceControlActivity.DeviceType.contains("BT-2-II")) {
            Device_BT_2_II();
        }
        /**決定往哪裡跑！！！！！！！！！！！！！！！！！*/

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        getActionBar().setTitle(DeviceScanActivity.Devicename);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

    }//onCreate

    private View.OnClickListener LoadDataFromSQLite = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            drawerLayout.closeDrawers();
            AlertDialog.Builder SQLLoadBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_load_sqlite_data, null);
            final Button btnLoad = (Button) view.findViewById(R.id.LoadButton);
            final ListView lvLoad = (ListView) view.findViewById(R.id.listview_Load);
            final Button btnClose = (Button) view.findViewById(R.id.closeDiaL);

            SQLLoadBuilder.setView(view);
            final AlertDialog dialog = SQLLoadBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                    null, null, null, null, null, null);
            final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            while (data.moveToNext()) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", data.getString(1));
                hashMap.put("id", data.getString(0));
                arrayList.add(hashMap);
            }





            final String[] from = {"name", "id"};
            int[] to = {android.R.id.text1};
            simpleAdapter =
                    new SimpleAdapter(getApplicationContext(), arrayList, android.R.layout.simple_list_item_1, from, to);
            lvLoad.setAdapter(simpleAdapter);

            lvLoad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    lvLoad.setSelector(R.color.solid);//設置選中的背景色
                    final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                            null, null, null, null, null, null);
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                    while (data.moveToNext()) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name", data.getString(1));
                        hashMap.put("id", data.getString(0));
                        arrayList.add(hashMap);
                    }
                    String Selected = arrayList.get(position).toString();
                    String str = Selected.substring(Selected.indexOf(", id="), Selected.indexOf("}"));
                    String strGet = str.substring(5);
                    GET_ITEM_POSITION = Integer.parseInt(strGet);
                }
            });
            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (GET_ITEM_POSITION != 100) {
                        Toast.makeText(getBaseContext(),"會Lag一下，請稍候喔",Toast.LENGTH_LONG).show();
                        new AlertDialog.Builder(DataDisplayActivity.this)
                                .setTitle("確認匯入")
                                .setMessage("確定要將資料匯入嗎？")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Cursor c = mCustomDb.rawQuery("SELECT Description FROM "
                                                + DB_TABLE + " WHERE _id=" + GET_ITEM_POSITION, null);
                                        String str = "";
                                        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                                        while (c.moveToNext()) {
                                            Log.v("BT", c.getString(0));
                                            str = c.getString(0);

                                        }

                                        try {

                                            JSONArray array = new JSONArray(str);
                                            for (int i = 0; i < array.length(); i++) {
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                JSONObject jsonObject = array.getJSONObject(i);
                                                String id = jsonObject.getString("id");
                                                String value = jsonObject.getString("value");
                                                hashMap.put("name",id);
                                                hashMap.put("values",value);
                                                arrayList.add(hashMap);
                                                if(id.contains("溫度補正")){
                                                    FromDataDisplaySendValue = "PV1"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("濕度補正")){
                                                    FromDataDisplaySendValue = "PV2"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("溫度上限警報")){
                                                    FromDataDisplaySendValue = "EH1"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("溫度下限警報")){
                                                    FromDataDisplaySendValue = "EL1"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("濕度上限警報")){
                                                    FromDataDisplaySendValue = "EH2"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("濕度下限警報")){
                                                    FromDataDisplaySendValue = "EL2"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("溫度顏色轉換")){
                                                    FromDataDisplaySendValue = "CR1"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("濕度顏色轉換")){
                                                    FromDataDisplaySendValue = "CR2"+value;
                                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                                    SystemClock.sleep(200);
                                                }else if(id.contains("警報聲")){
                                                    if (value.contains("off")){
                                                        FromDataDisplaySendValue = "SPK+0000.0";
                                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);

                                                    }else{
                                                        FromDataDisplaySendValue = "SPK+0001.0";
                                                        mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);

                                                    }

                                                }

                                                Log.v("BT", "解析:" + id + ",\t" + value);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.v("BT", "爆了");
                                        }
                                        String[] from = {"name","values"};
                                        int[] to = {R.id.TitleName, R.id.ResultValue};
                                        simpleAdapter =
                                                new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                        SimpleListView.setAdapter(simpleAdapter);
                                        SimpleListView.setOnItemClickListener(THClick);







                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("先不要好了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    } else {
                        Toast.makeText(getBaseContext(), "請選擇欲匯入物件", Toast.LENGTH_SHORT).show();
                    }


                }
            });//btnLoad

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


        }
    };//LoadDataFromSQLite


    private View.OnClickListener SaveDataToSQLite = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            drawerLayout.closeDrawers();
            AlertDialog.Builder SQLBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
            View view = getLayoutInflater().inflate(R.layout.diolog_save_data, null);
            final EditText edGetCustomName = (EditText) view.findViewById(R.id.edText);
            final Button btn_createData = (Button) view.findViewById(R.id.buttonCreateData);
            final Button btn_closeDialog = (Button) view.findViewById(R.id.closeDia);
            final Button btn_DeleteData = (Button) view.findViewById(R.id.deleteDataButton);
            final Button btn_modifyData = (Button) view.findViewById(R.id.LoadButton);
            final ListView lv_DisplayData = (ListView) view.findViewById(R.id.listview_SQLDisplay);
            SQLBuilder.setView(view);
            final AlertDialog dialog = SQLBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            /**如果沒有資料表，就建立一個；如果有則選擇之*/
            Cursor cursor = mCustomDb.rawQuery(
                    "select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE + "'", null);

            if (cursor != null) {
                if (cursor.getCount() == 0)
                    mCustomDb.execSQL("CREATE TABLE " + DB_TABLE + " (" + "_id INTEGER PRIMARY KEY," + "name TEXT," + "Description TEXT);");
                cursor.close();
            }


            final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                    null, null, null, null, null, null);
            final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            while (data.moveToNext()) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", data.getString(1));
                hashMap.put("id", data.getString(0));
                arrayList.add(hashMap);
            }

            final String[] from = {"name", "id"};
            int[] to = {android.R.id.text1};
            simpleAdapter =
                    new SimpleAdapter(getApplicationContext(), arrayList, android.R.layout.simple_list_item_1, from, to);
            lv_DisplayData.setAdapter(simpleAdapter);

            lv_DisplayData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    lv_DisplayData.setSelector(R.color.solid);//設置選中的背景色
                    final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                            null, null, null, null, null, null);
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                    while (data.moveToNext()) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name", data.getString(1));
                        hashMap.put("id", data.getString(0));
                        arrayList.add(hashMap);
                    }
                    String Selected = arrayList.get(position).toString();
                    String str = Selected.substring(Selected.indexOf(", id="), Selected.indexOf("}"));
                    String strGet = str.substring(5);
                    GET_ITEM_POSITION = Integer.parseInt(strGet);


                }
            });

            btn_createData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (edGetCustomName.getText().toString().length() > 0) {

                        try {
                            JSONObject json_MySet0 = new JSONObject();
                            json_MySet0.put("id", "裝置名稱");
                            json_MySet0.put("value", DeviceName);

                            JSONObject json_MySet = new JSONObject();
                            json_MySet.put("id", Name1);
                            json_MySet.put("value", PV1);

                            JSONObject json_MySet1 = new JSONObject();
                            json_MySet1.put("id", Name2);
                            json_MySet1.put("value", PV2);

                            JSONObject json_MySet2 = new JSONObject();
                            json_MySet2.put("id", Name3);
                            json_MySet2.put("value", EH1);

                            JSONObject json_MySet3 = new JSONObject();
                            json_MySet3.put("id", Name4);
                            json_MySet3.put("value", EL1);

                            JSONObject json_MySet4 = new JSONObject();
                            json_MySet4.put("id", Name5);
                            json_MySet4.put("value", EH2);

                            JSONObject json_MySet5 = new JSONObject();
                            json_MySet5.put("id", Name6);
                            json_MySet5.put("value", EL2);

                            JSONObject json_MySet6 = new JSONObject();
                            json_MySet6.put("id", Name7);
                            json_MySet6.put("value", CR1);

                            JSONObject json_MySet7 = new JSONObject();
                            json_MySet7.put("id", Name8);
                            json_MySet7.put("value", CR2);

                            JSONObject json_MySet8 = new JSONObject();
                            json_MySet8.put("id", Name9);
                            json_MySet8.put("value", SPK);


                            jsonArray = new JSONArray();
                            jsonArray.put(0,json_MySet0);
                            jsonArray.put(1, json_MySet);
                            jsonArray.put(2, json_MySet1);
                            jsonArray.put(3, json_MySet2);
                            jsonArray.put(4, json_MySet3);
                            jsonArray.put(5, json_MySet4);
                            jsonArray.put(6, json_MySet5);
                            jsonArray.put(7, json_MySet6);
                            jsonArray.put(8, json_MySet7);
                            jsonArray.put(9, json_MySet8);


                            Log.v("BT", "JSON: " + jsonArray);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        ContentValues newRow = new ContentValues();
                        newRow.put("name", edGetCustomName.getText().toString().trim());
                        newRow.put("Description", String.valueOf(jsonArray));
                        mCustomDb.insert(DB_TABLE, null, newRow);
                        Toast.makeText(getBaseContext(), "新增成功!", Toast.LENGTH_SHORT).show();
                        edGetCustomName.setText("");

                        final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                                null, null, null, null, null, null);
                        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                        while (data.moveToNext()) {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("name", data.getString(1));
                            hashMap.put("id", data.getString(0));
                            arrayList.add(hashMap);
                        }
                        final String[] from = {"name", "id"};
                        int[] to = {android.R.id.text1};
                        simpleAdapter =
                                new SimpleAdapter(getApplicationContext(), arrayList, android.R.layout.simple_list_item_1, from, to);
                        lv_DisplayData.setAdapter(simpleAdapter);
                    } else {
                        Toast.makeText(getBaseContext(), "請取個喜歡的名字吧", Toast.LENGTH_LONG).show();
                    }
                }
            });//btn_create
            btn_modifyData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (GET_ITEM_POSITION == 100) {
                        Toast.makeText(getBaseContext(), "請選擇欲修改之物件", Toast.LENGTH_SHORT).show();
                    } else {
                        final AlertDialog.Builder modifyDialog = new AlertDialog.Builder(DataDisplayActivity.this);
                        View view1 = getLayoutInflater().inflate(R.layout.dialog_input_modift_function, null);
                        final EditText edtModify = (EditText) view1.findViewById(R.id.editTextINput);


                        modifyDialog.setView(view1);
                        modifyDialog.setTitle("請輸入修改的值");
                        final Cursor c = mCustomDb.rawQuery("SELECT *  FROM " + DB_TABLE + " WHERE _id=" + GET_ITEM_POSITION, null);
                        while (c.moveToNext()) {
                            edtModify.setText(c.getString(1));
                        }

                        modifyDialog.setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        modifyDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        final AlertDialog dialog = modifyDialog.create();
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (edtModify.getText().toString().length() > 0) {
                                    ContentValues values = new ContentValues();
                                    values.put("name", edtModify.getText().toString().trim());
                                    mCustomDb.update(DB_TABLE, values, "_id=" + GET_ITEM_POSITION, null);
                                    //UPDATE BT2TH SET  WHERE
                                    //UPDATE "表格" SET "欄位1" = [值1], "欄位2" = [值2]WHERE "條件";
                                    final Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"_id", "name", "Description"},
                                            null, null, null, null, null, null);
                                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                                    while (data.moveToNext()) {
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("name", data.getString(1));
                                        hashMap.put("id", data.getString(0));
                                        arrayList.add(hashMap);
                                    }
                                    final String[] from = {"name", "id"};
                                    int[] to = {android.R.id.text1};
                                    simpleAdapter =
                                            new SimpleAdapter(getApplicationContext(), arrayList, android.R.layout.simple_list_item_1, from, to);
                                    lv_DisplayData.setAdapter(simpleAdapter);
                                    GET_ITEM_POSITION = 100;
                                    dialog.dismiss();


                                } else {
                                    Toast.makeText(getBaseContext(), "此處不可為空", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }//判斷是否選擇
                }
            });//modify
            btn_DeleteData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (GET_ITEM_POSITION == 100) {
                        Toast.makeText(getBaseContext(), "請選擇欲刪除之物件", Toast.LENGTH_LONG).show();
                    } else {
                        mCustomDb.delete(DB_TABLE, "_id=" + GET_ITEM_POSITION, null);
                        Toast.makeText(getBaseContext(), "刪除成功!", Toast.LENGTH_SHORT).show();
                        Cursor data = mCustomDb.query(true, DB_TABLE, new String[]{"name"},
                                null, null, null, null, null, null);
                        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                        while (data.moveToNext()) {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("name", data.getString(0));
                            arrayList.add(hashMap);
                        }
                        String[] from = {"name"};
                        int[] to = {android.R.id.text1};
                        simpleAdapter =
                                new SimpleAdapter(getApplicationContext(), arrayList, android.R.layout.simple_list_item_1, from, to);
                        lv_DisplayData.setAdapter(simpleAdapter);
                        GET_ITEM_POSITION = 100;
                    }


                }
            });//DeleteData

            btn_closeDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GET_ITEM_POSITION = 100;
                    dialog.dismiss();
                }
            });//closeDialog


        }
    };//btnSaveData

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            drawerLayout.closeDrawers();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void Device_BT_2_II() {
        Intent intent = this.getIntent();
        Name10 = intent.getStringExtra("IH1");
        Name10 = "第一排最大量程";
        IH1 = intent.getStringExtra("IH1_Value");

        Name11 = intent.getStringExtra("IL1");
        Name11 = "第一排最小量程";
        IL1 = intent.getStringExtra("IL1_Value");

        Name12 = intent.getStringExtra("IH2");
        Name12 = "第二排最大量程";
        IH2 = intent.getStringExtra("IH2_Value");

        Name13 = intent.getStringExtra("IL2");
        Name13 = "第二排最小量程";
        IL2 = intent.getStringExtra("IL2_Value");

        Name1 = intent.getStringExtra("PV1");
        Name1 = "第一排補正";
        PV1 = intent.getStringExtra("PV1_Value");

        Name2 = intent.getStringExtra("PV2");
        Name2 = "第二排補正";
        PV2 = intent.getStringExtra("PV2_Value");

        Name3 = intent.getStringExtra("EH1");
        Name3 = "第一排上限警報";
        EH1 = intent.getStringExtra("EH1_Value");

        Name5 = intent.getStringExtra("EL1");
        Name5 = "第一排下限警報";
        EL1 = intent.getStringExtra("EL1_Value");

        Name4 = intent.getStringExtra("EH2");
        Name4 = "第二排上限警報";
        EH2 = intent.getStringExtra("EH2_Value");

        Name6 = intent.getStringExtra("EL2");
        Name6 = "第二排下限警報";
        EL2 = intent.getStringExtra("EL2_Value");

        Name7 = intent.getStringExtra("CR1");
        Name7 = "第一排顏色轉換";
        CR1 = intent.getStringExtra("CR1_Value");

        Name8 = intent.getStringExtra("CR2");
        Name8 = "第二排顏色轉換";
        CR2 = intent.getStringExtra("CR2_Value");

        Name9 = intent.getStringExtra("SPK");
        Name9 = "警報聲";
        SPK = intent.getStringExtra("SPK_Value");

        Name14 = intent.getStringExtra("DP1");
        Name14 = "第一排小數點";
        DP1 = intent.getStringExtra("DP1_Value");
        if (DP1.contains("0000.0")) {
            DP1 = "off";
        } else {
            DP1 = "on";
        }

        Name15 = intent.getStringExtra("DP2");
        Name15 = "第二排小數點";
        DP2 = intent.getStringExtra("DP2_Value");
        if (DP2.contains("0000.0")) {
            DP2 = "off";
        } else {
            DP2 = "on";
        }

        final String[] nameItems = {"裝置名稱", Name10, Name11, Name12, Name13, Name1, Name2, Name3, Name5, Name4
                , Name6, Name7, Name8, Name9, Name14, Name15};
        final String[] valuesItems = {DeviceName, IH1, IL1, IH2, IL2, PV1, PV2, EH1, EH2, EL1, EL2, CR1, CR2, SPK
                , DP1, DP2};

        SimpleListView = findViewById(R.id.listView);

        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for (int i = 0; i < nameItems.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", nameItems[i]);
            hashMap.put("values", valuesItems[i]);
            arrayList.add(hashMap);
        }
        String[] from = {"name", "values"};
        int[] to = {R.id.TitleName, R.id.ResultValue};
        simpleAdapter =
                new SimpleAdapter(this, arrayList, R.layout.style_listview, from, to);
        SimpleListView.setAdapter(simpleAdapter);


        SimpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String GetName = nameItems[position];
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
                View v = getLayoutInflater().inflate(R.layout.alertdialog_use, null);
                final EditText edInput = (EditText) v.findViewById(R.id.editText1);
                final Switch swInput = (Switch) v.findViewById(R.id.theSwitch);
                final Switch swInputDP1 = (Switch) v.findViewById(R.id.theSwitchDP1);
                final Switch swInputDP2 = (Switch) v.findViewById(R.id.theSwitchDP2);

                if (DP1.contains("on")) {
                    if (GetName.contains("第一") && GetName.contains("補正")) {
                        edInput.setHint("-99.9~99.9");
                    } else if (GetName.contains("第一排")) {
                        edInput.setHint("-199.9~999.9");
                    }
                } else {
                    if (GetName.contains("第一") && GetName.contains("補正")) {
                        edInput.setHint("-999~999");
                    } else if (GetName.contains("第一排")) {
                        edInput.setHint("-999~9999");
                    }
                }
                if (DP2.contains("on")) {
                    if (GetName.contains("第二") && GetName.contains("補正")) {
                        edInput.setHint("-99.9~99.9");
                    } else if (GetName.contains("第二排")) {
                        edInput.setHint("-199.9~999.9");
                    }
                } else {
                    if (GetName.contains("第二") && GetName.contains("補正")) {
                        edInput.setHint("-999~999");
                    } else if (GetName.contains("第二排")) {
                        edInput.setHint("-999~9999");
                    }
                }

                switch (GetName) {
                    case "第一排最大量程":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }

                            }
                        });

                        break;//文字判斷的
                    case "第二排最大量程":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogA = mBuilder.create();
                        dialogA.setCanceledOnTouchOutside(false);
                        dialogA.setCancelable(false);
                        dialogA.show();
                        dialogA.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IH2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogA.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第一排最小量程":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog1.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第二排最小量程":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogC = mBuilder.create();
                        dialogC.setCanceledOnTouchOutside(false);
                        dialogC.setCancelable(false);
                        dialogC.show();
                        dialogC.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogC.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第一排補正":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-99.9~99.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog2.dismiss();
                                        } else if (toDoubleForInput > 99) {
                                            edInput.setText("99");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -99) {
                                            edInput.setText("-99");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第二排補正":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogD = mBuilder.create();
                        dialogD.setCanceledOnTouchOutside(false);
                        dialogD.setCancelable(false);
                        dialogD.show();
                        dialogD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-99.9~99.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "PV2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogD.dismiss();
                                        } else if (toDoubleForInput > 99) {
                                            edInput.setText("99");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為99",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -99) {
                                            edInput.setText("-99");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-99",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;

                    case "第一排上限警報":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog3.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第一排下限警報":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "IL1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog4.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第二排上限警報":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogE = mBuilder.create();
                        dialogE.setCanceledOnTouchOutside(false);
                        dialogE.setCancelable(false);
                        dialogE.show();
                        dialogE.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EH2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogE.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第二排下限警報":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogF = mBuilder.create();
                        dialogF.setCanceledOnTouchOutside(false);
                        dialogF.setCancelable(false);
                        dialogF.show();
                        dialogF.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "EL2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogF.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第一排顏色轉換":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialog5.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;
                    case "第二排顏色轉換":
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
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
                        final AlertDialog dialogG = mBuilder.create();
                        dialogG.setCanceledOnTouchOutside(false);
                        dialogG.setCancelable(false);
                        dialogG.show();
                        dialogG.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String Input = edInput.getText().toString().trim();
                                String HintText = edInput.getHint().toString();
                                double toDoubleForInput = Double.parseDouble(Input);
                                switch (HintText) {
                                    case "-999~9999":
                                        if (toDoubleForInput >= 0 && toDoubleForInput <= 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput >= 1000 && toDoubleForInput <= 9999) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2+" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();

                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -999) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR2-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput > 9999) {
                                            edInput.setText("9999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為9999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -999) {
                                            edInput.setText("-999");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-999",
                                                    Toast.LENGTH_SHORT).show();

                                        }


                                        break;
                                    case "-199.9~999.9":
                                        if (toDoubleForInput >= 0 && toDoubleForInput < 9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+000" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+000" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput >= 10 && toDoubleForInput <= 99) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+00" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+00" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput >= 100 && toDoubleForInput <= 999.9) {
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "+0" + Input + ".0");
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1+0" + Input + ".0";
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput <= 0 && toDoubleForInput >= -9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-000" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-000" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput <= -10 && toDoubleForInput >= -99) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-00" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-00" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput <= -100 && toDoubleForInput >= -199.9) {
                                            toDoubleForInput = Math.abs(toDoubleForInput);
                                            String InputMiner = String.valueOf(toDoubleForInput);
                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("name", nameItems[position]);
                                            hashMap.put("values", "-0" + InputMiner);
                                            arrayList.set(position, hashMap);
                                            simpleAdapter.notifyDataSetChanged();
                                            FromDataDisplaySendValue = "CR1-0" + InputMiner;
                                            mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                            dialogG.dismiss();
                                        } else if (toDoubleForInput > 999) {
                                            edInput.setText("999");
                                            Toast.makeText(getBaseContext(), "超過最大值,設定為999",
                                                    Toast.LENGTH_SHORT).show();

                                        } else if (toDoubleForInput < -199) {
                                            edInput.setText("-199");
                                            Toast.makeText(getBaseContext(), "超過最小值,設定為-199",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        break;//內容的
                                }


                            }
                        });
                        break;

                    case "警報聲":
                        if (SPK.contains("on")) {
                            swInput.setChecked(true);
                        } else {
                            swInput.setChecked(false);
                        }
                        edInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);

                        swInput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "on");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "SPK+0001.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "off");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "SPK+0000.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
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
                    case "第一排小數點":
                        if (DP1.contains("on")) {
                            swInputDP1.setChecked(true);
                        } else {
                            swInputDP1.setChecked(false);
                        }
                        edInput.setVisibility(View.GONE);
                        swInput.setVisibility(View.GONE);
                        swInputDP2.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        swInputDP1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "on");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "DP1+0001.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "off");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "DP1+0000.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                }
                            }
                        });
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialog9 = mBuilder.create();
                        dialog9.setCanceledOnTouchOutside(false);
                        dialog9.setCancelable(false);
                        dialog9.show();
                        break;
                    case "第二排小數點":
                        if (DP2.contains("on")) {
                            swInputDP2.setChecked(true);
                        } else {
                            swInputDP2.setChecked(false);
                        }
                        edInput.setVisibility(View.GONE);
                        swInput.setVisibility(View.GONE);
                        swInputDP1.setVisibility(View.GONE);
                        mBuilder.setTitle(GetName);
                        swInputDP2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "on");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "DP2+0001.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "off");
                                    arrayList.set(position, hashMap);
                                    simpleAdapter.notifyDataSetChanged();
                                    FromDataDisplaySendValue = "DP2+0000.0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                }
                            }
                        });
                        mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mBuilder.setView(v);
                        final AlertDialog dialogB = mBuilder.create();
                        dialogB.setCanceledOnTouchOutside(false);
                        dialogB.setCancelable(false);
                        dialogB.show();
                        break;

                }


            }
        });
    }


    private void Device_BT_2_TH() {
        Intent intent = this.getIntent();
        Name1 = intent.getStringExtra("PV1");
        Name1 = "溫度補正";
        PV1 = intent.getStringExtra("PV1_Value");

        Name2 = intent.getStringExtra("PV2");
        Name2 = "濕度補正";
        PV2 = intent.getStringExtra("PV2_Value");

        Name3 = intent.getStringExtra("EH1");
        Name3 = "溫度上限警報";
        EH1 = intent.getStringExtra("EH1_Value");

        Name4 = intent.getStringExtra("EL1");
        Name4 = "溫度下限警報";
        EH2 = intent.getStringExtra("EL1_Value");

        Name5 = intent.getStringExtra("EH2");
        Name5 = "濕度上限警報";
        EL1 = intent.getStringExtra("EH2_Value");

        Name6 = intent.getStringExtra("EL2");
        Name6 = "濕度下限警報";
        EL2 = intent.getStringExtra("EL2_Value");

        Name7 = intent.getStringExtra("CR1");
        Name7 = "溫度顏色轉換";
        CR1 = intent.getStringExtra("CR1_Value");

        Name8 = intent.getStringExtra("CR2");
        Name8 = "濕度顏色轉換";
        CR2 = intent.getStringExtra("CR2_Value");

        Name9 = intent.getStringExtra("SPK");
        Name9 = "警報聲";
        SPK = intent.getStringExtra("SPK_Value");

        final String[] nameItems = {"裝置名稱", Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9};
        final String[] valuesItems = {DeviceName, PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK};

        SimpleListView = findViewById(R.id.listView);

        final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for (int i = 0; i < nameItems.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", nameItems[i]);
            hashMap.put("values", valuesItems[i]);
            arrayList.add(hashMap);
        }
        String[] from = {"name", "values"};

        int[] to = {R.id.TitleName, R.id.ResultValue};
        simpleAdapter =
                new SimpleAdapter(this, arrayList, R.layout.style_listview, from, to);
        SimpleListView.setAdapter(simpleAdapter);
        SimpleListView.setOnItemClickListener(THClick);


    }//Device_BT_2_TH

    private AdapterView.OnItemClickListener THClick = (new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            final String[] nameItems = {"裝置名稱", Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9};
            final String[] valuesItems = {DeviceName, PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK};
            final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            for (int i = 0; i < nameItems.length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", nameItems[i]);
                hashMap.put("values", valuesItems[i]);
                arrayList.add(hashMap);
            }
            String GetName = nameItems[position];
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(DataDisplayActivity.this);
            View v = getLayoutInflater().inflate(R.layout.alertdialog_use, null);
            final EditText edInput = (EditText) v.findViewById(R.id.editText1);
            final Switch swInput = (Switch) v.findViewById(R.id.theSwitch);
            final Switch swInputDP1 = (Switch) v.findViewById(R.id.theSwitchDP1);
            final Switch swInputDP2 = (Switch) v.findViewById(R.id.theSwitchDP2);

            switch (GetName) {
                case "溫度補正":

                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble > 5) {
                                edInput.setText("5");
                            } else if (toDouble < -5) {
                                edInput.setText("-5");
                            } else {
                                if (toDouble >= 0) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);

                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);

                                    FromDataDisplaySendValue = "PV1+000" + Input + ".0";
                                    PV1 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog.dismiss();
                                } else {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-000" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "PV1-000" + InputMiner;
                                    PV1 = "-000" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "濕度補正":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble > 5) {
                                edInput.setText("5");
                            } else if (toDouble < -5) {
                                edInput.setText("-5");
                            } else {
                                if (toDouble >= 0) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "PV2+000" + Input + ".0";
                                    PV2 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog1.dismiss();
                                } else {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-000" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "PV2-000" + InputMiner;
                                    PV2 = "-000" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog1.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "溫度上限警報":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble > 65) {
                                edInput.setText("65");
                            } else if (toDouble < -10) {
                                edInput.setText("-10");
                            } else {
                                if (toDouble >= 0 && toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH1+000" + Input + ".0";
                                    EH1 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog2.dismiss();
                                } else if (toDouble > 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH1+00" + Input + ".0";
                                    EH1 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog2.dismiss();
                                } else if (toDouble == -10) {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-00" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH1-00" + InputMiner;
                                    EH1 = "-00" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog2.dismiss();
                                } else {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-000" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH1-000" + InputMiner;
                                    EH1 = FromDataDisplaySendValue;
                                    EH1 = "-000" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog2.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "溫度下限警報":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble > 65) {
                                edInput.setText("65");
                            } else if (toDouble < -10) {
                                edInput.setText("-10");
                            } else {
                                if (toDouble >= 0 && toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL1+000" + Input + ".0";
                                    EL1 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog3.dismiss();
                                } else if (toDouble > 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL1+00" + Input + ".0";
                                    EL1 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog3.dismiss();
                                } else if (toDouble == -10) {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-00" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL1-00" + InputMiner;
                                    EL1 = "-00" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog3.dismiss();
                                } else {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-000" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL1-000" + InputMiner;
                                    EL1 = "-000" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog3.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "濕度上限警報":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble < 0) {
                                edInput.setText("0");
                            } else if (toDouble > 100) {
                                edInput.setText("100");
                            } else {
                                if (toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH2+000" + Input + ".0";
                                    EH2 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog4.dismiss();
                                } else if (toDouble > 10 && toDouble <= 99) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH2+00" + Input + ".0";
                                    EH2 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog4.dismiss();
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+0" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EH2+0" + Input + ".0";
                                    EH2 = "+0" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog4.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "濕度下限警報":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble < 0) {
                                edInput.setText("0");
                            } else if (toDouble > 100) {
                                edInput.setText("100");
                            } else {
                                if (toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL2+000" + Input + ".0";
                                    EL2 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog5.dismiss();
                                } else if (toDouble > 10 && toDouble <= 99) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL2+00" + Input + ".0";
                                    EL2 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog5.dismiss();
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+0" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "EL2+0" + Input + ".0";
                                    EL2 = "+0" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog5.dismiss();
                                }
                            }

                        }
                    });

                    break;
                case "溫度顏色轉換":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble > 65) {
                                edInput.setText("65");
                            } else if (toDouble < -10) {
                                edInput.setText("-10");
                            } else {
                                if (toDouble >= 0 && toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR1+000" + Input + ".0";
                                    CR1 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog6.dismiss();
                                } else if (toDouble > 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR1+00" + Input + ".0";
                                    CR1 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog6.dismiss();
                                } else if (toDouble == -10) {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-00" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR1-00" + InputMiner;
                                    CR1 = "-00" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog6.dismiss();
                                } else {
                                    toDouble = Math.abs(toDouble);
                                    String InputMiner = String.valueOf(toDouble);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "-000" + InputMiner);
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR1-000" + InputMiner;
                                    CR1 = "-000" + InputMiner;
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog6.dismiss();
                                }
                            }


                        }
                    });

                    break;
                case "濕度顏色轉換":
                    swInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
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
                            if (toDouble < 0) {
                                edInput.setText("0");
                            } else if (toDouble > 100) {
                                edInput.setText("100");
                            } else {
                                if (toDouble <= 9) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+000" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR2+000" + Input + ".0";
                                    CR2 = "+000" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog7.dismiss();
                                } else if (toDouble >= 10 && toDouble <= 99) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+00" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR2+00" + Input + ".0";
                                    CR2 = "+00" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog7.dismiss();
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("name", nameItems[position]);
                                    hashMap.put("values", "+0" + Input + ".0");
                                    arrayList.set(position, hashMap);
                                    String[] from = {"name", "values"};
                                    int[] to = {R.id.TitleName, R.id.ResultValue};
                                    simpleAdapter =
                                            new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                    SimpleListView.setAdapter(simpleAdapter);
                                    FromDataDisplaySendValue = "CR2+0" + Input + ".0";
                                    CR2 = "+0" + Input + ".0";
                                    mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                                    dialog7.dismiss();
                                }
                            }

                        }
                    });

                    break;
                case "警報聲":
                    if (SPK.contains("on")) {
                        swInput.setChecked(true);
                    } else {
                        swInput.setChecked(false);
                    }
                    edInput.setVisibility(View.GONE);
                    swInputDP1.setVisibility(View.GONE);
                    swInputDP2.setVisibility(View.GONE);
                    mBuilder.setTitle(GetName);

                    swInput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("name", nameItems[position]);
                                hashMap.put("values", "on");
                                arrayList.set(position, hashMap);
                                String[] from = {"name", "values"};
                                int[] to = {R.id.TitleName, R.id.ResultValue};
                                simpleAdapter =
                                        new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                SimpleListView.setAdapter(simpleAdapter);
                                FromDataDisplaySendValue = "SPK+0001.0";
                                SPK = "on";
                                mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
                            } else {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("name", nameItems[position]);
                                hashMap.put("values", "off");
                                arrayList.set(position, hashMap);
                                String[] from = {"name", "values"};
                                int[] to = {R.id.TitleName, R.id.ResultValue};
                                simpleAdapter =
                                        new SimpleAdapter(getBaseContext(), arrayList, R.layout.style_listview, from, to);
                                SimpleListView.setAdapter(simpleAdapter);
                                FromDataDisplaySendValue = "SPK+0000.0";
                                SPK = "off";
                                mBluetoothLeService.setCharacteristicNotification(DeviceControlActivity.theData, true);
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
                drawerLayout.openDrawer(GravityCompat.START);
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
        if (data.contains("SPK+0001.0")) {
            SPK = "on";
        } else if (data.contains("SPK+0000.0")) {
            SPK = "off";
        } else if (data.contains("DP1+0001.0")) {
            DP1 = "on";
        } else if (data.contains("DP1+0000.0")) {
            DP1 = "off";
        } else if (data.contains("DP2+0001.0")) {
            DP2 = "on";
        } else if (data.contains("DP2+0000.0")) {
            DP2 = "off";
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

