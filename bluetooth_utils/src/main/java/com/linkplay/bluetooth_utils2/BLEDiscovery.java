package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class BLEDiscovery extends BTAdapter {

    private static final String TAG = "LP_BleDiscovery";

    private boolean mScanning;
    private final List<ScanFilter> mScanFilters = new ArrayList<>();
    private ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

    private Runnable mStopScanTask = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private ScanCallback mBleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result == null) return;

            int rssi = result.getRssi();
            BluetoothDevice bluetoothDevice = result.getDevice();

            ScanRecord scanRecord = result.getScanRecord();

            if (scanRecord == null) return;

            byte[] scanRecordBytes = scanRecord.getBytes();
            if (scanRecordBytes == null || scanRecordBytes.length == 0) return;

            onDiscoveryBle(bluetoothDevice, scanRecordBytes, rssi);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            loge(TAG, "onScanFailed ---->  errorCode: "+errorCode);
        }
    };

    BLEDiscovery(Context context) {
        super(context);
    }

    boolean isScanning() {
        return mScanning;
    }

    void clearFilter() {
        mScanFilters.clear();
    }

    void addScanFilterForServiceUUID(ParcelUuid serviceUuid){
        mScanFilters.add(new ScanFilter.Builder().setServiceUuid(serviceUuid).build());
    }

    /**
     * 第一次进入等待分配权限
     */
    private Runnable mTryScan = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    boolean startScan() {

        if(isScanning() || mBluetoothAdapter == null) return false;

        if (!canStartScan()) {
            log(TAG," [LP] Don't scan");
            mHandler.removeCallbacks(mTryScan);
            mHandler.postDelayed(mTryScan, 1000);
            return false;
        }

        mHandler.removeCallbacks(mTryScan);

        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothLeScanner != null) {

            log(TAG, " [LP] StartScan....");

            mScanning = true;

            bluetoothLeScanner.startScan(mScanFilters, scanSettings, mBleScanCallback);

            onDiscoveryBleStatus(true);

            mHandler.removeCallbacks(mStopScanTask);
            mHandler.postDelayed(mStopScanTask, 60 * 1000);

            return true;
        }

        onDiscoveryBleStatus(false);
        return false;
    }


    boolean stopScan() {

        if (!isScanning() || mBluetoothAdapter == null) return false;

        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner != null) {

            Log.i(TAG, " [LP] StopScan");

            mScanning = false;
            bluetoothLeScanner.stopScan(mBleScanCallback);

            onDiscoveryBleStatus(false);

            return true;
        }
        Log.i(TAG, " [LP] bluetoothLeScanner == null");
        return false;
    }


}
