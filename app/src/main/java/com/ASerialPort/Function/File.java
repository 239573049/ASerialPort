package com.ASerialPort.Function;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class File  {
    /**
     * @param content 需要添加的内容
     * @param Name    需要添加的文件名字
     */
    public static Boolean save(String content, String Name, Context context) {
        FileOutputStream file = null;
        try {
            file = context.openFileOutput(Name, MODE_PRIVATE);
            file.write(content.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param Name 需要获取的文件名称
     * @param context 环境
     * @return 文件的内容,如果找不到文件返回false字符串
     *
     */
    public static String read(String Name, Context context) throws IOException {
        FileInputStream file = null;
        try {
            file = context.openFileInput(Name);
            byte[] buff = new byte[1024];
            StringBuffer sb = new StringBuffer();
            int len = 0;
            while ((len = file.read(buff)) > 0) {
                sb.append(new String(buff, 0, len));
            }
            return sb.toString();
        } catch (FileNotFoundException es) {
            //捕捉空文件异常
            Log.d(TAG, "read: " + es.getMessage());
            return "false";
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "read: " + e.getMessage());
            return e.getMessage();
        } finally {
            if (file != null) {
                file.close();
            }

        }
    }
}
