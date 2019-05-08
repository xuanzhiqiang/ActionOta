package com.linkplay.actionota.ble.interfaces;



import com.linkplay.bluetooth_utils2.LP_BLEDevice;

import java.util.UUID;

public interface MultipleBleDeviceListener {


    void onStartScan();

    void onStopScan();

    void onFound(LP_BLEDevice bleDevice);

    void onCharacteristicChanged(String address, UUID serviceUuid, UUID characteristicUuid, byte[] data);

    void onStateChange(LP_BLEDevice device);

    void startOta(LP_BLEDevice device);

    int OTA_SUCCESS = 1;
    int OTA_FAIL = 2;

    void stopOta(LP_BLEDevice device, int state);
    void otaProgress(LP_BLEDevice device, double progress);


    void onCommandResponse(int errorCode, int commandCode, int parameterLength, byte[] data) throws Exception;

    void onCommand(String address, int command, int length, byte[] parameter);
}
