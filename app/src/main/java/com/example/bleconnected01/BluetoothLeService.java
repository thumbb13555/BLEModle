/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bleconnected01;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;//藍芽管理器
    private BluetoothAdapter mBluetoothAdapter;//藍芽適配器
    private String mBluetoothDeviceAddress;//藍芽設備位址
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    BluetoothGattCharacteristic characteristic;
    private String PV1, PV2, EH1, EL1, EH2, EL2, CR1, CR2, SPK, SPKO;
    private String Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9;
    private String mDeviceName;
    private String mDeviceAddress;




    private static final int STATE_DISCONNECTED = 0;//設備無法連接
    private static final int STATE_CONNECTING = 1;//設備正在連接
    private static final int STATE_CONNECTED = 2;//設備連接完畢

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";//已連接到GATT服務器
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";//未連接GATT服務器
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";//未發現GATT服務
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";//接收到來自設備的數據，可通過讀取或操作獲得

    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"; //其他數據

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public static String Service_uuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String Characteristic_uuid_TX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";//這是接收的
    public static String Characteristic_uuid_RX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";//這是發射的
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    /**
     * 通過BLE API的不同類型的連接方法
     */


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**當連接狀態發生改變*/
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {//當設備已連接
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//當設備無法連接
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        /**當發現新的服務器*/
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**讀寫特性*/
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v("BT", "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /**廣播更新*/
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            readCharacteristic(characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("BT", "AT onDescriptorRead. " + "descriptor: "
                    + descriptor.getUuid() + ". characteristic: "
                    + descriptor.getCharacteristic().getUuid()
                    + ". status: " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("BT", "花送成功");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            writeCustomCharacteristic();

        }
    };


    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * 當一個特定的回調函數被觸發時，他會調用相應的broadcastUpdate()輔助方法
     * 並傳遞給他一個action，以下官網範例為藍芽心率測量文件範例
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

            /**對於所有其他配置文件，以十六進制寫數據*/
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    /**
     * 在這裏讀取藍芽中數據
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        String record = characteristic.getStringValue(0);
        Log.v("BT", "readCharacteristic回傳: " + record);
//        SystemClock.sleep(100);


            if (record.contains("PV1")) {
                Name1 = record.substring(0, 3);
                Name1 = "溫度補正";
                PV1 = record.substring(3, 10);

            } else if (record.contains("PV2")) {
                Name2 = record.substring(0, 3);
                Name2 = "濕度補正";
                PV2 = record.substring(3, 10);

            } else if (record.contains("EH1")) {
                Name3 = record.substring(0, 3);
                Name3 = "溫度上限警報";
                EH1 = record.substring(3, 10);

            } else if (record.contains("EL1")) {
                Name4 = record.substring(0, 3);
                Name4 = "溫度下限警報";
                EL1 = record.substring(3, 10);

            } else if (record.contains("EH2")) {
                Name5 = record.substring(0, 3);
                Name5 = "濕度上限警報";
                EH2 = record.substring(3, 10);

            } else if (record.contains("EL2")) {
                Name6 = record.substring(0, 3);
                Name6 = "濕度下限警報";
                EL2 = record.substring(3, 10);

            } else if (record.contains("CR1")) {
                Name7 = record.substring(0, 3);
                Name7 = "溫度顏色轉換";
                CR1 = record.substring(3, 10);

            } else if (record.contains("CR2")) {
                Name8 = record.substring(0, 3);
                Name8 = "濕度顏色轉換";
                CR2 = record.substring(3, 10);

            } else if (record.contains("SPK")) {
                Name9 = record.substring(0, 3);
                Name9 = "警報聲";
                SPK = record.substring(4, 10);
                if (SPK.contains("0000.0")) {
                    SPKO = "off";
                } else {
                    SPKO = "on";
                }

            }
            if (record.contains("OVER")) {
                Intent intent = new Intent(this, DataDisplayActivity.class);
                intent.putExtra("PV1", Name1);
                intent.putExtra("PV1_Value", PV1);
                intent.putExtra("PV2", Name2);
                intent.putExtra("PV2_Value", PV2);
                intent.putExtra("EH1", Name3);
                intent.putExtra("EH1_Value", EH1);
                intent.putExtra("EH2", Name4);
                intent.putExtra("EH2_Value", EH2);
                intent.putExtra("EL1", Name5);
                intent.putExtra("EL1_Value", EL1);
                intent.putExtra("EL2", Name6);
                intent.putExtra("EL2_Value", EL2);
                intent.putExtra("CR1", Name7);
                intent.putExtra("CR1_Value", CR1);
                intent.putExtra("CR2", Name8);
                intent.putExtra("CR2_Value", CR2);
                intent.putExtra("SPK", Name9);
                intent.putExtra("SPK_Value", SPKO);
                startActivity(intent);

            }





    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        /**接收GATT通知:當設備上的特性(應該是Characteristic)改變時會通知BLE應用程序
         * 以下為顯示如何使用setCharacteristicNotification()給一個特性設置通知*/
        /**照前輩的說法這邊是開啟訂閱*/
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.v("BT","開始寫入或讀取程序(setCharacteristicNotification)");
        UUID ServiceUUID = UUID.fromString(Service_uuid);
        UUID TXUUID = UUID.fromString(Characteristic_uuid_TX);
        if (!mBluetoothGatt.equals(null)) {
            BluetoothGattService service = mBluetoothGatt.getService(ServiceUUID);
            if (service != null) {
                BluetoothGattCharacteristic chara = service.getCharacteristic(TXUUID);
                if (chara != null) {
                    boolean success = mBluetoothGatt.setCharacteristicNotification(chara, enabled);

                    BluetoothGattDescriptor descriptor = chara.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                    if (descriptor != null) {
                    }
                    if (enabled) {
                        Log.v("BT", "訂閱(enable)= " + enabled);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    } else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    /**加了這行就成功了; https://blog.csdn.net/kieven2008/article/details/56489900*/
                    for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(dp);
                    }
                    mBluetoothGatt.writeDescriptor(descriptor);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                }
            }
        }//All if
    }


    public void writeCustomCharacteristic() {
        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(Service_uuid));
        if (RxService == null) {
            Log.v("BT", "Rx service not found on RxService!");
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(Characteristic_uuid_RX));
        if (RxChar == null) {
            Log.v("BT", "Rx char not found on RxService!");
            return;
        }

        Log.v("BT","SendType = "+DeviceControlActivity.Sendtype);
        Log.v("BT","FromDataDisplaySendValue= "+DataDisplayActivity.FromDataDisplaySendValue);

            if(DeviceControlActivity.Sendtype != "") {
                byte[] strBytes = DeviceControlActivity.Sendtype.getBytes();
                RxChar.setValue(strBytes);
                mBluetoothGatt.writeCharacteristic(RxChar);
                DeviceControlActivity.Sendtype = "";
            }else if(DataDisplayActivity.FromDataDisplaySendValue != ""){
                byte[] strBytes = DataDisplayActivity.FromDataDisplaySendValue.getBytes();
                RxChar.setValue(strBytes);
                mBluetoothGatt.writeCharacteristic(RxChar);
                DataDisplayActivity.FromDataDisplaySendValue ="";
            }

    }



    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

}
