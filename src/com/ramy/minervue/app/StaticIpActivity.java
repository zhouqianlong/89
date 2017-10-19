package com.ramy.minervue.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle.Control;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bhj.staticwifisetting.StaticIpSet;
import com.ramy.minervue.R;
import com.ramy.minervue.adapter.SettingAdapter;
import com.ramy.minervue.bean.UsersBean;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.util.ConfigUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class StaticIpActivity extends Activity implements OnClickListener{
	private WifiManager mWifi;
	private ListView lv_view;
	private List<ScanResult> list = null; 
	private SettingAdapter settingAdapter;
	private EditText et_kzName,et_pwd;
	private EditText et_ipadd,et_wangguan,et_yumin;//高级选项
	private LinearLayout ll_gaoji;
	private Button btn_kz,btn_senior;//扩展按钮
	private ImageView iv_password_is_show;//密码显示
	private boolean isGj = false;
	private LinearLayout ll_conn,ll_pwd;
	public String IPADDRESS="10.10.10.135";
	private Button btn_canle;
	
	/**
     * 获取手机mac地址<br/>
     * 错误返回12个0
     */
    public  String getMacAddress(Context context) {
        // 获取mac地址：
        String macAddress = "000000000000";
        try {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr
                    .getConnectionInfo());
            if (null != info) {
                if (!TextUtils.isEmpty(info.getMacAddress())){
                	String[] macs = info.getMacAddress().split(":");
                	int a = Integer.valueOf(macs[macs.length-2],16);
                	int b = Integer.valueOf(macs[macs.length-1],16);
//                	IPADDRESS = "10.10."+a+"."+b;
                	IPADDRESS = "10.10.10."+b;
                    macAddress = info.getMacAddress().replace(":", ".");
                }
                else
                    return macAddress;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return macAddress;
        }
        return macAddress;
    }
	private void initTitle() {
		View view = findViewById(R.id.il_title);
		TextView tv_back_title = (TextView) view.findViewById(R.id.tv_back_title);
		tv_back_title.setText(R.string.back);

		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		tv_title.setText(R.string.back);

		View btn_back_title =  view.findViewById(R.id.btn_back_title);
		View btn_other = view.findViewById(R.id.btn_other);
		btn_back_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn_other.setVisibility(View.GONE);
	}


    boolean flag = false;
    String ip;
	private String serverip;
	private String username;
	private String wangguan;
	private String yuming;
	private String deriverid;
	private String xinhaobaohu;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conn_wifi_setting);
		ip = getIntent().getExtras().getString("ip");
		deriverid = getIntent().getExtras().getString("deriverid");
		serverip = getIntent().getExtras().getString("serverip");
		username = getIntent().getExtras().getString("username");
		wangguan = getIntent().getExtras().getString("wangguan");
		yuming = getIntent().getExtras().getString("yuming");
		xinhaobaohu = getIntent().getExtras().getString("xinhaobaohu");
		
		
	
		
		
		ll_conn = (LinearLayout) findViewById(R.id.ll_conn);
		ll_gaoji = (LinearLayout) findViewById(R.id.ll_gaoji);
		ll_pwd = (LinearLayout) findViewById(R.id.ll_pwd);
		et_kzName = (EditText) findViewById(R.id.et_kzName);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_ipadd = (EditText) findViewById(R.id.et_ipadd);
		et_wangguan = (EditText) findViewById(R.id.et_wangguan);
		et_yumin = (EditText) findViewById(R.id.et_yumin);
		iv_password_is_show = (ImageView) findViewById(R.id.iv_password_is_show);
		mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
		iv_password_is_show.setOnClickListener(etTextViewOnclickListener);
		initTitle();
		btn_kz = (Button) findViewById(R.id.btn_kz);
		btn_canle = (Button) findViewById(R.id.btn_canle);
		btn_kz.setOnClickListener(this);
		btn_canle.setOnClickListener(this);
		btn_senior = (Button) findViewById(R.id.btn_senior);
		btn_senior.setOnClickListener(this);
		lv_view = (ListView) findViewById(R.id.lv_view);
		ll_conn.setVisibility(View.GONE);
		flag = true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (flag) {
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mHandler.sendEmptyMessage(100);
				}
				
				
			}
		}).start();
		String mac = getMacAddress(getApplicationContext());
		Toast.makeText(getApplicationContext(), mac, 0).show();
		lv_view.setOnItemClickListener(lv_view_ItemClick_Listener);
		et_ipadd.setText(ip);
		et_wangguan.setText(wangguan);
		et_yumin.setText(yuming);
//		String[] ips = ip.split("\\.");
//		et_wangguan.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
//		et_yumin.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
//		et_ipadd.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//
//			}
//
//			public void afterTextChanged(Editable s) {
//				if(isboolIP(s.toString())==true){
//					String[] ips = s.toString().split("\\.");
//					et_wangguan.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
//					et_yumin.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
//				}else{
//					et_wangguan.setHint("4、请输入网关");
//					et_yumin.setHint("5、请输入域名");
//				}
//			}
//		});
		et_pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length()>=8){
					btn_kz.setText(R.string.connect);
				}else{
					btn_kz.setText(R.string.password_min_error	);
				}
			}
		});
		
		SharedPreferences settings = getSharedPreferences("AddressManagerActivity", 0);
		settings.edit().putString("et_server_address", serverip).commit();  //服务器地址
		settings.edit().putString("et_driver_name", username);  //设备名称
		settings.edit().putBoolean("init", false).commit();  //是否是第一次
		ConfigUtil.changeFile( serverip,username);
		settings.edit().putString("et_wifidb",xinhaobaohu).commit();  
		MainActivity.DBERROR = Integer.valueOf(xinhaobaohu);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		flag = false; 
	}

	private void setAdapter() {
		String wifiId = getSSID();
		list = 	mWifi.getScanResults();
		list = trimList(list);
		if(settingAdapter ==null){
			settingAdapter = new SettingAdapter(list, getApplicationContext());
			settingAdapter.setConnPosition(wifiId,getMac());
			lv_view.setAdapter(settingAdapter);
		}else{
			settingAdapter.setConnPosition(wifiId,getMac());
			settingAdapter.setList(list);;
			settingAdapter.notifyDataSetChanged();
		}
	}

	public List<ScanResult> trimList(List<ScanResult> list){
		List<ScanResult> tmp = new ArrayList<ScanResult>();
		for(int i = 0 ; i< list.size();i++){
			if(list.get(i).SSID.equals("NVRAM WARNING: Err = 0x10")||list.get(i).SSID.equals("")){
				System.out.println("ssid==null");
			}else{
				tmp.add(list.get(i));
			}
		}
		
//		ScanResult temp;//定义一个临时变量
//	        for(int i=0;i<tmp.size()-1;i++){//冒泡趟数
//	            for(int j=0;j<tmp.size()-i-1;j++){
//	            	
//	                if(tmp.get(j+1).level<tmp.get(j).level){
//	                    temp = tmp.get(j);
//	                    tmp.remove(j);
//	                    tmp.add(j,  tmp.get(j+1));
//	                    tmp.remove(j+1);
//	                    tmp.add(j+1,  temp);
//	                }
//	            }
//	        }

		   Collections.sort(tmp, new Comparator<ScanResult>() {

			@Override
			public int compare(ScanResult lhs, ScanResult rhs) {
				if(rhs.level==lhs.level){
					return 0;
				}
				if(rhs.level>lhs.level){
					return 1;
				}
				if(rhs.level<lhs.level){
					return -1;
				}
				return 0;
			}
		   });

		
		
		return tmp;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!mWifi.isWifiEnabled()) {  
			mWifi.setWifiEnabled(true);  
		} 
		setAdapter();
	}
	private String getSSID() {
		WifiInfo info = mWifi.getConnectionInfo();
		String wifiId = info != null ? info.getSSID() : "";
		if(wifiId!=""){   
			if(wifiId.equals("0x")||info.getLinkSpeed()<0){
				setMyTitle(getString(R.string.no_conn_wifi));
			}else{
				setMyTitle(wifiId);
			}
		}else{
			setMyTitle(getString(R.string.conn_wifi));
		}
		return wifiId;
	}
	private String getMac() {
		try {
			WifiInfo info = mWifi.getConnectionInfo();
			return info.getBSSID();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

 



	public static boolean isboolIP(String ipAddress){ 
		try {
			String ip="(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"; 
			Pattern pattern = Pattern.compile(ip); 
			Matcher matcher = pattern.matcher(ipAddress); 
			return matcher.matches();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	} 
	String mac = "";
	//wifi客户端  listView点击事件 
	OnItemClickListener lv_view_ItemClick_Listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			settingAdapter.setSelectPosition(position);
			settingAdapter.notifyDataSetChanged();
			et_kzName.setText(list.get(position).SSID+"");
			mac = list.get(position).BSSID+"";
			Toast.makeText(getApplicationContext(), mac, 0).show();
			ll_conn.setVisibility(View.VISIBLE);
			ll_gaoji.setVisibility(View.GONE);
			String capa = list.get(position).capabilities;

			if (!TextUtils.isEmpty(capa)) {
				if (capa.contains("WPA") || capa.contains("wpa")) {
					ll_pwd.setVisibility(View.VISIBLE);
					isOpen = false;
				} else if (capa.contains("WEP") || capa.contains("wep")) {
					ll_pwd.setVisibility(View.VISIBLE);
					isOpen = false;
				} else {//无加密
					ll_pwd.setVisibility(View.GONE);
					btn_kz.setText(R.string.connect);
					isOpen = true;
				}
			}else{
				isOpen = false;
			}

		}
	};

	//wifi密码显示模式点击事件
	OnClickListener etTextViewOnclickListener = new OnClickListener() {
		int clickCount = 0 ;
		@Override
		public void onClick(View v) {
			clickCount++;
			if(clickCount%2==0){
				iv_password_is_show.setBackgroundResource(R.drawable.layout_check_out_password_icon);
				et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}else{
				iv_password_is_show.setBackgroundResource(R.drawable.layout_others_icon);
//				Toast.makeText(getApplicationContext(), "密码已显示,请防止泄露密码。", 0).show();
				et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			}
		}
	};
	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}
	private void unconnetedGj(String SSID,String password,String mac,WifiCipherType Type){
		Toast.makeText(getApplicationContext(), R.string.conn_ing, Toast.LENGTH_SHORT).show();

		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		config.BSSID = mac;
		// nopass
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			// config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// config.wepTxKeyIndex = 0;
		}
		// wep
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			if (!TextUtils.isEmpty(password)) {
				if (isHexWepKey(password)) {
					config.wepKeys[0] = password;
				} else {
					config.wepKeys[0] = "\"" + password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wpa
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
			.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
			.set(WifiConfiguration.PairwiseCipher.TKIP);
			// 此处需要修改否则不能自动重联
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
			.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}

		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
	
		//获得ip数据包
		String ipAddress = et_ipadd.getText().toString();//ip地址
		int preLength = 24;//网络前缀长度
		String getWay =et_wangguan.getText().toString();//网关
		String dns1 = et_yumin.getText().toString();//域名

		//接受ip数据包，配置指定的wifi配置对象
		new StaticIpSet(this, ipAddress, preLength, getWay, dns1).confingStaticIp(config);
		//		mWifi.setWifiEnabled(false);
		//连接指定wifiConfig
		if (config != null)
		{
			try
			{
				setStaticIpConfiguration(wifiManager, config,
						InetAddress.getByName(ipAddress), 24,
						InetAddress.getByName(getWay),
						new InetAddress[] { InetAddress.getByName(dns1), InetAddress.getByName("10.0.0.4") });
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		boolean isConn = 	wifiManager.enableNetwork(wifiManager.addNetwork(config), true); 
		//	 	Toast.makeText(getApplicationContext(), "isConn："+isConn, 0).show();
		setMyTitle(getString(R.string.conn_ing));
		isWifiConnected(getApplicationContext());
		
	}

	private static boolean isHexWepKey(String wepKey) {
		final int len = wepKey.length();

		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if (len != 10 && len != 26 && len != 58) {
			return false;
		}

		return isHex(wepKey);
	}
	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
					&& c <= 'f')) {
				return false;
			}
		}

		return true;
	}
	



	@SuppressWarnings("unchecked")
	private static void setStaticIpConfiguration(WifiManager manager, WifiConfiguration config, InetAddress ipAddress, int prefixLength, InetAddress gateway, InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException
	{
		// First set up IpAssignment to STATIC.
		Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
		callMethod(config, "setIpAssignment", new String[] { "android.net.IpConfiguration$IpAssignment" }, new Object[] { ipAssignment });

		// Then set properties in StaticIpConfiguration.
		Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
		Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[] { InetAddress.class, int.class }, new Object[] { ipAddress, prefixLength });

		setField(staticIpConfig, "ipAddress", linkAddress);
		setField(staticIpConfig, "gateway", gateway);
		getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
		for (int i = 0; i < dns.length; i++)
			getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

		callMethod(config, "setStaticIpConfiguration", new String[] { "android.net.StaticIpConfiguration" }, new Object[] { staticIpConfig });
		manager.updateNetwork(config);
		manager.saveConfiguration();
	}

	private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
	{
		return newInstance(className, new Class<?>[0], new Object[0]);
	}

	private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
	{
		Class<?> clz = Class.forName(className);
		Constructor<?> constructor = clz.getConstructor(parameterClasses);
		return constructor.newInstance(parameterValues);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
	{
		Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
		return Enum.valueOf(enumClz, enumValue);
	}

	private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
	{
		Field field = object.getClass().getDeclaredField(fieldName);
		field.set(object, value);
	}

	private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
	{
		Field field = object.getClass().getDeclaredField(fieldName);
		return type.cast(field.get(object));
	}

	private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
	{
		Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++)
			parameterClasses[i] = Class.forName(parameterTypes[i]);

		Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
		method.invoke(object, parameterValues);
	}
	
	//是否连接WIFI
	public void isWifiConnected(final Context context)
	{
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					for(int i = 0 ; i< 5;i++){
						Thread.sleep(1000);
						if(mWifi.getConnectionInfo().getLinkSpeed()>=0&&mWifi.getConnectionInfo().getSSID().equals("\""+et_kzName.getText().toString()+"\"")){
							mHandler.sendEmptyMessage(1);
							return;
						}
					}
					mHandler.sendEmptyMessage(2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}
	//其中networdId必须更具SSID来获取，方法如下：
	public  void removeNetwork(){
		List<WifiConfiguration> wifiConfigurationList = mWifi.getConfiguredNetworks();
		if(wifiConfigurationList != null && wifiConfigurationList.size() != 0){
			for (int i = 0; i < wifiConfigurationList.size(); i++) {
				WifiConfiguration wifiConfiguration = wifiConfigurationList.get(i);
				// wifiSSID就是SSID
				if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals(mWifi.getConnectionInfo().getSSID())) {
					mWifi.removeNetwork(wifiConfiguration.networkId); 
					mWifi.saveConfiguration();
				}
			}
		}
	}

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what==1) {
				setMyTitle(getString(R.string.str_conn));
				settingAdapter.setConnPosition(mWifi.getConnectionInfo().getSSID(),getMac());
				settingAdapter.notifyDataSetChanged();
				if(mWifi.getConnectionInfo().getSSID().equals("\""+et_kzName.getText().toString()+"\"")){
					ll_conn.setVisibility(View.GONE);
				}
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
			}else if(msg.what==2){
				setAdapter();
				Toast.makeText(getApplicationContext(), R.string.str_notconn, Toast.LENGTH_SHORT).show();
			}
			
			if(msg.what==100){
				setAdapter();
			}
			
			
		};

	};

	//	public void onBackPressed() {
	//		if(isGj==true){
	////			btn_senior.setVisibility(View.VISIBLE);
	//			isGj = false;
	//			return;
	//		}else{
	//			finish();
	//		}
	//	};
	boolean isOpen = false;
	@Override
	public void onClick(View v) {
		if(v==btn_kz){
			if(!et_kzName.getText().toString().isEmpty()){
				if(true){
					if(!isboolIP(et_ipadd.getText().toString())){
						Toast.makeText(getApplicationContext(), R.string.ip_error, Toast.LENGTH_SHORT).show();
						return;
					}
					if(!isboolIP(et_wangguan.getText().toString())){
						Toast.makeText(getApplicationContext(), R.string.net_mac_error, Toast.LENGTH_SHORT).show();
						return;
					}
					if(!isboolIP(et_yumin.getText().toString())){
						Toast.makeText(getApplicationContext(),R.string.yuming_error, Toast.LENGTH_SHORT).show();
						return;
					}
					if(getSSID().equals("\""+et_kzName.getText().toString()+"\"")){
						removeNetwork();
					} 

					if(isOpen == true){
						unconnetedGj(et_kzName.getText().toString(), "",mac,WifiCipherType.WIFICIPHER_NOPASS);//无加密
					}else{
						unconnetedGj(et_kzName.getText().toString(), et_pwd.getText().toString(),mac,WifiCipherType.WIFICIPHER_WPA);//加密
					}
				}/*else{
					unconneted(et_kzName.getText().toString(), et_pwd.getText().toString());
				}*/
				et_pwd.clearFocus(); 
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   
				imm.hideSoftInputFromWindow(et_pwd.getWindowToken(),0);   
			}else{
				Toast.makeText(getApplicationContext(),R.string.conn_input_pwd, Toast.LENGTH_SHORT).show();
			}
		}
		if(btn_senior==v){
//			btn_senior.setVisibility(View.GONE);
			ll_gaoji.setVisibility(View.VISIBLE);
			isGj = true;
		}
		if(btn_canle==v){
			ll_conn.setVisibility(View.GONE);
		}
	}
	public void setMyTitle(String msg){
		View include = findViewById(R.id.il_title);
		TextView tv_title = (TextView) include.findViewById(R.id.tv_title);
		tv_title.setText(msg);
	}

}
