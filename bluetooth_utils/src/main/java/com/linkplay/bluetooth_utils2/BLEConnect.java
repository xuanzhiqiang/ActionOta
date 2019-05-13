package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;


class BLEConnect extends BLEDiscovery {

    private static final String TAG = "BLEConnect";

    private final List<String> mConnectedAddressList = new CopyOnWriteArrayList<>();
    private final Map<String, BluetoothGatt> mBluetoothGattMap = new ConcurrentHashMap<>();

    private int WRITE_DATA_BLOCK_SIZE = 20;
    private List<byte[]> mDatas = new ArrayList<>();

    BLEConnect(Context context) {
        super(context);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (gatt == null || gatt.getDevice() == null) return;

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            log(TAG, " [LP] onConnectionStateChange  "+device.getName()+"  newState: "+newState+"  status: "+status);

            mHandler.removeCallbacksAndMessages(address); // 取消超时消息

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED && !mConnectedAddressList.contains(gatt.getDevice().getAddress())){
                    loge(TAG, " [LP] requestMtu  515");
                    if (!gatt.requestMtu(515)) {
                        onBleConnection(device, LP_BLE_STATE_NOT_CONNECTED);
                    }
                }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                    log(TAG, " [LP] state_disconnected");
                    removeDevice(address);
                    onBleConnection(device, LP_BLE_STATE_NOT_CONNECTED);
                }
            }else{
                log(TAG, " [LP] state_disconnected  error");
                removeDevice(address);
                onBleConnection(device, LP_BLE_STATE_NOT_CONNECTED);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {// 会协商多次

            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS){
                WRITE_DATA_BLOCK_SIZE = mtu - 3;
                onBleDataBlockChanged(gatt.getDevice(), WRITE_DATA_BLOCK_SIZE);

                if (!mConnectedAddressList.contains(gatt.getDevice().getAddress())){
                    log(TAG, " [LP] ----- discoverServices -----");
                    if (!gatt.discoverServices()) {
                        log(TAG, " [LP] Attempting to start service discovery: false");
                        onBleConnection(gatt.getDevice(), LP_BLE_STATE_NOT_CONNECTED);
                    }
                }

            }else if(gatt != null){
                onBleConnection(gatt.getDevice(), LP_BLE_STATE_NOT_CONNECTED);
            }

        }


        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            if (gatt == null || status != BluetoothGatt.GATT_SUCCESS){
                loge(TAG, "displayGattServices:  status = "+status);
                return;
            }

            synchronized (mConnectedAddressList) {
                mConnectedAddressList.add(gatt.getDevice().getAddress());
                mBluetoothGattMap.put(gatt.getDevice().getAddress(), gatt);
            }

            String ServiceUUID;
            String uuid;
            int i = 1;
            // Loops through available GATT Services.
            for (final BluetoothGattService gattService : gatt.getServices()) {
                ServiceUUID = gattService.getUuid().toString();
                log(TAG, "displayGattServices: " + ServiceUUID);
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    log(TAG, "   Characteristic_uuid: " + uuid);
                    // 开启通知
                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        final UUID Characteristic_uuid = UUID.fromString(uuid);
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                log(TAG, "ENABLE NOTIFY : " + Characteristic_uuid + " : "
                                        + enableBLEDeviceNotification(gatt, gattService, gattCharacteristic));
                            }
                        }, i * 150);
                        i++;
                    }
                }
            }

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBleConnection(gatt.getDevice(), LP_BLE_STATE_CONNECTED);
                }
            }, i * 150);

        }


        private boolean enableBLEDeviceNotification(BluetoothGatt bluetoothGatt,
                                                    BluetoothGattService gattService,
                                                    BluetoothGattCharacteristic characteristic) {

            if (null == bluetoothGatt || null == gattService || null == characteristic) {
                return false;
            }

            if (bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                        bluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }

            return true;
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            final BluetoothDevice device = gatt.getDevice();
            final UUID serviceUuid = characteristic.getService().getUuid();
            final UUID characteristicUuid = characteristic.getUuid();
            onBleDataNotification(device, serviceUuid, characteristicUuid, data);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            if (null != characteristic) {
                UUID uuid = characteristic.getUuid();
                loge(TAG,"onDescriptorWrite  UUID: "+uuid.toString()+ " : " + (0 == status));
            }
        }

    };


    private void removeDevice(String address){
        log(TAG, "mConnectedAddressList.remove : "+ address);
        synchronized (mConnectedAddressList){
            mConnectedAddressList.remove(address);
            BluetoothGatt bluetoothGatt = mBluetoothGattMap.remove(address);
            if (bluetoothGatt != null)  bluetoothGatt.close();
        }
    }


    boolean connect(String address) {

        if (TextUtils.isEmpty(address)) return false;

        if (mBluetoothAdapter == null) {
            loge(TAG, "BleManager : " + "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (isScanning()) {
            stopScan();
        }

        final BluetoothDevice bluetoothDevice = getRemoteDevice(address);

        if (mConnectedAddressList.contains(address) && mBluetoothGattMap.containsKey(address)) {
            loge(TAG, "mConnectedAddressList.contains("+address+") == true; This is device already connected.");
            onBleConnection(bluetoothDevice, LP_BLE_STATE_CONNECTED);
            return false;
        }

        // getRemoteDevice(address) will throw an exception if the device address is invalid,
        // so it's necessary to check the address
        boolean isValidAddress = BluetoothAdapter.checkBluetoothAddress(address);
        if (!isValidAddress) {
            loge(TAG, "the device address is invalid");
            return false;
        }


        if (bluetoothDevice == null) {
            loge(TAG, "BleManager : " + "no device");
            return false;
        }

        onBleConnection(bluetoothDevice, LP_BLE_STATE_CONNECTING);

        Message obtain = Message.obtain(mHandler, new Runnable() {
            @Override
            public void run() {
                BLEConnect.this.onBleConnection(bluetoothDevice, LP_BLE_STATE_CONNECT_TIMEOUT);
            }
        });
        obtain.obj = bluetoothDevice.getAddress();
        mHandler.sendMessageDelayed(obtain, 15 * 1000); // 15 秒超时

        BluetoothGatt bluetoothGatt;
        bluetoothGatt = bluetoothDevice.connectGatt(mContext, false, mGattCallback, TRANSPORT_LE);

        return bluetoothGatt != null;
    }


    boolean disconnect(String address) {
        if (TextUtils.isEmpty(address)) return false;
        if (mBluetoothAdapter == null || mBluetoothGattMap.get(address) == null) {
            loge(TAG, "disconnect： BluetoothAdapter not initialized");
            return false;
        }
        log(TAG, "disconnect: "+ address);
        Objects.requireNonNull(mBluetoothGattMap.get(address)).disconnect();
        return true;
    }


    synchronized boolean writeDataToBLEDevice(String address, UUID serviceUUID, UUID characteristicUUID, final byte[] writeData) {

        if(TextUtils.isEmpty(address)){
            this.loge(TAG, "TextUtils.isEmpty(address)");
            return false;
        }

        if (null == writeData || 0 == writeData.length) {
            this.loge(TAG, "null == writeData || 0 == writeData.length");
            return false;
        }

        BluetoothGatt bluetoothGatt = mBluetoothGattMap.get(address);
        if (null == bluetoothGatt) {
            this.loge(TAG, "null == bluetoothGatt");
            return false;
        }

        BluetoothGattService gattService = bluetoothGatt.getService(serviceUUID);
        if (null == gattService) {
            this.loge(TAG, "null == gattService");
            return false;
        }

        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characteristicUUID);
        if (null == characteristic) {
            this.loge(TAG, "null == characteristic");
            return false;
        }

        // 根据MTU切割发送数据包
        this.mDatas.clear();
        int length = writeData.length;
        int iBlockCount = length / this.WRITE_DATA_BLOCK_SIZE;

        for(int i = 0; i < iBlockCount; ++i) {
            byte[] mBlockData = new byte[this.WRITE_DATA_BLOCK_SIZE];
            System.arraycopy(writeData, i * this.WRITE_DATA_BLOCK_SIZE, mBlockData, 0, mBlockData.length);
            this.mDatas.add(mBlockData);
        }

        if (0 != length % this.WRITE_DATA_BLOCK_SIZE) {
            byte[] noBlockData = new byte[length % this.WRITE_DATA_BLOCK_SIZE];
            System.arraycopy(writeData, length - length % this.WRITE_DATA_BLOCK_SIZE, noBlockData, 0, noBlockData.length);
            this.mDatas.add(noBlockData);
        }

        for (int i = 0; i < mDatas.size(); i++) {
            characteristic.setValue(mDatas.get(i));
            if(!bluetoothGatt.writeCharacteristic(characteristic)){
                return false;
            }
            try {
                Thread.sleep(10L);
            } catch (Exception ignored) { }
        }

        return true;
    }

    BluetoothGatt getBluetoothGatt(String address) {
        return mBluetoothGattMap.get(address);
    }
}
