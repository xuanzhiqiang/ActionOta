package com.linkplay.actionota;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.IActionCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.linkplay.actionota.ble.MultipleBle.BleManager;
import com.linkplay.actionota.ble.MultipleBle.HexUtil;
import com.linkplay.actionota.ble.MultipleBle.jlota.JLOtaManager;
import com.linkplay.actionota.ble.MultipleBle.jlota.JLOtaManager.SendDataListener;
import com.linkplay.actionota.ble.interfaces.MultipleBleDeviceListener;
import com.linkplay.bluetooth_utils2.BaseCode;
import com.linkplay.bluetooth_utils2.LP_BLEDevice;
import com.linkplay.lpvr.blelib.ota.ActionOtaManager;
import com.linkplay.lpvr.blelib.ota.LPAVSOTAManager;
import com.linkplay.lpvr.lpvrbean.OtaNotifyEntity;
import com.linkplay.lpvr.lpvrcallback.LPAVSOTAManagerCallback;
import com.linkplay.lpvr.lpvrlistener.ActionOtaListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MyAdapter adapter;
    private BleManager bleManager;
    private LPAVSOTAManager actionOtaManager;
    private JLOtaManager jlOtaManager;

    private LP_BLEDevice currentDeice;
    private String otaPath= "";

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean mOtaing = false;
    private View otaPage;
    private TextView tipText;
    private Button btStart;
    private ListView listLog;
    private LogAdapter logAdapter;
    private View btBack;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bleManager = BleManager.init(getApplicationContext());
        jlOtaManager = JLOtaManager.initOTAManager(getApplicationContext(), bleManager);

        listView = findViewById(R.id.list_view);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearData();
                        if (currentDeice != null){
                            adapter.addDevice(currentDeice);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        bleManager.startScan();
                    }
                }, 500);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LP_BLEDevice device = adapter.getItem(position);
                if (device.getState() != 0){
                    bleManager.connect(device.getAddress());
                }else{
                    switchPage(true);
                }
            }
        });

//        otaPath = Environment.getExternalStorageDirectory().getPath()
//                + File.separator+"actionOtaFile"+File.separator+"action.OTA";

        otaPath = Environment.getExternalStorageDirectory().getPath()
                + File.separator+"actionOtaFile"+File.separator+"updata.bfu";

        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "actionOtaFile");
        if (!file.exists()) {
            file.mkdirs();
        }


        otaPage = findViewById(R.id.ota_page);
        tipText = findViewById(R.id.tip_text);
        btStart = findViewById(R.id.start);
        btBack = findViewById(R.id.back);
        listLog = findViewById(R.id.list_log);

        tipText.setText("放入升级文件："+otaPath);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new File(otaPath).exists()) {
                    mOtaing = true;

//                    actionOtaManager.startOTA(otaPath);
                    startJLOta(otaPath);
                }else{
                    showToast("文件不存在");
                }
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPage(false);
            }
        });

        logAdapter = new LogAdapter(this);
        listLog.setAdapter(logAdapter);

        addListener();
        requestPermission();

//        initActionOta();
    }


    private void switchPage(boolean showOtaPage){
        if (showOtaPage){
            otaPage.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }else{
            otaPage.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }


    private void startJLOta(String otaPath) {
        Log.e(TAG, "--------------    startJLOta    ----------------");
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            BluetoothDevice bluetoothDevice = defaultAdapter.getRemoteDevice(currentDeice.getAddress());

            if (bluetoothDevice == null){
                Log.e(TAG, "--------------    RemoteDevice == null    ----------------");
                return ;
            }

            btStart.setEnabled(false);
            logAdapter.clear();

            jlOtaManager.getBluetoothOption().setFirmwareFilePath(otaPath);
            jlOtaManager.setOtaListener(new SendDataListener(){
                @Override
                public void onSendData(final byte[] bytes) {
                    if (bytes.length < 60){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String send = "Send: "+ HexUtil.encodeHexStr(bytes, false);
                                logAdapter.addOneData(send);
                            }
                        }, 20);

                    }else{
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                byte[] copyOf = Arrays.copyOf(bytes, 35);
                                String send = "Send: "+ HexUtil.encodeHexStr(copyOf, false);
                                logAdapter.addOneData(send + "......");
                            }
                        }, 20);
                    }
                }
            });

            jlOtaManager.startOTA(new IUpgradeCallback() {
                @Override
                public void onStartOTA() {
                    Log.i(TAG, "--------------    onStartOTA    ----------------");
                }

                @Override
                public void onProgress(final float v) {
                    Log.i(TAG, "onProgress:    " + String.valueOf(v));
                    if ( btStart != null) {
                        handler.postDelayed(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                btStart.setText("升级中："+v + " ,  时间： "+ (jlOtaManager.getTotalTime() / 1000) + "秒");
                            }
                        }, 20);
                    }
                }

                @Override
                public void onStopOTA() {
                    Log.i(TAG, "--------------    onStopOTA    ----------------");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btStart.setText("onStopOTA Success");
                            btStart.setEnabled(true);
                            Toast.makeText(MainActivity.this.getApplicationContext(),
                                    " 升级结束: 耗时："+(jlOtaManager.getTotalTime() / 1000)+"秒",
                                    Toast.LENGTH_LONG).show();
                        }
                    }, 20);
                }

                @Override
                public void onCancelOTA() {
                    Log.i(TAG, "--------------    onCancelOTA    ----------------");

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btStart.setText("onCancelOTA，点击重新开始");
                            btStart.setEnabled(true);
                        }
                    }, 20);
                }

                @Override
                public void onError(BaseError baseError) {
                    Log.e(TAG, "--------------    onError    ----------------\n"+baseError.toString());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btStart.setText("升级失败，点击重新开始");
                            btStart.setEnabled(true);
                        }
                    }, 20);
                }
            });
        }
    }


    void initActionOta(){
        actionOtaManager = new ActionOtaManager(getApplicationContext());
        actionOtaManager.setActionOtaListener(new ActionOtaListener(){
            @Override
            public void onSendData(final byte[] bytes) {
                Log.i(TAG, "onSendData:  mOtaing is "+mOtaing);
                if(mOtaing){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bytes.length < 60){
                                String send = "Send: "+ HexUtil.encodeHexStr(bytes, false);
                                logAdapter.addOneData(send);
                            }else{
                                byte[] copyOf = Arrays.copyOf(bytes, 35);
                                String send = "Send: "+ HexUtil.encodeHexStr(copyOf, false);
                                logAdapter.addOneData(send + "......");
                            }
                        }
                    });
                    Log.i(TAG, "sendDataAsyn:   "+ HexUtil.encodeHexStr(bytes,false));
                    bleManager.sendDataAsyn(currentDeice.getAddress(), bytes);
                }
            }
        });
        actionOtaManager.setLPAVSOTAManagerCallback(new LPAVSOTAManagerCallback() {
            @Override
            public void lpavsOTAUpgrading(int i, final float v) {
                Log.i(TAG, "lpavsOTAUpgrading: "+ v);
                if ( btStart != null) {
                    handler.postDelayed(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            btStart.setText("升级中："+(v*100)+"%");
                        }
                    }, 20);
                }

            }

            @Override
            public void lpavsOTAUpgradeSuccess() {
                Log.i(TAG, "lpavsOTAUpgradeSuccess");
                if (btStart != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btStart.setText("升级成功");
                        }
                    }, 20);
                }
                mOtaing = false;
            }

            @Override
            public void lpavsOTAUpgradeFailedError(int i) {
                Log.i(TAG, "lpavsOTAUpgradeFailedError");
                if (btStart != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btStart.setText("升级失败，点击重新开始");
                            btStart.setEnabled(true);
                        }
                    }, 20);
                }
                mOtaing = false;
            }

            @Override
            public void lpavsOTAManagerCanUpgradeNotify(OtaNotifyEntity otaNotifyEntity) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        bleManager.startScan();
    }

    void showToast(String toast){
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    void addListener(){
        bleManager.addListener(new MultipleBleDeviceListener() {
            @Override
            public void onStartScan() {
                Log.i(TAG, "onStartScan" );
            }

            @Override
            public void onStopScan() {
                Log.i(TAG, "onStopScan" );
            }

            @Override
            public void onFound(LP_BLEDevice bleDevice) {
                Log.i(TAG, "onFound: "+ bleDevice );
                adapter.addDevice(bleDevice);
            }

            @Override
            public void onMTUChange(int mtu) {
                if(jlOtaManager != null ){
                    BluetoothGatt deviceGatt = bleManager.getDeviceGatt();
                    if (deviceGatt!=null) {
                        jlOtaManager.onMtuChanged(deviceGatt, mtu+3, 0);
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(String address, UUID serviceUuid, UUID characteristicUuid, final byte[] data) {
                final String receive = "Receive: "+ HexUtil.encodeHexStr(data, false);
                Log.i(TAG, characteristicUuid.toString()+"  |  "+receive);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        logAdapter.addOneData(receive);
                    }
                });

                if(jlOtaManager != null && bleManager.isJLOtaNotifyData(characteristicUuid)){
                    jlOtaManager.onReceiveDeviceData(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address), data);
                }
            }

            @Override
            public void onStateChange(final LP_BLEDevice device) {
                Log.i(TAG, String.format("onStateChange: %s  state=%d ", device.getName(), device.getState()));
                adapter.updateDevice(device);

                final int deviceState = device.getState();

                if (device.getState() == BaseCode.LP_BLE_STATE_CONNECTED && jlOtaManager != null && !jlOtaManager.isOTA()){
                    Log.i(TAG, "queryMandatoryUpdate: " );
                    jlOtaManager.queryMandatoryUpdate(new IActionCallback<TargetInfoResponse>() {
                        @Override
                        public void onSuccess(TargetInfoResponse targetInfoResponse) {
                            Log.i(TAG, "queryMandatoryUpdate: onSuccess : " + targetInfoResponse.toString());
                            startJLOta(otaPath);
                        }
                        @Override
                        public void onError(BaseError baseError) {
                        }
                    });
                }

                if (jlOtaManager != null && jlOtaManager.isOTA()){
                    int JL_CODE;
                    if (deviceState == BaseCode.LP_BLE_STATE_CONNECTED){
                        JL_CODE = StateCode.CONNECTION_OK;
                    }else if(deviceState == BaseCode.LP_BLE_STATE_CONNECTING){
                        JL_CODE = StateCode.CONNECTION_CONNECTING;
                    }else{
                        JL_CODE = StateCode.CONNECTION_DISCONNECT;
                    }
                    Log.i(TAG, "通知杰理蓝牙状态改变 ---> "+JL_CODE);
                    jlOtaManager.onBtDeviceConnection(bleManager.getCurrentDevice(), JL_CODE);

                    if (deviceState == BaseCode.LP_BLE_STATE_NOT_CONNECTED || deviceState == BaseCode.LP_BLE_STATE_CONNECT_TIMEOUT){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bleManager.connect(device.getAddress());
                            }
                        }, 500);
                    }
                    return;
                }

                if (deviceState == 0){
                    currentDeice = device;
                    switchPage(true);
                }else if(currentDeice != null && currentDeice.getAddress().equals(device.getAddress())){
                    currentDeice = null;
                    adapter.removeDevice(device);
                    handler.postDelayed(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            btStart.setText("开始升级");
                            btStart.setEnabled(true);
                            logAdapter.clear();
                            switchPage(false);
                        }
                    }, 20);
                }else{
                    handler.postDelayed(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            btStart.setText("开始升级");
                            btStart.setEnabled(true);
                            logAdapter.clear();
                            switchPage(false);
                        }
                    }, 20);
                }

            }

            @Override
            public void startOta(LP_BLEDevice device) {
                Log.i(TAG, "startOta: "+ device.getName() );
            }

            @Override
            public void stopOta(LP_BLEDevice device, int state) {
                Log.i(TAG, "stopOta: "+ device.getName() + " state: "+state);
            }

            @Override
            public void otaProgress(LP_BLEDevice device, double progress) {
                Log.i(TAG, "otaProgress: "+ device.getName() + " progress: "+progress);
            }

            @Override
            public void onCommandResponse(int errorCode, int commandCode, int parameterLength, byte[] data) throws Exception {

            }

            @Override
            public void onCommand(String address, int command, int length, byte[] parameter) {

            }


        });
    }


    ///////////////////////////////////////////////////////////////////////////
    // 动态权限
    ///////////////////////////////////////////////////////////////////////////



    private List<String> mRequestPermissions = new ArrayList<>();
    private int mPermissionIdx = 0x10;//请求权限索引
    private SparseArray<GrantedResult> mPermissions = new SparseArray<>();//请求权限运行列表

    public void requestPermission() {
        mRequestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mRequestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mRequestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(mRequestPermissions, new GrantedResult() {
            @Override
            public void onResult(boolean granted) {
                if (!granted) {
                    Toast.makeText(getApplicationContext(), "权限申请失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }


    public void requestPermission(List<String> permissions, GrantedResult runnable) {
        if (runnable == null) {
            return;
        }
        runnable.mGranted = false;
        if (Build.VERSION.SDK_INT < 23 || permissions == null || permissions.size() == 0) {
            runnable.mGranted = true;//新添加
            runOnUiThread(runnable);
            return;
        }
        final int requestCode = mPermissionIdx++;
        mPermissions.put(requestCode, runnable);

		/*
            是否需要请求权限
		 */
        boolean granted = true;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                granted = granted && checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            }
        }

        if (granted) {
            runnable.mGranted = true;
            runOnUiThread(runnable);
            return;
        }

		/*
            是否需要请求弹出窗
		 */
        boolean request = true;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request = request && !shouldShowRequestPermissionRationale(permission);
            }
        }

        final String[] permissionTemp = new String[permissions.size()];
        if (!permissions.isEmpty()) {
            for (int i = 0; i < permissions.size(); i++) {
                permissionTemp[i] = permissions.get(i);
            }
        }
        if (!request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionTemp, requestCode);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionTemp, requestCode);
            }
        }
    }


    public static abstract class GrantedResult implements Runnable {
        private boolean mGranted;

        public abstract void onResult(boolean granted);

        @Override
        public void run() {
            onResult(mGranted);
        }
    }

}
