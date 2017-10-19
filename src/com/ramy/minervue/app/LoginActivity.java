package com.ramy.minervue.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.ConfigUtil;
import com.ramy.minervue.util.PreferenceUtil;
import com.ramy.minervue.ffmpeg.MP4Muxer;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peter on 10/21/13.
 */
public class LoginActivity extends Activity {

	private static final String TAG = "RAMY-LoginActivity";

	private Dialog serviceDialog = null;

	private Dialog registerDialog = null;

	private LicenseValidationTask validationTask = null;

	private TextView textViewMac;

	private EditText editTextSN;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (serviceDialog != null) {
				serviceDialog.dismiss();
			}
			if (MainService.getInstance().verifyCurrentRegistration()) {
				//第二次进入（包括第二次）直接进入主界面，已经设定好了，只需要验证注册码就行了
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			} else {
				//第一次进入，填写验证码
				//				setContentView(R.layout.login_activity);
				//				textViewMac = (TextView) findViewById(R.id.tv_machine_mac);
				//				editTextSN = (EditText) findViewById(R.id.et_sn);
				//				textViewMac.setText(MainService.getInstance().getMachineDigest());
				if(MainService.getInstance().getMachineDigest()==null){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finish();
					//					textViewMac.setText(MainService.getInstance().getMachineDigest());
					//					if (MainService.getInstance().verifyCurrentRegistration()) {
					//						//第二次进入（包括第二次）直接进入主界面，已经设定好了，只需要验证注册码就行了
					//						startActivity(new Intent(LoginActivity.this, MainActivity.class));
					//						finish();
					//					} 
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			startService(new Intent(getApplicationContext(), MainService.class));
			bindService(new Intent(getApplicationContext(), MainService.class), connection, BIND_AUTO_CREATE);
		};
	};
	//	#*#*66*#*#
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = new Builder(this);
		if(LocalFileUtil.existSDcard()==false){
			Log.e(TAG, "No valid config file found, exiting.");
			builder.setTitle("提示").setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			}).setMessage("系统发生错误:SD卡被占用,请重启设备！");
		}else if(android.os.Build.DISPLAY.indexOf("KTW")!=0
				&&android.os.Build.DISPLAY.indexOf("YM89")!=0
				&&android.os.Build.DISPLAY.indexOf("LMY47D test-keys")!=0
				&&android.os.Build.DISPLAY.indexOf("ALPS")!=0
				&&android.os.Build.DISPLAY.indexOf("S700")!=0
				&&android.os.Build.DISPLAY.indexOf("PE-TL20")!=0 
				&&android.os.Build.DISPLAY.indexOf("rk312x-userdebug")!=0 
				&&android.os.Build.DISPLAY.indexOf("Che2-UL00")!=0
				&&android.os.Build.DISPLAY.indexOf("MHA-AL00C00B231")!=0 
				&&android.os.Build.DISPLAY.indexOf("MHA-AL00C00B213")!=0
				&&android.os.Build.DISPLAY.indexOf("OPPO R7sm")!=0){
			builder.setTitle("提示").setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			}).setMessage("系统发生未知错误！");
		}else{
			//			setContentView(R.layout.login_activity);
			PreferenceManager.setDefaultValues(this, R.xml.preference, true);
			serviceDialog = ProgressDialog.show(this, "",
					getString(R.string.waiting_for_background_service), true, false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
//				MainService.method1("/sdcard/music.txt", "loginActivity150"+new Date());
				mHandler.sendEmptyMessage(1);
				
			}
		}).start();
		}
		AlertDialog alertDialog = builder.create();
		alertDialog.setCancelable(false);
		alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_SEARCH)
				{
					return true;
				}
				else
				{
					return false; //默认返回 false
				}
			}
		});
		alertDialog.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	private void validateSerial(String sn) {
		Date[] date = MainService.getInstance().verifyRegistration(sn);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			builder.setTitle(R.string.succeeded);
			String content = getString(R.string.valid_date) + "\n"
					+ getString(R.string.from) + sdf.format(date[0]) + "\n"
					+ getString(R.string.to) + sdf.format(date[1]) + "\n";
			builder.setMessage(content);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(LoginActivity.this, MainActivity.class));
					finish();
				}
			});
			builder.show();
		} else {
			builder.setTitle(R.string.failed);
			builder.setMessage(R.string.please_check_your_sn);
			builder.setPositiveButton(android.R.string.ok, null);
			builder.show();
		}
	}

	public void onRegister(View view) {
		Editable editable = editTextSN.getText();
		if (editable == null) {
			return;
		}
		validateSerial(editable.toString());
	}

	public void onAutoRegister(View view) {
		ConfigUtil config = MainService.getInstance().getConfigUtil();
		validationTask = new LicenseValidationTask(config.getServerAddr());
		validationTask.start();
		registerDialog = ProgressDialog.show(this, "",
				getString(R.string.fetching_remote_sn), true, true);
		registerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (validationTask != null) {
					validationTask.cancel(true);
					validationTask = null;
				}
			}
		});
	}
	public void OnSetting(View view){  
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
		.setTitle("密码") 
		.setMessage("请输入密码") 
		.setView(inputServer)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				String inputName = inputServer.getText().toString();
				if(inputName.equals("2013110110"))
				{
					Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
					settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	
					startActivity(settings);
					//					initColor();
				}
				else
				{
					Toast.makeText(LoginActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
					//					initColor();
				}
			} 
		}) 
		.setNegativeButton("取消", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				//取消按钮事件 
				//	finish(); 
				//				initColor();
			} 
		})
		.show();
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
	public void onCopyMac(View view) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Machine ID", textViewMac.getText());
		String leng  = textViewMac.getText().toString();
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
	}

	public class LicenseValidationTask extends ATask<Void, Void, String> {

		private static final String TAG = "RAMY-LicenseValidationTask";
		private static final String LOGIN_NAME = "ramy";
		private static final String LOGIN_PASS = "ramy";

		private String serverAddr;
		private FTPClient client = null;

		public LicenseValidationTask(String serverAddr) {
			this.serverAddr = serverAddr;
		}

		private boolean connect() {
			try {
				client = new FTPClient();
				client.setConnectTimeout(5000);
				Log.i(TAG, "Connecting to " + serverAddr + "...");
				client.connect(InetAddress.getByName(serverAddr));
				if (client.login(LOGIN_NAME, LOGIN_PASS)) {
					return true;
				} else {
					disconnect();
				}
			} catch (IOException e) {
				disconnect();
			}
			return false;
		}

		private void disconnect() {
			if (client != null) {
				try {
					client.logout();
					client.disconnect();
				} catch (IOException e) {
					// Expected.
				}
				client = null;
			}
		}

		private boolean makeEnter(String dirName) {
			if (dirName == null) {
				disconnect();
				return false;
			}
			try {
				client.makeDirectory(dirName);
				if (!client.changeWorkingDirectory(dirName)) {
					disconnect();
					return false;
				}
				return true;
			} catch (IOException e) {
				disconnect();
				return false;
			}
		}

		private String download(String filename) {

			try {
				client.enterLocalPassiveMode();//将FTP的传输方式设为被动模式
				InputStream is = client.retrieveFileStream(filename);
				String result = null;
				if (is != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					result = reader.readLine();
					Log.d(TAG, result);
					reader.close();
					client.completePendingCommand();
				}
				return result == null ? "" : result;
			} catch (IOException e) {
				return "";
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (!connect()) {
				Log.e(TAG, "Failed to connect.");
				return "";
			}
			if (!makeEnter(LocalFileUtil.DIR_PUB)) {
				Log.e(TAG, "Failed to enter public directory.");
				return "";
			}
			String digest = MainService.getInstance().getMachineDigest();
			String result = download(digest + ".dat");
			disconnect();
			return result;
		}

		@Override
		protected void onPostExecute(String s) {
			if (registerDialog != null && registerDialog.isShowing()) {
				registerDialog.dismiss();
			}
			validateSerial(s);
		}

	}

}
