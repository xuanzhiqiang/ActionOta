package com.linkplay.bluetooth_utils2;

import android.bluetooth.BluetoothDevice;

public class LP_BLEDevice {

    private String name;
    private String address;
    private int state = BaseCode.LP_BLE_STATE_NOT_CONNECTED;
    private int customerID = -1;

    private boolean mMtuChanged;
    private boolean mServiceDiscovered;
    private String mMac;

    public LP_BLEDevice(BluetoothDevice device) {
        this.address = device.getAddress();
        this.name = device.getName();
    }


    public boolean isServiceDiscovered() {
        return mServiceDiscovered;
    }

    public void setServiceDiscovered(boolean mServiceDiscovered) {
        this.mServiceDiscovered = mServiceDiscovered;
    }

    public boolean isMtuChanged() {
        return mMtuChanged;
    }

    public void setMtuChanged(boolean mMtuChanged) {
        this.mMtuChanged = mMtuChanged;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getMac() {
        return mMac;
    }

    public void setMac(String mMac) {
        this.mMac = mMac;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LP_BLEDevice && address.equals(((LP_BLEDevice) o).getAddress());
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return "LP_BLEDevice{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", state=" + state +
                ", customerID=" + customerID +
                ", mMtuChanged=" + mMtuChanged +
                ", mServiceDiscovered=" + mServiceDiscovered +
                ", mMac='" + mMac + '\'' +
                '}';
    }
}
