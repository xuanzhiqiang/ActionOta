package com.linkplay.bluetooth_utils2;

public class BaseCode {

    //ble已连接
    public static final int LP_BLE_STATE_CONNECTED = 0;
    //ble未连接
    public static final int LP_BLE_STATE_NOT_CONNECTED = 1;
    //ble连接中
    public static final int LP_BLE_STATE_CONNECTING = 2;
    //不合法设备
    public static final int LP_BLE_STATE_INVALID = 3;
    //蓝牙未连接
    public static final int LP_BLE_STATE_NOBT = 4;
    //蓝牙未打开
    public static final int LP_BLE_STATE_BT_UNPOWER = 5;
    //ble连接超时
    public static final int LP_BLE_STATE_CONNECT_TIMEOUT = 6;

}
