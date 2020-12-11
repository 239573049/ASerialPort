package com.ASerialPort.Function;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Http {
    /**
     * get的方式请求
     *
     * @param Url 接口地址
     * @return  json数据
     */
    public static String Get(String Url) {
        HttpURLConnection conn = null;//声明连接对象
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(Url); //URL对象
            conn = (HttpURLConnection) url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("GET"); //使用get请求

            if (conn.getResponseCode() == 200) {//返回200表示连接成功
                is = conn.getInputStream(); //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine = "";
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine;
                }
            }
            return  resultData;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    /**
     * Post请求方式
     *
     * @param Url 接口地址
     * @param Data 上传数据
     * @return
     * */
    public static String Post(String Url,String Data){
        HttpURLConnection conn=null;
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(Url); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("POST"); //使用POST请求
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8\n");
            conn.setRequestProperty("Content-Length", String.valueOf(Data.length()));
            //参数字符串
            String param="name="+ URLEncoder.encode(Data,"UTF-8");

            //用输出流向服务器发出参数，要求字符，所以不能直接用getOutputStream
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();

            if(conn.getResponseCode()==200) {//返回200表示相应成功
                is = conn.getInputStream();   //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine = "";
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine;
                }
            }
            return  resultData;

        } catch (IOException e) {
            e.printStackTrace();
            return  e.getMessage();
        }
    }
}
