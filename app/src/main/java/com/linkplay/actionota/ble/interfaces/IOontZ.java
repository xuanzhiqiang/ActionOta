package com.linkplay.actionota.ble.interfaces;

public interface IOontZ {

    void setEq(String address, int bass, int treble);
    void getEq(String address);

    void setVolume(String address, int sync, int left, int right);
    void getVolume(String address);

    void setSoundMode(String address, int type, int action);
    void getSoundMode(String address);

    void setAudioSource(String address, int type, int MIX);
    void getAudioSource(String address);

    void startOta(String address, int index, int type);

    void getDeviceFeature(String address);

    void powerOnOff(String address, int type);
    void getPowerOnOffState(String address);

}
