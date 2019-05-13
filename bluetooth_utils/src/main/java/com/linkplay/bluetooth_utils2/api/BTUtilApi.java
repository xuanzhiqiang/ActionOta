package com.linkplay.bluetooth_utils2.api;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.ParcelUuid;

import com.linkplay.bluetooth_utils2.Dispatcher;
import com.linkplay.bluetooth_utils2.LP_BluetoothListener;

import java.util.ArrayList;
import java.util.UUID;

public interface BTUtilApi {


    /**
     * 开启蓝牙
     */
    boolean enable();


    /**
     * 关闭蓝牙
     */
    boolean disable();


    /**
     * 蓝牙是否开启
     */
    boolean isEnabled();


    /**
     * 获取蓝牙设备
     * @param address 设备的MAC地址
     */
    BluetoothDevice getRemoteDevice(String address);


    /**
     * @return 已链接 BT 的蓝牙设备MAC地址集合
     */
    ArrayList<String> getConnectBTDeviceAddress();



    boolean startScan();
    boolean stopScan();
    void addScanFilterForServiceUUID(ParcelUuid serviceUuid);
    void clearFilter();


    boolean connect(String address);
    boolean disconnect(String address);
    boolean writeDataToBLEDevice(String address, UUID serviceUUID, UUID characteristicUUID, final byte[] writeData);

    BluetoothGatt getBluetoothGatt(BluetoothDevice bluetoothDevice);

    /**
     * @param logEnable 是否开启log， 默认开启
     */
    void setLogEnable(boolean logEnable);

    /**
     * 重定向log
     */
    void logRedirection(Dispatcher.LogRedirection logRedirection);

    /**
     * 注册监听
     */
    boolean registerBluetoothCallback(LP_BluetoothListener callback);

    /**
     * 注销监听
     */
    boolean unregisterBluetoothCallback(LP_BluetoothListener callback);


}
