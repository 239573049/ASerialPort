package com.ASerialPort.ui.Modbus;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ASerialPort.Function.Date;
import com.ASerialPort.Function.File;
import com.ASerialPort.Function.Modbus_data;
import com.ASerialPort.R;
import com.hd.serialport.listener.UsbMeasureListener;
import com.hd.serialport.method.DeviceMeasureController;
import com.hd.serialport.param.UsbMeasureParameter;
import com.hd.serialport.usb_driver.UsbSerialPort;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;
import static com.ASerialPort.MainActivity.bytes;

public class ModbusFragment extends Fragment {
    public static String ByteDate = "";

    public static UsbSerialPort getSetPort() {
        return setPort;
    }

    public static void setSetPort(UsbSerialPort setPort) {
        ModbusFragment.setPort = setPort;
    }
    public static UsbSerialPort setPort;
    //串口下拉框显示
    public static Spinner Port;
    public static Boolean status = false;
    //自动发送当前状态
    public static Boolean AutoSendStatus=false;
    //是否解析
    public static Boolean Analysis=false;
    //接收数据定时器
    public static Timer ReceiveTimer;
    //发送数据定时器
    public static Timer StartTimer;
    //串口状态监听
    public   static Timer PortStatus;
    //串口监听状态
    public static Boolean PortsStatus=false;
    //数据接收状态
    public static String  AcceptingState="false";
    //当前在线usb设备
    public static int Ports=0;
    //当前串口保存地址
    public static List<String> ShowList=new ArrayList<>();
    //当前操作串口
    public static String serialPort=null;
    public static int getSendmNumber() {
        return SendmNumber;
    }

    public static void setSendmNumber(int shenmNumber) {
        SendmNumber = shenmNumber;
    }

    //当前发送数据数量
    public static int SendmNumber=0;

    public static int getReception() {
        return reception;
    }

    public static void setReception(int reception) {
        ModbusFragment.reception = reception;
    }

    //当前接收数据数量
    public static int reception=0;
    public static String getReceivedata() {
        return Receivedata;
    }

    public static void setReceivedata(String receivedata) {
        Receivedata = receivedata;
    }

    public  String getSenddata() {
        return Senddata;
    }

    public void setSenddata(String senddata) {
        Senddata = senddata;
    }
    //当前接收数据
    public static String Receivedata="";
    //当前发送数据
    public String Senddata="";
        private ViewGroup container;
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_modbus, container, false);
        this.container=container;
        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        try{
            PortStatus.cancel();
            Log.i(TAG, "关闭modbus");
        }catch (Exception e){
            Log.i(TAG, "onDestroy: "+e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
       Button Op=container.findViewById(R.id.OpenPort);
       //等待View加载完成在运行程序注意：界面未加载完成直接运行程序会导致配置文件加载不上去！
       Op.post(new Runnable() {
           @Override
           public void run() {
               main();
           }
       });
    }
    /**
     * 运行的主程序！
     * */
    public void main(){
        PortStatus = new Timer();
        new Thread(){
            @Override
            public void run(){
                //加载上次数据
                GetModbusConfig();
            }
        }.start();
        //监听插入拔出usb设备事件
        PortStatus.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (Ports < DeviceMeasureController.INSTANCE.scanUsbPort().size()) {
                        (getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "出现新的设备！！！", Toast.LENGTH_LONG).show();
                                Ports=DeviceMeasureController.INSTANCE.scanUsbPort().size();
                                //加载串口数据
                                SerialPort();
                                ShowView("出现新的设备！！！");
                            }});
                        PortsStatus=true;
                    }else if (Ports>DeviceMeasureController.INSTANCE.scanUsbPort().size()){
                        Ports=DeviceMeasureController.INSTANCE.scanUsbPort().size();
                        Log.i(TAG, "run: "+Ports);
                        (getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "有设备离线！！！", Toast.LENGTH_LONG).show();
                                ShowView("有设备离线！！！");
                                setSetPort(null);
                                //判断掉线设备是否为当前设备状态
                                Boolean status=false;
                                //判断是否有串口存在
                                for (int i = 0; i < DeviceMeasureController.INSTANCE.scanUsbPort().size(); i++) {
                                    for (int j = 0; j <DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().size() ; j++) {
                                        if (serialPort.equals(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString())) {
                                            Log.i(TAG, "掉线设备不是当前设备！");
                                            status=true;
                                            break;
                                        }
                                    }
                                }
                                //加载串口数据
                                SerialPort();
                                if (!status){
                                    ErroStop("设备掉线！");
                                }
                            }});
                        PortsStatus=true;
                    }
                    Log.d(TAG, "PortsStatus:"+PortsStatus);
                    if (PortsStatus){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        PortsStatus=false;
                    }

                } catch (RuntimeException e) {
                    Log.d(TAG, "异常信息: " + e.getMessage());
                }
            }
        }, 1000,3000);

        Switch AutomaticSending=container.findViewById(R.id.AutomaticSending);
        AutomaticSending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    ModbusFragment.AutoSendStatus=true;
                else
                    ModbusFragment.AutoSendStatus=false;
            }
        });
        Switch Analysis=container.findViewById(R.id.Analysis);
        Analysis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    ModbusFragment.Analysis=true;
                    setReception(0);
                    setSendmNumber(0);
                    EmptyText();
                }
                else{
                    ModbusFragment.Analysis=false;
                    setReception(0);
                    setSendmNumber(0);
                    EmptyText();
                }
            }
        });
        //加载串口
        SerialPort();
        //发送数据点击事件
        container.findViewById(R.id.OpenPort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPort(v);
            }
        });
        //清空显示
        container.findViewById(R.id.EmptyText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmptyText();
            }
        });
        //复制到粘贴板
        container.findViewById(R.id.StartPort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPort(v);
            }
        });
    }
    /**
     * 获取Modbus配置文件信息
     * */
    public void GetModbusConfig(){
        try {
            String config= File.read("ModbusConfig.json",getContext());
            Log.d(TAG, "ModbusConfig: "+config);
            if (config == "false") {
                Log.d(TAG, "没有配置文件！");
                return;
            }
            JSONObject jsonObject = new JSONObject(config);
            EditText Id= container.findViewById(R.id.Id);
            EditText FunctionCore=container.findViewById(R.id.FunctionCore);
            EditText address=container.findViewById(R.id.address);
            EditText length=container.findViewById(R.id.length);
            Id.setText(jsonObject.get("Id").toString());
            FunctionCore.setText(jsonObject.get("FunctionCore").toString());
            address.setText(jsonObject.get("address").toString());
            length.setText(jsonObject.get("length").toString());
            Log.d(TAG, "配置完成");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "获取配置文件异常");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "配置文件解析异常");
        }catch (Exception e){
            Log.d(TAG, "配置文件错误"+e.getMessage());
        }
    }
    /**
     * 加载存在串口
     */
    public void SerialPort() {
        ShowList.clear();
        try {
            ArrayAdapter<String> adapter;
            Port = container.findViewById(R.id.Port);
            String List="串口顺序号：";
            ArrayList<String> Data = new ArrayList<>();
            for (int i = 0; i < DeviceMeasureController.INSTANCE.scanUsbPort().size() ; i++) {
                for (int j = 0; j <DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().size() ; j++) {
                    Data.add(List+i);
                    ShowList.add(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString());
                    status = true;
                }
            }
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, Data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Port.setAdapter(adapter);
        } catch (RuntimeException e) {
            status = false;
            Log.d(TAG, "搜索串口异常信息: " + e.getMessage());
        }
    }
    /**
     * 发送数据点击事件
     * */
    public void OpenPort(View v){
        //保存操作配置文件
        ModbusConfig();
        Button StartPort = container.findViewById(R.id.StartPort);
        final Button OpenPort = container.findViewById(R.id.OpenPort);
        if (StartPort.getText().equals("开启串口")) {
            Log.d(TAG, "开启串口: ");
            Toast.makeText(getActivity(), "未打开串口", Toast.LENGTH_SHORT).show();
            return;
        }
        if (OpenPort.getText().toString().equals("停止发送")) {
            Log.d(TAG, "停止发送: ");
            StartTimer.cancel();
            OpenPort.setText(R.string.OpenPort);
            OpenPort.setBackgroundResource(R.color.RoyalBlue2);
            return;
        }
        final EditText Id = container.findViewById(R.id.Id);
        final EditText address = container.findViewById(R.id.address);
        final EditText length = container.findViewById(R.id.length);
        final EditText FunctionCore = container.findViewById(R.id.FunctionCore);
        String[] show = new String[]{"设备id不能为空", "设备地址不能为空", "搜索长度不能为空"};
        String[] shows = new String[]{Id.getText().toString(), address.getText().toString(), length.getText().toString()};
        //判断是否有空数据
        for (int i = 0; i < show.length; i++){
            if (shows[i].length()==0) {
                Toast.makeText(getActivity(), show[i], Toast.LENGTH_SHORT).show();
                return ;
            }
        }
        Log.i(TAG, "OpenPort: "+getSetPort());
        if (ModbusFragment.AutoSendStatus){
            StartTimer = new Timer();
            StartTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        OpenPort.setText(R.string.StopData);
                        OpenPort.setBackgroundResource(R.color.SaddleBrown);
                        //int转16进制03代表功能码FunctionCore
                        String Data = Modbus_data.HexString(Id.getText().toString(), true) + Modbus_data.HexString(FunctionCore.getText().toString(), true) + Modbus_data.HexString(address.getText().toString(), false) + Modbus_data.HexString(length.getText().toString(), false);
                        final byte[] datas = Modbus_data.getCRC(Data);
                        final String sendData=Modbus_data.BinaryToHexString(datas);
                        setSendmNumber(getSendmNumber()+1);
                        //UI线程更新
                        (getActivity()).runOnUiThread(new Runnable() {
                            public void run() {
                                setSenddata(sendData);
                                ShowView("发送("+getSendmNumber()+"):" + sendData);
                                AcceptingState="false";
                            }
                        });
                        try{
                            getSetPort().write(Modbus_data.getCRC(Data), 3000);
                        }catch (Exception e){
                            ErroStop(e.getMessage());
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                        ErroStop(e.getMessage());

                        Log.d(TAG, "异常" + e.getMessage());
                    }
                    try {
                        for (int i = 0; i < 100; i++) {
                            Thread.sleep(30);
                            if (AcceptingState=="Yes"){
                                AcceptingState="true";
                                break;
                            }
                        }
                        if (AcceptingState.equals("false")) {
                            (getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "接收等待超时，请检查设备状态！！！", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        Thread.sleep(800);
                        if (!ModbusFragment.AutoSendStatus){
                            OpenPort.setText(R.string.OpenPort);
                            OpenPort.setBackgroundResource(R.color.RoyalBlue2);
                            StartTimer.cancel();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 800, 800);
        }else {
            try {
                OpenPort.setEnabled(false);
                OpenPort.setText(R.string.StopData);
                //int转16进制03代表功能码FunctionCore
                String Data = Modbus_data.HexString(Id.getText().toString(), true) + Modbus_data.HexString(FunctionCore.getText().toString(), true) + Modbus_data.HexString(address.getText().toString(), false) + Modbus_data.HexString(length.getText().toString(), false);
                final byte[] datas = Modbus_data.getCRC(Data);
                //UI线程更新
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data=Modbus_data.BinaryToHexString(datas);
                        setSenddata(data);
                        setSendmNumber(getSendmNumber()+1);
                        ShowView("发送("+getSendmNumber()+"):" +data );
                        AcceptingState="false";
                        OpenPort.setText(R.string.OpenPort);
                    }
                });
                try {
                    getSetPort().write(Modbus_data.getCRC(Data), 3000);
                }catch (Exception e){
                    ErroStop(e.getMessage());
                }
                new Thread(){
                    @Override
                    public void run() {
                        for (int i = 0; i < 100; i++) {
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (AcceptingState=="Yes"){
                                AcceptingState="true";
                                break;
                            }
                        }
                            (getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (AcceptingState.equals("false")) {
                                        Toast.makeText(getActivity(), "接收等待超时，请检查设备状态！！！", Toast.LENGTH_LONG).show();
                                    }
                                    OpenPort.setEnabled(true);

                                }
                            });
                    }
                }.start();
            } catch (final Exception e) {
                e.printStackTrace();
                try{
                   ErroStop(e.getMessage());
                }catch (Exception ex){
                    Log.d(TAG, "异常: "+ex);

                }

                Log.d(TAG, "异常" + e.getMessage());
            }
        }
    }

    public void  ErroStop(final String e){
        //UI线程更新
        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button OpenPort=container.findViewById(R.id.OpenPort);
                Toast.makeText(getActivity(), "异常！"+e, Toast.LENGTH_LONG).show();
                Button StartPort=container.findViewById(R.id.StartPort);
                try{
                    ReceiveTimer.cancel();
                }catch (Exception e){

                }
                StartPort.setText(R.string.StartPort);
                StartPort.setBackgroundResource(R.color.RoyalBlue2);
                OpenPort.setEnabled(true);
                OpenPort.setBackgroundResource(R.color.RoyalBlue2);
                OpenPort.setText(R.string.OpenPort);
                try {
                    StartTimer.cancel();
                }catch (Exception e){

                }
            }
        });
    }
    /**
     * 保存Modbus配置文件
     * */
    public void ModbusConfig(){
        try {
            EditText Id= container.findViewById(R.id.Id);
            EditText FunctionCore=container.findViewById(R.id.FunctionCore);
            EditText address=container.findViewById(R.id.address);
            EditText length=container.findViewById(R.id.length);
            String Data="{\"Id\":"+Id.getText()+",\"FunctionCore\":"+FunctionCore.getText()+",\"address\":"+address.getText()+",\"length\":"+length.getText()+"}";
            File.save(Data,"ModbusConfig.json",getActivity());
            Log.i(TAG, "保存配置文件成功");;
        }catch (Exception e){
            Log.d(TAG, "保存个人配置异常："+e.getMessage());
        }
    }
    /**
     * 清空数据点击事件
     * */
    public void EmptyText(){
        TextView Show = container.findViewById(R.id.show);
        Show.setText("");
    }
    /**
     * 打开串口点击事件
     * */
    public void StartPort(View v){
        Button StartPort = container.findViewById(R.id.StartPort);
        Button OpenPort = container.findViewById(R.id.OpenPort);
        if (StartPort.getText().equals("关闭串口")) {
            ReceiveTimer.cancel();
            try {
                setPort.close();
                StartPort.setText(R.string.StartPort);
                OpenPort.setEnabled(true);
                OpenPort.setBackgroundResource(R.color.RoyalBlue2);
                StartPort.setBackgroundResource(R.color.RoyalBlue2);
                if (OpenPort.getText().toString().equals("停止发送")) {
                    Log.d(TAG, "OpenPort");
                    StartTimer.cancel();
                    OpenPort.setText(R.string.OpenPort);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "关闭串口异常" + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            return;
        }
        if (!status) {
            Toast.makeText(getActivity(), "没有串口无法打开！", Toast.LENGTH_SHORT).show();
            return;
        }
        StartPort.setText(R.string.StopPort);
        StartPort.setBackgroundResource(R.color.SaddleBrown);
        ReceiveTimer = new Timer();
        ReceiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ByteDate.length() != 0) {
                    for (int i = 0; i < 20; i++) {
                        try {
                            int length = ByteDate.length();
                            Thread.sleep(50);
                            if (ByteDate.length() == length) {
                                Log.d(TAG, "数据"+ByteDate);
                                final  String data=ByteDate;
                                (getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView Show = container.findViewById(R.id.show);
                                        //限制显示数据最大五万长度
                                        if (Show.getText().length()>50000)
                                            Show.setText("");
                                        if (data.length() != 0){
                                            setReceivedata(data);
                                            setReception(getReception()+1);
                                            ShowView("接收("+getReception()+"):" + data);
                                            AcceptingState="Yes";
                                        }else {
                                            Log.i(TAG, "接受数据为空");
                                        }
                                    }
                                });
                                ByteDate = "";
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.i(TAG, "run: "+e.getMessage());
                            ByteDate = "";
                        }
                    }
                    ByteDate = "";
                }
            }

        }, 200, 200);
        Spinner Port = container.findViewById(R.id.Port);
        int Ports = (int) Port.getSelectedItemId();
        //判断是否有串口存在
        Boolean PortStatus=false;
        for (int i = 0; i < DeviceMeasureController.INSTANCE.scanUsbPort().size(); i++) {
            for (int j = 0; j <DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().size() ; j++) {
                if (ShowList.get(Ports).equals(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString())) {
                    setSetPort(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j));
                    serialPort=DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString();
                    PortStatus=true;
                    break;
                }
            }
        }
        if (!PortStatus) {
            ReceiveTimer.cancel();
            StartPort.setText(R.string.StartPort);
            StartPort.setBackgroundResource(R.color.RoyalBlue2);
            OpenPort.setBackgroundResource(R.color.RoyalBlue2);
            Toast.makeText(getActivity(), "请检查串口是否正常连接！！！", Toast.LENGTH_SHORT).show();
            return;
        }
        UsbMeasureParameter p = new UsbMeasureParameter();
        String Baud = null;
        String DataBits = null;
        String CheckDigit = null;
        String stopBits = null;
        try{
            String _Data =File.read("SerialPort.json",getActivity());
            if (_Data!="false"){
                JSONObject _json =new JSONObject(_Data);
                Baud=_json.get("Baud").toString();
                DataBits=_json.get("DataBits").toString();
                CheckDigit=_json.get("CheckDigit").toString();
                stopBits=_json.get("StopBits").toString();
            }else {
                Log.i(TAG, "未找到配置文件！");
            }
        }catch (Exception e){
            Log.i(TAG, "解析串口参数数据异常："+e.getMessage());
        }
        //设置波特率
        p.setBaudRate(Integer.parseInt(Baud));
        //设置数据位
        p.setDataBits(Integer.parseInt(DataBits));
        //设置校验位
        p.setParity(Integer.parseInt(CheckDigit));
        //设置停止位
        p.setStopBits(Integer.parseInt(stopBits));
        DeviceMeasureController.INSTANCE.measure(getSetPort(), p, new UsbMeasureListener() {
            @Override
            public void measureError(Object o, String s) {
                Log.d(TAG, "接受数据异常: " + s);
                ByteDate = "";
            }

            @Override
            public void measuring(Object o, UsbSerialPort Port, byte[] b) {
                ByteDate += bytes(b);
            }

            @Override
            public void write(Object o, UsbSerialPort setPort) {
            }
        });
    }
    /**
     * 数据滚动
     * @param Data 数据
     */
    public void ShowView(final String Data) {
        try {
            if (ModbusFragment.Analysis){
                Log.i(TAG, "getReceivedata："+getReceivedata());
                Map<String,String> _analysis= Modbus_data.ModbusData(getReceivedata());
                String ShowText="发送数据帧("+getSendmNumber()+")："+getSenddata()+"\n 接收数据帧("+getReception()+")："+getReceivedata()+"\n\n\n 解析数据：\n\n";
                EditText address= container.findViewById(R.id.address);
                int Address= Integer.parseInt(address.getText().toString());
                for (int i = 0; i < _analysis.get("Data").split(",").length; i++) {
                    ShowText+="寄存器"+(Address+i)+":              "+_analysis.get("Data").split(",")[i]+"\n";
                }
                final String data= ShowText;
                final TextView Show = container.findViewById(R.id.show);
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Show.setText(data);
                    }
                });
                Log.i(TAG, "ShowText"+ShowText);
            }else {
                final TextView Show = container.findViewById(R.id.show);
                final ScrollView ShowView = container.findViewById(R.id.ShowView);
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Show.setText(Show.getText() + "\n" + Date.getCurrentDate() + Data);
                        ShowView.post(new Runnable() {
                            @Override
                            public void run() {
                                ShowView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
        }
    }
}