package com.ASerialPort.Function;


import android.icu.util.Calendar;

public class Date {
    /**
     * 获取时间类
     * @return 获取的时间
     * */
    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);  //获取小时
        int minute = c.get(Calendar.MINUTE); //获取分钟
        int mi = c.get(Calendar.SECOND);//秒
        return hour+":"+minute+":"+mi;
    }
}
