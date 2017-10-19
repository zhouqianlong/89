package com.ramy.minervue.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.bean.BlueBean;
import com.ramy.minervue.bean.Dervic;
import com.ramy.minervue.control.ControlManager;
import com.ramy.minervue.control.QueryTask;
import com.ramy.minervue.control.ControlManager.PacketListener;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.media.Monitor;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.sync.SyncManager;
import com.ramy.minervue.util.AudioRecordDemo;
import com.ramy.minervue.util.ConfigUtil;
import com.ramy.minervue.util.FileUtils;
import com.ramy.minervue.util.HourlyAlarm;
import com.ramy.minervue.util.License;
import com.ramy.minervue.util.PreferenceUtil;
import com.way.ftp.FtpServerService;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex;
import com.wifitalk.Activity.RemoteVideoThread;
import com.wifitalk.Activity.SendSoundsThread_oldSpeex;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import we_smart.com.beaconconfig.App;
import we_smart.com.beaconconfig.StatusbarTranslucent;
import we_smart.com.dao.iBeaconDao;
import we_smart.com.data.BufProcessor;
import we_smart.com.data.CoreData;
import we_smart.com.data.PollingInfo;
import we_smart.com.data.UniId;
import we_smart.com.utils.Hex;
import we_smart.com.utils.TaskPool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Created by peter on 11/7/13.
 */
@SuppressLint("NewApi")
public class MainService extends Service implements
Thread.UncaughtExceptionHandler {
	public Object object = new Object();
	public boolean system_Action  = true;//true代表系统在运行中,false代表系统需要休眠
	public int zu = 0;//对讲切换组 
	public DBHelper dbHelper; 
	public AudioManager audioManager;
	public static boolean SHUAXIN = true;//对讲刷新界面的开关
	public static boolean JINYIN = true;//静音  	
	public static boolean JUJIE = true;//拒接
	private static final int NOTIFICATION_ID = 1;
	private static final String TAG = "RAMY-MainService";
	public static int thread_Number = 0;
	public static boolean battery = true;
	private static MainService instance = null;
	public static final int stopLeve = 8;
	private KeyguardManager km;
	private KeyguardLock kl;
	private PowerManager pm;	
	private PowerManager.WakeLock wl;
	private ControlManager controlManager;
	private PreferenceUtil preferenceUtil;
	private ConfigUtil configUtil;
	private SyncManager syncManager;
	private iBeaconDao         mBeaconDao;
	private PowerManager.WakeLock wakeLock = null;
	private boolean upgradeAttempted = false;
	private boolean timeAdjustAttempted = false;
	private boolean isWifiAvailable = false;
	private boolean isServerOnline = false;
	private boolean registeredFunctionStarted = false;
	public int pindao =1;//对讲机频道
	public String model = "组";
	public ReceiveSoundsThread_oldSpeex receiveSoundsThread;//播放声音线程  ReceiveSoundsThread
	public SendSoundsThread_oldSpeex sendSoundsThread;//发送声音线程
	private ArrayList<ServerOnlineStatusListener> onlineStatusListeners = new ArrayList<ServerOnlineStatusListener>();
	private MonitorActivity activeMonitor;
	private License license;
	private HourlyAlarm hourlyAlarm;
	private LocalBinder binder = new LocalBinder();
	public static  boolean MONITOR_STATU = false;
	public List<UserBean> video_list_userBean = new ArrayList<UserBean>();//视频对讲勾选的用户
	public List<UserBean> audio_list_userBean = new ArrayList<UserBean>();//语音对讲勾选的用户

	public List<UserBean> getVideoUserBeans(){
		return video_list_userBean;
	}
	public void setVideoUserBeans( List<UserBean> videolist){
		if(videolist==null){
			Log.i(TAG, "视频设置空");
			video_list_userBean = new ArrayList<UserBean>();
		}else{
			video_list_userBean = videolist;
			Log.i(TAG, "视频设置："+videolist.size());
		}
	}



	public List<UserBean> getAudioUserBeans(){
		return audio_list_userBean;
	}
	public void setAudioUserBeans( List<UserBean> audiolist){
		if(audiolist==null){
			Log.i(TAG, "语音设置空");
			audio_list_userBean = new ArrayList<UserBean>();
		}else{
			audio_list_userBean = audiolist;
			Log.i(TAG, "语音设置："+audiolist.size());
		}
	}



	public static MainService getInstance() {
		return instance;
	}

	public IBinder onBind(Intent intent) {
		return binder;
	}

	private Notification getNotification(boolean online) {
		String titleText = getString(R.string.app_name);
		String contentText = getString(online ? R.string.app_is_online
				: R.string.app_is_offline);
		Intent intent = new Intent(this, LoginActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT); 
		Notification.Builder builder = new Notification.Builder(this)
		.setContentTitle(titleText).setContentText(contentText)
		.setSmallIcon(R.drawable.icon).setContentIntent(pendingIntent);
		return builder.build();
	}
	public int netWorkType = 0;
	public  int getConnectedType() {  
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {  
			netWorkType = mNetworkInfo.getType();
			return netWorkType;  
		}  
		netWorkType = -1;
		return netWorkType;  
	}
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	int count ;
	private RemoteVideoThread remoteVideoThread;//接受视频数据现场

	private List<UserBean> listUser = new ArrayList<UserBean>();

	public List<UserBean> getDB_ZU_ALL_List() {
		return listUser;
	}
	public static void method1(String file, String conent) {  
		BufferedWriter out = null;  
		try {  
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));  
			//			out.write(conent+"电量："+MainService.getInstance().getmLeve()+"%\n");  
			out.write(conent+"\n");  
			//			Log.i("syso", conent+"电量："+MainService.getInstance().getmLeve()+"%\n");
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				out.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}  
	}
	public void startWakeAndUnlock(){
		count ++;
		new Thread(new Runnable() {
			@Override
			public void run() {
				//				int time = 0;
				while(count==1){
					try {
						//						time ++;
						Thread.sleep(1000*600);
						//						try {
						//							method1("/sdcard/tk3.txt", time+":"+(isServerOnline==true?"在线":"离线")+"："+(netWorkType==1?"本地wifi连接：":"本地wifi未连接：")+sdf.format(new Date()));
						//						} catch (Exception e) { 
						//						}
						Log.i(TAG, "wifi："+ getConnectedType());
						if(getConnectedType()==-1){
							wakeAndUnlock(true);
							wakeAndUnlock(false);
							Log.i("DBug", "亮一次小屏幕"+1000*600);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	public void wakeAndUnlock(boolean b) {
		try {
			if (b) {
				// 获取电源管理器对象
				pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				// 获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
				wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
				// 点亮屏幕
				wl.acquire();
				// 得到键盘锁管理器对象
				km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
				kl = km.newKeyguardLock("unLock");
				// 解锁
				kl.disableKeyguard();
			} else {
				// 锁屏
				kl.reenableKeyguard();
				// 释放wakeLock，关灯
				wl.release();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getCurrentVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public ControlManager getControlManager() {
		return controlManager;
	}

	public PreferenceUtil getPreferenceUtil() {
		return preferenceUtil;
	}

	public ConfigUtil getConfigUtil() {
		return configUtil;
	}

	public SyncManager getSyncManager() {
		return syncManager;
	}
	//心跳包
	public void videoStop(){

		MonitorActivity activity = getActiveMonitorActivity();
		if (activity != null) {
			Monitor.printStatu("1分钟没有响应导致的结束监控");
			activity.stopMonitor();
		} 
	}
	public void startRegisteredFunctions() {
		controlManager.addPacketListener(new ControlManager.PacketListener() {
			@Override
			public void onPacketArrival(JSONObject packet) {
				if (!timeAdjustAttempted) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						Date date = sdf.parse(packet.getString("time"));
						long offset = date.getTime() - System.currentTimeMillis();
						preferenceUtil.setTimeOffset(offset);
						Log.i(TAG, "App time adjusted.");
					} catch (ParseException e) {
						Log.e(TAG, "Error parsing new time.");
					} catch (JSONException e) {
						Log.e(TAG, "Error getting new time.");
					}
					timeAdjustAttempted = true;
				}
				try {
					int version = packet.getInt("versionCode");
					if (!upgradeAttempted) {
						upgradeApplication(version);
					}
				} catch (JSONException e) {
					// Ignored.
				}
			}
			@Override
			public String getPacketType() {
				return "keep-alive";
			}
		});

		//关闭监控
		controlManager.addPacketListener(new ControlManager.PacketListener() {

			@Override
			public void onPacketArrival(JSONObject packet) {
				if(MainService.MONITOR_STATU==true){
					Log.i(TAG, packet.toString());
					MonitorActivity activity = getActiveMonitorActivity();
					if (activity != null) {
						Monitor.printStatu("收到服务器发送的关闭监控命令导致的结束监控");
						activity.stopMonitor();
					} 
				}

			}
			@Override
			public String getPacketType() {
				// TODO Auto-generated method stub
				return "video-stop";
			}
		});


		//打开监视器
		controlManager.addPacketListener(new ControlManager.PacketListener() {

			@Override
			public void onPacketArrival(JSONObject packet) {
				try {
					if(StatusManager.isRemotePhoto()){
						Log.i("StatusManager", "用户在远程拍照");
						sendRequest(getPacketType(),"unavailable");
						return;
					}else{
						sendRequest(getPacketType(),"ok");
					}
					//TODO 查看用户是否在sip电话中  2015年10月8日12:16:19
					HashMap<String, String>  statu = StatusManager.getTKStatu();
					if(statu!=null){
						if(statu.get("object").equals("tk_call")){
							if(statu.get("statu").equals("true")){
								sendRequest(getPacketType(),"video");
								return;
							}
						}
					}
					if ("video-start".equals(packet.getString("action"))) {
						wakeAndUnlock(true);
						Log.i(TAG, "Received requested-video.");
						try {
							Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//给定类型的Uri的默认铃声。
							Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
							r.play();
						} catch (Exception e1) {
							Log.e(TAG, "Cannot play notification ringtone.");
						}
						//打开监视器
						if (activeMonitor == null) {
							Log.i(TAG, "Starting new monitor.");
							Intent intent = new Intent(MainService.this, MonitorActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(getPackageName() + ".StartNow", true);
							startActivity(intent);
						} else {
							Log.i(TAG, "Active monitor exists, starting passively.");
							activeMonitor.startPassively();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public String getPacketType() {
				return "video-start";
			}
		});

		//远程拍照
		controlManager.addPacketListener(new ControlManager.PacketListener() {

			@Override
			public void onPacketArrival(JSONObject packet) {//{"action":"photo-start","result":"inside1"}
				if(StatusManager.isGasPhoto()){
					Log.i("StatusManager", "用户在瓦斯检测");
					sendRequest(getPacketType(),"sensor");
					return;
				}else if(StatusManager.isMonitor()){
					sendRequest(getPacketType(),"monitor");
					Log.i("StatusManager", "monitor");
					return;
				}else if(StatusManager.isVideo()){
					Log.i("StatusManager", "用户在录像");
					sendRequest(getPacketType(),"video");
					return;
				}else{
					sendRequest(getPacketType(),"ok");
				}
				try {
					String mtype;
					Uri uri = RingtoneManager.getDefaultUri(
							RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
					r.play();
					wakeAndUnlock(true);
					String photoNumber =   packet.getString("result").substring(packet.getString("result").length()-1, packet.getString("result").length());
					Intent intent = new Intent(MainService.this, VideoActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(getPackageName() + ".StartNow", true);
					intent.putExtra("Phtot_count_int", Integer.parseInt(photoNumber));

					if("inside".equals( packet.getString("result").substring(0, packet.getString("result").length()-1))){
						//内置
						mtype = null;
						mtype = "内置";
					}else{
						//外置
						intent.putExtra("type", "外置拍摄");
						mtype = "外置拍摄";
					}
					if(videoActivity!=null){
						videoActivity.startPassively(Integer.parseInt(photoNumber), mtype, true);
						return ;
					}
					startActivity(intent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//				System.out.println(packet);
			}

			@Override
			public String getPacketType() {
				return "photo-start";
			}
		});


		controlManager.addPacketListener(new ControlManager.PacketListener() {
			@Override
			public void onPacketArrival(JSONObject packet) {
				MonitorActivity activity = getActiveMonitorActivity();
				if (activity != null) {
					activity.onSwitchCamera(null);
				}
			}
			@Override
			public String getPacketType() {
				return "switch-camera";
			}
		});
		controlManager.addPacketListener(new ControlManager.PacketListener() {
			@Override
			public void onPacketArrival(JSONObject packet) {
				syncManager.startSync();
			}
			@Override
			public String getPacketType() {
				return "sync-file";
			}
		});
		controlManager.addPacketListener(new ControlManager.PacketListener() {
			@Override
			public void onPacketArrival(JSONObject packet) {
				syncManager.startSync();
			}
			@Override
			public String getPacketType() {
				return "intercom";
			}
		});
		isWifiAvailable = isWifiAvailable();
		controlManager.updateWifiAvailability(isWifiAvailable);
		syncManager.updateWifiAvailability(isWifiAvailable);
		if (isWifiAvailable) {  // TODO 取消WIFi一直保持的在线状态
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "backLock");
			wakeLock.acquire();
		}
		hourlyAlarm.start();
		registeredFunctionStarted = true;
		Log.v(TAG, "Registered function started. Wifi availability: "  + isWifiAvailable + ".");
	}
	public void callStartActivity(UserBean bean){
		Intent mIntent = new Intent(MainService.this, CameraActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.putExtra("UserBean",bean);
		mIntent.putExtra("isTask", true);
		startActivity(mIntent);
	}

	/**
	 * Periodically call this function to ensure detection of license
	 * expiration.
	 */
	public void regularRegistrationCheck() {
		if (registeredFunctionStarted && !license.verifyCurrentLicense()) {
			Log.w(TAG, "License expired, exiting.");
			stopSelf();
		} else {
			Log.i(TAG, "Hourly check: license ok.");
		}
	}

	public String getMachineDigest() {
		return license.getMachineDigest();
	}

	public String getUUID() {
		return configUtil.getUDID();
	}
	public String getServerAddr(){
		return configUtil.getServerAddr();
	}

	/**x
	 * Verify given serial number and start registered functions upon success.
	 * 
	 * @param base64
	 *            The serial number.
	 * @return null if failed to verify, {startDate, endDate} otherwise.
	 */
	public Date[] verifyRegistration(String base64) {
		Date[] region = license.verifyLicense(base64);
		if (region != null) {
			preferenceUtil.setSerialNumber(base64);
			if (!registeredFunctionStarted) {
				startRegisteredFunctions();
			}
		}
		return region;
	}

	public boolean verifyCurrentRegistration() {
		return license.verifyCurrentLicense();
	}

	@Override
	public void onCreate() {
		startWifi();
		instance = this;
		configUtil = ConfigUtil.getInstance();
		system_Action = true;
		if (configUtil == null) {
			//无配置文件
			Log.e(TAG, "No valid config file found, exiting.");
			ConfigUtil.createFile();
			configUtil = ConfigUtil.getInstance();
			if (configUtil == null) {
				System.exit(0);
				return;
			}
		}
		// Sets the default uncaught exception handler. This handler is invoked
		// in case any Thread dies due to an unhandled exception.
		//		Toast.makeText(getApplicationContext(), "oncreate", 1).show();
		Thread.setDefaultUncaughtExceptionHandler(this);
		PreferenceManager.setDefaultValues(this, R.xml.preference, true);
		// 将服务设置为前台服务，这样呢在手机休眠的时候才不会被杀死，并在状态栏显示一个通知，是否online
		startForeground(NOTIFICATION_ID, getNotification(false));
		preferenceUtil = new PreferenceUtil(getApplicationContext());
		controlManager = new ControlManager();
		syncManager = new SyncManager(this);
		dbHelper = new DBHelper(this);
		PrepareCallCheckUser.pindaoID = dbHelper.getPingdaoId();
		// 许可证明
		license = new License();
		// 每隔一个小时发送一个闹钟广播
		hourlyAlarm = new HourlyAlarm(getApplicationContext(),
				AlarmReceiver.class);
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);    
		BatteryReceiver batteryReceiver = new BatteryReceiver();  
		registerReceiver(batteryReceiver, intentFilter);    
		// 验证注册码是否有效和是否是本机的注册码
		if (license.verifyCurrentLicense()) {
			startRegisteredFunctions();
		}
		//		startWakeAndUnlock();
		new TvThread().start();
		audioManager =  (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		receiveSoundsThread= new ReceiveSoundsThread_oldSpeex(getIPadd(),MainService.this);
		remoteVideoThread = new RemoteVideoThread(this);
		sendSoundsThread = new SendSoundsThread_oldSpeex();
		sendSoundsThread.setRunning(false);
		sendSoundsThread.start();
		receiveSoundsThread.setRunning(true);
		receiveSoundsThread.start();
		remoteVideoThread.start();
		remoteVideoThread.setRunning(true);
		//		MainService.method1("/sdcard/music.txt", "start music"+new Date());

		Log.e("ReceiveThread", "ReceiveSoundsThread"+System.currentTimeMillis());
		Log.e("ReceiveThread", "ReceiveVideosThread"+System.currentTimeMillis());
		initUserDB();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				 Intent		intent = new Intent(MainService.this, FtpServerService.class);
					if (!FtpServerService.isRunning()) {
						if (Environment.MEDIA_MOUNTED.equals(Environment
								.getExternalStorageState())) {
							startService(intent);
						}
					}else{
						stopService(intent);
					}
				
			}
		}).start();
	
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {



		}else{

			BluetoothManager blManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
			blAdapter = blManager.getAdapter();
			if (blManager == null) {//使用默认
				blAdapter = BluetoothAdapter.getDefaultAdapter();
			}else{
				mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
					@Override
					public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {


						final byte broadcastThree[] = { 0x1e, (byte) 0xff, 0x57, 0x53, 0x10, 0x02 };
						final byte threePrefix[] = Arrays.copyOf(scanRecord, broadcastThree.length);
						final byte beaconinfo[]  = { 0x02, 0x01, 0x06, 0x1A, (byte) 0xFF, 0x4C, 0x00, 0x02, 0x15 };



						final byte prefixinfo[]  = Arrays.copyOf(scanRecord, beaconinfo.length);


						//先根据macadress从数据库获取BeaconInfoThree信息
						synchronized (device) {
							if (Arrays.equals(broadcastThree, threePrefix) || Arrays.equals(beaconinfo, prefixinfo)) {
								mIsNewBconThree = true;
								for (int i = 0 ; i < mBeaconList.size() ; i++) {
									synchronized (device) {
										PollingInfo mPollingInfo = mBeaconList.get(i);
										if (device.getAddress().equals(mBeaconList.get(i).mac)) {
											mIsNewBconThree = false;
											//更新数据
											if (Arrays.equals(beaconinfo, prefixinfo)) {
												BufProcessor bp = new BufProcessor(scanRecord, prefixinfo.length);
												mPollingInfo.uuid = new UniId(bp.Get(0L), bp.Get(0L));
												mPollingInfo.major = bp.Get((short) 0) & 0xFFFF;
												mPollingInfo.minor = bp.Get((short) 0) & 0xFFFF;
												mPollingInfo.MRssi = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.risi = rssi;
												mPollingInfo.mac = device.getAddress();
												//												mPollingInfo.name = device.getName();
												mBeaconList.set(i, mPollingInfo);
												Log.i("MYDEBUG",device.getName()+"beaconinfo:"+device.getAddress()+"==="+Hex.encodeHexStr(scanRecord));
											} else if (Arrays.equals(broadcastThree, threePrefix)) {
												BufProcessor bp = new BufProcessor(scanRecord, threePrefix.length + 2);
												mPollingInfo.status = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.supports = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.battery = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.beaconSendGap = bp.Get((short) 0) & 0xFFFF;
												mPollingInfo.beaconSendPower = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.statusSendGap = bp.Get((byte) 0) & 0xFF;
												mPollingInfo.risi = rssi;
												mPollingInfo.mac = device.getAddress();
												//												mPollingInfo.name = device.getName();
												mBeaconList.set(i, mPollingInfo);
												Log.i("MYDEBUG",	device.getName()+mPollingInfo.battery+"% broadcastThree:"+device.getAddress()+"==="+Hex.encodeHexStr(scanRecord));
											}
										}
									}
								}
								if (mIsNewBconThree) {
									PollingInfo beaconInfoThree = new PollingInfo();
									//得到数据
									if (Arrays.equals(beaconinfo, prefixinfo)) {
										BufProcessor bp = new BufProcessor(scanRecord, prefixinfo.length);
										beaconInfoThree.updateBeaconInfo(new UniId(bp.Get(0L), bp.Get(0L)), bp.Get((short) 0) & 0xFFFF, bp.Get((short) 0) & 0xFFFF, bp.Get((byte) 0) & 0xFF);
									} else if (Arrays.equals(broadcastThree, threePrefix)) {
										BufProcessor bp = new BufProcessor(scanRecord, threePrefix.length + 2);
										beaconInfoThree.updateBroadcastInfo(bp.Get((byte) 0), bp.Get((byte) 0), bp.Get((byte) 0), bp.Get((short) 0), bp.Get((byte) 0), bp.Get((byte) 0));
									}
									String name = mBeaconDao.queryName(device.getAddress());
									if (name != null) {
										if (name.equals("未获取")) {
											beaconInfoThree.name = name;
										}
									}
									beaconInfoThree.name = device.getName();
									beaconInfoThree.mac = device.getAddress();
									beaconInfoThree.risi = rssi;
									mBeaconList.add(beaconInfoThree);
									Log.i("MYDEBUG",device.getName()+" init:"+device.getAddress()+"==="+Hex.encodeHexStr(scanRecord));
								}
							}
							Collections.sort(mBeaconList, new Comparator<PollingInfo>() {
								@Override
								public int compare(PollingInfo lhs, PollingInfo rhs) {
									return rhs.risi - lhs.risi;
								}

							}
									);
							//TODO 本地存储轨迹方法
							if(mBeaconList.size()>0){
								saveGuiJI(mBeaconList.get(0));
							}

						}
					}
				};
			}

			TaskPool.DefRandTaskPool().PushTask(startScan);
			mBeaconDao = new iBeaconDao(this);

		}






	}


	private final Runnable startScan   = new Runnable() {
		public void run() {
			blAdapter.startLeScan(mLeScanCallback);
		}
	};
	private final Runnable stopPolling = new Runnable() {
		public void run() {
			blAdapter.stopLeScan(mLeScanCallback);
		}
	};

	//蓝牙扫描数据
	private BluetoothAdapter.LeScanCallback mLeScanCallback = null;


	//	private final Runnable startScan   = new Runnable() {
	//		public void run() {
	//
	//			if(android.os.Build.VERSION.SDK_INT>17){
	//				BluetoothManager blManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
	//				blAdapter = blManager.getAdapter();
	//				mLeScanCallback= new BluetoothAdapter.LeScanCallback() {
	//					@Override
	//					public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
	//
	//
	//						final byte broadcastThree[] = { 0x1e, (byte) 0xff, 0x57, 0x53, 0x10, 0x02 };
	//						final byte threePrefix[] = Arrays.copyOf(scanRecord, broadcastThree.length);
	//						final byte beaconinfo[]  = { 0x02, 0x01, 0x06, 0x1A, (byte) 0xFF, 0x4C, 0x00, 0x02, 0x15 };
	//
	//
	//
	//						final byte prefixinfo[]  = Arrays.copyOf(scanRecord, beaconinfo.length);
	//
	//						//先根据macadress从数据库获取BeaconInfoThree信息
	//						synchronized (device) {
	//							if (Arrays.equals(broadcastThree, threePrefix) || Arrays.equals(beaconinfo, prefixinfo)) {
	//								mIsNewBconThree = true;
	//								for (int i = 0 ; i < mBeaconList.size() ; i++) {
	//									synchronized (device) {
	//										PollingInfo mPollingInfo = mBeaconList.get(i);
	//										if (device.getAddress().equals(mBeaconList.get(i).mac)) {
	//											mIsNewBconThree = false;
	//											//更新数据
	//											if (Arrays.equals(beaconinfo, prefixinfo)) {
	//												BufProcessor bp = new BufProcessor(scanRecord, prefixinfo.length);
	//												mPollingInfo.uuid = new UniId(bp.Get(0L), bp.Get(0L));
	//												mPollingInfo.major = bp.Get((short) 0) & 0xFFFF;
	//												mPollingInfo.minor = bp.Get((short) 0) & 0xFFFF;
	//												mPollingInfo.MRssi = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.risi = rssi;
	//												mPollingInfo.lastPollingTime = System.currentTimeMillis()+"";
	//												mPollingInfo.mac = device.getAddress();
	//												if(device.getName()!=null){
	//													mPollingInfo.name = device.getName();
	//												}
	//												mBeaconList.set(i, mPollingInfo);
	//												Log.v("MYDEBUG",mPollingInfo.name+"==="+device.getAddress());//+"==="+Hex.encodeHexStr(scanRecord)
	//											} else if (Arrays.equals(broadcastThree, threePrefix)) {
	//												BufProcessor bp = new BufProcessor(scanRecord, threePrefix.length + 2);
	//												mPollingInfo.status = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.supports = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.battery = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.beaconSendGap = bp.Get((short) 0) & 0xFFFF;
	//												mPollingInfo.beaconSendPower = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.statusSendGap = bp.Get((byte) 0) & 0xFF;
	//												mPollingInfo.lastPollingTime = System.currentTimeMillis()+"";
	//												mPollingInfo.risi = rssi;
	//												mPollingInfo.mac = device.getAddress();
	//												if(device.getName()!=null){
	//													mPollingInfo.name = device.getName();
	//												}
	//												mBeaconList.set(i, mPollingInfo);
	//												Log.d("MYDEBUG",	mPollingInfo.name+"==="+mPollingInfo.battery+"%==="+device.getAddress());
	//											}
	//										}
	//									}
	//								}
	//								if (mIsNewBconThree) {
	//									PollingInfo beaconInfoThree = new PollingInfo();
	//									//得到数据
	//									if (Arrays.equals(beaconinfo, prefixinfo)) {
	//										BufProcessor bp = new BufProcessor(scanRecord, prefixinfo.length);
	//										beaconInfoThree.updateBeaconInfo(new UniId(bp.Get(0L), bp.Get(0L)), bp.Get((short) 0) & 0xFFFF, bp.Get((short) 0) & 0xFFFF, bp.Get((byte) 0) & 0xFF);
	//									} else if (Arrays.equals(broadcastThree, threePrefix)) {
	//										BufProcessor bp = new BufProcessor(scanRecord, threePrefix.length + 2);
	//										beaconInfoThree.updateBroadcastInfo(bp.Get((byte) 0), bp.Get((byte) 0), bp.Get((byte) 0), bp.Get((short) 0), bp.Get((byte) 0), bp.Get((byte) 0));
	//									}
	//									if(device.getName()!=null){
	//										beaconInfoThree.name = device.getName();
	//									}
	//									beaconInfoThree.mac = device.getAddress();
	//									beaconInfoThree.risi = rssi;
	//									beaconInfoThree.lastPollingTime = System.currentTimeMillis()+"";
	//									mBeaconList.add(beaconInfoThree);
	//									Log.i("MYDEBUG",device.getName()+"init:"+device.getAddress());
	//								}
	//							}
	//							Collections.sort(mBeaconList, new Comparator<PollingInfo>() {
	//								@Override
	//								public int compare(PollingInfo lhs, PollingInfo rhs) {
	//									return rhs.risi - lhs.risi;
	//								}
	//
	//							}
	//									);
	//
	//						}
	//					}
	//				};
	//				blAdapter.getScanMode();
	//				new Thread(new Runnable() {
	//
	//					@SuppressLint("NewApi")
	//					@Override
	//					public void run() {
	//						while (instance!=null) {
	//							try {
	//								if(system_Action==false){
	//									Thread.sleep(3000);
	//									continue;
	//								}
	//								blAdapter.startLeScan(mLeScanCallback);
	//								Thread.sleep(3000);
	//								blAdapter.stopLeScan(mLeScanCallback);
	//								for(int i = 0 ; i < mBeaconList.size();i++){
	//									long now = System.currentTimeMillis();
	//									long old = Long.valueOf(mBeaconList.get(i).lastPollingTime);
	//									if(now-old>15000){//10秒丢失
	//										mBeaconList.remove(i);
	//									}
	//								}
	//
	//								if(mBeaconList.size()>0)
	//									saveGuiJI(mBeaconList.get(0));
	//							} catch (Exception e) {
	//								e.printStackTrace();
	//							}
	//
	//
	//							//TODO 本地存储轨迹方法
	//							if(mBeaconList.size()>0){
	//								saveGuiJI(mBeaconList.get(0));
	//							}
	//						}
	//					}
	//				}).start();
	//			} 
	//
	//
	//
	//		}
	//	};
	private String lasMAc;

	//本地存储轨迹方法TODO
	private void saveGuiJI(PollingInfo info){
		if(info.risi==127)
			return;
		if(lasMAc==null){
			lasMAc =info.mac;  
			dbHelper.addBlueAddress(configUtil.getUDID(), info.mac, System.currentTimeMillis()+"", info.risi+"");
		}
		if(lasMAc.equals(info.mac)){
			return;
		}else{
			lasMAc = info.mac;
			dbHelper.addBlueAddress(configUtil.getUDID(), info.mac, System.currentTimeMillis()+"", info.risi+"");
		}


	}

	private boolean mIsNewBconThree = true;
	//蓝牙适配器
	BluetoothAdapter blAdapter = null;
	//信息对象
	public List<PollingInfo> mBeaconList = new ArrayList<PollingInfo>();
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					Log.i("asdf","Background App:");
					return true;
				}else{
					Log.i("asdf","Foreground App:");
					return false;
				}
			}
		}
		return false;
	}

	public void startWifi(){
		WifiManager mWm = (WifiManager)  this.getSystemService(Context.WIFI_SERVICE);  
		if (!mWm.isWifiEnabled()) {  
			mWm.setWifiEnabled(true);  
		}  
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public void startReceiver() {
		final IntentFilter filter = new IntentFilter();  
		// 屏幕灭屏广播  
		filter.addAction(Intent.ACTION_SCREEN_OFF);  
		// 屏幕亮屏广播  
		filter.addAction(Intent.ACTION_SCREEN_ON);  
		// 屏幕解锁广播  
		filter.addAction(Intent.ACTION_USER_PRESENT);  
		// 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播  
		// example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，  
		// 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框   
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);  
		final String TAG = "asdf"; 
		BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {  
			@Override  
			public void onReceive(final Context context, final Intent intent) {  
				if(MainActivity.getInstance!=null){
					Log.d(TAG, "onReceive");  
					try {
						String action = intent.getAction();  
						if (Intent.ACTION_SCREEN_ON.equals(action)) {  
							Log.d(TAG, "screen on");  
							system_Action = true;
							new Thread(new Runnable() {

								@Override
								public void run() {
									synchronized (object) {
										object.notify();
										sendSoundsThread.myNotify();
									}
								}
							}).start();
							sendSoundsThread.recorder.startRecording(); 
						} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {  
							Log.d(TAG, "screen off");  
							system_Action = false;
							sendSoundsThread.recorder.stop();
						} else if (Intent.ACTION_USER_PRESENT.equals(action)) {  
							Log.d(TAG, "screen unlock");  

						} else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {  
							Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");  

						}
					} catch (Exception e) {
						e.printStackTrace();
					}  
				}
			}  
		};  
		Log.d(TAG, "registerReceiver");  
		registerReceiver(mBatInfoReceiver, filter);
	}
	public ReceiveSoundsThread_oldSpeex getReceiveSoundsThread(){
		return receiveSoundsThread;
	}
	public void initUserDB() {
		listUser = new ArrayList<UserBean>();
		List<UserBean> ls =dbHelper.findUserTable(1);
		for(int i = 0 ; i<ls.size();i++ ){
			listUser.add(ls.get(i));
		}
		ls = dbHelper.findUserTable(2);
		for(int i = 0 ; i<ls.size();i++ ){
			listUser.add(ls.get(i));
		}
		ls = dbHelper.findUserTable(3);
		for(int i = 0 ; i<ls.size();i++ ){
			listUser.add(ls.get(i));
		}
		Log.i(TAG, "listUser:"+listUser.size());
	}

	public AudioManager getAudioManager(){
		if(audioManager!=null){
			return audioManager;
		}else{
			audioManager  =  (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			return audioManager;
		}
	}
	private int mBatteryLevel;  
	//	private int mBatteryScale;  
	private int mLeve = 100;
	public int getmLeve() {
		return mLeve;
	}

	public SendSoundsThread_oldSpeex getsendSoundsThread(){
		return sendSoundsThread;
	}
	public void sendSoundsThread(boolean isrunning){
		sendSoundsThread.setRunning(isrunning);
	}
	public void sendSoundsThread(boolean isrunning,int pindao){
		sendSoundsThread.setpdRunning(isrunning,pindao);
	}

	public void setmLeve(int mLeve) {
		this.mLeve = mLeve;
	}

	class BatteryReceiver extends BroadcastReceiver{    

		@Override    
		public void onReceive(Context context, Intent intent) {    
			//判断它是否是为电量变化的Broadcast Action    
			if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){    
				//获取当前电量    
				mBatteryLevel = intent.getIntExtra("level", 0);    
				//电量的总刻度    
				//				mBatteryScale = intent.getIntExtra("scale", 100);    
				if(mBatteryLevel<=100){
					battery = true;
					setmLeve(mBatteryLevel);
					if(mBatteryLevel<=5){
						battery = false;
					}
				}
			}    
		}    
	}   
	/**
	 * 主要的作用是当Service进程被意外kill掉时，Service服务下一步要做哪些行为，主要有3种值。
	 * 
	 * START_STICKY：Service被异外终止时不调用onDestroy()回调，并且终止后自动重启Service服务，
	 * 只执行Service对象的onCreate()生命周期方法。
	 * 
	 * START_NOT_STICKY：Service被异外终止时不调用onDestroy()回调，并且不自动重启服务。
	 * 
	 * START_REDELIVER_INTENT：Service被异外终止时不调用onDestroy()回调，并且终止后自动重启Service服务，
	 * 还要执行Service对象的onCreate()和onStartCommand()生命周期方法，并且从Intent中能取到值。
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 服务死掉后自行启动，并执行oncreate 方法。不执行ondestory方法。
		return START_STICKY;
	}

	public MonitorActivity getActiveMonitorActivity() {
		return activeMonitor;
	}

	public void setActiveMonitorActivity(MonitorActivity monitor) {
		activeMonitor = monitor;
	}

	public boolean isWifiAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI
				&& info.isConnected();
	}

	public String getLocalIp() {
		if (!isWifiAvailable) {
			return null;
		}
		Enumeration<NetworkInterface> en = null;
		try {
			en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface intf = en.nextElement();
				Enumeration<InetAddress> addrs = intf.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					String ip = addr.getHostAddress();
					if (!addr.isLoopbackAddress() && !addr.isMulticastAddress()
							&& InetAddressUtils.isIPv4Address(ip)) {
						return ip;
					}
				}
			}
		} catch (SocketException e) {
			return null;
		}
		return null;
	}

	public void updateWifiAvailability(boolean available) {
		try {
			if(available==false){
				PrepareCallCheckUser.instances.setSpeakWifi(true,"");
			}else{
				MainService.getInstance().getReceiveSoundsThread().initIp();
				PrepareCallCheckUser.instances.setSpeakWifi(false,"");
			}
		} catch (Exception e) {
		}
		Log.i(TAG, "Wifi availability changed to: " + available + ".");
		if (isWifiAvailable != available && registeredFunctionStarted) {
			//			Log.i(TAG, "Wifi availability changed to: " + available + ".");
			controlManager.updateWifiAvailability(available);
			syncManager.updateWifiAvailability(available);
		}
		isWifiAvailable = available;
		if (!isWifiAvailable) {
			updateServerOnline(false);
		}
		if (!isWifiAvailable && wakeLock != null) {  //取消wifi一直保持的状态
			wakeLock.release();//TODO  让cpu保持运行 ， 取消释放锁
			wakeLock = null;
		} else if (isWifiAvailable && wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"backLock");
			wakeLock.acquire();
		}
	}

	@Override
	public void onDestroy() {
		instance = null;
		//updateWifiAvailability(false);
		stopForeground(true);
		controlManager.release();
		preferenceUtil.release();
		configUtil.release();
		hourlyAlarm.cancel();
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
		Log.i(TAG, "onDestroy");
		//解除注册md

		//		unregisterReceiver(mBluetReceiver);
		System.exit(0);
	}


	public void onBlueResult(){

		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		// 开始搜索蓝牙设备,搜索到的蓝牙设备通过广播返回
		mBluetoothAdapter.startDiscovery();
	}
	public void upgradeApplication(int version) {
		if (version > getCurrentVersion()) {
			Log.i(TAG, "New version detected: " + version + ".");
			String name = "minervue-" + version + ".apk";
			File file = syncManager.getLocalFileUtil().getFile(
					LocalFileUtil.DIR_PUB, name);
			if (file.exists() && !LocalFileUtil.isFileInUse(file)) {
				Log.i(TAG, "Apk available, upgrading.");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				upgradeAttempted = true;
				startActivity(intent);
			}
		}
	}

	// 更新是否连接上服务器
	public void updateServerOnline(boolean online) {
		isServerOnline = online;
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, getNotification(online));
		for (ServerOnlineStatusListener l : onlineStatusListeners) {
			l.onServerOnlineStatusChanged(online);
		}
	}


	public boolean getServerOnlineStatus() {
		return isServerOnline;
	}

	public void addServerOnlineStatusListener(
			ServerOnlineStatusListener listener) {
		if (listener != null) {
			onlineStatusListeners.add(listener);
		}
	}

	public void removeServerOnlineStatusListener(
			ServerOnlineStatusListener listener) {
		onlineStatusListeners.remove(listener);
	}

	// 当系统含有没有被捕获的异常时会被调用。
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		BugReportActivity.start(this, ex);
		// 非零表示非正常停止
		Process.killProcess(Process.myPid());
		System.exit(-1);
	}

	// 这个接口是和服务器的连接是否改变（离线===》在线；在线====》离线）

	public interface ServerOnlineStatusListener {
		public void onServerOnlineStatusChanged(boolean isOnline);

	}

	public class LocalBinder extends Binder {

		public MainService getService() {
			return MainService.this;
		}

	}

	public static class AlarmReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			MainService service = MainService.getInstance();
			if (service != null) {
				service.regularRegistrationCheck();
			}
		}

	}
	private VideoActivity videoActivity;
	public void setActivityVideoActivity(VideoActivity video) {
		videoActivity = video;
	}
	public String getIPadd(){
		String IP_Address;
		try {  
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
			while(en.hasMoreElements()) 
			{  
				NetworkInterface intf = en.nextElement();  
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
				while(enumIpAddr.hasMoreElements()) 
				{  
					InetAddress inetAddress = enumIpAddr.nextElement();  
					if (!inetAddress.isLoopbackAddress()&& InetAddressUtils.isIPv4Address(IP_Address=inetAddress.getHostAddress())) 
					{  
						return IP_Address;  
					}  
				}  
			}  
		} catch (SocketException ex) { 
		}  
		return "未联网"; 
	}



	class TvThread extends Thread{  
		@Override  
		public void run(){  
			do{  
				try{  
					synchronized (object) {
						if(system_Action==false){//休眠中
							//熄灭
							Log.i("asd", "休眠中!");
							object.wait();
						}else{//正常情况  每秒获取一次
							//							Log.i("asd", " 每秒获取一次");
							Thread.sleep(1000);  
							Message msg = new Message();  
							msg.what = 138444;//what，int类型，未定义的消息，以便接收消息者可以鉴定消息是关于什么的。每个句柄都有自己的消息命名空间，不必担心冲突   
							if(MainActivity.getInstance!=null){
								MainActivity.getInstance.mHandler.sendMessage(msg);  
							}
						} 
					}
				}  
				catch (InterruptedException e){  
					e.printStackTrace();  
				}  
			}while (true);  

		}  

	}  

	public String getMacAddress(){
		return getMac();
	}

	/** 
	 * 获取手机的MAC地址 
	 * @return 
	 */  
	public String getMac(){  
		String str="";  
		String macSerial="";  
		try {  
			java.lang.Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");  
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());  
			LineNumberReader input = new LineNumberReader(ir);  

			for (; null != str;) {  
				str = input.readLine();  
				if (str != null) {  
					macSerial = str.trim();// 去空格  
					break;  
				}  
			}  
		} catch (Exception ex) {  
			ex.printStackTrace();  
		}  
		if (macSerial == null || "".equals(macSerial)) {  
			try {  
				return loadFileAsString("/sys/class/net/eth0/address")  
						.toUpperCase().substring(0, 17);  
			} catch (Exception e) {  
				e.printStackTrace();  

			}  

		}  
		return macSerial;  
	}  
	public static String loadFileAsString(String fileName) throws Exception {  
		FileReader reader = new FileReader(fileName);    
		String text = loadReaderAsString(reader);  
		reader.close();  
		return text;  
	}  
	public static String loadReaderAsString(Reader reader) throws Exception {  
		StringBuilder builder = new StringBuilder();  
		char[] buffer = new char[4096];  
		int readLength = reader.read(buffer);  
		while (readLength >= 0) {  
			builder.append(buffer, 0, readLength);  
			readLength = reader.read(buffer);  
		}  
		return builder.toString();  
	}  




	private String macAddr = null;  
	public String getMacAddressLastSix(){
		if(macAddr==null){
			try {
				String mac = getMac();
				if(mac ==null){
					return "123456";
				}
				mac =mac.replaceAll(":", "");
				macAddr =  mac.substring(mac.length()-6);
			} catch (Exception e) {
				try {
					WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = wifi.getConnectionInfo();
					macAddr = info.getMacAddress();
					macAddr =macAddr.replaceAll(":", "");
					macAddr =  macAddr.substring(macAddr.length()-6);
				} catch (Exception e1) {
					return "ym89";
				}
			}

		}
		return macAddr;
	}
	QueryTask queryTask = null;
	public void sendRequest(String action,String result) {
		JSONObject json = new JSONObject();
		try {
			json.put("uuid", MainService.getInstance().getUUID());
			json.put("action", action);
			json.put("result", result);
			ControlManager control = MainService.getInstance().getControlManager();
			queryTask = control.createQueryTask(json, listener, 1);
			queryTask.start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	private BluetoothAdapter mBluetoothAdapter;
	public String getLocation() {
		if(mBeaconList.size()>0){

			return getDesByMac(mBeaconList.get(0).mac);
		}
		return "无位置";
	}

	private List<Dervic> decodeJson(String json){
		List<Dervic> list = new ArrayList<Dervic>();
		try {
			JSONArray objectArray = new JSONArray(json);
			for(int i = 0 ; i < objectArray.length();i++){
				JSONObject obj = objectArray.getJSONObject(i);
				list.add(new Dervic(obj.getString("Mac"), obj.getString("Des"), obj.getString("Type"),obj.getString("Statu"),obj.getString("url")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}








	public List<Dervic> dervicList = null;
	public String getDesByMac(String mac){
		if(dervicList==null){
			dervicList = decodeJson(FileUtils.readFile(MainService.getInstance().getSyncManager().getLocalFileUtil().getFile(LocalFileUtil.DIR_PUB, "device.log").getPath()));
		}
		if(dervicList==null)
			return mac;
		for(int i = 0 ; i < dervicList.size();i++){
			if(dervicList.get(i).getMac().equals(mac)){//mac = FF:FF:38:BD:A0:3D    der = FF:FF:38:8D:A0:3D
				return dervicList.get(i).getDes()+"["+dervicList.get(i).getStatu()+"]";
			}
		}
		return "未定义名称";
	}





	//	private BroadcastReceiver mBluetReceiver = new BroadcastReceiver() {
	//		@SuppressLint("NewApi")
	//		@Override
	//		public void onReceive(Context context, Intent intent) {
	//			// TODO Auto-generated method stub
	//
	//			String action = intent.getAction();
	//			// 获得已经搜索到的蓝牙设备
	//			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
	//				BluetoothDevice device = intent
	//						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	//				// 搜索到的不是已经绑定的蓝牙设备
	//				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	//					byte rssi = (byte) intent.getExtras().getShort( 
	//			                BluetoothDevice.EXTRA_RSSI);
	//					BlueBean bean = new BlueBean();
	//					bean.mac = device.getAddress();
	//					bean.name = device.getName();
	//					bean.db = rssi+"db";
	//					bean.time =  System.currentTimeMillis();
	//					addBlueBean(bean);
	//				} 
	//				// 搜索完成
	//			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) { 
	//				if(blueToothBean==null)
	//					return;
	//				for(int i = 0 ; i<blueToothBean.size();i++){
	//					 if(System.currentTimeMillis() - blueToothBean.get(i).time>=3000){
	//						blueToothBean.remove(i);
	//						i--;
	//					}
	//				}
	//			}
	//		}
	//	};

	//	public List<BlueBean> blueToothBean;
	//	public void addBlueBean(BlueBean bean){
	//		if(blueToothBean ==null){
	//			blueToothBean = new ArrayList<BlueBean>();
	//		}
	//		boolean isExits = false;
	//		for(int i = 0 ; i<blueToothBean.size();i++){
	//			if(blueToothBean.get(i).mac.equals(bean.mac)){
	//				blueToothBean.get(i).db = bean.db;
	//				blueToothBean.get(i).time =bean.time;
	//				isExits = true;
	//			}
	//			 if(System.currentTimeMillis() - blueToothBean.get(i).time>=3000){
	//				blueToothBean.remove(i);
	//				i--;
	//			}
	//		}
	//		if(isExits==false){
	//			blueToothBean.add(bean);
	//		}
	//	}

}
