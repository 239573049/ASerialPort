package com.ASerialPort;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.ASerialPort.Function.File;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SetActivity extends AppCompatActivity {
private String TAG="";
    private static Spinner Baud;
    private static Spinner DataBits;
    private static Spinner CheckDigit;
    private static Spinner StopBits;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        Baud=findViewById(R.id.Baud);
        DataBits=findViewById(R.id.DataBits);
        CheckDigit=findViewById(R.id.CheckDigit);
        StopBits=findViewById(R.id.stopBits);
        upload();
        findViewById(R.id.SavePorts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Map<String, Integer> json = new HashMap<>();
                    json.put("Baud", Integer.parseInt(String.valueOf(Baud.getSelectedItem())));
                    json.put("DataBits",Integer.parseInt(String.valueOf(DataBits.getSelectedItem())));
                    json.put("CheckDigit", Integer.parseInt(String.valueOf(CheckDigit.getSelectedItemId())));
                    json.put("StopBits", Integer.parseInt(String.valueOf(StopBits.getSelectedItem())));
                    //保存数据内容json文件
                    File.save(new ObjectMapper().writeValueAsString(json),"SerialPort.json",SetActivity.this);
                    json.clear();
                    json.put("Baud", Integer.parseInt(String.valueOf(Baud.getSelectedItemId())));
                    json.put("DataBits",Integer.parseInt(String.valueOf(DataBits.getSelectedItemId())));
                    json.put("CheckDigit", Integer.parseInt(String.valueOf(CheckDigit.getSelectedItemId())));
                    json.put("StopBits", Integer.parseInt(String.valueOf(StopBits.getSelectedItemId())));
                    //保存设置默认值
                    File.save(new ObjectMapper().writeValueAsString(json),"SerialPorDefault.json",SetActivity.this);
                }catch (Exception e){
                    Log.i(TAG, "异常："+e.getMessage());
                }
            }
        });
    }
    private void upload(){
        try{
            Log.i(TAG, "数据："+Baud.getScrollBarSize());
            for (int i = 0; i <Baud.getScrollBarSize() ; i++) {
                Log.i(TAG, "数据："+Baud.getBaseline());
            }
            String Data=File.read("SerialPorDefault.json",this);
            JSONObject json=new JSONObject(Data);
            Baud.setSelection((Integer) json.get("Baud"),true);
            DataBits.setSelection((Integer) json.get("DataBits"),true);
            CheckDigit.setSelection((Integer) json.get("CheckDigit"),true);
            StopBits.setSelection((Integer) json.get("StopBits"),true);
        }catch (Exception e){
            Log.i(TAG, "加载配置异常"+e.getMessage());
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
