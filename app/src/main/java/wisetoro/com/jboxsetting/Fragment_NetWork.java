package wisetoro.com.jboxsetting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.Utils;
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by guhh on 2017/9/25.
 */

public class Fragment_NetWork extends Fragment {
    private static WifiUtils wifiUtil;
    private final static String TAG = "Fragment_NetWork";

    private RecyclerView wifi_rv;
    private MyAdapter wifi_sa;
    private TextView wifiState_tv;
    private CheckBox wifi_cb;
    private CheckBox ethernet_cb;
    private List<ScanResult> wifi_data = new ArrayList<>();

    private MyBroadCastReceiver myBroadCastReceiver = new MyBroadCastReceiver();

    private static Handler handler;
//    private Timer searchTimer;
    private static Runnable searchWifiRunnable;

    //连接wifi  dialog
    private AlertDialog connectDialog;
    private View view;
    private TextView securityType_tv;
    private EditText pwd_et;
    private CheckBox showPwd_cb;
    private CheckBox moreSet_cb;
    private Spinner ipv4_sp;
    private LinearLayout ipv4_ll;
    private LinearLayout ipv4Para_ll;
    private EditText ip_et;
    private EditText wg_et;
    private EditText wlqzcd_et;
    private EditText dns1_et;
    private EditText dns2_et;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network,container,false);
        wifiUtil = new WifiUtils(getActivity().getApplicationContext());
        initView(view);//初始化view
        bindView();//view 绑定事件
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(myBroadCastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myBroadCastReceiver);
    }

    private void initView(View view){
        //初始化WiFi列表recycleView
        wifi_rv = (RecyclerView) view.findViewById(R.id.wifi_rv);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setSmoothScrollbarEnabled(true);
        mLinearLayoutManager.setAutoMeasureEnabled(true);
        wifi_rv.setLayoutManager(mLinearLayoutManager);
        wifi_rv.setHasFixedSize(true);
        wifi_rv.setNestedScrollingEnabled(false);
        wifi_sa = new MyAdapter();
        wifi_rv.setAdapter(wifi_sa);

        wifiState_tv = (TextView) view.findViewById(R.id.wifiState_tv);
        wifi_cb = (CheckBox) view.findViewById(R.id.wifi_cb);
        if(WifiManager.WIFI_STATE_ENABLED == wifiUtil.getWifiState()){
            wifi_cb.setChecked(true);
        }
        ethernet_cb = (CheckBox) view.findViewById(R.id.ethernet_cb);


    }

    private void bindView(){
        wifi_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){//如果选中就开启wifi
                    openWifi();
                }else{//关闭wifi
                    closeWifi();
                }
            }
        });

        ethernet_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }

    private void initConnectDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_connectwifi,null);

        ipv4_ll = (LinearLayout) view.findViewById(R.id.ipv4_ll);
        ipv4Para_ll = (LinearLayout) view.findViewById(R.id.ipv4Para_ll);
        ipv4_sp = (Spinner) view.findViewById(R.id.ipv4_sp);
        securityType_tv = (TextView) view.findViewById(R.id.securityType_tv);
        pwd_et = (EditText) view.findViewById(R.id.pwd_et);
        showPwd_cb = (CheckBox) view.findViewById(R.id.showPwd_cb);
        moreSet_cb = (CheckBox) view.findViewById(R.id.moreSet_cb);
        ip_et = (EditText) view.findViewById(R.id.ip_et);
        wg_et = (EditText) view.findViewById(R.id.wg_et);
        wlqzcd_et = (EditText) view.findViewById(R.id.wlqzcd_et);
        dns1_et = (EditText) view.findViewById(R.id.dns1_et);
        dns2_et = (EditText) view.findViewById(R.id.dns2_et);

        //绑定事件
        MyTextWatch myTextWatch = new MyTextWatch();
        pwd_et.addTextChangedListener(myTextWatch);
        ip_et.addTextChangedListener(myTextWatch);
        wg_et.addTextChangedListener(myTextWatch);
        wlqzcd_et.addTextChangedListener(myTextWatch);
        dns1_et.addTextChangedListener(myTextWatch);
        dns2_et.addTextChangedListener(myTextWatch);


        showPwd_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pwd_et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    pwd_et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        moreSet_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ipv4_ll.setVisibility(View.VISIBLE);
                }else{
                    ipv4_ll.setVisibility(View.GONE);
                    ipv4Para_ll.setVisibility(View.GONE);
                    ipv4_sp.setSelection(0);
                }
            }
        });

        ipv4_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){//如果选中了静态
                    ipv4Para_ll.setVisibility(View.VISIBLE);
                }else{
                    ipv4Para_ll.setVisibility(View.GONE);
                }
                connectDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(validate());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(view);
        builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("取消",null);
        connectDialog = builder.create();
    }

    private void openWifi(){
        wifiUtil.openWifi();
    }

    private void closeWifi(){
        wifiUtil.closeWifi();
        wifi_data = new ArrayList<>();
        wifi_sa.notifyDataSetChanged();
    }

    private void startSearchWifi(){
        if(handler == null){
            handler = new Handler();
        }
        if(searchWifiRunnable == null){
            searchWifiRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"searchWifiRunnable");
                    if(wifiUtil!=null){
                        wifi_data = wifiUtil.getWifiAccessPointList();
                        //对wifi_data进行排序
                        Collections.sort(wifi_data, new Comparator<ScanResult>() {

                            @Override
                            public int compare(ScanResult o1, ScanResult o2) {
                                return o2.level-o1.level;
                            }
                        });
                        wifi_sa.notifyDataSetChanged();
                        handler.postDelayed(searchWifiRunnable,1000);
                    }
                }
            };
        }
        handler.postDelayed(searchWifiRunnable,1000);
    }

    private void stopSearchWifi(){
        if(handler!= null){
            handler.removeCallbacks(searchWifiRunnable);
        }
    }

    private void changeWifiState(int wifiState) {
        switch (wifiState){
            case WifiManager.WIFI_STATE_DISABLING:
                wifiState_tv.setText("正在关闭WIFI...");
                wifi_cb.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                wifiState_tv.setText("已关闭WIFI");
                wifi_cb.setEnabled(true);
               stopSearchWifi();//停止扫描
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                wifiState_tv.setText("正在开启WIFI...");
                wifi_cb.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                wifiState_tv.setText("已开启WIFI");
                wifi_cb.setEnabled(true);
                Log.i(TAG,"WIFI_STATE_ENABLED");
                stopSearchWifi();//因为会多次进入case 所以先停止 保留最后一次 timer
                startSearchWifi();//一秒扫描一次wifi
                break;
        }
    }

    private void setWifiLevelIcon(ImageView imageView, int level) {
        switch (level){
            case 0:
                imageView.setImageResource(R.drawable.wifi_level_02);
                break;
            case 1:
                imageView.setImageResource(R.drawable.wifi_level_03);
                break;
            case 2:
                imageView.setImageResource(R.drawable.wifi_level_04);
                break;
            case 3:
                imageView.setImageResource(R.drawable.wifi_level_05);
                break;
        }
    }

    private boolean validate() {
        String pwd = pwd_et.getText().toString().trim();
        String ip = ip_et.getText().toString().trim();
        String wg = wg_et.getText().toString().trim();
        String wlqzcd = wlqzcd_et.getText().toString().trim();
        String dns1 = dns1_et.getText().toString().trim();
        String dns2 = dns2_et.getText().toString().trim();
        if(ipv4Para_ll.getVisibility() == View.VISIBLE){
            if(pwd.length()>=8 && Util.isboolIp(ip) && !wlqzcd.equals("") && Util.isboolIp(wg) && Util.isboolIp(dns1)){
                return true;
            }else{
                return false;
            }
        }else{
            if(pwd.length()>=8){
                return true;
            }else{
                return false;
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Fragment_NetWork.this.getContext()).inflate(R.layout.item_wifiinfo,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            myViewHolder.rootview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
//                    wifiUtil.connect2AccessPoint(wifi_data.get(position),"sunpnsoft");
                    if(connectDialog == null)
                        initConnectDialog();
                    connectDialog.show();
                    connectDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            });
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.rootview.setTag(position);

            ScanResult wifiInfo  = wifi_data.get(position);
            holder.wifiName_tv.setText(wifiInfo.SSID);//设置wifi名字
            int level = WifiManager.calculateSignalLevel(wifiInfo.level, 4);//计算wifi信号等级
            setWifiLevelIcon(holder.imageView,level);//设置wifi的图标
            holder.wifiStatus_tv.setText(wifiInfo.capabilities);

        }

        @Override
        public int getItemCount() {
            return wifi_data.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            private LinearLayout rootview;

            private ImageView imageView;
            private TextView wifiName_tv;
            private TextView wifiStatus_tv;
            public MyViewHolder(View itemView) {
                super(itemView);
                rootview = (LinearLayout) itemView.findViewById(R.id.rootview);
                imageView = (ImageView) itemView.findViewById(R.id.icon);
                wifiName_tv = (TextView) itemView.findViewById(R.id.wifi_name_tv);
                wifiStatus_tv = (TextView) itemView.findViewById(R.id.wifi_status_tv);
            }
        }
    }

    class MyBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi状态改变
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                Log.i(TAG,wifiState+"----");
                changeWifiState(wifiState);
            }
        }
    }

    class MyTextWatch implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            connectDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(validate());

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
