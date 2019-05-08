package com.linkplay.actionota.ble.MultipleBle;


/**
 * This class sets various static property values for Bluetooth
 * Created by YeZhongJi on 2017/10/25.
 */

public class BleConfig {


    @SuppressWarnings("FieldCanBeLocal")
    public static String instruction_service_uuid = "0000ffc0-0000-1000-8000-00805F9B34FB";
    public static String instruction_notifys_uuid = "0000ffc2-0000-1000-8000-00805F9B34FB";
    public static String instruction_writes_uuid = "0000ffc1-0000-1000-8000-00805F9B34FB";


    public static String jl_ota_service_uuid = "0000ae00-0000-1000-8000-00805F9B34FB";
    public static String jl_ota_writes_uuid = "0000ae01-0000-1000-8000-00805F9B34FB";
    public static String jl_ota_notifys_uuid = "0000ae02-0000-1000-8000-00805F9B34FB";



    public static String jl_ota_service_uuid_1 = "00001812-0000-1000-8000-00805f9b34fb";
    public static String[] support_service_uuid = {
            instruction_service_uuid,
            jl_ota_service_uuid,
            jl_ota_service_uuid_1
    };


}
