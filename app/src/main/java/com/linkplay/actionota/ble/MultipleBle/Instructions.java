package com.linkplay.actionota.ble.MultipleBle;

public class Instructions {



    // OontZ 指令


    public final static String[] ACTIVE_INSTRUCTION= {
            "0C80", // OP_LINK_CMD : 0x800c : 设备主动断开BLE
    };


    /**
     *  数据长度2字节：
     *  1字节 bass:   { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }
     *  1字节 treble： { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }
     */
    private final static byte[] OP_SET_EQ = { 0x00, 0x70, 0x01, (byte) 0x80, 0x02, 0x00, 0x00, 0x00};

    public static byte[] getOpSetEq(byte bass, byte treble){
        OP_SET_EQ[6] = bass;
        OP_SET_EQ[7] = treble;
        return OP_SET_EQ;
    }

    /**
     *  GET 指令， 无需发送数据
     *  响应数据格式： 长度2字节
     *  1字节 bass:   { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }
     *  1字节 treble： { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }
     */
    public final static byte[] OP_GET_EQ = {0x00, 0x70, 0x02, (byte) 0x80, 0x00, 0x00};


    /**
     *  数据长度4字节：
     *  1字节 sync:   { 0x00, 0x01 , ... , 0x1F }
     *  1字节 left： { 0x00, 0x01 , ... , 0x1F }
     *  1字节 right: { 0x00, 0x01 , ... , 0x1F }
     */
    private final static byte[] OP_SET_VOLUME = {0x00, 0x70, 0x03, (byte) 0x80, 0x03, 0x00, 0x00, 0x00, 0x00};

    public static byte[] getOpSetVolume(byte sync, byte left, byte right) {
        OP_SET_VOLUME[6] = sync;
        OP_SET_VOLUME[7] = left;
        OP_SET_VOLUME[8] = right;
        return OP_SET_VOLUME;
    }

    /**
     *  GET 指令， 无需发送数据
     *  响应数据格式：长度3字节 0xFF 表示没有设备
     *  1字节 sync:   { 0x00, 0x01 , ... , 0x1F }
     *  1字节 left： { 0x00, 0x01 , ... , 0x1F }
     *  1字节 right: { 0x00, 0x01 , ... , 0x1F }
     */
    public final static byte[] OP_GET_VOLUME = {0x00, 0x70, 0x04, (byte) 0x80, 0x00, 0x00};



    /**
     *  数据长度2字节：
     *  类型1字节 type:   { 0x00：Dual Stereo, 0x01：Left/Right }
     *  动作1字节 action：{ 0x00：disconnect, 0x01：connect }
     */
    private final static byte[] OP_SET_SOUND_MODE = {0x00, 0x70, 0x05, (byte) 0x80, 0x02, 0x00, 0x00, 0x00};

    public static byte[] getOpSetSoundMode(byte type, byte action) {
        OP_SET_SOUND_MODE[6] = type;
        OP_SET_SOUND_MODE[7] = action;
        return OP_SET_SOUND_MODE;
    }

    /**
     *  GET 指令， 无需发送数据
     *  响应数据格式： 长度2字节
     *      type(2byte) : { 0x0000：Dual Stereo, 0x0001：Left/Right }
     */
    public final static byte[] OP_GET_SOUND_MODE = {0x00, 0x70, 0x06, (byte) 0x80, 0x00, 0x00};


    /**
     *  数据长度2字节：
     *  类型2字节 type:   { 0x0000：Aux In or Optical, 0x0001：Mix Both, 0x0002：Bluetooth }
     */
    private final static byte[] OP_SET_AUDIO_SOURCE = {0x00, 0x70, 0x07, (byte) 0x80, 0x02, 0x00, 0x00, 0x00};

    public static byte[] getOpSetAudioSource(byte type, byte MIX) {
        OP_SET_AUDIO_SOURCE[6] = type;
        OP_SET_AUDIO_SOURCE[7] = MIX;
        return OP_SET_AUDIO_SOURCE;
    }


    /**
     *  GET 指令， 无需发送数据
     *  响应数据格式： 长度2字节
     *      type(2byte) : { 0x0000：Aux In or Optical, 0x0001：Mix Both, 0x0002：Bluetooth }
     */
    public final static byte[] OP_GET_AUDIO_SOURCE = {0x00, 0x70, 0x08, (byte) 0x80, 0x00, 0x00};


    /**
     *  GET 指令， 无需发送数据
     *  响应数据格式： 标准JSON
     *      [{“devicename”:”xxx”,”version”:”xxx”，...},{“devicename”:”xxx”,”version”:”xxx”，...}...]
     */
    public final static byte[] OP_GET_DEVICE_INFO = {0x00, 0x70, 0x09, (byte) 0x80, 0x00, 0x00};


    private final static byte[] OP_START_OTA = {0x00, 0x70, 0x0a, (byte) 0x80, 0x02, 0x00, 0x00, 0x00};
    public static byte[] getOpStartOta(byte index, byte type) {
        OP_START_OTA[6] = index;
        OP_START_OTA[7] = type;
        return OP_START_OTA;
    }


    /**
     * 获取机器支持的功能
     */
    public final static byte[] OP_GET_DEVICE_FEATURE = {0x00, 0x70, 0x0b, (byte) 0x80, 0x00, 0x00};


    /**
     * 设置开关
     */
    private final static byte[] OP_SET_POWER_ON_OFF = {0x00, 0x70, 0x0e, (byte) 0x80, 0x01, 0x00, 0x00};
    public static byte[] getOpSetPowerOnOff(byte type) {
        OP_SET_POWER_ON_OFF[6] = type;
        return OP_SET_POWER_ON_OFF;
    }


    /**
     * 获取开关状态
     */
    public final static byte[] OP_GET_POWER_ON_OFF = {0x00, 0x70, 0x0f, (byte) 0x80, 0x00, 0x00};


    public final static byte[] OP_START_ACTION_OTA = {0x02, 0x02, 0x01};
    public final static byte[] OP_STOP_ACTION_OTA = {0x02, 0x02, 0x02};



}
