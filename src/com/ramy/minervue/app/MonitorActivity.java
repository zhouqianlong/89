package com.ramy.minervue.app;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.camera.MyCamera;
import com.ramy.minervue.camera.PreviewSizeAdapter;
import com.ramy.minervue.control.ControlManager;
import com.ramy.minervue.media.Monitor;
import com.ramy.minervue.media.VideoCodec;
import com.ramy.minervue.control.QueryTask;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.util.ATask;
import com.wifitalk.Utils.ProgressDialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by peter on 11/7/13.
 */
public class MonitorActivity extends BaseSurfaceActivity implements MainService.ServerOnlineStatusListener, Monitor.OnAbortListener {
	public static boolean down = false;
	private static final String TAG = "RAMY-MonitorActivity";
	public static final int PREFERRED_WIDTH = 640;
	public static final int PREFERRED_HEIGHT = 480;
	public AudioManager am;
	private TextView infoText;
	private Button actionButton;
	PowerManager pm= null;  
	//��ȡ��Դ����������  
	PowerManager.WakeLock wl = null;  
	KeyguardLock kl = null;
	private ProgressDialog dialog;
	private QueryTask queryTask = null;
	private Timer timer = null;
	private boolean isPassive = false;
	private ControlManager.PacketListener listener = new PacketListener();
	private boolean flag = false;;
	int count = 0;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		count = 0;
		  WindowManager windowManager = getWindowManager();    
	        Display display = windowManager.getDefaultDisplay();    
	        Point size = new Point();
	        display.getSize(size);
	        int width = size.x;
	        int height = size.y;
	        if(width==240){
	        	//����
	        	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }else{
//	          	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }
	        
		MainService.getInstance().setActiveMonitorActivity(this);
		MainService.getInstance().addServerOnlineStatusListener(this);
		setContentView(R.layout.monitor_activity);
		am =  (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.setMicrophoneMute(true);//���ر�mid������mic��
		textureView = (TextureView) findViewById(R.id.tv_video_preview);
		textureView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(MainService.MONITOR_STATU==true){
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//����
						showTextToast(getText(R.string.speak),0);
//						MonitorActivity.down = true;
						am.setMicrophoneMute(false);//���ر�mid������mic��
						getVideoCodec().getCurrentCamera().cancelAutoFocus();
						break;
					case MotionEvent.ACTION_UP:
						//�ɿ�
//						MonitorActivity.down = false;
//						am.setMicrophoneMute(true);//�ر�mic
//						showTextToast(getText(R.string.speak_on),0);
						break;
					}
				}
				return true;
			}
		});
		monitor.setOnAbortListener(this);
		isPassive = getIntent().getBooleanExtra(getPackageName() + ".StartNow",
				false);
//		ledSwitch.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				if(ledSwitch.isChecked()){
////					//					showTextToast("�����ѿ���", 0);
////				}else{
////					//					showTextToast("�����ѹر�", 0);
////				}
//			}
//		});
		
		showTextToast(MainService.getInstance().getServerAddr(), 1);
		if(MainService.getInstance().getServerOnlineStatus()==false){
			Toast.makeText(this, R.string.photo_not_connt_server, Toast.LENGTH_LONG).show();
			return;
		}
		
	}
	
	private Handler monitorHander = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==201){
				if(checktime == starttime){
					if(MainService.MONITOR_STATU){
						Monitor.printStatu("checktime:"+checktime+"=checktime:"+starttime+"����50�뵼�½������");
						stopMp4();
					}
				}
			}
		};
	};
	private Monitor monitor = new Monitor(monitorHander,this);
	private boolean isPassive() {
		return isPassive;
	}

	public void startPassively() {
		if (!monitor.isCapturing()) {
			isPassive = true;
			onServerCall(null);
		}
	}

	@Override
	public void onBackPressed() {
		if (!isPassive) { 
			super.onBackPressed();
		}else{
			showTextToast(getText(R.string.server_monitor), 0);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MainService.getInstance().setActiveMonitorActivity(null);
		try {
			MainService.getInstance().wakeAndUnlock(false);
		} catch (Exception e) {
		}
		am.setMicrophoneMute(false);//����
		stopMp4();
		if(MainService.thread_Number>180){
			System.exit(-1); 
		}



	}
	private void sendRequest() {
		JSONObject json = new JSONObject();
		Camera.Size size = getVideoCodec().getCurrentCamera()
				.getCurrentPreviewSize();
		try {
			json.put("uuid", MainService.getInstance().getUUID());
			json.put("action", "video");
//			json.put("width",  size.height);
//			json.put("height",size.width);
			json.put("width", size.width);
			json.put("height", size.height);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ControlManager control = MainService.getInstance().getControlManager();
		queryTask = control.createQueryTask(json, listener, -1);
		queryTask.start();
	}

	public void onServerCall(View view) {
		if (monitor.isCapturing()) {
			stopMonitor();
		} else {
			if (dialog != null && dialog.isShowing()) {
				return;
			}
			
			
			if(MainService.getInstance().getServerOnlineStatus()==false){
				Toast.makeText(this, R.string.photo_not_connt_server, Toast.LENGTH_LONG).show();
				return;
			}

			//			pm=(PowerManager) getSystemService(Context.POWER_SERVICE);  
			//			wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");  
			//			KeyguardManager  km= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
			//			//��ȡPowerManager.WakeLock����,����Ĳ���|��ʾͬʱ��������ֵ,������LogCat���õ�Tag  
			//			wl.acquire();         //������Ļ 
			//			//�õ�����������������  
			//			kl = km.newKeyguardLock("unLock");    
			//			//������LogCat���õ�Tag  
			//			kl.disableKeyguard();    
			String info = getString(R.string.contacting_server);
			timer = new Timer();
			monitor.prepare(0);
			dialog = ProgressDialog.show(MonitorActivity.this, "", info, true, false);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					timer.cancel(true);//��ʱ��  10/s�ȴ�ʱ��
					timer.cancelAndClear(true);//�ر�8200
					queryTask.cancel(true);//ȡ����������
					queryTask.cancelAndClear(true);
					monitor.reset();//�ر�8200    //�رղ�����
					queryTask = null;
				}
			});
			timer.start();
			sendRequest();
		}
		if(MainService.getInstance().getmLeve()<= MainService.stopLeve){
			//�������ͣ����ر�¼��״̬
			mhander.sendEmptyMessage(101);
		}

	}
	Handler mhander = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==101){
				Toast.makeText(MonitorActivity.this, R.string.phone_not_electricity, Toast.LENGTH_LONG).show();
				Monitor.printStatu("�������ͣ����ر�¼��״̬���½������");
				stopMp4();
			}
			if(msg.what==500){
				queryTask.cancel(true);
			}

		};
	};
	public void startMonitor() {//start 
		MainService.MONITOR_STATU  = true;
		if (!monitor.isCapturing()) {
			count =0;

			//			if(isPassive){
			//				actionButton.setBackgroundResource(android.R.color.holo_red_light);
			//			}
			//			wl.release();  
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			setUILevel(UI_LEVEL_BUSY);
			LocalFileUtil util = MainService.getInstance().getSyncManager()
					.getLocalFileUtil();
			//			if(LocalFileUtil.sdStatu==false){				
			//				monitor.start("",this);
			//			}else{
			monitor.start(util.generateVideoFilename(1),this);
			//			}

		}
	}

	public void stopMonitor() {//stop
		StatusManager.setMonitor(false);
		if (monitor.isCapturing()) {
			MainService.MONITOR_STATU  = false;
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			try {
				toast(getString(R.string.saved) + monitor.getMuxer().getFilePath());
				monitor.stop();
				Monitor.MONITOR_SEND_STATU = false;
				setUILevel(UI_LEVEL_NORMAL);
				MainService.getInstance().getSyncManager().startSync();//FTP�ϴ�����
				//TODO  111111111111   MainService.getInstance().setActiveMonitorActivity(null); //��ǰACTIVITY ʵ��Ϊ��   �����ϲ�����ʾ��ؽ���    ����ʾMainActivity��
				if (isPassive()) {
					finish();
				}
				MainService.getInstance().getControlManager().killKeepCount();
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected VideoCodec getVideoCodec() {
		return monitor.getVideoCodec();
	}

	@Override
	protected void setUILevel(int level) {
		if (level == UI_LEVEL_BUSY) {
			actionButton.setText(R.string.stop_monitor);
			infoText.setText(R.string.being_monitored);
		} else {
			actionButton.setText(R.string.start_monitor);
			boolean isOnline = MainService.getInstance().getServerOnlineStatus();
			infoText.setText(isOnline ? R.string.app_is_online : R.string.app_is_offline);
		}
		actionButton.setEnabled(!isPassive() && level >= UI_LEVEL_BUSY);
		super.setUILevel(level);
	}

	private Toast toast = null;
	/**
	 * 
	 * @param msg ����
	 * @param i  ��ʾʱ��   0 ��ʱ��   | 1 ��ʱ��
	 */
	private void showTextToast(String msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}
	
	private void autoFocus() {
		if(flag==false){
			flag = true;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mhander.post(new Runnable() {
							
							@Override
							public void run() {
								try {
									getVideoCodec().getCurrentCamera().cancelAutoFocus();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
				}
			}).start();
	
		}
	}
	
	
	/**
	 * 
	 * @param msg ����
	 * @param i  ��ʾʱ��   0 ��ʱ��   | 1 ��ʱ��
	 */
	private void showTextToast(CharSequence msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}
	@Override
	public void onContentChanged() {
		infoText = (TextView) findViewById(R.id.tv_monitor_info);
		actionButton = (Button) findViewById(R.id.bt_call_server);
		super.onContentChanged();
	}



	@Override
	public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
		Log.d(TAG, "Surface available, start preview.");
		ProgressDialogUtils.showProgressDialog(MonitorActivity.this, "��������ͷ��");
		final VideoCodec codec = monitor.getVideoCodec();
		new Thread(new Runnable() {

			@Override
			public void run() {
				//����Ԥ��
				if (codec.startPreview(surface, PREFERRED_WIDTH, PREFERRED_HEIGHT)) {
					mhander.post(new Runnable() {

						@Override
						public void run() {
							autoFocus();
							ProgressDialogUtils.dismissProgressDialog();
							setUILevel(UI_LEVEL_NORMAL);
							updateUIForCamera(codec.getCurrentCamera());
							if (isPassive()) {
								Log.i(TAG, "Starting passively.");
								onServerCall(null);
							}
						}
					});

				} else {
					mhander.post(new Runnable() {
						@Override
						public void run() {
							ProgressDialogUtils.dismissProgressDialog();
							toast(getString(R.string.unknown_error));
							}
					});
				}
			}
		}).start();
		
	}
	public void stopMp4(){
		if (monitor.getVideoCodec().isPreviewing()) {
			if (dialog != null && dialog.isShowing()) {
				timer.cancel(true);
				queryTask.cancel(true);
				monitor.reset();
				queryTask = null;
				dialog.dismiss();
			}
			stopMonitor();
			//			if(MainService.thread_Number>100){
			//				System.exit(-1); 
			//			}
			getVideoCodec().stopPreview();
			MainService.getInstance().removeServerOnlineStatusListener(this);
			MainService.getInstance().setActiveMonitorActivity(null);
			finish();
		}
	}
	long starttime  =0;//WIFI�Ͽ�ʱ��
	long checktime = 0;//��֤ʱ��
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static boolean ONLINE = true;
	@Override
	public void onServerOnlineStatusChanged(final boolean isOnline) {
		if (!monitor.isCapturing()) {
			infoText.setText(isOnline ? R.string.app_is_online: R.string.app_is_offline);
			if(isOnline){
				infoText.setTextColor(Color.GREEN);
			}else{
				infoText.setTextColor(Color.RED);
			}

		}
		Log.i(TAG, "452:isonline:"+isOnline);
		if(isOnline==false){
			ONLINE = false;
			Log.i(TAG, "456:isonline:"+isOnline);
			if(count==0&&MainService.MONITOR_STATU){
				//				toast("�������ߣ�"+sdf.format(new Date()));
				starttime  = System.currentTimeMillis();//WIFI�Ͽ�ʱ��
				checktime = starttime;
				Log.i(TAG, "458:isonline:"+isOnline+"count:"+count);
				count++;
				new Thread(new Runnable() {
					@Override
					public void run() {
						for(int i = 0;i<50;i++){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(checktime == starttime&&count==1){//count ��ʶ��ǰֻ��һ���߳̿��������رռ�ص�����
							count=0;
							monitorHander.sendEmptyMessage(201);
							
						}else{
							count=0;
						}
						Log.i(TAG, "HANDER:483:isonline:"+isOnline+"starttime:"+starttime);
					}
				}).start();
			}
		}
		boolean request_statu = true;//���󿪹�
		if(isOnline==true&&MainService.MONITOR_STATU){
			ONLINE = true;
			//			Log.i(TAG, "��;���ӳɹ�:489:isonline:"+isOnline+"starttime:"+starttime);
			checktime =  System.currentTimeMillis();//��;���ӳɹ�||
			if(MainService.MONITOR_STATU&&Monitor.MONITOR_WIFI==false){
				Monitor.MONITOR_WIFI = true;
				//				toast("����ָ�����!"+sdf.format(new Date()));
				monitor.prepare(1);
				monitor.monitorstart("RESULT",this);//����
				//				if(Monitor.MONITOR_SEND_STATU == true&& request_statu == true){
				//					request_statu = false;
				//						sendRequest_conn();
				//				}
				Monitor.MONITOR_COUNT  =  0 ;
			}
		}else{
			request_statu = true;
		}
	}

	@Override
	public void onAbort() {
		toast(getString(R.string.connection_lost));
		toast(getString(R.string.saved) + monitor.getMuxer().getFilePath());
		setUILevel(UI_LEVEL_NORMAL);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		MainService.getInstance().getSyncManager().startSync();
		if (isPassive()) {
			finish();
		}
	}

	private class Timer extends ATask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// Ignored.
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if (dialog == null || !dialog.isShowing()) {
				return;
			}
			dialog.cancel();
			toast(getString(R.string.server_no_respond));
			finish();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopMp4();
		Monitor.printStatu("onPause���½������");
	}

	private class PacketListener implements ControlManager.PacketListener {

		@Override
		public String getPacketType() {
			return "video";
		}

		@Override
		public void onPacketArrival(JSONObject packet) {
			if (dialog == null || !dialog.isShowing()) {
				Log.w(TAG, "Invalid remote response.");
				return;
			}
			timer.cancel(true);
			try {
				boolean available = "ok".equals(packet.getString("result"));
				if (available) {
					Log.i(TAG, "Remote accepted monitor request, starting.");
					am.setMicrophoneMute(true);//���ر�mid������mic��
					startMonitor();
					StatusManager.setMonitor(true);
				} else {
					monitor.reset();
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					toast(getString(R.string.server_rejected_you));

					timer.cancel(true);//��ʱ��  10/s�ȴ�ʱ��
					timer.cancelAndClear(true);//�ر�8200
					queryTask.cancel(true);//ȡ����������
					queryTask.cancelAndClear(true);
					monitor.reset();//�ر�8200    //�رղ�����
					queryTask = null;
				}
			} catch (JSONException e) {
				// Ignored.
			}
			dialog.dismiss();
		}

	}

	int pathLength = 0;
	@Override
	public int onUpdateTimeForSave(int count, String path) { 
		if(pathLength==0){
			pathLength = path.length();//�洢��ַ+�ļ���
		}
		monitor.monitorstop();//ֹͣ
		toast(getString(R.string.saved) + monitor.getMuxer().getFilePath());
		MainService.getInstance().getSyncManager().startSync();
		String paths = path.substring(0, pathLength-4)+"_"+count+".mp4";//  �ļ���=( Դ�ļ��� - .mp4) + _1.mp4
		monitor.monitorstart(paths,this);//����
		return 0;
	}

	@Override
	public int onBatteryError() {
		if(MainService.MONITOR_STATU){
			MainService.getInstance().setActiveMonitorActivity(null);
			stopMp4();
			Monitor.printStatu("onBatteryError���½������");
			System.exit(-1); 
		}
		return 0;
	}

	@Override
	public void onTimerFresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLog(String log) {
		// TODO Auto-generated method stub
		
	}

}