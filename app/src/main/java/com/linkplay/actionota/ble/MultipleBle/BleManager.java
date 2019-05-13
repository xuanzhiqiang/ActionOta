package com.linkplay.actionota.ble.MultipleBle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.linkplay.actionota.ble.interfaces.MultipleBleDeviceListener;
import com.linkplay.bluetooth_utils2.BaseCode;
import com.linkplay.bluetooth_utils2.LP_BLEDevice;
import com.linkplay.bluetooth_utils2.LP_BTClient;
import com.linkplay.bluetooth_utils2.LP_BluetoothListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BleManager {

    private static final String TAG = "MultipleBleManager";

    private List<MultipleBleDeviceListener> mListeners = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private LP_BTClient mlpBluetooth;
    private int mtu = 512;
    private BluetoothDevice mCurrentDevice;
    private LP_BLEDevice mCurrentLPDevice;
    private final ArrayList<LP_BLEDevice> mScanDevices = new ArrayList<>();
    private final ArrayList<LP_BLEDevice> mConnectingDevices = new ArrayList<>();


    private Handler mReceiveDataHandler;
    private Handler mWriteDataHandler;

    private final UUID UUID_SERVICE = UUID.fromString(BleConfig.instruction_service_uuid);
    private final UUID UUID_NOTIFYS = UUID.fromString(BleConfig.instruction_notifys_uuid);
    private final UUID UUID_WRITE = UUID.fromString(BleConfig.instruction_writes_uuid);  // 用于发送数据到设备

    private final UUID JL_OTA_UUID_SERVICE = UUID.fromString(BleConfig.jl_ota_service_uuid);
    private final UUID JL_OTA_UUID_WRITE = UUID.fromString(BleConfig.jl_ota_writes_uuid);
    private final UUID JL_OTA_UUID_NOTIFYS = UUID.fromString(BleConfig.jl_ota_notifys_uuid);

    private SharedPreferences autoConnectionSP;
    private final static String AUTO_CONNECTION_ADDRESS = "auto_connection_address";

    @SuppressLint("StaticFieldLeak")
    private static BleManager INSTANCE = null;
    private Object autoConnectToken = new Object();


    private BleManager(Context context) {

        HandlerThread mReceiveDataThread = new HandlerThread("ReceiveDataThread:");
        mReceiveDataThread.start();
        HandlerThread mWriteDataThread = new HandlerThread("WriteDataThread:");
        mWriteDataThread.start();
        mReceiveDataHandler = new Handler(mReceiveDataThread.getLooper());
        mWriteDataHandler = new Handler(mWriteDataThread.getLooper());

        mlpBluetooth = LP_BTClient.init(context);
        mlpBluetooth.enable();

        LP_BluetoothListener mLP_BluetoothListener = new LP_BluetoothListener() {

            @Override
            public void onDiscoveryBleStatus(boolean bStart) {
                for (MultipleBleDeviceListener listener : mListeners) {
                    if (bStart) {
                        listener.onStartScan();
                    } else {
                        listener.onStopScan();
                    }
                }
            }

            @Override
            public void onDiscoveryBle(BluetoothDevice device, byte[] scanRecord, int rssi) {
                super.onDiscoveryBle(device, scanRecord, rssi);

                if (device == null || scanRecord == null)
                    return;

                final LP_BLEDevice bleDevice = new LP_BLEDevice(device);
                String name = bleDevice.getName();
                Log.i(TAG, "scan :   ---->    name: " + name + " mac: "+ bleDevice.getAddress());
                if (!mScanDevices.contains(bleDevice)) {

                // TODO  解析广播包： 区分设备类型
//                if (scanRecord.length > 0) {
//                    byte[] data = Arrays.copyOfRange(scanRecord, 0, scanRecord.length);
//
//                    String receivedPackage = HexUtil.encodeHexStr(scanRecord, false);
//                    Log.i(TAG, "name: " + name + "  receivedPackage：  " + receivedPackage);
//
//                    try {
//                        // 参考 OontZ 广播包协议
//                        int len;
//                        while ((len = data[0] & 0xFF) > 0) {
//                            Log.i(TAG, "len === " + len);
//
//                            if ((data[1] & 0xFF) == 0xFF) {
//                                data = Arrays.copyOfRange(data, 0, len + 1);
//
//                                // BT的 MAC地址
//                                byte[] macBytes = Arrays.copyOfRange(data, 4, 10);
//                                String mac = HexUtil.encodeHexStr(macBytes, false);
//                                bleDevice.setMac(mac);
//                                 Log.i(TAG,"mac：  "+ mac);
//
//                                // int version = (data[11]&0xFF)<<8 | (data[10]&0xFF);    // 版本信息
//                                int customerID;
//                                if (len == 0x09) { // 兼容老版本
//                                    customerID = 30;
//                                } else {
//                                    customerID = (data[13] & 0xFF) << 8 | data[12] & 0xFF;
//                                }
//
//                                bleDevice.setCustomerID(customerID);
//                                break;
//
//                            } else {
//                                data = Arrays.copyOfRange(data, len + 1, data.length);
//                            }
//
//                        }
                        synchronized (mScanDevices) {
                            mScanDevices.add(bleDevice);
                        }
                        for (MultipleBleDeviceListener listener : mListeners) {
                            listener.onFound(bleDevice);
                        }
//
                        String autoConnectionAddress = getAutoConnectionAddress();
                        if (TextUtils.equals(autoConnectionAddress, bleDevice.getAddress())) {
                            autoConnect(bleDevice.getAddress());
                        }
//
//                    } catch (Exception ignored) {
//                        ignored.printStackTrace();
//                    }
//                }

                }
            }

            @Override
            public void onBleConnection(final BluetoothDevice device, int status) {
                Log.i(TAG, device.getName() + " onBleConnection :  " + status);

                LP_BLEDevice scanBleDevice = getScanBleDevice(device.getAddress());
                if (scanBleDevice == null){
                    scanBleDevice = getConnectingBleDevice(device.getAddress());
                }
                if (scanBleDevice != null){
                    scanBleDevice.setState(status);

                    if (status == BaseCode.LP_BLE_STATE_CONNECTED) {
                        synchronized (mConnectingDevices) {
                            mConnectingDevices.remove(scanBleDevice);
                            mCurrentLPDevice = scanBleDevice;
                            mCurrentDevice = device;
                        }
                    }else if(status == BaseCode.LP_BLE_STATE_CONNECTING){
                        synchronized (mConnectingDevices) {
                            mScanDevices.remove(scanBleDevice);
                            mConnectingDevices.add(scanBleDevice);
                        }
                    }

                    for (MultipleBleDeviceListener listener : mListeners) {
                        listener.onStateChange(scanBleDevice);
                    }

                }else{
                    mCurrentDevice = null;
                    if (mCurrentLPDevice != null) {
                        mCurrentLPDevice.setState(status);
                        if(status == BaseCode.LP_BLE_STATE_NOT_CONNECTED
                                && !mScanDevices.contains(mCurrentLPDevice)
                                && !mConnectingDevices.contains(mCurrentLPDevice)){

                            synchronized (mScanDevices) {
                                mScanDevices.add(mCurrentLPDevice);
                            }
                            for (MultipleBleDeviceListener listener : mListeners) {
                                listener.onFound(mCurrentLPDevice);
                            }

                        }
                        for (MultipleBleDeviceListener listener : mListeners) {
                            listener.onStateChange(mCurrentLPDevice);
                        }
                    }
                    mCurrentLPDevice = null;
                }
            }

            @Override
            public void onBleDataBlockChanged(BluetoothDevice device, int block) {
                Log.i(TAG, device.getName() + " 修改MTU： " + block);
                BleManager.this.mtu = block;
                for (MultipleBleDeviceListener listener : mListeners) {
                    listener.onMTUChange(block);
                }
            }


            @Override
            public void onBleDataNotification(final BluetoothDevice device, final UUID serviceUuid, final UUID characteristicsUuid, final byte[] data) {
                mReceiveDataHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "mReceiveDataHandler run: 开始处理接收到数据");
                        for (MultipleBleDeviceListener listener : mListeners) {
                            listener.onCharacteristicChanged(device.getAddress(), serviceUuid, characteristicsUuid, data);
                        }
                    }
                });
            }

        };
        mlpBluetooth.registerBluetoothCallback(mLP_BluetoothListener);


        autoConnectionSP = context.getSharedPreferences("AutoConnection", Context.MODE_PRIVATE);
    }



    public static BleManager init(Context context) {
        if (INSTANCE == null) {
            synchronized (BleManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BleManager(context);
                }
            }
        }
        return INSTANCE;
    }


    private String getAutoConnectionAddress(){
        return autoConnectionSP.getString(AUTO_CONNECTION_ADDRESS, null);
    }

    private void updateAutoConnectionAddress(String address){
        autoConnectionSP.edit().putString(AUTO_CONNECTION_ADDRESS, address).apply();
    }

    public void clearAutoConnectionAddress(){
        Log.i(TAG, "clearAutoConnectionAddress -------");
        autoConnectionSP.edit().remove(AUTO_CONNECTION_ADDRESS).apply();
    }

    private LP_BLEDevice getScanBleDevice(String address) {
        synchronized (mScanDevices) {
            for (LP_BLEDevice device : mScanDevices) {
                if (device.getAddress().equals(address))
                    return device;
            }
            return null;
        }
    }
    private LP_BLEDevice getConnectingBleDevice(String address) {
        synchronized (mConnectingDevices) {
            for (LP_BLEDevice device : mConnectingDevices) {
                if (device.getAddress().equals(address))
                    return device;
            }
            return null;
        }
    }


    public boolean btIsConnectedByAddress(String btAddress) {
        Log.i(TAG, "btIsConnectedByAddress:  btAddress= " + btAddress);
        return mlpBluetooth.getConnectBTDeviceAddress().contains(btAddress);
    }

    public void clearListener() {
        this.mListeners.clear();
    }

    public void addListener(MultipleBleDeviceListener listener) {
        this.mListeners.add(listener);
    }


    public void startScan() {
        mlpBluetooth.clearFilter();

        for (String serviceUuid : BleConfig.support_service_uuid) {
            mlpBluetooth.addScanFilterForServiceUUID(ParcelUuid.fromString(serviceUuid));
        }

        synchronized (mScanDevices) {
            mScanDevices.clear();
        }
        boolean start = mlpBluetooth.startScan();
        Log.i(TAG,"startScan is " + start);
    }

    public void stopScan() {
        boolean stop = mlpBluetooth.stopScan();
        Log.i(TAG, "stopScan is " + stop);
    }

    private void autoConnect(final String address){

        if(TextUtils.isEmpty(address)) return;

        handler.removeCallbacksAndMessages(autoConnectToken);
        Message message = Message.obtain(handler, new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "autoConnect:  "+ address);
                connect(address);
            }
        });
        message.obj = autoConnectToken;
        handler.sendMessageDelayed(message, 1000);

    }

    public void connect(String address) {
        boolean connect = mlpBluetooth.connect(address);
        Log.i(TAG, "connect: " + address + " is " + connect);
    }


    public void disconnectAndClearAutoConnectionAddress(String address){
        updateAutoConnectionAddress(null);
        disconnect(address);
    }

    public void disconnect(String address) {
        boolean disconnect = mlpBluetooth.disconnect(address);
        Log.i(TAG, "disconnect: " + address + " is " + disconnect);
    }

    public boolean sendData(String address, byte[] data) {
        boolean send = mlpBluetooth.writeDataToBLEDevice(address, UUID_SERVICE, UUID_WRITE, data);
        Log.i(TAG, "sendData: " + send+ " |  "+ HexUtil.encodeHexStr(data));
        return send;
    }

    public boolean sendDataJL(String address, byte[] data) {
        boolean send = mlpBluetooth.writeDataToBLEDevice(address, JL_OTA_UUID_SERVICE, JL_OTA_UUID_WRITE, data);
        Log.i(TAG, "sendDataJL: " + send+ " |  "+ HexUtil.encodeHexStr(data));
        return send;
    }


    public void sendDataAsyn(final String address, final byte[] data) {
        mWriteDataHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean send = mlpBluetooth.writeDataToBLEDevice(address, UUID_SERVICE, UUID_WRITE, data);
            }
        });
    }


    public BluetoothGatt getDeviceGatt() {
        return mlpBluetooth.getBluetoothGatt(mCurrentDevice);
    }

    public int getMTU() {
        return this.mtu;
    }

    public boolean isJLOtaNotifyData(UUID ae02) {
        return JL_OTA_UUID_NOTIFYS.equals(ae02);
    }

    public BluetoothDevice getCurrentDevice() {
        return mCurrentDevice;
    }
}
