package com.ASerialPort.Function;

import android.content.Intent;
import android.util.Log;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.service.controls.ControlsProviderService.TAG;
public  class  Modbus_data {


    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        byte[] bytes = new BigInteger(str, 16).toByteArray();
        return bytes;
    }


    /**
     * 给十六进制数据加前导0防止数据异常
     *
     * @param data   数据
     * @param status 如果为true数据为俩位，如果为false数据为四位
     * @return 十六进制数据
     */
    public static String HexString(String data, Boolean status) {
        String Data;
        if (status) {
            Data = Integer.toHexString(Integer.parseInt(data));
            if (Data.length() == 1) {
                Data = "0" + Data;
            }
        } else {
            Data = Integer.toHexString(Integer.parseInt(data));
            Log.d(TAG, "HexString: " + Data.length());
            if (Data.length() == 3)
                Data = "0" + Data;
            else if (Data.length() == 2)
                Data = "00" + Data;
            else if (Data.length() == 1)
                Data = "000" + Data;
        }
        return Data;
    }
/**
 *
 * @param bytes

 * @return 将二进制转换为十六进制字符输出

 */
    private static String hexStr = "0123456789ABCDEF"; //全局

    public static String BinaryToHexString(byte[] bytes) {
        String result = "";
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            //字节高4位
            hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
            result += hex+" ";
        }
        return result;
    }
    /**
     * Modbus协议解析数据
     * @param data 解析数据
     * @return Map返回
     * */
    public static Map<String,String> ModbusData(String data) {
        Map<String, String> map = null;
        try {
            map = new HashMap<>();
            String[] Data = data.split(" ");
            data = "";
            map.put("Site", Data[0]);
            map.put("FunctionCode", Data[1]);
            map.put("Length", Data[2]);
            int length = Integer.parseInt(String.valueOf(Covert(Data[2])));
            Log.i(TAG, "le: "+length);
            Log.i(TAG, "le: "+Data[2]);
            String js= "";

            for (int i = 2; i < length+2;) {
                i=i+1;
                data+=Covert(Data[i]);
                i=i+1;
                data+=Covert(Data[i]);
                if (i==length+3)
                    js+=data;
                else
                    js+=data+",";
                data="";
            }
            map.put("Data", js);
            map.put("verify", Data[length + 1] + Data[length + 2]);
        } catch (Exception e) {
            Log.i(TAG, "解析错误！"+e.getMessage());
            map.put("err",e.getMessage());
            return map;
        }
        return map;
    }
    /**
     * @param: [content]
     * @return: int
     * @description: 十六进制转十进制
     */
    public static int Covert(String content){
        if (content.length()<=0)
            return 0;
        int number=0;
        String [] HighLetter = {"A","B","C","D","E","F"};
        Map<String,Integer> map = new HashMap<>();
        for(int i = 0;i <= 9;i++){
            map.put(i+"",i);
        }
        for(int j= 10;j<HighLetter.length+10;j++){
            map.put(HighLetter[j-10],j);
        }
        String[]str = new String[content.length()];
        for(int i = 0; i < str.length; i++){
            str[i] = content.substring(i,i+1);
        }
        for(int i = 0; i < str.length; i++){
            number += map.get(str[i])*Math.pow(16,str.length-1-i);
        }
        return number;
    }
        /**
         * 计算CRC16/Modbus校验码  低位在前,高位在后
         *
         * @param str 十六进制字符串byte
         * @return 如果为空就解析失败
         */
        public static byte[] getCRC (String str){
            try {
                byte[] bytes = toBytes(str);
                int CRC = 0x0000ffff;
                int POLYNOMIAL = 0x0000a001;

                int i, j;
                for (i = 0; i < bytes.length; i++) {
                    CRC ^= ((int) bytes[i] & 0x000000ff);
                    for (j = 0; j < 8; j++) {
                        if ((CRC & 0x00000001) != 0) {
                            CRC >>= 1;
                            CRC ^= POLYNOMIAL;
                        } else {
                            CRC >>= 1;
                        }
                    }
                }
                String crc = Integer.toHexString(CRC);
                if (crc.length() == 2) {
                    crc = "00" + crc;
                } else if (crc.length() == 3) {
                    crc = "0" + crc;
                }
                String data = "";
                //取出字符串数据
                for (int k = 0; k < str.length(); ) {
                    data += str.substring(k, k = k + 2) + ",";
                }
                //把输入的十六进制和校验拼接
                crc = crc.substring(2, 4) + "," + crc.substring(0, 2);
                data += crc;
                Log.d(TAG, "校验码数据: "+data);
                //将连接一块的数据分成字符串数组
                String[] arrstring = data.split(",");
                //将字符串十六进制转换成byte格式
                byte[] ss = new byte[]{(byte) Integer.parseInt(arrstring[0], 16), (byte) Integer.parseInt(arrstring[1], 16), (byte) Integer.parseInt(arrstring[2], 16), (byte) Integer.parseInt(arrstring[3], 16), (byte) Integer.parseInt(arrstring[4], 16), (byte) Integer.parseInt(arrstring[5], 16), (byte) Integer.parseInt(arrstring[6], 16), (byte) Integer.parseInt(arrstring[7], 16)};
                return ss;
            } catch (Exception e) {
                Log.d("", "解析异常: " + e.getMessage());
                return new byte[]{};
            }

        }


    }
