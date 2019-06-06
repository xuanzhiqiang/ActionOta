package com.linkplay.actionota.ble.MultipleBle.jlota;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.util.Log;

import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.IActionCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.linkplay.actionota.ble.MultipleBle.BleManager;

public class JLOtaManager extends BluetoothOTAManager {


    private static final String TAG = "JLOtaManager";

    private BleManager bleManager;
    private static JLOtaManager otaManager;

    private boolean isqQeryMandatoryUpdate = false;

    private int BTStatus = 0;

    public int getBTStatus() {
        return BTStatus;
    }

    public void setBTStatus(int BTStatus) {
        this.BTStatus = BTStatus;
    }

    private JLOtaManager(Context context) {
        super(context);
    }

    public static JLOtaManager getInstance(Context context){
        if (otaManager == null){
            synchronized (JLOtaManager.class){
                if (otaManager == null){
                    otaManager = new JLOtaManager(context);
                }
            }
        }
        return otaManager;
    }

    @Override
    public void queryMandatoryUpdate(final IActionCallback<TargetInfoResponse> callback) {
        isqQeryMandatoryUpdate = true;
        super.queryMandatoryUpdate(new IActionCallback<TargetInfoResponse>() {
            @Override
            public void onSuccess(TargetInfoResponse targetInfoResponse) {
                isqQeryMandatoryUpdate = false;
                callback.onSuccess(targetInfoResponse);
            }

            @Override
            public void onError(BaseError baseError) {
                isqQeryMandatoryUpdate = false;
                callback.onError(baseError);
            }
        });
    }

    public boolean isQeryMandatoryUpdate() {
        return isqQeryMandatoryUpdate;
    }


    @Override
    public BluetoothDevice getConnectedDevice() {
        if(getBTStatus() != StateCode.CONNECTION_OK){
            return null;
        }
        return bleManager.getCurrentDevice();
    }

    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {
        if(getBTStatus() != StateCode.CONNECTION_OK){
            return null;
        }
        return bleManager.getDeviceGatt();
    }

    @Override
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
    }

    @Override
    public void disconnectBluetoothDevice(BluetoothDevice bluetoothDevice) {
    }

    @Override
    public void onReceiveDeviceData(BluetoothDevice device, byte[] data) {
        super.onReceiveDeviceData(device, data);
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {
        BluetoothDevice currentDevice = bleManager.getCurrentDevice();
        if (currentDevice != null) {
            String address = currentDevice.getAddress();
            if (mSendDataListener != null){
                mSendDataListener.onSendData(bytes);
            }
            return bleManager.sendDataJL(address, bytes);
        }
        return false;
    }

    @Override
    public void errorEventCallback(BaseError baseError) {
        Log.e(TAG, baseError.toString());
    }

    @Override
    public void release() {
        super.release();
        otaManager = null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // 升级流程
    ///////////////////////////////////////////////////////////////////////////


    // 1、初始化
    public synchronized static JLOtaManager initOTAManager(Context context, BleManager bleManager){
        if (bleManager == null){
            throw new RuntimeException("bleManager  cannot be empty");
        }

        BluetoothOTAConfigure configure = new BluetoothOTAConfigure();
        configure.setUseAuthDevice(false);
        configure.setMtu(bleManager.getMTU());
        configure.setPriority(BluetoothOTAConfigure.PREFER_BLE);
        configure.setBleIntervalMs(500); //默认是500毫秒
        configure.setTimeoutMs(2000); //超时时间
        byte[] scanData = "JLAISDK".getBytes();//根据固件配置
        configure.setScanFilterData(new String(scanData));
        configure.setUseJLServer(false);

        otaManager = getInstance(context);
        otaManager.bleManager = bleManager;
        otaManager.configure(configure); //设置OTA参数

        Log.i(TAG, "---- initOTAManager -----");

        return otaManager;
    }


    // 2、开始升级
    public void startOTA(IUpgradeCallback callback){
        super.startOTA(callback);
    }


    public void setOtaListener(SendDataListener sendDataListener) {
        this.mSendDataListener = sendDataListener;
    }

    private SendDataListener mSendDataListener;

    public void setSendDataListener(SendDataListener mSendDataListener) {
        this.mSendDataListener = mSendDataListener;
    }

    public interface SendDataListener{
        void onSendData(byte[] data);
    }
}
