package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


import java.util.ArrayList;
import java.util.UUID;

public class Dispatcher extends BaseCode {

    private static final String TAG = "Dispatcher";

    Context mContext;
    Handler mHandler = new Handler();
    private LogRedirection logRedirection;
    private boolean logEnable = true;

    private final ArrayList<LP_BluetoothListener> mCallbackList = new ArrayList<>();

    Dispatcher(Context context) {
        this.mContext = context;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    void logRedirection(LogRedirection logRedirection) {
        this.logRedirection = logRedirection;
    }

    public interface LogRedirection{
        void log(String TAG, String message);
        void loge(String TAG, String message);
    }


    void log(String TAG, String message) {
        if (this.logEnable) {
            if (logRedirection != null){
                logRedirection.log(TAG, message);
            }else{
                Log.d(TAG, message);
            }
        }
    }

    void loge(String TAG, String message) {
        if (this.logEnable) {
            if (logRedirection != null){
                logRedirection.loge(TAG, message);
            }else{
                Log.e(TAG, message);
            }
        }
    }


    boolean registerBluetoothCallback(LP_BluetoothListener callback) {
        if (null != callback && !this.mCallbackList.contains(callback)) {
            this.mCallbackList.add(callback);
            this.log(TAG, "registerBluetoothCallback: " + callback);
            return true;
        }
        return false;
    }


    boolean unregisterBluetoothCallback(LP_BluetoothListener callback) {
        if (callback != null && this.mCallbackList.contains(callback)) {
            this.mCallbackList.remove(callback);
            this.log(TAG, "unregisterBluetoothCallback: " + callback);
            return true;
        }
        return false;
    }


    void postDelayed(Runnable runnable, long delayMillis){
        mHandler.postDelayed(runnable, delayMillis);
    }


    /**
     * 蓝牙状态
     * @param open true： 开启
     */
    void onAdapterStatus(final boolean open) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Dispatcher.this.log(TAG, "onAdapterStatus: " + open);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onAdapterStatus(open);
                }
            }
        });
    }

    void onBluetoothStatus(final boolean bEnabled, final boolean bHasBle) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                log(TAG, "onBluetoothStatus: " + bEnabled + " has BLE: " + bHasBle);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onBluetoothStatus(bEnabled, bHasBle);
                }
            }
        });
    }


    void onDiscoveryBleStatus(final boolean bStart) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Dispatcher.this.log(TAG, "onDiscoveryStatus: " + bStart);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onDiscoveryBleStatus(bStart);
                }
            }
        });
    }

    void onDiscoveryBle(final BluetoothDevice device, final byte[] scanRecord, final int rssi) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {

                Dispatcher.this.log(TAG, "onDiscovery: " + device.getName() + "  rssi: " + rssi);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onDiscoveryBle(device, scanRecord, rssi);
                }
            }
        });
    }


    void onBleConnection(final BluetoothDevice device, final int status) {
        this.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Dispatcher.this.log(TAG, "onBleConnection: " + device.getName() + " status: " + status);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onBleConnection(device, status);
                }
            }
        }, 100);
    }


    void onBleDataBlockChanged(final BluetoothDevice device, final int block) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Dispatcher.this.log(TAG, "onBleNotificationStatus: " + device.getName() + " block: " + block);
                for (LP_BluetoothListener callback : Dispatcher.this.mCallbackList) {
                    callback.onBleDataBlockChanged(device, block);
                }
            }
        });
    }

    void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
        log(TAG, "onBleDataNotification: " + serviceUuid + ":" + characteristicsUuid + ":" + device.getAddress());
        for (LP_BluetoothListener callback : this.mCallbackList) {
            callback.onBleDataNotification(device, serviceUuid, characteristicsUuid, data);
        }
    }

    void onDescriptorWriteResult(UUID characteristicsUuid, int state){
        for (LP_BluetoothListener callback : this.mCallbackList) {
            callback.onDescriptorWriteResult(characteristicsUuid, state);
        }
    }

}
