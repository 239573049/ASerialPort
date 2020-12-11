package com.ASerialPort.ui.DLT645;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.ASerialPort.Function.DLT645_data;
import com.ASerialPort.Function.File;
import com.ASerialPort.MainActivity;
import com.ASerialPort.R;
import com.hd.serialport.listener.UsbMeasureListener;
import com.hd.serialport.method.DeviceMeasureController;
import com.hd.serialport.param.UsbMeasureParameter;
import com.hd.serialport.usb_driver.UsbSerialPort;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import static androidx.constraintlayout.motion.widget.MotionScene.TAG;
import static com.ASerialPort.Function.DLT645_data.analysis;
import static com.ASerialPort.MainActivity.bytes;
public class DLT645Fragment extends Fragment {
    //发送次数
    private  TextView number;
    //当前环境
    private ViewGroup container;
    //接收数据
    private static String ByteDate = "";
    //加载串口状态
    private static Boolean status = false;

    public static UsbSerialPort getSetPort() {
        return setPort;
    }

    public static void setSetPort(UsbSerialPort setPort) {
        DLT645Fragment.setPort = setPort;
    }

    //当前的串口
    private static UsbSerialPort setPort;
    //当前串口的下拉框
    private static Spinner Port645;
    //数据接收状态
    private static String AcceptingState = "false";
    //当前在线usb设备
    public static int Ports = 0;

    public static int getSendmNumber() {
        return SendmNumber;
    }

    public static void setSendmNumber(int sendmNumber) {
        SendmNumber = sendmNumber;
    }

    //当前发送数据数量
    private static int SendmNumber = 0;

    public static int getReception() {
        return reception;
    }

    public static void setReception(int reception) {
        DLT645Fragment.reception = reception;
    }

    public static Boolean getAutoSendStatus() {
        return AutoSendStatus;
    }

    private static void setAutoSendStatus(Boolean autoSendStatus) {
        AutoSendStatus = autoSendStatus;
    }

    //自动发送当前状态
    private static Boolean AutoSendStatus = false;
    //当前接收数据数量
    private static int reception = 0;
    //接收数据定时器
    private static Timer ReceiveTimer;
    //发送数据定时器
    private static Timer StartTimer;

    public static String getReceivedata() {
        return Receivedata;
    }

    public static void setReceivedata(String receivedata) {
        Receivedata = receivedata;
    }

    public String getSenddata() {
        return Senddata;
    }

    public void setSenddata(String senddata) {
        Senddata = senddata;
    }

    //当前接收数据
    public static String Receivedata = "";
    //当前发送数据
    public String Senddata = "";
    //串口状态监听
    private static Timer PortStatus;
    //当前操作串口
    private static String serialPort = null;
    //串口监听状态
    private static Boolean PortsStatus = false;

    //当前参数的json
    public static JSONArray getParameter() {
        return Parameter;
    }

    public static void setParameter(JSONArray parameter) {
        Parameter = parameter;
    }

    private LayoutInflater inflater;

    private static JSONArray Parameter;
    //当前串口保存地址
    private static List<String> ShowList = new ArrayList<>();
    private static ArrayAdapter<String> adapter;
    //显示的面板
    private ScrollView ShowView645;
    //解析
    private Switch Analysis645;
    //自动发送
    private Switch AutomaticSending645;
    //抄表按钮
    private Button checkAll;
    //解析模块
    private ScrollView ShowView6451;
    //表号
    private AutoCompleteTextView TableNumber;
    //数据帧显示模块
    private TextView Show645;
    //功能参数
    private Map<String, String> TOP = new HashMap<>();
    //功能的id
    public Map<String, Integer> _FunctionId;
    //自动发送定时器
    private Timer _automaticSend;
    //查看表号
    private Button examineTabNumber;
    //打开串口
    private Button StartPort645;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dlt645, container, false);
        this.container = container;
        this.inflater = inflater;
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //等待按钮加载完成启动程序！
        container.findViewById(R.id.Port645).post(new Runnable() {
            @Override
            public void run() {
                main();
            }
        });

    }
    /**
     * 清空数据点击事件
     */
    public void EmptyText645() {
        TextView Show = container.findViewById(R.id.Show645);
        setSendmNumber(0);
        number.setText("发送次数："+getSendmNumber());
        Show.setText("");
    }
    /**
     * 主程序
     */
    public void main() {
        initializeClick();
        DLT645config();
        Port();
        SerialPort();
    }
    /**
     * 复制到粘贴板事件处理
     * */
    public void Derive645(View v){
        try{
            TextView Show=container.findViewById(R.id.Show645);
            ClipboardManager cmd =(ClipboardManager)getActivity().getSystemService(MainActivity.CLIPBOARD_SERVICE);
            cmd.setText(Show.getText().toString());
            Toast.makeText(getActivity(),"数据已复制至剪贴板！",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(getActivity(),"复制至剪贴板异常："+e.getMessage(),Toast.LENGTH_LONG).show();
            Log.d(TAG, "复制至剪贴板异常"+e.getMessage());
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        try{
            PortStatus.cancel();
        }catch (Exception e){
            Log.i(TAG, "关闭异常："+e.getMessage());
        }
    }
    /**
     * 监听插入拔出usb设备事件
     * */
    public void Port(){
        PortStatus=new Timer();
        //监听插入拔出usb设备事件
        PortStatus.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (Ports < DeviceMeasureController.INSTANCE.scanUsbPort().size()) {
                        (getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "出现新的设备！！！", Toast.LENGTH_SHORT).show();
                                Ports=DeviceMeasureController.INSTANCE.scanUsbPort().size();
                                //加载串口数据
                                SerialPort();
                                Show645("出现新的设备！！！");
                            }});
                        PortsStatus=true;
                    }else if (Ports>DeviceMeasureController.INSTANCE.scanUsbPort().size()){
                        Ports=DeviceMeasureController.INSTANCE.scanUsbPort().size();
                        Log.i(TAG, "run: "+Ports);
                        (getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "有设备离线！！！", Toast.LENGTH_SHORT).show();
                                Show645("有设备离线！！！");
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
    }
    /**
     * 点击事件初始化
     */
    public void initializeClick() {
        TableNumber = container.findViewById(R.id.TableNumber);
        Show645=container.findViewById(R.id.Show645);
        ShowView645=container.findViewById(R.id.ShowView645);
        AutomaticSending645 = container.findViewById(R.id.AutomaticSending645);
        number=container.findViewById(R.id.number645);
        examineTabNumber=container.findViewById(R.id.examineTabNumber);
        ShowView6451 = container.findViewById(R.id.ShowView6451);
        Analysis645 = container.findViewById(R.id.Analysis645);
        StartPort645=container.findViewById(R.id.StartPort645);
        container.findViewById(R.id.Derive645).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Derive645(v);
            }
        });
        StartPort645.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPort645(v);
            }
        });
        container.findViewById(R.id.EmptyText645).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmptyText645();
            }
        });
        examineTabNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                examineTabNumber();
            }
        });
        Analysis645.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ShowView645.setVisibility(View.GONE);
                    ShowView6451.setVisibility(View.VISIBLE);
                    initializeid();
                    Log.i(TAG, "切换解析状态");
                } else {
                    ShowView6451.setVisibility(View.GONE);
                    ShowView645.setVisibility(View.VISIBLE);
                    Log.i(TAG, "切换数据帧状态");

                }
            }
        });
        AutomaticSending645.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    setAutoSendStatus(true);
                    automaticSend();
                }
                else{
                    setAutoSendStatus(false);
                    try{
                        _automaticSend.cancel();
                    }catch (Exception e){

                    }
                }
            }
        });

    }
    /**
     * 查询表号
     * */
    public void examineTabNumber(){
        try{
            if (StartPort645.getText().equals("开启串口")) {
                Log.d(TAG, "开启串口: ");
                Toast.makeText(getActivity(), "未打开串口", Toast.LENGTH_SHORT).show();
                return;
            }
            examineTabNumber.setEnabled(false);
            getSetPort().write(DLT645_data.SendData("FE FE FE FE 68 AA AA AA AA AA AA 68 13 00 DF 16"), 3000);
            Thread t = new Thread() {
                @Override
                public void run() {
                    String TabNumber = null;
                    for (int i = 0; i < 600; i++) {
                        try {
                            Thread.sleep(5);
                            if (AcceptingState == "Yes") {
                                TabNumber=analysis(ByteDate,"tab");
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            AcceptingState = "true";
                        }
                    }
                    TableNumber.setText(TabNumber);
                    Show645("表号："+TabNumber);
                    AcceptingState = "true";
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AcceptingState.equals("false")) {
                                Toast.makeText(getActivity(), "接收等待超时，请检查设备状态！！！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            };
            t.start();
            t.join();
        }catch (Exception e){
            Log.i(TAG, "异常exa"+e.getMessage());
        }finally {
            examineTabNumber.setEnabled(true);
        }
    }

    /**
     * 功能id初始化
     */
    public void initializeid() {
        try {
            _FunctionId = new HashMap<>();
            _FunctionId.clear();
            _FunctionId.put("VA", R.id.VA);
            _FunctionId.put("VB", R.id.VB);
            _FunctionId.put("VC", R.id.VC);
            _FunctionId.put("AA", R.id.AA);
            _FunctionId.put("AB", R.id.AB);
            _FunctionId.put("AC", R.id.AC);
            _FunctionId.put("PA", R.id.PA);
            _FunctionId.put("PB", R.id.PB);
            _FunctionId.put("PC", R.id.PC);
            _FunctionId.put("QA", R.id.QA);
            _FunctionId.put("QB", R.id.QB);
            _FunctionId.put("QC", R.id.QC);
            _FunctionId.put("Uab", R.id.Uab);
            _FunctionId.put("Ubc", R.id.Ubc);
            _FunctionId.put("Uca", R.id.Uca);
            _FunctionId.put("ZQ", R.id.ZQ);
            _FunctionId.put("ZP", R.id.ZP);
            _FunctionId.put("F", R.id.F);
            _FunctionId.put("Zhp", R.id.Zhp);
            _FunctionId.put("zp", R.id.zp);
            _FunctionId.put("fq", R.id.fq);
            _FunctionId.put("Zhq1", R.id.Zhq1);
            _FunctionId.put("Zhq2", R.id.Zhq2);
            Set<Map.Entry<String, Integer>> entry = _FunctionId.entrySet();
            TextView id ;
            for (Map.Entry<String, Integer> m : entry) {
                int value = m.getValue();
                id = container.findViewById(value);
                final String key = m.getKey();
                String FunctionCode = null;
                for (int i = 0; i < getParameter().length(); i++) {
                    JSONObject jsonObject = new JSONObject(getParameter().get(i).toString());
                    if (key.equals(jsonObject.get("name"))) {
                        FunctionCode = String.valueOf(jsonObject.get("FunctionCode"));
                    }
                }
                Log.i(TAG, "i:");
                final String finalFunctionCode = FunctionCode;
                id.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveTabNumber(TableNumber.getText().toString());
                        MeterReading(finalFunctionCode, key);
                    }
                });
            }
        } catch (Exception e) {
            Log.i(TAG, "初始化功能参数异常：" + e.getMessage());
        }
    }
    /**
     * 发送数据
     * @param FunctionCode 参数码
     * @param name 参数名称
     */
    public void MeterReading(String FunctionCode, final String name) {
        try {
            Log.i(TAG, "FunctionCode" + FunctionCode);
            Log.i(TAG, "name" + name);
            if (StartPort645.getText().equals("开启串口")) {
                Log.d(TAG, "开启串口: ");
                Toast.makeText(getActivity(), "未打开串口", Toast.LENGTH_SHORT).show();
                return;
            }
            String _TableNumber= String.valueOf(TableNumber.getText());
            if (_TableNumber.length() == 0) {
                _TableNumber="999999999999";
            }else {

            }
            final String Data = DLT645_data.makeCheck(FunctionCode, _TableNumber);
            setSenddata(name);
            setSendmNumber(getSendmNumber() + 1);

            AcceptingState = "false";
            Show645("发送："+Data);
            getSetPort().write(DLT645_data.SendData(Data), 3000);
            Thread t = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        try {
                            Thread.sleep(30);
                            if (AcceptingState == "Yes") {
                                Thread.sleep(10);
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            AcceptingState = "true";
                        }
                    }
                    AcceptingState = "true";
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AcceptingState.equals("false")) {
                                Toast.makeText(getActivity(), "接收等待超时，请检查设备状态！！！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            };
            t.start();
            t.join();

        } catch (Exception e) {
            Log.i(TAG, "异常信息s：" + e.getMessage());
        }

    }
    /**
     * 自动发送数据
     * */
    public void automaticSend(){
        try {
            _automaticSend=new Timer();
            _automaticSend.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray=getParameter();
                        for (int i = 0; i <jsonArray.length() ; i++) {
                            JSONObject jsonObject=new JSONObject(jsonArray.get(i).toString());
                            long startTime=System.currentTimeMillis();   //获取开始时间
                            MeterReading(jsonObject.get("FunctionCode").toString(),jsonObject.get("name").toString());
                            long endTime=System.currentTimeMillis(); //获取结束时间
                            Log.i(TAG, "程序运行时间： "+(endTime-startTime)+"ms");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!getAutoSendStatus()) {
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },1000,800);
        }catch (Exception e){

        }
    }
    /**
     * 显示解析数据到界面
     * */
    public void ShowView645(String Data) {
        try {
            Integer value;
            Set<Map.Entry<String, Integer>> entry = _FunctionId.entrySet();
            TextView id ;
            for (Map.Entry<String, Integer> m : entry) {
                String key = m.getKey();
                value = m.getValue();
                if (key.equals(getSenddata())) {
                    id = container.findViewById(value);
                    for (int i = 0; i <getParameter().length() ; i++) {
                        JSONObject jsonObject=new JSONObject(getParameter().get(i).toString());
                        if (jsonObject.get("name").equals(key)) {
                            Data=analysis(Data,jsonObject.get("DataFormat").toString());
                            break;
                        }
                    }
                    final TextView finalId = id;
                    final String finalData = Data;
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finalId.setText(finalData);
                        }
                    });
                    break;
                }
            }


        } catch (Exception e) {
            Log.i(TAG, "显示异常：" + e.getMessage());
        }
    }
    /**
     *
     * */
    public void saveTabNumber(String data){
        try {
            File.save(data,"TabData.txt",getContext());
        }catch (Exception e){
            Log.i(TAG, "saveTabNumber: "+e.getMessage());
        }
    }
    /**
     * 加载存在串口
     */
    public void SerialPort() {
        try {
            ShowList.clear();
            Port645 = container.findViewById(R.id.Port645);
            String List = "串口顺序号：";
            ArrayList<String> Data = new ArrayList<>();
            for (int i = 0; i < DeviceMeasureController.INSTANCE.scanUsbPort().size(); i++) {
                for (int j = 0; j < DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().size(); j++) {
                    Data.add(List + i);
                    ShowList.add(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString());
                    status = true;
                }
            }
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, Data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Port645.setAdapter(adapter);
        } catch (RuntimeException e) {
            status = false;
            Log.d(TAG, "搜索串口异常信息: " + e.getMessage());
        }
    }
    /**
     * 打开串口
     */
    public void StartPort645(View v) {
        Button StartPort = container.findViewById(R.id.StartPort645);
        if (StartPort.getText().equals("关闭串口")) {
            try {
                StartPort.setBackgroundResource(R.color.RoyalBlue2);
                ReceiveTimer.cancel();
                setPort.close();
                StartPort.setText(R.string.StartPort);
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
                                Log.d(TAG, "数据:" + ByteDate);
                                final String data = ByteDate;
                                if (data.length() != 0) {
                                    setReception(getReception() + 1);
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try {
                                                ShowView645(data);
                                                Show645("接收："+data);
                                            }catch (final Exception e){
                                                (getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(),"接收异常："+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                        }
                                    }.start();
                                    AcceptingState = "Yes";
                                } else {
                                    Log.i(TAG, "接受数据为空");
                                }
                                Thread.sleep(50);
                                ByteDate = "";
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.i(TAG, "run: " + e.getMessage());
                            ByteDate = "";
                        }
                    }
                    ByteDate = "";
                }
            }

        }, 200, 200);
        Spinner Port = container.findViewById(R.id.Port645);
        int Ports = (int) Port.getSelectedItemId();
        //判断是否有串口存在
        Boolean PortStatus = false;
        for (int i = 0; i < DeviceMeasureController.INSTANCE.scanUsbPort().size(); i++) {
            for (int j = 0; j < DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().size(); j++) {
                if (ShowList.get(Ports).equals(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString())) {
                    setSetPort(DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j));
                    serialPort = DeviceMeasureController.INSTANCE.scanUsbPort().get(i).getPorts().get(j).toString();
                    PortStatus = true;
                    break;
                }
            }
        }
        if (!PortStatus) {
            ReceiveTimer.cancel();
            StartPort.setText(R.string.StartPort);
            StartPort.setBackgroundResource(R.color.RoyalBlue2);
            Toast.makeText(getActivity(), "请检查串口是否正常连接！！！", Toast.LENGTH_SHORT).show();
            return;
        }
        UsbMeasureParameter p = new UsbMeasureParameter();
        String Baud = null;
        String DataBits = null;
        String CheckDigit = null;
        String stopBits = null;
        try {
            String _Data = File.read("SerialPort.json", getActivity());
            if (_Data != "false") {
                JSONObject _json = new JSONObject(_Data);
                Baud = _json.get("Baud").toString();
                DataBits = _json.get("DataBits").toString();
                CheckDigit = _json.get("CheckDigit").toString();
                stopBits = _json.get("StopBits").toString();
                Log.i(TAG, "CheckDigit: " + CheckDigit);
            } else {
                Log.i(TAG, "未找到配置文件！");
            }
        } catch (Exception e) {
            Log.i(TAG, "解析串口参数数据异常：" + e.getMessage());
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
                Log.i(TAG, "measuring: " + ByteDate);
            }

            @Override
            public void write(Object o, UsbSerialPort setPort) {
            }
        });
    }
    /**
     * 串口状态处理
     * */
    public void  ErroStop(final String e){
        //UI线程更新
        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "异常！"+e, Toast.LENGTH_SHORT).show();
                Button StartPort645=container.findViewById(R.id.StartPort645);
                try{
                    ReceiveTimer.cancel();
                }catch (Exception e){

                }
                StartPort645.setText(R.string.StartPort);
                StartPort645.setBackgroundResource(R.color.RoyalBlue2);
                try {
                    StartTimer.cancel();
                }catch (Exception e){

                }
            }
        });
    }
    /**
     * 数据帧显示
     * @param Data 需要显示的数据
     * */
    public void Show645(final String Data){
        try{
            (getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (Show645.getText().length()>50000)
                        Show645.setText("");
                    Show645.setText(Show645.getText()+"\n"+Data);
                    number.setText("发送次数："+getSendmNumber());
                    ShowView645.post(new Runnable() {
                        @Override
                        public void run() {
                            ShowView645.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            });
        }catch (Exception e){
            Log.i(TAG, "异常信息："+e.getMessage());
        }
    }
    /**
     * 加载功能参数
     */
    public void DLT645config() {
        try {
            String DATA = "[\n" +
                    "  {\n" +
                    "    \"name\": \"VA\",\n" +
                    "    \"FunctionCode\": \"33 34 34 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\":\"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"VB\",\n" +
                    "    \"FunctionCode\": \"33 35 34 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\":\"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"VC\",\n" +
                    "    \"FunctionCode\": \"33 36 34 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\":\"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"AA\",\n" +
                    "    \"FunctionCode\": \"33 34 35 35\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\":\"###.000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"AB\",\n" +
                    "    \"FunctionCode\": \"33 35 35 35\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\":\"###.000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"AC\",\n" +
                    "    \"FunctionCode\": \"33 36 35 35\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\":\"###.000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"PA\",\n" +
                    "    \"FunctionCode\": \"33 34 36 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\":\"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"PB\",\n" +
                    "    \"FunctionCode\": \"33 35 36 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\":\"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"PC\",\n" +
                    "    \"FunctionCode\": \"33 36 36 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"QA\",\n" +
                    "    \"FunctionCode\": \"34 34 37 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"QB\",\n" +
                    "    \"FunctionCode\": \"35 34 37 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"QC\",\n" +
                    "    \"FunctionCode\": \"36 34 37 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Uab\",\n" +
                    "    \"FunctionCode\": \"33 34 3A 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\": \"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Ubc\",\n" +
                    "    \"FunctionCode\": \"33 35 3A 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\": \"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Uca\",\n" +
                    "    \"FunctionCode\": \"33 36 3A 35\",\n" +
                    "    \"Format\": \"0.1\",\n" +
                    "    \"DataFormat\": \"###.0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"ZQ\",\n" +
                    "    \"FunctionCode\": \"33 33 36 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"ZP\",\n" +
                    "    \"FunctionCode\": \"33 34 37 35\",\n" +
                    "    \"Format\": \"0.0001\",\n" +
                    "    \"DataFormat\": \"##.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"F\",\n" +
                    "    \"FunctionCode\": \"33 33 39 35\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"#.0000\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Zhp\",\n" +
                    "    \"FunctionCode\": \"33 33 33 33\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"######.00\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"zp\",\n" +
                    "    \"FunctionCode\": \"33 33 34 33\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"######.00\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"fq\",\n" +
                    "    \"FunctionCode\": \"33 33 35 33\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"######.00\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Zhq1\",\n" +
                    "    \"FunctionCode\": \"33 33 36 33\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"######.00\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"Zhq2\",\n" +
                    "    \"FunctionCode\": \"33 33 37 33\",\n" +
                    "    \"Format\": \"0.001\",\n" +
                    "    \"DataFormat\": \"######.00\"\n" +
                    "  }\n" +
                    "]";

            String Data = File.read("DLT645config.json", getContext());
            if (Data.equals("false")) {
                File.save(DATA, "DLT645config.json", getContext());
                Data = File.read("DLT645config.json", getContext());
            }
            setParameter(new JSONArray(Data));
            try{
                String da=File.read("TabData.txt", getContext());
                if (da.equals("false")) {
                    return;
                }
                TableNumber.setText(da);
            }catch (Exception e){
                Log.i(TAG, "异常T:"+e.getMessage());
            }
        } catch (Exception e) {
            Log.i(TAG, "DLT645config异常:" + e.getMessage());
        }
    }
}