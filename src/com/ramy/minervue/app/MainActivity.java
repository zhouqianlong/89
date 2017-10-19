package com.ramy.minervue.app;




import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ClipData.Item;
import android.content.pm.PackageManager;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.bhj.staticwifisetting.StaticIpSet;
import com.mediatek.engineermode.io.EmGpio;
import com.ramy.minervue.R;
import com.ramy.minervue.app.StaticIpActivity.WifiCipherType;
import com.ramy.minervue.bean.XY;
import com.ramy.minervue.control.ControlManager;
import com.ramy.minervue.control.QueryTask;
import com.ramy.minervue.control.ControlManager.PacketListener;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.util.PreferenceUtil;
import com.tk.ch4gas.GasMainActivity;
import com.way.app.WayMainActivity;
import com.way.ftp.Defaults;
import com.way.ftp.FtpServerService;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex;
import com.wifitalk.Utils.GpsView;
import com.wifitalk.Utils.ProgressDialogUtils;

/**
 * Created by peter on 11/2/13.
 */
public class MainActivity extends Activity implements OnClickListener,MainService.ServerOnlineStatusListener, PreferenceUtil.PreferenceListener {
	public WifiInfoViewListener wifiInfoViewListener;//wifi状态监听器
	public static  boolean frameIng = false;//编码器的状态
	private static final int WIFI_FALSE = 404;//wifi关闭  Handler
	private static final int WIFI_TRUE = 200;//wifi开启 Handler
	public static  int db_values = 50;
	public SharedPreferences sharedPreferences;
	public static  MainActivity getInstance = null;
	private TextView infoText,tvwifi,tv_phone_gps;
	private TextView unreadText,tv_work_download;
	private WifiManager mWm;
	public AudioManager audioManager;
	public LocationManager locationManager;  
	public Location location;  
	public double longitude =0;//经度
	public double latitude =0 ;//纬度
	public TextView onGasDetection ,tv_video_phone,tvname,tv_title;//环境检测模块
	public TextView tv_sos;
	public TextView tv_led;
	public String llasbPosition = "";//GPS 经度纬度风速高度方向位置
	public ImageView iv_setting_audio;
	public ImageView iv_led_lamp;
	public DBHelper dbHelper  = null;
	//	public int DB_RE_WIFI = -65;//分别V额
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		getInstance = this;
		WindowManager windowManager = getWindowManager();    
		Display display = windowManager.getDefaultDisplay();    
		Point size = new Point();
		display.getSize(size);
		audioManager =  (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		sharedPreferences = this.getSharedPreferences("wifiSett", 0);
		getInstance.time= sharedPreferences.getInt("n_time", 10);
		//		getInstance.DB_RE_WIFI = sharedPreferences.getInt("n_xh", -70);//分贝值
		//		getInstance.DB_RE_WIFI = -30;//_____++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		db_values  = Integer.valueOf(sharedPreferences.getString("db", "50"));
		onGasDetection = (TextView) findViewById(R.id.onGasDetection);
		tv_video_phone = (TextView) findViewById(R.id.tv_video_phone);
		tv_led = (TextView) findViewById(R.id.tv_led);
		tv_sos = (TextView) findViewById(R.id.tv_sos);
		tv_sos.setOnClickListener(this);
		tv_led.setOnClickListener(this);
		dbHelper = new DBHelper(getApplicationContext());
		tv_title = (TextView) findViewById(R.id.tv_title);
		tvname = (TextView) findViewById(R.id.tvname);
		//		tv_video_phone.setBackgroundResource(R.drawable.ic_menu_call);
		//		ic_menu_cal
		//				tv_video_phone.setText("气体检测");
		tv_video_phone.setOnClickListener(this);
		iv_setting_audio = (ImageView) findViewById(R.id.iv_setting_audio);
		iv_led_lamp = (ImageView) findViewById(R.id.iv_led_lamp);
		iv_led_lamp.setOnClickListener(this);
		iv_setting_audio.setOnClickListener(this);
		if(android.os.Build.DISPLAY.indexOf("ALPS.KK")==0){

		}else if(android.os.Build.DISPLAY.indexOf("ALPS")==0){
			//			LinearLayout tv_setting =  (LinearLayout) findViewById(R.id.tv_setting);
			//			tv_setting.setVisibility(View.GONE);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
		}
		//		if(android.os.Build.DISPLAY.indexOf("ALPS")==0||android.os.Build.DISPLAY.indexOf("S700")==0){
		//			onGasDetection.setText(R.string.address_peizhi);
		//		}
		PackageManager manager = getPackageManager();
		String version = "";
		try {
			//得到当前所安装的应用版本号
			version = manager.getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
			// Ignore.
		}
		setTitle(getString(R.string.app_name) + " v" + version);
		infoText = (TextView) findViewById(R.id.tv_online_status);
		tvwifi = (TextView) findViewById(R.id.tvwifi);
		tvwifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dbHelper.selectBlueAddress();
				//				iv_led_lamp.performClick();
				//				openOptionsMenu();
				//								LayoutInflater inflater = getLayoutInflater();
				//								View layout = inflater.inflate(R.layout.wifi_fazhi,null);
				//				final AlertDialog  dialog =  new AlertDialog.Builder(MainActivity.this).setTitle("请输入信息").setIcon(
				//						android.R.drawable.ic_dialog_info).setView(layout
				//								).show();
				//
				//				final EditText time = (EditText) layout.findViewById(R.id.et_time);
				//				final EditText xh =  (EditText) layout.findViewById(R.id.et_xh);
				//				final EditText et_db =  (EditText) layout.findViewById(R.id.et_db);
				//				time.setText(getInstance.time+"");
				//				xh.setText(getInstance.XH+"");
				//				et_db.setText(getInstance.db_values+"");
				//				Button ok =  (Button) layout.findViewById(R.id.btn_ok);
				//				ok.setOnClickListener(new OnClickListener() {
				//					@Override
				//					public void onClick(View v) {
				//						String db_value = et_db.getText().toString();
				//						String u_time = time.getText().toString();
				//						String u_xh = xh.getText().toString();
				//						int n_time = 0;
				//						int n_xh =0;
				//						try {
				//							n_time = Integer.parseInt(time.getText().toString());
				//							n_xh = Integer.parseInt(xh.getText().toString());
				//							Integer.valueOf(db_value);
				//						} catch (NumberFormatException e) {
				//							Toast.makeText(MainActivity.this, "参数必须是整数", Toast.LENGTH_SHORT).show();
				//							return;
				//						}
				//						if(u_time.isEmpty()){
				//							Toast.makeText(MainActivity.this, "持续时长 不能为空", Toast.LENGTH_SHORT).show();
				//						}else if(u_xh.isEmpty()){
				//							Toast.makeText(MainActivity.this, "漫游阀值不能为空", Toast.LENGTH_SHORT).show();
				//						}else if(db_value.isEmpty()){
				//							Toast.makeText(MainActivity.this, "分贝值不能为空", Toast.LENGTH_SHORT).show();
				//						}else if(n_time<5||n_time>60){
				//							Toast.makeText(MainActivity.this, "持续时长范围为5到60秒之间", Toast.LENGTH_SHORT).show();
				//						}else  if(n_xh>-60||n_xh<-80){
				//							Toast.makeText(MainActivity.this, "漫游阀值范围为-60到-80", Toast.LENGTH_SHORT).show();
				//						}else if(Integer.valueOf(db_value)<40&&Integer.valueOf(db_value)>80){
				//							Toast.makeText(getApplicationContext(), "分贝范围必须是40至80之间", 0).show();
				//						}else{
				//							sharedPreferences.edit().putInt("n_time", n_time).commit();
				//							sharedPreferences.edit().putInt("n_xh", n_xh).commit();
				//							sharedPreferences.edit().putString("db", db_value).commit();
				//							MainActivity.db_values = Integer.valueOf(db_value);
				//							getInstance.XH = n_xh;
				//							getInstance.time =n_time;
				//							dialog.dismiss();
				//							Log.i("TEST", sharedPreferences.getInt("n_time", 60)+":"+sharedPreferences.getInt("n_xh", 60));
				//						}
				//					}
				//				});

				//				execShellCmd("input keyevent 82");//home  


				//				sharedPreferences.edit().putInt("xh", 0).commit();
				//				sharedPreferences.edit().putInt("xh", 0).commit();
			}
		});
		//		TextView tv_test = (TextView) findViewById(R.id.tv_test);
		unreadText = (TextView) findViewById(R.id.tv_unread_download);
		tv_work_download = (TextView) findViewById(R.id.tv_work_download);
		MainService service = MainService.getInstance();
		//判断是否在线。
		boolean isOnline;
		try {
			isOnline = service.getServerOnlineStatus();
			//是否有没有读取的文件（文件数量）
			int unread = service.getPreferenceUtil().getUnreadDownload(this);
			//		int unread = 1;
			if (unread > 0) {
				unreadText.setText(Integer.toString(unread));
				unreadText.setVisibility(View.VISIBLE);
			}else{
				unreadText.setVisibility(View.GONE);
			}


			unread = service.getPreferenceUtil().getWorkDownload(this);
			if (unread > 0) {
				tv_work_download.setText(Integer.toString(unread));
				tv_work_download.setVisibility(View.VISIBLE);
			} else {
				tv_work_download.setText("");
				tv_work_download.setVisibility(View.INVISIBLE);
			}


			//		tv_test.setOnClickListener(new OnClickListener() {
			//			
			//			@Override
			//			public void onClick(View arg0) {
			//				startActivity(new Intent(getApplicationContext(), TestLOGActivity.class));
			//				
			//			}
			//		});

			//							startActivity(new Intent(getApplicationContext(), TestUI.class));


			service.addServerOnlineStatusListener(this);
			locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);  
			location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);  
			updateView(location);  
			MainService.getInstance().startReceiver();
			SharedPreferences address_sharPreferences = getSharedPreferences("AddressManagerActivity", 0);
			DBERROR = Integer.valueOf(address_sharPreferences.getString("et_wifidb", "-80"));//获取wifi阀值成功
			Log.i("WIFI_DB_SEND", "获取wifi阀值成功:"+DBERROR);
		} catch (Exception e) {
			isOnline = false;
		}
		//isOnline = true;
		infoText.setText(isOnline ? R.string.app_is_online : R.string.app_is_offline);
		if(isOnline){
			infoText.setTextColor(Color.GREEN);
		}else{
			infoText.setTextColor(Color.RED);
		}


		SharedPreferences settings = getSharedPreferences("AddressManagerActivity", 0);
		if(settings.getBoolean("init", true)){
			startActivity(new Intent(getApplicationContext(), AddUserActivity.class));
		}
		//		startActivity(new Intent(getApplicationContext(), WayMainActivity.class));


		//


	}

	LocationListener locationListener = new LocationListener() {  

		@Override  
		public void onStatusChanged(String provider, int status, Bundle extras) {  
			// TODO Auto-generated method stub  

		}  

		@Override  
		public void onProviderEnabled(String provider) {  
			// TODO Auto-generated method stub  
			//  
			updateView(locationManager.getLastKnownLocation(provider));  
		}  

		@Override  
		public void onProviderDisabled(String provider) {  
			// TODO Auto-generated method stub  
			updateView(null);  
		}  

		@Override  
		public void onLocationChanged(Location location) {  
			// TODO Auto-generated method stub  
			//location为变化完的新位置，更新显示  
			updateView(location);  
		}  
	};

	//更新显示内容的方法  
	public void updateView(Location location)  
	{  
		StringBuffer buffer=new StringBuffer();  
		if(location==null)  
		{  
			//			textview.setText("未获得服务");  
			//			Toast.makeText(getApplicationContext(), "未开启GPS服务", 1).show();
			return;  
		}  
		longitude  = location.getLongitude();
		latitude  = location.getLatitude();
		if(longitude!=0&&latitude!=0){
			sharedPreferences.edit().putFloat("longitude", (float)longitude).commit();
			sharedPreferences.edit().putFloat("latitude", (float)latitude).commit();
			try {
				GpsView.getInstances.gpsChange(longitude,latitude);
				GpsActivity.instances.textview.setText(llasbPosition);
				Log.i("sharedPreferences", "设置进度纬度："+longitude+","+latitude);
			} catch (Exception e) {
			}
		}
		buffer.append("经度："+location.getLongitude()+"\n");  
		buffer.append("纬度："+location.getLatitude()+"\n");  
		buffer.append("高度："+location.getAltitude()+"\n");  
		buffer.append("速度："+location.getSpeed()+"\n");  
		buffer.append("方向："+location.getBearing()+"\n");  
		llasbPosition = buffer.toString();
		Toast.makeText(getApplicationContext(), llasbPosition, 1).show();
		//		textview.setText(buffer.toString());  

		//		double jingdu = MainActivity.getGetInstance().getLongitude();
		//		double weidu = MainActivity.getGetInstance().getLatitude();
		//		sendUDP("AnswerlongitudeAndlatitude:"+MainService.getInstance().getIPadd()+":"+jingdu+":"+weidu, strarray[1]);
		//	
		//		if(gpsCallBack!=null){
		//			XY xy = new XY(location.getLongitude(), location.getLatitude(), MainService.getInstance().getIPadd());
		//			gpsCallBack.GpsChange(xy);
		//		}

	}  

	private GPSCallBack gpsCallBack = null;
	public void setGPSCallBackListener(GPSCallBack gpsCallBack){
		this.gpsCallBack = gpsCallBack;
	}

	public interface GPSCallBack {  
		public void GpsChange(XY xy);
	}     
	@Override
	protected void onResume() {
		if(android.os.Build.DISPLAY.indexOf("YM89")==0){
			try {
				if(EmGpio.setGpioDataHigh(217)&&EmGpio.setGpioDataHigh(216)){
					//			Toast.makeText(this, "setGpioDataHigh成功", 0).show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i("asd", "main onResume");
		tvname.setText(MainService.getInstance().getUUID());
		audioManager.setMicrophoneMute(false);//开启mic
		ReceiveSoundsThread_oldSpeex.videoStatu = false;
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//全部 
		showSize();
		//		Intent	intent = new Intent(this, FtpServerService.class);
		//		if (!FtpServerService.isRunning()) {
		//			if (Environment.MEDIA_MOUNTED.equals(Environment
		//					.getExternalStorageState())) {
		//				startService(intent);
		//			}
		//		}
		super.onResume();
	}
	public void setWifi(boolean isEnable) {  
		//  
		if (mWm == null) {  
			mWm = (WifiManager)  this.getSystemService(Context.WIFI_SERVICE);  
			return;  
		}  
		if (isEnable) {// 开启wifi  
			if (!mWm.isWifiEnabled()) {  
				mWm.setWifiEnabled(true);  
			}  
		} else {  
			// 关闭 wifi  
			if (mWm.isWifiEnabled()) {  
				mWm.setWifiEnabled(false);  
			}  
		}  
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MainService service = MainService.getInstance();
		service.removeServerOnlineStatusListener(this);
		service.getPreferenceUtil().removeListener(PreferenceUtil.KEY_UNREAD_DOWNLOAD, this);
	}

	public void onGasDetection(View view) {
		//		System.out.println(1/0);
		//		if(LocalFileUtil.existSDcard()==false){
		//			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
		//			return;
		//		}
		//		if(onGasDetection.getText().equals(getString(R.string.address_peizhi))){
		//			startActivity(new Intent(this, AddressManagerActivity.class));
		//		}else{
		//			if(android.os.Build.DISPLAY.indexOf("YM89")!=0){
		//				//				Toast.makeText(MainActivity.this,"该设备不支持检测", 0).show();
		//				Toast.makeText(getApplicationContext(),  "设备不支持环境检测", 0).show();
		//			}else{
		//				startActivity(new Intent(this, GasActivity.class));
		//			}
		//		}

		startActivity(new Intent(this, AddressManagerActivity.class));
	}

	@Override
	public void onBackPressed() {
		openOptionsMenu();
		return ;
	}
	public void onSetting(View view) {  


		//		startActivity(new Intent("com.android.systemui"));
		openOptionsMenu();
		//		setting(view);

	}


	public void onWorkHandler(View view) {  
		File file = new File(MainService.getInstance().getSyncManager().getLocalFileUtil().getRoot().getPath()+"/up_work");
		Intent intent = new Intent(getApplicationContext(), WorkHandlerActivity.class);
		intent.putExtra(getPackageName() + ".ListFile", file);
		startActivity(intent);

		JSONObject json = new JSONObject();
		try {
			json.put("uuid", MainService.getInstance().getUUID());
			json.put("action", "sync-file-work-notiy");
			json.put("result", MainService.getInstance().getUUID()+"上传了一份工单");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ControlManager control = MainService.getInstance().getControlManager();
		QueryTask queryTask = control.createQueryTask(json, listener, 1);
		queryTask.start();

	}
	private ControlManager.PacketListener listener = new PacketListener() {

		@Override
		public void onPacketArrival(JSONObject packet) {

		}

		@Override
		public String getPacketType() {
			// TODO Auto-generated method stub
			return "asdkaskdk";
		}
	};


	private void setting(View view) {
		//		Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
		//		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		//				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
		//		startActivity(settings);
		final EditText inputServer = new EditText(this);
		String digits = "0123456789";
		//限制只能输入数字
		inputServer.setKeyListener(DigitsKeyListener.getInstance(digits));   
		InputMethodManager inputManager =(InputMethodManager)inputServer.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInputFromInputMethod(view.getWindowToken(), 0);
		new AlertDialog.Builder(this) 
		.setTitle(R.string.mi_ma) 
		.setMessage(R.string.q_s_r_m_m) 
		.setView(inputServer)
		.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				String inputName = inputServer.getText().toString();
				if(inputName.equals("2013110110"))
				{
					Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
					settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
					startActivity(settings);

				}
				else
				{
					Toast.makeText(MainActivity.this, R.string.mima_error, Toast.LENGTH_SHORT).show();

				}
			} 
		}) 
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				//取消按钮事件 
				//	finish(); 
			} 
		})
		.show();
		//		Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
		//		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		//				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
		//		startActivity(settings);
		//		new Thread(new Runnable() {
		//			@Override
		//			public void run() {
		//				try {
		//					Thread.sleep(500);
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}
		//				Timer timer = new Timer();
		//				timer.schedule(new TimerTask() {
		//					@Override
		//					public void run() {
		//						inputServer.dispatchTouchEvent( MotionEvent.obtain(
		//								SystemClock.uptimeMillis(),
		//								SystemClock.uptimeMillis(),
		//								MotionEvent.ACTION_DOWN,
		//								inputServer.getRight(),
		//								inputServer.getRight() + 5, 0));
		//						inputServer.dispatchTouchEvent(
		//								MotionEvent.obtain(
		//										SystemClock.uptimeMillis(),
		//										SystemClock.uptimeMillis(),
		//										MotionEvent.ACTION_UP,
		//										inputServer.getRight(),
		//										inputServer.getRight() + 5, 0));
		//					}
		//				}, 200);
		//
		//			}
		//		}).start();		//		startActivity(new Intent(this, SettingActivity.class));initColor();

		//				Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
		//				settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		//						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
		//				startActivity(settings);
	}

	public void onBrowsing(View view) {
		if(LocalFileUtil.existSDcard()==false){
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}

		LocalFileUtil util = MainService.getInstance().getSyncManager().getLocalFileUtil();
		Intent intent = new Intent(this, FileListActivity.class);
		intent.putExtra(getPackageName() + ".ListFile", util.getRoot());
		startActivity(intent);
	}
	public void onALL(View view){

		//全部
		startActivity(new Intent(this, PackageManagerActivity.class));
		//		startActivity(new Intent(this, H264AndroidTest.class));
		//		startActivity(new Intent(this, MusicMainActivity.class));
		//		startActivity(new Intent(this, CameraActivity.class));

	}
	public void onDuiJiang(View view){
		if(LocalFileUtil.existSDcard()==false){
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}
		Intent mIntent = new Intent(this, PrepareCallCheckUser.class);
		mIntent.putExtra("type", 0);//语音
		startActivity(mIntent);
	}
	public void onVideoDuiJiang(View view){
		if(LocalFileUtil.existSDcard()==false){
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}
		Intent mIntent = new Intent(this, PrepareCallCheckUser.class);
		mIntent.putExtra("type", 1);//视频
		startActivity(mIntent);
	}
	@Override
	protected void onPause() {
		if(sosStatu==false){
			if(m_Camera!=null){
				m_Camera.release();
				m_Camera = null;
				iv_led_lamp.setTag(null);
			}
		}
		//		Intent	intent = new Intent(this, FtpServerService.class);
		//		stopService(intent);
		super.onPause();
	}
	public void onGPSDuiJiang(View view){
		if(LocalFileUtil.existSDcard()==false){
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}
		startActivity(new Intent(getApplicationContext(), GpsActivity.class));
	}
	public void onRecording(View view) { 
		if(LocalFileUtil.existSDcard()==false){   
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}
		//		ProgressDialogUtils.showProgressDialog(MainActivity.this, "正在打开请稍后...");
		startActivity(new Intent(this, VideoActivity.class));
	}
	public void startAPP(String appPackageName){ 
		try{ 
			Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName); 
			startActivity(intent); 
		}catch(Exception e){ 
			Toast.makeText(this, "请安装主程序", Toast.LENGTH_LONG).show(); 
		} 


	}
	public void onMonitoring(View view) {
		if(LocalFileUtil.existSDcard()==false){
			Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
			return;
		}

		startActivity(new Intent(this, MonitorActivity.class));
	}

	@Override
	public void onServerOnlineStatusChanged(final boolean isOnline) {
		infoText.setText(isOnline ? R.string.app_is_online : R.string.app_is_offline);
		if(isOnline){
			infoText.setTextColor(Color.GREEN);
		}else{
			infoText.setTextColor(Color.RED);
		}
	}

	@Override
	public void onPreferenceChanged() {
		MainService service = MainService.getInstance();
		int unread = service.getPreferenceUtil().getUnreadDownload(null);
		if (unread > 0) {
			unreadText.setText(Integer.toString(unread));
			unreadText.setVisibility(View.VISIBLE);
		} else {
			unreadText.setText("");
			unreadText.setVisibility(View.INVISIBLE);
		}

		unread = service.getPreferenceUtil().getWorkDownload(null);
		if (unread > 0) {
			tv_work_download.setText(Integer.toString(unread));
			tv_work_download.setVisibility(View.VISIBLE);
		} else {
			tv_work_download.setText("");
			tv_work_download.setVisibility(View.INVISIBLE);
		}


	}

	/**
	 * 根据当前网络环境，决定是否发送wifi数据
	 * @return
	 */
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public boolean isSendWifiData(){
//		if(wifiDB<=DBERROR){
//			Log.e("WIFI_DB_SEND", wifiDB+"发送ERROR"+sdf.format(new Date()));
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					mHandler.sendEmptyMessage(138443);
//				}
//			}).start();
//			return false;
//		}else{
//			Log.i("WIFI_DB_SEND", wifiDB+"发送成功"+sdf.format(new Date()));
//			mHandler.sendEmptyMessage(138442);
//			return true;
//		}
				return true;
	}
	private Toast toast = null;
	/**
	 * 
	 * @param msg 内容
	 * @param i  显示时间   0 短时间   | 1 长时间
	 */
	private void showTextToast(String msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	float dataAvg = 0;
	/**
	 * 信号加权平均
	 * data  当前的信号强度
	 */
	public float JiaQuanPinJunShuanFa(float data){
		if(data==-127){
			return 0;
		}
		if(dataAvg==0){//默认赋初始值
			dataAvg=data;

		} 
		data = data*2;//取当前信号的3分之2
		dataAvg  = (data+dataAvg)/3;
		return dataAvg;
	}


	private WifiInfo wi;            // WifiInfo在包android.net.wifi.WifiInfo中  
	private int strength;           //信号强度  
	public static int DBERROR = -80;//低于多少情况下不发送数据  并且重启wifi
	public int wifiDB = -80;//默认情况wifi正常
	public Handler mHandler = new Handler(){  
		@Override  
		public void handleMessage(Message msg) {  

			super.handleMessage(msg);  
			switch(msg.what){  
			case 138442:
				try {
					if(PrepareCallCheckUser.instances!=null){
						PrepareCallCheckUser.instances.setImageStatu(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 138443:
				try {
					if(PrepareCallCheckUser.instances!=null){
						PrepareCallCheckUser.instances.setImageStatu(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
								showTextToast("当前信号弱", 1);
				break;
			case 138444:  
				mWm = (WifiManager) getSystemService(WIFI_SERVICE); //getSystemService(String)，通过名字来返回系统级服务的句柄。返回类型随要求变化。  
				//使用getSystemService(String)来取回WifiManager然后处理wifi接入，  
				wi = mWm.getConnectionInfo();//getConnectionInfo返回wifi连接的动态信息  
				if (!mWm.isWifiEnabled()) {  
					mWm.setWifiEnabled(true);  
					tvwifi.setText("无网络");  
					tvwifi.setTextColor(Color.WHITE);
				}else{
					if(wi.getBSSID() != null)//getBSSID返回基本服务集标识符，如果未连接，返回null，否则返回MAC地址形式：XX:XX:XX:XX:XX  
						strength = wi.getRssi();//返回接收到的目前的802.11网络的信号强度  
					int wifi = ((Integer) strength);
					//					checkWIFI(strength,time);
					String wifiinfo ="";
					int color = 0;
					wifiDB = wifi;//将wifi型号强度复制到wifi变量中去
					if(wifi==0){
						wifiinfo="无网络";
						color = Color.WHITE;
					}else if(wifi>-45){
						wifiinfo=getString(R.string.verywell);
						color = Color.GREEN;//绿色
					}else if(wifi<=-45&&wifi>-68){
						wifiinfo=getString(R.string.good);
						color = Color.YELLOW;//黄色
					}else if(wifi<=-68){
						wifiinfo=getString(R.string.difference);
						color = Color.RED;//红色
					}
					if(wifi==-127){
						wifiinfo="无网络";
						color = Color.WHITE;
					}
					String result = getString(R.string.signal)+":"+wifiinfo+" "+wifi+"";
//					if(DBERROR>(wifiDB)){
//						tvwifi.setText(result+"网络差("+JiaQuanPinJunShuanFa(strength)+")");
//					}else{
//						tvwifi.setText(result);  
//					}
					tvwifi.setText(result);
					manyouQieHuan((int)JiaQuanPinJunShuanFa(strength));
					tvwifi.setTextColor(color);

					if(wifiInfoViewListener!=null){
						wifiInfoViewListener.wifiInfoChange(result,color);
					}
				}
				break;  
			case WIFI_FALSE:
				setWifi(false);
				break;
			case WIFI_TRUE:
				setWifi(true);
				break;
			default:  
				break;  
			}  

		}


	};  

	private boolean wifiCheck(int db) {
		String ssid  = wi.getSSID();//连接的 wifi SSID
		String mac = wi.getBSSID();//连接的 mac 地址
		list = mWm.getScanResults();//当前列表
		//验证ssid是不是有多个
		for(int i = 0;i<list.size();i++){
			if(list.get(i).BSSID.equals(mac)){//自己的mac跳出
				continue;
			}
			String ssid_o = "\""+list.get(i).SSID+"\"";

			if(ssid_o.equals(ssid)){//新的ssid
				if(list.get(i).level-5>db){
					return true;
				}
			}
		}
		return false;
	}  




	public int time = 0;
	int sumXH = 0 ;
	int count =0;
	private int maxVolume;
	private int currentVolume;
	private long hidenBlocks = 1024*1024*200;//隐藏内存  202M
	/**
	 * 
	 * @param xh   //1、漫游阀值   1)信号强度  
	 * @param time   //2、持续时长     1)    平均信号强度(总和)：     5秒    （-30 + -31+ -32+ -30+ -31）/5
	 *
    	时长的平均强度  小于 漫游阀值  重置wifi
	 */
	private List<ScanResult> list = null; //当前扫描的wifi列表
	//	public void checkWIFI(int xh,int time){
	//		if(xh<=-127){
	//			Log.i("TEST", "信号不处理："+xh);
	//			return;
	//		}
	////				Log.i("TEST", "信号："+xh);
	//		if(count>=0){
	//			count++;
	//			sumXH+=xh;
	//			countList.add(xh);
	//			if(count>=time){
	//				int age = getAvegCouList();
	////				Log.i("TEST", "平均值："+sumXH/time);
	////				Log.i("TEST", "丢弃最差的2个之后的平均值："+age);
	//				if(DBERROR>(sumXH/time)){
	//					if(wifiCheck()){
	//						Log.i("TEST", "重启：sumXH/time:"+sumXH/time);
	//						MainActivity.getGetInstance().restartWifi(200);
	//						count=-10;
	//						sumXH=0;
	//					}else{
	//						count=0;
	//						sumXH=0;
	//						Log.i("TEST", "不重启：sumXH/time:"+sumXH/time);
	//					}
	//				}else{
	//					Log.i("TEST", "不重启：sumXH/time:"+sumXH/time);
	//					count=0;
	//					sumXH=0;
	//				}
	//			}
	//		}else{
	//			count++;
	//			sumXH=0;
	//			getAvegCouList();//获取平均数  也一起清空平均数
	//		}
	//	
	//	}
	long lasTime =System.currentTimeMillis();
	public void manyouQieHuan(int db){
		if(DBERROR>db){
			if(wifiCheck(db)){
				Toast.makeText(getApplicationContext(), "切换中", 0).show();
				if(System.currentTimeMillis()-lasTime>=10000){
					lasTime = System.currentTimeMillis();
					MainActivity.getGetInstance().restartWifi(200);
					dataAvg = 0;
				}
			}
		}

	}


	public List<Integer> countList = new ArrayList<Integer>();
	/**
	 * 获取平均数
	 * @return
	 */
	public int getAvegCouList(){
		Collections.sort(countList);
		int sum = 0;
		for(int i = 2;i<countList.size();i++){
			sum+=countList.get(i);
		}
		int avg =sum/(countList.size()-2); 
		countList = new ArrayList<Integer>();
		return avg;
	}

	int rcount = 0;


	public void restartWifi(final int seconds){
		
		Log.i("TEST", "重新连接wifi");
		mWm.disconnect();
		mWm.reconnect();
		
		
//		rcount ++;

		//		new Thread(new Runnable() {
		//			@Override
		//			public void run() {
		//				mHandler.sendEmptyMessage(WIFI_FALSE);
		//				try {
		//					Thread.sleep(seconds);
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}
		//				mHandler.sendEmptyMessage(WIFI_TRUE);
		//
		//			}
		//		}).start();
		//		

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//			mWm.disconnect();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			WifiConfiguration config = new WifiConfiguration();
//			config.allowedAuthAlgorithms.clear();
//			config.allowedGroupCiphers.clear();
//			config.allowedKeyManagement.clear();
//			config.allowedPairwiseCiphers.clear();
//			config.allowedProtocols.clear();
//			config.SSID = wi.getSSID();
//			config.BSSID =  wi.getBSSID();
//
//			config.preSharedKey = "\"" + 123456789 + "\"";
//			config.hiddenSSID = true;
//			config.allowedAuthAlgorithms
//			.set(WifiConfiguration.AuthAlgorithm.OPEN);
//			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//			config.allowedPairwiseCiphers
//			.set(WifiConfiguration.PairwiseCipher.TKIP);
//			// 此处需要修改否则不能自动重联
//			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//			config.allowedPairwiseCiphers
//			.set(WifiConfiguration.PairwiseCipher.CCMP);
//			config.status = WifiConfiguration.Status.ENABLED;
//
//
//			//获得ip数据包
//			String ipAddress ="";
//			if(rcount%2==0){
//				ipAddress="10.10.10.111";//ip地址
//			}else{
//				ipAddress="10.10.10.222";//ip地址
//			}
//			int preLength = 24;//网络前缀长度
//			String getWay = "10.10.10.1";//网关
//			String dns1 ="10.10.10.1";//域名
//
//			//接受ip数据包，配置指定的wifi配置对象
//			new StaticIpSet(getApplicationContext(), ipAddress, preLength, getWay, dns1).confingStaticIp(config);
//			//		mWifi.setWifiEnabled(false);
//			//连接指定wifiConfig
//			if (config != null)
//			{
//				try
//				{
//					setStaticIpConfiguration(mWm, config,
//							InetAddress.getByName(ipAddress), 24,
//							InetAddress.getByName(getWay),
//							new InetAddress[] { InetAddress.getByName(dns1), InetAddress.getByName("10.0.0.4") });
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//			boolean isConn = 	mWm.enableNetwork(mWm.addNetwork(config), true); 
//}
//		}).start();
		


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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
	{
		Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
		return Enum.valueOf(enumClz, enumValue);
	}

	private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
	{
		Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++)
			parameterClasses[i] = Class.forName(parameterTypes[i]);

		Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
		method.invoke(object, parameterValues);
	}

	private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
	{
		return newInstance(className, new Class<?>[0], new Object[0]);
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
	private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
	{
		Class<?> clz = Class.forName(className);
		Constructor<?> constructor = clz.getConstructor(parameterClasses);
		return constructor.newInstance(parameterValues);
	}


	public interface WifiInfoViewListener{
		public void wifiInfoChange(String msg,int color);
	}
	public void setwifiInfoViewListener(WifiInfoViewListener wifiInfoViewListener){
		this.wifiInfoViewListener = wifiInfoViewListener;
	}

	MenuItem action_sos;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		action_sos=   (MenuItem) menu.findItem(R.id.action_sos);
		MenuItem	action_server=   (MenuItem) menu.findItem(R.id.action_server);
		MenuItem	action_open_gps=   (MenuItem) menu.findItem(R.id.action_open_gps);
		MenuItem	action_close_gps=   (MenuItem) menu.findItem(R.id.action_close_gps);

		action_open_gps.setVisible(false);
		action_close_gps.setVisible(false);
		action_server.setVisible(false);
		return true;
	}

	@Override  
	public boolean onOptionsItemSelected(MenuItem item) {  
		// TODO Auto-generated method stub  
		switch(item.getItemId()){  
		case R.id.action_server:
			startActivity(new Intent(getApplicationContext(), WayMainActivity.class));
			break;
		case R.id.action_open_gps:             
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, locationListener); 
			Toast.makeText(getApplicationContext(), "开启GPS会增加耗电量", 1).show();
			break;  
		case R.id.action_close_gps:             
			locationManager.removeUpdates(locationListener); 
			Toast.makeText(getApplicationContext(), "已关闭GPS,最后一次在"+"经度:"+getLongitude()+","+"纬度:"+getLatitude(), 1).show();
			break;  
		case R.id.action_sos:             
			//			startActivity(new Intent(getApplicationContext(), AddressManagerActivity.class));
			SOS();
			break;  
		case R.id.action_setting:
			startActivity(new Intent(getApplicationContext(), BlueToothManagerActivity.class));
			//			audioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			break;

		case R.id.action_exit:
//						stopService(new Intent(this, MainService.class));
//						MainService.getInstance().stopForeground(false);
						finish();
//			System.exit(-1);
			break;
		default:  
			break;  
		}  
		return super.onOptionsItemSelected(item);  
	}  

	boolean sosStatu = false;
	public void SOS(){
		try {
			if(sosStatu==false){
				try {
					action_sos.setTitle("取消SOS");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				tv_sos.setText("取消SOS");
				tv_title.setText("可视对讲机(SOS) \n"+sdf.format(new Date())+"求救中");
				sosStatu = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(m_Camera==null){

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									ProgressDialogUtils.showProgressDialog(MainActivity.this, "正在打开请稍后...");
								}
							});			
							m_Camera = Camera.open();
						}
						ProgressDialogUtils.dismissProgressDialog();
						int count = 0;
						while (sosStatu) {
							if(iv_led_lamp.getTag()==null){
								try{

									Camera.Parameters mParameters;
									mParameters = m_Camera.getParameters();
									mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
									m_Camera.setParameters(mParameters);
									mHandler.post(new Runnable() {

										@Override
										public void run() {
											iv_led_lamp.setBackgroundResource(R.drawable.led_lamp2);
											iv_led_lamp.setTag(this);
										}
									});
								} catch(Exception ex){}
							}else{
								try{
									Camera.Parameters mParameters;
									mParameters = m_Camera.getParameters();
									mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
									m_Camera.setParameters(mParameters);
									mHandler.post(new Runnable() {

										@Override
										public void run() {
											iv_led_lamp.setBackgroundResource(R.drawable.led_lamp);
											iv_led_lamp.setTag(null);
										}
									});		
									//							m_Camera.release();
									//							m_Camera = null;
								} catch(Exception ex){}
							}			
							try {
								count++;
								if(count<=6){
									Thread.sleep(500);
								}else{
									if(count>=12){
										count=0;
									}
									Thread.sleep(1000);
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}

					}
				}).start();
			}else{
				sosStatu = false;
				tv_title.setText("可视对讲机");
				try {
					action_sos.setTitle("SOS求助");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tv_sos.setText("SOS求助");
				Camera.Parameters mParameters;
				mParameters = m_Camera.getParameters();
				mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				m_Camera.setParameters(mParameters);
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						iv_led_lamp.setBackgroundResource(R.drawable.led_lamp);
						iv_led_lamp.setTag(null);
					}
				});		
				//							m_Camera.release();
				//							m_Camera = null;

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/** 
	 * 执行shell命令 
	 *  
	 * @param cmd 
	 */  
	private void execShellCmd(String cmd) {  

		try {  
			// 申请获取root权限，这一步很重要，不然会没有作用  
			Process process = Runtime.getRuntime().exec("su");  
			// 获取输出流  
			OutputStream outputStream = process.getOutputStream();  
			DataOutputStream dataOutputStream = new DataOutputStream(  
					outputStream);  
			dataOutputStream.writeBytes(cmd);  
			dataOutputStream.flush();  
			dataOutputStream.close();  
			outputStream.close();  
		} catch (Throwable t) {    
			t.printStackTrace();  
		}  
	}  

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 经度 113.92441  X
	 * @return
	 */
	public double getLongitude() {


		Log.i("sharedPreferences", "经度:"+sharedPreferences.getFloat("longitude", 0));
		return sharedPreferences.getFloat("longitude", 0);
		//		return 116.262122;
	}

	/**
	 * 纬度 22.5440166666666668  Y
	 * @return
	 */
	public double getLatitude() {
		Log.i("sharedPreferences", "纬度:"+sharedPreferences.getFloat("latitude", 0));
		return sharedPreferences.getFloat("latitude", 0);
		//		return 39.585388;
	}

	public static MainActivity getGetInstance() {
		return getInstance;
	}
	@Override
	public void onClick(View v) {
		if(v == tv_sos){
			SOS();
		}
		if(v==tv_video_phone){

			if(LocalFileUtil.existSDcard()==false){
				Toast.makeText(getApplicationContext(), "SD卡被占用,请重启设备", 0).show();
				return;
			}
			startAPP("org.linphonehk");
//			startAPP("com.unionbroad.app");
			//			startAPP("com.oifield.app");

			//									startActivity(new Intent(this, GasMainActivity.class));

		}

		if(v==iv_setting_audio){
			Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
			startActivity(intent);
			
//			audioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
		}

		if(v==tv_led){
			iv_led_lamp.performClick();
		}
		if(v==iv_led_lamp){
			new Thread(new Runnable() {
				@Override
				public void run() {
					if(m_Camera==null){

						mHandler.post(new Runnable() {

							@Override
							public void run() {
								ProgressDialogUtils.showProgressDialog(MainActivity.this, "正在打开请稍后...");
							}
						});			
						m_Camera = Camera.open();
					}
					ProgressDialogUtils.dismissProgressDialog();
					if(iv_led_lamp.getTag()==null){
						try{

							Camera.Parameters mParameters;
							mParameters = m_Camera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							m_Camera.setParameters(mParameters);
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									iv_led_lamp.setBackgroundResource(R.drawable.led_lamp2);
									iv_led_lamp.setTag(this);
								}
							});
						} catch(Exception ex){}
					}else{
						try{
							Camera.Parameters mParameters;
							mParameters = m_Camera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							m_Camera.setParameters(mParameters);
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									iv_led_lamp.setBackgroundResource(R.drawable.led_lamp);
									iv_led_lamp.setTag(null);
								}
							});		
							//							m_Camera.release();
							//							m_Camera = null;
						} catch(Exception ex){}
					}					
				}
			}).start();
		}
	}

	boolean dialogStatu = false;
	public static float memory = 100.00f;// 当前剩余大小 100%
	private void showSize() {
		try {

			/* 判断存储卡是否插入 */
			// if (sta.equals(Environment.MEDIA_MOUNTED) ||
			// sta.equals(Environment.MEDIA_SHARED)) {
			if (LocalFileUtil.existSDcard()) {
				File path = Environment.getExternalStorageDirectory();
				/* StatFs看文件系统空间使用状况 */
				StatFs statFs = new StatFs(path.getPath());
				//				StatFs statFs = new StatFs("/storage/sdcard0");
				/* Block的size */
				long blockSize = statFs.getBlockSize();
				/* 总Block数量 */
				long totalBlocks = statFs.getBlockCount();

				/* 已使用的Block数量 */
				long availableBlocks = statFs.getAvailableBlocks();

				String[] total = fileSize(totalBlocks * blockSize - hidenBlocks);
				String[] available = fileSize(availableBlocks * blockSize
						- hidenBlocks);


				if (Float.valueOf(total[0].replaceAll(",", "")) > 0) {
					memory = Float.valueOf(available[0].replaceAll(",", ""))
							* 100
							/ Float.valueOf(total[0].replaceAll(",", ""));
				}
				String text = "   可用" + total[0] + total[1] + "\n";
				text += "   剩余" + available[0] + available[1];
				Log.i("CameraZQL", "剩余容量" + (int) memory + "%"+"\n"+text);

				if (memory <= 30) {
					if(dialogStatu==false){
						dialogStatu = true;
						new AlertDialog.Builder(MainActivity.this).setTitle("内存不足是否自动清理一些旧的文件").setCancelable(false).setPositiveButton("是",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialogStatu = false;
								File file = new File(LocalFileUtil.FILEPATH);
								getAllFiles(file);
								int num = filelist.size();
								fileList_file = new File[num];
								for (int i = 0; i < filelist.size(); i++) {
									fileList_file[i] = filelist.get(i);
								}
								Arrays.sort(fileList_file, new NameComparatorByDate());
								List<Boolean> deleteCheck = new ArrayList<Boolean>();
								for (int i = 0; i < fileList_file.length / 3; i++) {
									deleteCheck.add(fileList_file[i].delete());
								}
								boolean status = true;
								if (status) {
									if(fileList_file.length / 3>0){
										Toast.makeText(MainActivity.this,"系统自动清理了" + fileList_file.length / 3 + "个文件,请您手动删除一些较大的文件,以防文件丢失",Toast.LENGTH_LONG).show();
									}else{
										Toast.makeText(MainActivity.this,"实在是没有文件可以删了,请您手动删除一些较大的文件吧",Toast.LENGTH_LONG).show();
									}
								}
							}
						}).setNegativeButton("否", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialogStatu = false;								
							}
						}).create().show();

					}
				}



			} else if (Environment.getExternalStorageState().equals( Environment.MEDIA_REMOVED)) {
			}
		}
		catch (Exception e) { }
	}
	private ArrayList<File> filelist = new ArrayList<File>();
	private File[] fileList_file;
	private void getAllFiles(File root) {
		filelist = new ArrayList<File>();
		File files[] = root.listFiles();

		if (files != null)
			for (File f : files) {

				if (f.isDirectory()) {
					getAllFiles(f);
				} else {
					this.filelist.add(f);
				}
			}
	}

	/**
	 * 根据文件生成时间排序
	 * 
	 * @author 周乾龙
	 * 
	 */
	private class NameComparatorByDate implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			String[] paths = lhs.getName().split("-");// 第一个时间
			String[] paths2 = rhs.getName().split("-");// 第二个时间
			if (paths.length < 2 || paths2.length < 2) {
				return 0;
			}
			if (paths[paths.length - 2].length() < 7
					|| paths[paths.length - 1].length() < 5) {
				return 1;
			} else if (paths2[paths2.length - 2].length() < 7
					|| paths2[paths2.length - 1].length() < 5) {
				return 1;
			} else {
				String path1 = paths[paths.length - 2].substring(
						paths[paths.length - 2].length() - 8,
						paths[paths.length - 2].length())
						+ paths[paths.length - 1].substring(0, 6);
				String path2 = paths2[paths2.length - 2].substring(
						paths2[paths2.length - 2].length() - 8,
						paths2[paths2.length - 2].length())
						+ paths2[paths2.length - 1].substring(0, 6);
				// if(path1.length()!=14&&path2.length()!=14){
				// return -1;
				// }else if(path1.length()!=14&&path2.length()==14){
				// return -3;
				// }else if(path1.length()==14&&path2.length()!=14){
				// return -2;
				// }
				if (path1.length() != 14) {
					return 0;
				}
				if (path2.length() != 14) {
					return 0;
				}
				if (Long.valueOf(path1) > Long.valueOf(path2))
					return 1;
				return -1;
			}
		}
	}

	/**
	 * 返回为字符串数组[0]为大小, [1]为单位KB或MB
	 */
	private String[] fileSize(float size) {
		if (size == hidenBlocks * -1) {
			return new String[] { "0", "0" };
		}
		String str = "";
		if (size >= 1024) {
			str = "KB";
			size /= 1024;
			if (size >= 1024) {
				str = " GB";
				size /= 1024;
			}
		}

		DecimalFormat formatter = new DecimalFormat();
		/* 每3个数字用,分隔如：1,000 */// float = 29491.0
		formatter.setGroupingSize(3);
		formatter.setMaximumFractionDigits(0);
		String result[] = new String[2];
		result[0] = formatter
				.format(size)
				.replaceAll(",", ".")
				.substring(
						0,
						formatter.format(size).replaceAll(",", ".").length() - 1);
		result[1] = str;
		return result;
	}


	private Camera m_Camera =null;
}
