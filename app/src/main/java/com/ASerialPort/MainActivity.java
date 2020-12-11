package com.ASerialPort;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import com.ASerialPort.Function.File;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.hd.serialport.method.DeviceMeasureController;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    public static String TAG="MainActivitu";
    public static AppBarConfiguration mAppBarConfiguration;
    public static String hexStr = "0123456789ABCDEF";
    NavigationView navigationView=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
             String file=File.read("SerialPort.json",MainActivity.this);
            if (file.equals("false"))
                defaultSetting();
        }catch (Exception e){
            Log.i(TAG, "异常："+e.getMessage());
        }
        try {
            DeviceMeasureController.INSTANCE.init(this, BuildConfig.DEBUG);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "串口初始化失败！", Toast.LENGTH_SHORT).show();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // 将每个菜单ID作为一组ID传递，因为每个菜单ID
        ////菜单应视为顶级目的地。
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.ModbusView,
                R.id.AboutView,R.id.DLT645View)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 膨胀的菜单;如果操作栏存在，这将向操作栏添加项目。
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    /**
     * 生成默认配置的串口参数
     * */
    public void defaultSetting(){
        Map<String, Integer> json = new HashMap<>();
        try {
        json.put("Baud", 9600);
        json.put("DataBits",8);
        json.put("CheckDigit", 0);
        json.put("StopBits", 1);
        //保存数据内容json文件
            File.save(new ObjectMapper().writeValueAsString(json),"SerialPort.json",MainActivity.this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制到粘贴板点击事件
     *
     * */
    public void Derive(View v){
        try{
            TextView Show=findViewById(R.id.show);
            ClipboardManager cmd =(ClipboardManager)getSystemService(MainActivity.CLIPBOARD_SERVICE);
            cmd.setText(Show.getText().toString());
            Toast.makeText(MainActivity.this,"数据已复制至剪贴板！",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"复制至剪贴板异常："+e.getMessage(),Toast.LENGTH_LONG).show();
            Log.d(TAG, "复制至剪贴板异常"+e.getMessage());
        }
    }
    /**
     * 二进制转换为十六进制字符
     * @param bytes
     * @return 将二进制转换为十六进制字符输出
     */
    public static String bytes(byte[] bytes) {

        String result = "";
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            //字节高4位
            hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
            result += hex + " ";
        }
        return result;
    }
    /**
     * 进入设置界面
     * */
    public void Set(MenuItem item) {
        try{
            startActivity(new Intent(MainActivity.this, SetActivity.class));
        }catch (Exception e){
            Log.i(TAG, "Set: "+e.getMessage());
        }
    }


}
