package com.linkplay.actionota.ble.interfaces;


import com.linkplay.lpvr.lpvrbean.DeviceInformation;

public interface IBLEManager {


    boolean btIsConnectedByAddress(String address);

    void startScan();

    void stopScan() ;


    boolean sendData(String address, byte[] data) ;


    void connect(String address) ;


    void disconnect(String address) ;

    void disconnectAndClearAutoConnectionAddress(String address) ;


    void  clearListener();

    void addListener(MultipleBleDeviceListener multipleBleDeviceListener);

    DeviceInformation getDeviceInformation(String address);

    void clearAutoConnectionAddress();

    void getDeviceFeatureInfo(final String address);

}
