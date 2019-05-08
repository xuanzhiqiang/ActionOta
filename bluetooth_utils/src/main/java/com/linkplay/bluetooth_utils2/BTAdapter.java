package com.linkplay.bluetooth_utils2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BTAdapter extends Dispatcher {

    private static final String TAG = "BTAdapter";

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver mBluetoothAdapterReceiver;

    BTAdapter(Context context) {
        super(context);
        this.registerReceiver();
    }

    @Override
    protected void finalize() throws Throwable {
        this.unregisterReceiver();
        super.finalize();
    }

    private boolean hasBle() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    public boolean enable() {
        if (null != this.mContext && null != this.mBluetoothAdapter) {

            boolean hasBle =  hasBle();
            if (this.mBluetoothAdapter.isEnabled()) {
                this.onBluetoothStatus(true, hasBle);
            } else if (!this.mBluetoothAdapter.enable()) {
                this.onBluetoothStatus(false, hasBle);
            }
            return true;
        } else {
            this.onBluetoothStatus(false, false);
            return false;
        }
    }

    public boolean disable() {
        return this.mBluetoothAdapter.isEnabled() && this.mBluetoothAdapter.disable();
    }


    public boolean isEnabled() {
        return null != this.mContext && (null != this.mBluetoothAdapter && this.mBluetoothAdapter.isEnabled());
    }


    public BluetoothDevice getRemoteDevice(String address) {
        return this.mBluetoothAdapter.getRemoteDevice(address);
    }


    private boolean checkGPSIsOpen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    boolean canStartScan() {
        if (!isEnabled()) {
            loge(TAG,"ble not enable");
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!checkGPSIsOpen()) {
            loge(TAG,"GPS not opened");
            return false;
        }
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loge(TAG,"Permission denied： "+Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }


    private int getConnectionState(){
        int state = -1;
        try {
            @SuppressLint("PrivateApi")
            Method getConnectionState = BluetoothAdapter.class.getDeclaredMethod("getConnectionState", (Class[]) null);
            getConnectionState.setAccessible(true);
            state = (int) getConnectionState.invoke(mBluetoothAdapter, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return state;
    }

    private boolean isConnectByBluetoothDevice(BluetoothDevice device){
        boolean isConnected = false;
        try {
            @SuppressLint("PrivateApi")
            Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            isConnectedMethod.setAccessible(true);
            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public ArrayList<String> getConnectBTDeviceAddress() {
        ArrayList<String> addressList = new ArrayList<>();
        if (getConnectionState() == BluetoothAdapter.STATE_CONNECTED) {
            for (BluetoothDevice bluetoothDevice : mBluetoothAdapter.getBondedDevices()) {
                if (bluetoothDevice != null && isConnectByBluetoothDevice(bluetoothDevice)) {
                    String address = bluetoothDevice.getAddress().replace(":", "");
                    Log.i(TAG,"getConnectBTDeviceAddress: "+address);
                    addressList.add(address);
                }
            }
        }
        return addressList;
    }

    private int registerReceiver() {
        if (null == this.mBluetoothAdapterReceiver) {
            this.mBluetoothAdapterReceiver = new LP_BluetoothAdapterReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            this.mContext.registerReceiver(this.mBluetoothAdapterReceiver, intentFilter);
        }

        return 0;
    }

    private int unregisterReceiver() {
        if (null != this.mBluetoothAdapterReceiver) {
            this.mContext.unregisterReceiver(this.mBluetoothAdapterReceiver);
            this.mBluetoothAdapterReceiver = null;
        }

        return 0;
    }

    private class LP_BluetoothAdapterReceiver extends BroadcastReceiver {
        private LP_BluetoothAdapterReceiver() {
        }
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                if (BluetoothAdapter.STATE_OFF == mBluetoothAdapter.getState()) {
                    onAdapterStatus(false);
                } else if (BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()) {
                    onAdapterStatus(true);
                }
            }

        }
    }

}
