package com.linkplay.bluetooth_utils2.utils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CHexConver {
    private static final char[] mChars = "0123456789ABCDEF".toCharArray();

    public static boolean checkHexStr(String sHex) {
        String sTmp = sHex.toString().trim().replace(" ", "").toUpperCase(Locale.US);
        int iLen = sTmp.length();
        if (iLen > 1 && iLen % 2 == 0) {
            for(int i = 0; i < iLen; ++i) {
                if (!"0123456789ABCDEF".contains(sTmp.substring(i, i + 1))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static String str2HexStr(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = null;

        try {
            bs = str.getBytes("GBK");
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        if (bs == null) {
            return "";
        } else {
            for(int i = 0; i < bs.length; ++i) {
                sb.append(mChars[(bs[i] & 255) >> 4]);
                sb.append(mChars[bs[i] & 15]);
            }

            return sb.toString().trim();
        }
    }


    public static String byte2HexStr(byte[] b, int iLen) {
        StringBuilder sb = new StringBuilder();

        for(int n = 0; n < iLen; ++n) {
            sb.append(mChars[(b[n] & 255) >> 4]);
            sb.append(mChars[b[n] & 15]);
        }

        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static String int2HexStr(int[] b, int iLen) {
        StringBuilder sb = new StringBuilder();

        for(int n = 0; n < iLen; ++n) {
            sb.append(mChars[((byte)b[n] & 255) >> 4]);
            sb.append(mChars[(byte)b[n] & 15]);
        }

        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static String byte2String(byte[] b, int len) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            sb.append(String.valueOf(b[i]));
        }

        return sb.toString();
    }



    public static String strToUnicode(String strText) throws Exception {
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < strText.length(); ++i) {
            char c = strText.charAt(i);
            String strHex = Integer.toHexString(c);
            if (c > 128) {
                str.append("\\u");
            } else {
                str.append("\\u00");
            }

            str.append(strHex);
        }

        return str.toString();
    }


    public static String intToHexString(int num) {
        return String.format("%02x", num);
    }

    public static byte intToByte(int num) {
        return (byte)num;
    }

    public static int byteToInt(byte b) {
        return b & 255;
    }

    public static String byteToHexString(byte b) {
        return intToHexString(b & 255);
    }

    public static byte[] intToBytes(int n) {
        byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255), (byte)(n >> 16 & 255), (byte)(n >> 24 & 255)};
        return b;
    }

    public static int bytesToInt(byte h, byte l) {
        int result = (255 & h) << 8;
        result += 255 & l;
        return result;
    }

    public static byte[] shortToBytes(short n) {
        byte[] b = new byte[]{(byte)(n & 255), (byte)(n >> 8 & 255)};
        return b;
    }
}