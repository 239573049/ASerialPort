package com.ASerialPort.Function;

import android.util.Log;

import java.text.DecimalFormat;

import static com.ASerialPort.MainActivity.TAG;

public class DLT645_data {
    /**
     * 数据转换645协议格式16进制数据
     * @param data 参数码
     * @param TableNumber 表号
     * @return 16进制字符串
     * */
    public static String makeCheck(String data,String TableNumber) {
        Log.i(TAG, "makeCheck"+TableNumber);
        String[] Data=  data.split(" ");
        data="";
        for (int i = 0; i <Data.length ; i++) {
            data+=Data[i];
        }
        if (data == null || data.equals("")) {
            return "";
        }
        String Tablenumber = "";
        try{
            //将表号倒序
            for (int i = TableNumber.length(); i >0 ; ) {
                Tablenumber+= String.valueOf(TableNumber.charAt(i-2));
                Tablenumber+= String.valueOf(TableNumber.charAt(i-1));
                i=i-2;
            }
        }catch (Exception e){
            Log.i(TAG, "表号倒序异常："+e.getMessage());
            return "倒序异常";
        }
        String length;
        if (data.length()<10){
            length= "0"+ data.length() / 2;
        }else {
            length= String.valueOf(data.length()/2);
        }
        TableNumber="68"+Tablenumber+"6811"+length+data;
        int total = 0;
        int len = TableNumber.length();
        int num = 0;
        while (num < len) {
            String s = TableNumber.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        Tablenumber="";
        for (int i = 0; i <TableNumber.length() ; ) {
            Tablenumber+=String.valueOf(TableNumber.charAt(i));
            i=i+1;
            Tablenumber+=TableNumber.charAt(i)+" ";
            i=i+1;
        }
        /**
         * 用256求余最大是255，即16进制的FF
         */
        int mod = total % 256;
        String hex = Integer.toHexString(mod);
        len = hex.length();
        // 如果不够校验位的长度，补0,这里用的是两位校验
        if (len < 2) {
            hex = "0" + hex;
        }
        TableNumber="FE FE FE FE "+Tablenumber+hex.toUpperCase()+" 16";

        return TableNumber;
    }
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte[] abt = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte[] bbt = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }
    public static String bytesToHexString(byte[] src){

        StringBuilder stringBuilder = new StringBuilder();

        if (src == null || src.length <= 0) {

            return null;

        }

        for (int i = 0; i < src.length; i++) {

            int v = src[i] & 0xFF;

            String hv = Integer.toHexString(v);

            if (hv.length() < 2) {

                stringBuilder.append(0);

            }

            stringBuilder.append(hv);

        }

        return stringBuilder.toString();

    }
    /**
     * DLT645/2007协议解析解析数据
     * @param Data 需要解析的数据
     * @param ple 如果ple位“tab”解析数据直接返回
     * @return 结果
     * */
    public static String analysis(String Data,String ple){
        try{
        String[] data=Data.split(" ");
        int s=0;
        int le = 0;
        for (int i = 0; i <data.length ; i++) {
            if (data[i].equals("68")) {
                if (s==1) {
                    le=i;
                }
                s=1;
            }
        }
        int length= Integer.parseInt(data[le+2], 16);
        //存放临时数据
        String Da="";
            for (int i = (le+length+2); i>le+2; i--) {
                //645协议减33
                long x = Long.parseLong(data[i],16);
                long y = Long.parseLong(String.valueOf(33),16);
                if (Long.toHexString(x-y).length()<=1) {
                    Da+="0"+Long.toHexString(x-y);
                }else {
                    Da+=Long.toHexString(x-y);
                }
            }
            if (ple.equals("tab")) {
                return Da;
            }
            String[] lData=new String[Da.length()];
            //用来计算后面的数有几位
            int l=0;
            for (int i = 0; i <ple.length() ; i++) {
                if (!ple.substring(i,i+1).equals("#")) {
                    if (ple.substring(i,i+1).equals("0")) {
                        l=l+1;
                    }else {
                        le=i;
                        Log.i(TAG, "i"+i);
                    }

                }
            }
            for (int i = 0; i <lData.length ; i++) {
                lData[i]= String.valueOf(Da.charAt(i));
            }
            Da="";
            for (int i = 0; i <lData.length ; i++) {
                if (le == i) {
                    Da+=".";
                    Log.i(TAG, "i"+i);
                }
                Da+=lData[i];

            }
            DecimalFormat d=new DecimalFormat(ple);//构造方法的字符格式这里如果小数不足2位,会以0补足.
            Log.i(TAG, "Da"+Da);
            Da=d.format(Float.valueOf(Da));//format 返回的是字符串
            if (Da.substring(0,1).equals(".")) {
                Da="0"+Da;
            }
            Log.i(TAG, "结果！"+Da);
            return Da;
        }catch (Exception e){
            Log.i(TAG, "analysis异常："+e.getMessage());
            return "false";
        }
    }


    public static byte[] SendData(String TableNumber){
        String[] tab=TableNumber.split(" ");
        byte[] da=new byte[tab.length];
        for (int i = 0; i <tab.length ; i++) {
            da[i]= (byte) Integer.parseInt(tab[i], 16);
            Log.i(TAG, "Send"+Integer.parseInt(tab[i], 16));
            Log.i(TAG, "SendData: "+i);
        }
        Log.i(TAG, "SendData: "+da.clone());
        return da;
    }
    /**
     * 把16进制字符串转换成字节数组
     * @param hex
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
