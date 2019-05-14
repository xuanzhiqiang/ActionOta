package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import com.linkplay.bluetooth_utils2.api.BTUtilApi;

import java.util.ArrayList;
import java.util.UUID;

public class LP_BTClient implements BTUtilApi {

    private BLEConnect bleConnect;

    private LP_BTClient(Context context) {
        bleConnect = new BLEConnect(context);
    }

    private static LP_BTClient INSTANCE = null;


    public static LP_BTClient init(Context context){
        if (INSTANCE == null) {
            synchronized (LP_BTClient.class){
                if (INSTANCE == null) {
                    INSTANCE = new LP_BTClient(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }


    public static boolean isBleDevice(@NonNull BluetoothDevice device) {
        int type = device.getType();
        return type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL;
    }


    public static boolean deviceEquals(BluetoothDevice device1, BluetoothDevice device2) {
        return (null != device1 && null != device2) && device1.getAddress().equals(device2.getAddress());
    }


    @Override
    public boolean enable() {
        return bleConnect.enable();
    }

    @Override
    public boolean disable() {
        return bleConnect.disable();
    }

    @Override
    public boolean isEnabled() {
        return bleConnect.isEnabled();
    }

    @Override
    public BluetoothDevice getRemoteDevice(String address) {
        return bleConnect.getRemoteDevice(address);
    }

    @Override
    public ArrayList<String> getConnectBTDeviceAddress() {
        return bleConnect.getConnectBTDeviceAddress();
    }

    @Override
    public boolean startScan() {
        return bleConnect.startScan();
    }

    @Override
    public boolean stopScan() {
        return bleConnect.stopScan();
    }

    @Override
    public void addScanFilterForServiceUUID(ParcelUuid serviceUuid) {
        bleConnect.addScanFilterForServiceUUID(serviceUuid);
    }

    @Override
    public void clearFilter() {
        bleConnect.clearFilter();
    }

    @Override
    public boolean connect(String address) {
        return bleConnect.connect(address);
    }

    @Override
    public boolean disconnect(String address) {
        return bleConnect.disconnect(address);
    }

    @Override
    public boolean writeDataToBLEDevice(String address, UUID serviceUUID, UUID characteristicUUID, byte[] writeData) {
        return bleConnect.writeDataToBLEDevice(address, serviceUUID, characteristicUUID, writeData);
    }

    @Override
    public void setLogEnable(boolean logEnable) {
        bleConnect.setLogEnable(logEnable);
    }

    @Override
    public void logRedirection(Dispatcher.LogRedirection logRedirection) {
        bleConnect.logRedirection(logRedirection);
    }

    @Override
    public boolean registerBluetoothCallback(LP_BluetoothListener callback) {
        return bleConnect.registerBluetoothCallback(callback);
    }

    @Override
    public boolean unregisterBluetoothCallback(LP_BluetoothListener callback) {
        return bleConnect.unregisterBluetoothCallback(callback);
    }

    @Override
    public BluetoothGatt getBluetoothGatt(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            return bleConnect.getBluetoothGatt(bluetoothDevice.getAddress());
        }
        return null;
    }

    @Override
    public void setIntervalTime(int intervalTime) {
        bleConnect.setIntervalTime(intervalTime);
    }
}
