package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

public abstract class LP_BluetoothListener {

    public LP_BluetoothListener() {
    }

    public void onBluetoothStatus(boolean bEnabled, boolean bHasBle) {

    }

    public void onDiscoveryBleStatus(boolean bStart) {

    }

    public void onDiscoveryBle(BluetoothDevice device, byte[] scanRecord, final int rssi) {

    }


    public void onBleConnection(BluetoothDevice device, int status) {

    }


    public void onBleDataBlockChanged(BluetoothDevice device, int block) {

    }

    public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {

    }


    public void onAdapterStatus(boolean open) {

    }

    public void onDescriptorWriteResult(BluetoothGatt gatt, UUID characteristicsUuid, int state){

    }
}
