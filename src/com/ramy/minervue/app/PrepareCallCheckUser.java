package com.ramy.minervue.app;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.ramy.minervue.R;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.MainActivity.WifiInfoViewListener;
import com.ramy.minervue.bean.OnLineBean;
import com.ramy.minervue.control.ControlManager;
import com.ramy.minervue.control.QueryTask;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.util.AudioRecordDemo;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex;
import com.wifitalk.Activity.SendSoundsThread_oldSpeex;
import com.wifitalk.Activity.ZuActivity;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex.PrepareCallLintener;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;
import com.wifitalk.Utils.IPCheck;
import com.wifitalk.Utils.ProgressDialogUtils;
import com.wifitalk.adapter.PrepareCallCheckUserAdapter;
import com.wifitalk.adapter.PrepareCallCheckUserAdapter.MyHolder;
//import com.wifitalk.adapter.PrepareCallCheckUserAdapter.MyHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <!-- 选择对讲人进行呼叫界面(可多选) -->
 * 
 * @author Administrator adb shell monkey -p com.ramy.minervue -s 500
 *         --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v
 *         10000 > E:\monkey_log\java_monkey_log.txt
 */
public class PrepareCallCheckUser extends Activity implements
WifiInfoViewListener, OnClickListener, OnTouchListener,
OnItemLongClickListener, OnItemClickListener, OnLongClickListener ,PrepareCallLintener{
	protected static final int ACTION_UP = 1560;
	private boolean call_btn_statu = false;
	boolean lastSHUAXIN;
	private int type = 0;// 类型 默认语音
	private LinearLayout ll_meue;
	private LinearLayout fly_bottom;
	public AudioManager audioManager = MainService.getInstance().getAudioManager();
	public boolean isSuoDing = false;// 是否锁定 false不锁定
	public static PrepareCallCheckUser instances;
	private GridView lv_users;
	public Button btn_call, btn_z1, btn_z2, btn_z3, btn_qiehuan,btn_menu,sw_led;// 呼叫
	private PrepareCallCheckUserAdapter callAdapter;
	private QueryTask queryTask = null;
	public DBHelper db = new DBHelper(this);
	private ControlManager.PacketListener packetlistener = new PacketListener();
	public boolean startIndex = true; // 进入界面的的索引
	public boolean endIndex = false; // 退出界面的的索引
	public String deviceIp = MainService.getInstance().getIPadd();
	public TextView tv_speak_info, tv_list_info, tv_myip, tvwifi, tv_name,tv_speak_info_btn,
	tv_audio_info, tv_show,tv_speak_time_info;
	private UserBean userBean;
	private int bottonWidth = 0;
	private int bottonHeight = 0;
	//	private ImageView imb_statu/*iv_mac*/;
	private Dialog dialog_button = null;//MIC音量大小提示
	private ImageView dialog_img;//提示图
	private static int[] res = {R.drawable.mic_1, R.drawable.mic_2, R.drawable.mic_3,
		R.drawable.mic_4, R.drawable.mic_5,R.drawable.mic_6 };
	public long recodeTime = 0;//采样时间防止乱点
	public ImageView iv_setting_audio;
	private Button btn_pingdao,btn_back;
	public static String pindaoID = "1";//TODO  频道ID
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		if (width == 240) {
			setTheme(android.R.style.Theme_Black_NoTitleBar);
		}
		setContentView(R.layout.prepare_call_check_user_activity);
		MainService.getInstance().getReceiveSoundsThread().setGPSCallBackListener(this);
		instances = this;
		//		initTask();
		InitView();
		InitTitle();
		setViewLinstner();
		pindaoID = db.getPingdaoId();
		try {
			userBean = (UserBean) getIntent().getExtras().getSerializable("UserBean");
			if (userBean != null) {
				lastSHUAXIN = MainService.SHUAXIN;
				ll_meue.setVisibility(View.GONE);
				lv_users.setVisibility(View.GONE);
				tv_audio_info.setVisibility(View.VISIBLE);
				btn_qiehuan.setVisibility(View.VISIBLE);
				MainService.SHUAXIN = false;// 不刷新
				tv_audio_info.setText("与" + userBean.getUsername() + ":单独对讲中");
			} else {
				if (width == 240) {
					fly_bottom.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 2.0f));  
				}else{
					//ll_meue.setVisibility(View.VISIBLE);
				}
				lv_users.setVisibility(View.VISIBLE);
				tv_audio_info.setVisibility(View.GONE);
				setListAdapter();
			}
		} catch (Exception e1) {
			//ll_meue.setVisibility(View.VISIBLE);
			lv_users.setVisibility(View.VISIBLE);
		}
	}






	public void setListAdapter() {
		if (MainService.getInstance().zu <= 1) {
			btn_z1.requestFocus();
			btn_z1.performClick();
		} else if (MainService.getInstance().zu == 2) {
			btn_z2.requestFocus();
			btn_z2.performClick();
		} else {
			btn_z3.requestFocus();
			btn_z3.performClick();
		}
	}






	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.i("asd", keyCode+"");
		switch (keyCode) {
		case 8:
			btn_z1.requestFocus();
			btn_z1.performClick();
			showTextToast("已切换到"+btn_z1.getText().toString(), 0);
			break;
		case 9:
			btn_z2.requestFocus();
			btn_z2.performClick();
			showTextToast("已切换到"+btn_z2.getText().toString(), 0);
			break;
		case 10:
			btn_z3.requestFocus();
			btn_z3.performClick();
			showTextToast("已切换到"+btn_z3.getText().toString(), 0);
			break;
		default:
			break;
		}
		if(keyCode==132||keyCode==131){
			Log.i("asd", "停止发送");
			onActionUp();
		}
		return super.onKeyUp(keyCode, event);
	}
	public PrepareCallCheckUserAdapter getAdapter() {
		return callAdapter;
	}

	public Button getSpeakButton() {
		return btn_call;
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	public void setSpeakWifi(boolean isClose, String speakInfo) {
		if (isClose == true) {
			btn_call.setBackgroundResource(R.drawable.call_red);
			//			btn_call.setEnabled(false);
			tv_speak_info.setText(speakInfo);
			tv_speak_info.setVisibility(View.VISIBLE);
			tv_speak_info_btn.setVisibility(View.INVISIBLE);
			tv_speak_time_info.setText(sdf.format(new Date())+"最后发言人:"+speakInfo.substring(0, speakInfo.length()-4));
			//			if(dialog!=null){
			//				if (dialog.isShowing()) {
			//					dialog.dismiss();
			//				}
			//			}
			
		} else {
			if(call_btn_statu){
				btn_call.setBackgroundResource(R.drawable.call_blue_down);
			}else{
				btn_call.setBackgroundResource(R.drawable.call_blue_up);
			}
			tv_speak_info.setText(speakInfo);
			tv_speak_info.setVisibility(View.VISIBLE);
			tv_speak_info_btn.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==132||keyCode==131){
			onActionDown();
			Log.i("asd", "发送");
		}
		return super.onKeyDown(keyCode, event);
	}

	MenuItem action_adds;
	MenuItem action_suoding;
	MenuItem action_jinyin;
	MenuItem action_pindao;
	MenuItem action_mac_name;
	MenuItem action_switch_pindao;
	MenuItem action_shuaxing;
	String [] strpd = {"","一","二","三","四","五","六"};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.call_menu, menu);
		setIconEnable(menu, true);
		action_suoding = menu.findItem(R.id.action_suoding);
		action_adds = menu.findItem(R.id.action_adds);
		action_jinyin = menu.findItem(R.id.action_jinyin);
		action_pindao = menu.findItem(R.id.action_pindao);
		action_mac_name = menu.findItem(R.id.action_mac_name);
		action_switch_pindao = menu.findItem(R.id.action_switch_pindao);
		action_shuaxing = menu.findItem(R.id.action_shuaxing);
		action_mac_name.setVisible(false);
		if (MainService.JINYIN) {
			action_jinyin.setIcon(R.drawable.ic_volume_small);
		} else {
			action_jinyin.setIcon(R.drawable.ic_volume_off_small);

		}
		if (MainService.SHUAXIN == false) {
			action_suoding.setIcon(R.drawable.ic_menu_block);
		} else {
			action_suoding.setIcon(R.drawable.ic_menu_refresh);
		}
		action_pindao.setTitle("频道"+strpd[MainService.getInstance().pindao]);
		if(MainService.getInstance().model.equals("组")){
			action_switch_pindao.setTitle("切换到频道讲话");
		}else{
			action_switch_pindao.setTitle("切换到组讲话");
		}
		return true;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 200)
		{
			if (MainService.getInstance().zu < 2) {
				btn_z1.requestFocus();
				btn_z1.performClick();
			} else if (MainService.getInstance().zu == 2) {
				btn_z2.requestFocus();
				btn_z2.performClick();
			} else {
				btn_z3.requestFocus();
				btn_z3.performClick();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			if (MainService.getInstance().zu == 0) {
				new AlertDialog.Builder(PrepareCallCheckUser.this)
				.setTitle("请先选中所需添加到的用户组！")
				.setPositiveButton("确定", null).create().show();
			} else {
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.add_call_alert2, null);
				final AlertDialog dialog = new AlertDialog.Builder(
						PrepareCallCheckUser.this)
				.setTitle(
						String.format(
								getResources().getString(
										R.string.new_lxr_info),
										String.format(getResources().getString(
												R.string.group)
												+ "("
												+ MainService.getInstance().zu
												+ ")")))
												.setIcon(android.R.drawable.ic_menu_add).setView(layout// ic_menu_edit
														).show();
				final EditText ip = (EditText) layout.findViewById(R.id.et_ip);
				final EditText name = (EditText) layout
						.findViewById(R.id.et_name);
				final EditText tv_mac = (EditText) layout
						.findViewById(R.id.tv_mac);
				tv_mac.setEnabled(true);	
				Button ok = (Button) layout.findViewById(R.id.btn_ok);
				ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String userIp = ip.getText().toString();
						String userName = name.getText().toString();
						String mac = tv_mac.getText().toString();
						if (userName.isEmpty()) {
							Toast.makeText(PrepareCallCheckUser.this,
									R.string.name_not_null, Toast.LENGTH_SHORT)
									.show();
						} else if (userIp.isEmpty()) {
							Toast.makeText(PrepareCallCheckUser.this,
									R.string.ip_not_null, Toast.LENGTH_SHORT)
									.show();
						} else if (IPCheck.isboolIP(userIp) == false) {
							Toast.makeText(PrepareCallCheckUser.this,
									R.string.ip_error, Toast.LENGTH_SHORT)
									.show();
						} else if (userName.length() > 6) {
							Toast.makeText(PrepareCallCheckUser.this,
									R.string.name_len_error, Toast.LENGTH_SHORT)
									.show();
						} else if (mac.length() != 6) {
							Toast.makeText(PrepareCallCheckUser.this,
									"MAC地址必须等于6位", Toast.LENGTH_SHORT)
									.show();
						} else {
							UserBean userBean = new UserBean(userName, userIp);
							userBean.setMacAddress(mac);
							boolean result = db.saveUserTable(userBean,
									MainService.getInstance().zu);
							if(result){
								Toast.makeText(PrepareCallCheckUser.this,
										R.string.save_succ, Toast.LENGTH_SHORT)
										.show();
								callAdapter = new PrepareCallCheckUserAdapter(
										getApplicationContext(), type, db
										.findUserTable(MainService
												.getInstance().zu),
												PrepareCallCheckUser.instances);
								PrepareCallCheckUser.instances
								.deleteAudioAllCheckStatu();
								callAdapter.notifyDataSetChanged();
								lv_users.setAdapter(callAdapter);
								dialog.dismiss();
							}else{
								Toast.makeText(PrepareCallCheckUser.this,
										"添加失败,改设备已存在", Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
			}
			break;
		case R.id.action_adds:
			if (MainService.getInstance().zu == 0) {
				new AlertDialog.Builder(PrepareCallCheckUser.this)
				.setTitle("请先选中所需组加到的用户组！")
				.setPositiveButton("确定", null).create().show();
			} else {
				Intent intent = new Intent();
				intent.putExtra("user_zu", MainService.getInstance().zu);
				intent.putExtra("type", type);
				intent.setClass(getApplicationContext(), ZuActivity.class);
				startActivityForResult(intent, 200);
			}
			break;
		case R.id.action_camera:
			startActivity(new Intent(getApplicationContext(),
					RtspActivity.class));
			break;
		case R.id.action_jinyin:
			if (!MainService.JINYIN) {
				MainService.JINYIN = true;
				showTextToast("已开启声音", 1);
				action_jinyin.setIcon(R.drawable.ic_volume_small);
			} else {
				showTextToast("已关闭声音", 1);
				action_jinyin.setIcon(R.drawable.ic_volume_off_small);
				MainService.JINYIN = false;
			}
			break;
		case R.id.action_suoding:
			if (MainService.SHUAXIN == true) {
				MainService.SHUAXIN = false;// 不刷新
				showTextToast("已关闭自动刷新列表模式", 1);
				action_suoding.setIcon(R.drawable.ic_menu_block);
			} else {
				MainService.SHUAXIN = true;// 刷新
				showTextToast("已开启自动刷新列表模式", 1);
				action_suoding.setIcon(R.drawable.ic_menu_refresh);
			}
			break;
		case R.id.action_pindao:
			new AlertDialog.Builder(PrepareCallCheckUser.this)
			.setTitle("请选择需要切换的频道")
			.setItems(R.array.str_pindao,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0,
						final int position) {
					final String[] aryPindao = getResources()
							.getStringArray(
									R.array.str_pindao);
					new AlertDialog.Builder(PrepareCallCheckUser.this)
					.setTitle("是否切换到"+aryPindao[position]+"?")
					.setMessage(aryPindao[position])
					.setNegativeButton(
							"是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface arg0,
										int arg1) {
									MainService.getInstance().pindao= position+1;
									showTextToast("已选择"+aryPindao[position], 0);
									action_pindao.setTitle("频道"+strpd[position+1]);
								}
							}).setPositiveButton("否", null).show();
				}
			}).show();
			break;
		case R.id.action_switch_pindao:
			if(MainService.getInstance().model.equals("组")){
				MainService.getInstance().model="频道";
				tv_speak_info_btn.setText("按住频道讲话");
				action_switch_pindao.setTitle("切换到组讲话");
			}else{
				MainService.getInstance().model="组";
				tv_speak_info_btn.setText("按住讲话");
				action_switch_pindao.setTitle("切换到频道讲话");
			}
			break;

		case R.id.action_shuaxing:

			showTextToast("正在刷新列表", 1);
			new Thread(new Runnable() {

				@Override
				public void run() {
					MainService.getInstance().getReceiveSoundsThread().clearOnline();
					DatagramSocket clientSocket = null;
					List<UserBean> list = db.findUserTable(MainService
							.getInstance().zu);
					try {
						clientSocket = new DatagramSocket();
						StringBuffer str = new StringBuffer(
								"QuestlongitudeAndlatitude:"
										+ MainService.getInstance().getIPadd());
						// 构建数据包 头+体
						DataPacket dataPacket = new DataPacket(str.toString()
								.getBytes(), new byte[] { 01, 01, 01 });
						// // 构建数据报 +发送
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i).getUserIp().equals(deviceIp)) {
								continue;
							}
							clientSocket.send(new DatagramPacket(dataPacket
									.getAllData(),
									dataPacket.getAllData().length,
									InetAddress.getByName(list.get(i)
											.getUserIp()), AppConfig.PortAudio));
							Log.i("111", "send:" + list.get(i).getUserIp());
						}
						Log.i("111", "===============");
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (clientSocket != null) {
							clientSocket.close();
						}
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Message msg = Message.obtain();
					msg.obj = MainService.getInstance()
							.getReceiveSoundsThread().getOnlineList();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
			}).start();


			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
  

	public void setImageStatu(boolean statu){
		//		if(statu){
		//			btn_call.setBackgroundResource(R.drawable.call_blue_up);
		//			btn_call.setEnabled(true);   
		////			imb_statu.setImageResource(R.drawable.indicator_code_lock_point_area_green);
		//		}else{
		////			imb_statu.setImageResource(R.drawable.indicator_code_lock_point_area_red);
		//			btn_call.setBackgroundResource(R.drawable.call_red);
		//			btn_call.setEnabled(false);
		//		}
	}

	//显示dialog
	void showVoiceDialog(){
		dialog_button = new Dialog(PrepareCallCheckUser.this,R.style.DialogStyle);
		dialog_button.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog_button.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog_button.setContentView(R.layout.my_dialog);
		dialog_button.setCanceledOnTouchOutside(false);
		dialog_button.setCancelable(false);
		dialog_img=(ImageView)dialog_button.findViewById(R.id.mic_1);
		dialog_button.show();
	}


	// enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效
	private void setIconEnable(Menu menu, boolean enable) {
		try {
			Class<?> clazz = Class
					.forName("com.android.internal.view.menu.MenuBuilder");
			Method m = clazz.getDeclaredMethod("setOptionalIconsVisible",
					boolean.class);
			m.setAccessible(true);

			// MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
			m.invoke(menu, enable);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setViewLinstner() {
		btn_z1.setOnClickListener(this);
		btn_z2.setOnClickListener(this);
		btn_z3.setOnClickListener(this);

		btn_z1.setOnLongClickListener(this);
		btn_z2.setOnLongClickListener(this);
		btn_z3.setOnLongClickListener(this);

		btn_call.setOnClickListener(this);
		btn_call.setOnTouchListener(this);
		
		//		btn_call.setOnLongClickListener(new OnLongClickListener() {
		//			@Override
		//			public boolean onLongClick(View v) {
		//				//				MainActivity.this.flag = "talk";
		//				//				MainActivity.this.setImageButtonBackground();
		//				call_btn_statu = true;
		//				onActionDown();
		//				btn_call.setBackgroundResource(R.drawable.call_blue_down);
		//				showVoiceDialog();		
		//				return true;
		//			}
		//		});
		lv_users.setOnItemClickListener(this);
		lv_users.setOnItemLongClickListener(this);
		//		btn_online.setOnClickListener(this);
		btn_qiehuan.setOnClickListener(this);
		tv_name.setOnClickListener(this);
		tv_myip.setOnClickListener(this);
		btn_menu.setOnClickListener(this);

	}

	private void InitView() {
		ll_meue = (LinearLayout) findViewById(R.id.ll_meue);
		fly_bottom = (LinearLayout) findViewById(R.id.fly_bottom);
		lv_users = (GridView) findViewById(R.id.gv_gridview);
		tvwifi = (TextView) findViewById(R.id.tvwifi);
		tv_speak_info = (TextView) findViewById(R.id.tv_speak_info);
		tv_speak_time_info = (TextView) findViewById(R.id.tv_speak_time_info);
		tv_speak_info_btn = (TextView) findViewById(R.id.tv_speak_info_btn);
		tv_myip = (TextView) findViewById(R.id.tv_myip);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_list_info = (TextView) findViewById(R.id.tv_list_info);
		tv_list_info.setVisibility(View.GONE);
		tv_speak_info.setVisibility(View.GONE);
		btn_call = (Button) findViewById(R.id.btn_call);
		tv_audio_info = (TextView) findViewById(R.id.tv_audio_info);
		tv_show = (TextView) findViewById(R.id.tv_show);
		//		btn_online = (Button) findViewById(R.id.btn_online);
		btn_z1 = (Button) findViewById(R.id.btn_z1);
		btn_z2 = (Button) findViewById(R.id.btn_z2);
		btn_z3 = (Button) findViewById(R.id.btn_z3);
		sw_led = (Button) findViewById(R.id.sw_led);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		
		sw_led.setOnClickListener(this);
		btn_pingdao = (Button) findViewById(R.id.btn_pingdao);
		btn_pingdao.setOnClickListener(this);
		btn_qiehuan = (Button) findViewById(R.id.btn_qiehuan);
		btn_menu = (Button) findViewById(R.id.btn_menu);
		iv_setting_audio = (ImageView) findViewById(R.id.iv_setting_audio);
		iv_setting_audio.setOnClickListener(this);

		//		imb_statu = (ImageView) findViewById(R.id.imb_statu);
		if(MainService.getInstance().model.equals("组")){
			tv_speak_info_btn.setText("按住讲话");
		}else{
			tv_speak_info_btn.setText("按住频道讲话");
		}
		final LinearLayout fly_bottom = (LinearLayout) findViewById(R.id.fly_bottom);
		ViewTreeObserver vto = fly_bottom.getViewTreeObserver();
		//		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		//			@Override
		//			public void onGlobalLayout() {
		//				fly_bottom.getViewTreeObserver().removeGlobalOnLayoutListener(
		//						this);
		//				bottonWidth = fly_bottom.getWidth();
		//				bottonHeight = (int) (fly_bottom.getHeight()/1.5);
		//				FrameLayout.LayoutParams linearParams = (FrameLayout.LayoutParams) btn_call
		//						.getLayoutParams();
		//				linearParams.width = bottonHeight;
		//				linearParams.height = bottonHeight;
		//				btn_call.setLayoutParams(linearParams);
		//			}
		//		});
	}

	private void setNameIPText() {
		tv_name.setText("" + MainService.getInstance().getUUID());
		tv_myip.setText("" + MainService.getInstance().getIPadd());
	}

	private void InitTitle() {
		type = getIntent().getExtras().getInt("type");
		if (type == 0) {
			setTitle(getString(R.string.app_name) + "-语音对讲");
		} else {
			setTitle(getString(R.string.app_name) + "-视频对讲");
			tv_speak_info_btn.setText("启动视频");
		}
	}

	private void initTask() {
		MainActivity.getGetInstance().setwifiInfoViewListener(this);
		ControlManager control = MainService.getInstance().getControlManager();
		MainService.getInstance().initUserDB();
		JSONObject json = new JSONObject();
		try {
			json.put("uuid", MainService.getInstance().getUUID());
			json.put("action", "intercom");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		queryTask = control.createQueryTask(json, packetlistener, 1);
		queryTask.start();
	}

	public JSONObject CreateJson() {
		String[] name = { "ym89", "ym90", "ym91", "ym90" };
		String[] ip = { "192.168.1.125", "192.168.1.124", "192.168.1.149",
		"192.168.1.142" };
		try {
			JSONObject jsobj = new JSONObject();
			jsobj.put("time", "2015年7月7日12:19:23");
			JSONArray users = new JSONArray();
			for (int i = 0; i < name.length; i++) {
				JSONObject user = new JSONObject();
				user.put("username", name[i]);
				user.put("userip", ip[i]);
				users.put(user);
				// users.add(user);
			}
			jsobj.put("userinfo", users);
			return jsobj;
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	};

	public void deleteAudioAllCheckStatu() {
		MainService.getInstance().setAudioUserBeans(null);
		Log.i("XUANZ", "deleteAllCheckStatu");
	}

	public void deleteVideoAllCheckStatu() {
		MainService.getInstance().setVideoUserBeans(null);
		Log.i("XUANZ", "deleteAllCheckStatu");
	}

	public void setAudioCheckStatu(UserBean value) {
		if (value.getUserIp().equals(deviceIp)) {
			return;
		}
		for (int i = 0; i < MainService.getInstance().getAudioUserBeans()
				.size(); i++) {
			if (MainService.getInstance().getAudioUserBeans().get(i)
					.getUserIp().equals(value.getUserIp())) {
				Log.e("XUANZ", value.getUserIp() + "已经存在--"
						+ MainService.getInstance().getAudioUserBeans().size());
				return;// 终止添加
			}
		}
		Log.i("XUANZ", "setAudioCheckStatu\t ip:" + value.getUserIp() + "--"
				+ MainService.getInstance().getAudioUserBeans().size());
		MainService.getInstance().getAudioUserBeans().add(value);
	}

	public void setVideoCheckStatu(UserBean value) {
		for (int i = 0; i < MainService.getInstance().getVideoUserBeans()
				.size(); i++) {
			if (/* checkStatu.get(i).getUsername().equals(value.getUsername())&& */MainService
					.getInstance().getVideoUserBeans().get(i).getUserIp()
					.equals(value.getUserIp())) {
				return;// 终止添加
			}
		}
		Log.i("XUANZ", "setVideoCheckStatu\t ip:" + value.getUserIp());
		MainService.getInstance().getVideoUserBeans().add(value);
	}

	public void deleteVideoPositionCheckStatu(UserBean value) {
		for (int i = 0; i < MainService.getInstance().getVideoUserBeans()
				.size(); i++) {
			if (/* checkStatu.get(i).getUsername().equals(value.getUsername())&& */MainService
					.getInstance().getVideoUserBeans().get(i).getUserIp()
					.equals(value.getUserIp())) {
				Log.i("XUANZ", "deleteCheckStatu:"
						+ MainService.getInstance().getVideoUserBeans().get(i)
						.getUserIp());
				MainService.getInstance().getVideoUserBeans().remove(i);
			}
		}
	}

	public void deleteAudioPositionCheckStatu(UserBean value) {
		for (int i = 0; i < MainService.getInstance().getAudioUserBeans()
				.size(); i++) {
			if (/* checkStatu.get(i).getUsername().equals(value.getUsername())&& */MainService
					.getInstance().getAudioUserBeans().get(i).getUserIp()
					.equals(value.getUserIp())) {
				Log.i("XUANZ", "deleteCheckStatu:"
						+ MainService.getInstance().getAudioUserBeans().get(i)
						.getUserIp());
				MainService.getInstance().getAudioUserBeans().remove(i);
			}
		}
	}

	public List<UserBean> getVideoCheckStatu() {
		return MainService.getInstance().getVideoUserBeans();
	}

	public List<UserBean> getAudioCheckStatu() {
		return MainService.getInstance().getAudioUserBeans();
	}

	public String showSelectAuthors() {
		StringBuffer result = new StringBuffer("正在呼叫：");
		for (UserBean sh : MainService.getInstance().getAudioUserBeans()) {
			result.append("\n" + sh.getUsername() + ":" + sh.getUserIp() + "");
			Log.i("TEST", "呼叫" + sh.getUsername() + ":" + sh.getUserIp());
		}
		return result.toString();
	}

	private class PacketListener implements ControlManager.PacketListener {

		@Override
		public String getPacketType() {
			return "intercom";
		}

		@Override
		public void onPacketArrival(JSONObject packet) {
			//			try {
			//				boolean available = "intercom".equals(packet
			//						.getString("action"));
			//				db.deleteServerTB();// 删除之前的数据
			//				int count = 0;
			//				if (available) {
			//					// [ym89:192.168.1.125],[ym8955:192.168.1.124],
			//					String result = packet.getString("result");
			//					String[] strs = result.split("づぞ");
			//					for (int i = 0; i < strs.length; i++) {
			//						String[] nameip = strs[i].split("ぬぁ");
			//						Log.i("TEST", "姓名:" + nameip[0] + "\t" + "IP:"
			//								+ nameip[1]);
			//						String userName = nameip[0];
			//						String userIp = nameip[1];
			//						if (userIp.isEmpty()) {
			//							// showTextToast("设备名不能为空", Toast.LENGTH_SHORT);
			//						} else if (userIp.isEmpty()) {
			//							// showTextToast("ip地址不能为空", Toast.LENGTH_SHORT);
			//						}else {
			//							 db.saveUserTable(new UserBean(userName, userIp));
			//							 count++;
			//						 }
			//					}
			//					if (strs.length > 0) {
			//						String sAgeFormat1 = getResources().getString(
			//								R.string.service_gx);
			//						String sFinal1 = String.format(sAgeFormat1, count + "");
			//						showTextToast(sFinal1, Toast.LENGTH_SHORT);
			//		 
			//					}
			//				}
			//			} catch (JSONException e) {
			//				// Ignored.
			//			}
		}

	}

	private Toast toast = null;

	/**
	 * 
	 * @param msg
	 *            内容
	 * @param i
	 *            显示时间 0 短时间 | 1 长时间
	 */
	private void showTextToast(String msg, int i) {
		try {
			if (toast == null) {
				toast = Toast.makeText(getApplicationContext(), msg, i);
			} else {
				toast.setText(msg);
			}
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showTextToast(CharSequence msg, int i) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	@Override
	protected void onResume() {
		Log.i("AAAS", "onResume");
//		ProgressDialogUtils.showProgressDialog(PrepareCallCheckUser.this, "");
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				mHandler.post(new Runnable() {
//					
//					@Override
//					public void run() {
//						ProgressDialogUtils.dismissProgressDialog();
//					}
//				});
//			}
//		}).start();
		ReceiveSoundsThread_oldSpeex.videoStatu = false;
		try {
			MainActivity.getGetInstance().setwifiInfoViewListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
			setListAdapter();	
		pindaoID = db.getPingdaoId();
		btn_pingdao.setText(pindaoID);
		InitTitle();
		setNameIPText();
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MainService.getInstance().sendSoundsThread.setMacStatu(false);
		MainService.getInstance().sendSoundsThread(false);

		try{
			Camera.Parameters mParameters;
			mParameters = m_Camera.getParameters();
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			m_Camera.setParameters(mParameters);
			m_Camera.release();
			m_Camera=null;
			
		} catch(Exception ex){}
	

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		endIndex = true;
		audioManager.setMicrophoneMute(false);// 开启mic
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		SendSoundsThread_oldSpeex.shuanggong = false;
		MainActivity.getGetInstance().setwifiInfoViewListener(null); 
		MainService.getInstance().sendSoundsThread(false);
		if (userBean != null) {
			MainService.SHUAXIN = lastSHUAXIN;
		}
		instances = null;
		MainService.getInstance().getReceiveSoundsThread().destoryLintener(this);
		MainService.getInstance().sendSoundsThread.setMacStatu(false);

	}

	// 关闭扬声器
	public void CloseSpeaker() {
		try {
			if (audioManager != null) {
				if (audioManager.isSpeakerphoneOn()) {
					audioManager.setSpeakerphoneOn(false);
					// audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,currVolume,AudioManager.STREAM_VOICE_CALL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 关闭扬声器
	public void OpenSpeaker() {
		try {
			if (audioManager != null) {
				if (!audioManager.isSpeakerphoneOn()) {
					audioManager.setSpeakerphoneOn(true);
					// audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,currVolume,AudioManager.STREAM_VOICE_CALL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendUDP(final String heard, final String ip) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramSocket clientSocket = null;
				try {
					clientSocket = new DatagramSocket();
					StringBuffer str = new StringBuffer(heard + ":"
							+ MainService.getInstance().getIPadd());
					// 构建数据包 头+体
					DataPacket dataPacket = new DataPacket(str.toString()
							.getBytes(), new byte[] { 01, 01, 01 });
					// // 构建数据报 +发送
					clientSocket.send(new DatagramPacket(dataPacket
							.getAllData(), dataPacket.getAllData().length,
							InetAddress.getByName(ip), AppConfig.PortAudio));
					Log.i("CameraZQL", "sendUDP bye"+str.toString());
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (clientSocket != null) {
						clientSocket.close();
					}
				}
			}
		}).start();
	}

	@Override
	public void wifiInfoChange(String msg, int color) {
		tvwifi.setText(msg);
		tvwifi.setTextColor(color);
	}
	List<OnLineBean> listmsg =new ArrayList<OnLineBean>();
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				StringBuffer sb = new StringBuffer();
				listmsg = (List<OnLineBean>) msg.obj;
				for (int i = 0; i < listmsg.size(); i++) {
					sb.append(listmsg.get(i).getIp() + "\n");
				}
				callAdapter.setOnlineList(listmsg);
				callAdapter.notifyDataSetChanged();
				showTextToast("在线人数:"+listmsg.size(), 0);

				Log.i("UDP-Send-Audio",
						"在线人数:" + listmsg.size() + "/" + sb.toString());
			}
			if (msg.what == 2) {
				callAdapter.notifyDataSetChanged();
			}
			if (msg.what == 3) {
				showTextToast("呼叫结束!", 0);
			}
			if(msg.what==33){
				double f = SendSoundsThread_oldSpeex.volume;
				if (f < 26)
					dialog_img.setImageResource(res[0]);
				else if (f < 32)
					dialog_img.setImageResource(res[1]);
				else if (f < 38)
					dialog_img.setImageResource(res[2]);
				else if (f < 44)
					dialog_img.setImageResource(res[3]);
				else if (f < 50)
					dialog_img.setImageResource(res[4]);
				else
					dialog_img.setImageResource(res[5]);
				//				btn_call.setBackgroundResource(R.drawable.call_blue_down);
			}
			if(msg.what==34){//弹起对讲按钮事件
				audioManager.setMicrophoneMute(false);// 开启mic
				
				try {
					dialog_img.setImageResource(res[0]);
//					if(dialog_button!=null){
//						if (dialog_button.isShowing()) {
//							dialog_button.dismiss();
//						}
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	};
	private int checkNum;
	private Camera m_Camera = null;

	class MaxTextLengthFilter implements InputFilter{

		private int mMaxLength;

		public MaxTextLengthFilter(int max){
			mMaxLength = max - 1;
			toast = Toast.makeText(PrepareCallCheckUser.this,"字符不能超过5个",1000);
			toast.setGravity(Gravity.TOP, 0, 235);
		}

		public CharSequence filter(CharSequence source, int start, int end, 
				Spanned dest, int dstart ,int dend){
			int keep = mMaxLength - (dest.length() - (dend - dstart));
			if(keep < (end - start)){
				toast.show();
			}
			if(keep <= 0){
				return "";
			}else if(keep >= end - start){
				return null;
			}else{
				return source.subSequence(start,start + keep);
			}
		}
	}


	@Override
	public void onClick(View v) {
		if(v==btn_back){
			finish();
		}
		if(v==sw_led){
			
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					if(m_Camera==null){

						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								ProgressDialogUtils.showProgressDialog(PrepareCallCheckUser.this, "正在打开请稍后...");
							}
						});			
						m_Camera = Camera.open();
					}
					ProgressDialogUtils.dismissProgressDialog();
					if(sw_led.getTag()==null){
						try{
							
							Camera.Parameters mParameters;
							mParameters = m_Camera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							m_Camera.setParameters(mParameters);
							sw_led.setTag(this);
							sw_led.setTextColor(Color.RED);
						} catch(Exception ex){}
					}else{
						try{
							Camera.Parameters mParameters;
							mParameters = m_Camera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							m_Camera.setParameters(mParameters);
							sw_led.setTag(null);
							sw_led.setTextColor(Color.WHITE);
						} catch(Exception ex){}
					}					
				}
			}).start();
			
			
		

		}
		if (v == btn_z1) {

			List<UserBean> dblist = db.findUserTable(1);
			if (type == 0) {
				deleteAudioAllCheckStatu();
			} else {
				deleteVideoAllCheckStatu();
			}
			callAdapter = new PrepareCallCheckUserAdapter(
					getApplicationContext(), dblist,
					PrepareCallCheckUser.instances, 1, type);
			callAdapter.setOnlineList(listmsg);
			lv_users.setAdapter(callAdapter);

			tv_list_info.setVisibility(View.GONE);
			btn_z1.setBackgroundColor(Color.GREEN);
			btn_z2.setBackgroundColor(Color.TRANSPARENT);
			btn_z3.setBackgroundColor(Color.TRANSPARENT);
			btn_z1.setTextColor(Color.BLACK);
			btn_z2.setTextColor(Color.WHITE);
			btn_z3.setTextColor(Color.WHITE);
			MainService.getInstance().zu = 1;
			btn_z2.setTag(null);
			btn_z3.setTag(null);
			gvselect(v, dblist);
		}
		if (v == btn_z2) {
			List<UserBean> dblist = db.findUserTable(2);
			if (type == 0) {
				deleteAudioAllCheckStatu();
			} else {
				deleteVideoAllCheckStatu();
			}
			callAdapter = new PrepareCallCheckUserAdapter(
					getApplicationContext(), dblist,
					PrepareCallCheckUser.instances, 2, type);
			callAdapter.setOnlineList(listmsg);
			lv_users.setAdapter(callAdapter);
			tv_list_info.setVisibility(View.GONE);
			btn_z1.setBackgroundColor(Color.TRANSPARENT);
			btn_z2.setBackgroundColor(Color.GREEN);
			btn_z3.setBackgroundColor(Color.TRANSPARENT);
			btn_z1.setTextColor(Color.WHITE);
			btn_z2.setTextColor(Color.BLACK);
			btn_z3.setTextColor(Color.WHITE);
			btn_z1.setTag(null);
			btn_z3.setTag(null);
			MainService.getInstance().zu = 2;
			gvselect(v, dblist);
		}
		if (v == btn_z3) {
			List<UserBean> dblist = db.findUserTable(3);
			if (type == 0) {
				deleteAudioAllCheckStatu();
			} else {
				deleteVideoAllCheckStatu();
			}
			callAdapter = new PrepareCallCheckUserAdapter(
					getApplicationContext(), dblist,
					PrepareCallCheckUser.instances, 3, type);
			callAdapter.setOnlineList(listmsg);
			lv_users.setAdapter(callAdapter);
			tv_list_info.setVisibility(View.GONE);
			btn_z1.setBackgroundColor(Color.TRANSPARENT);
			btn_z2.setBackgroundColor(Color.TRANSPARENT);
			btn_z3.setBackgroundColor(Color.GREEN);
			btn_z1.setTextColor(Color.WHITE);
			btn_z2.setTextColor(Color.WHITE);
			btn_z3.setTextColor(Color.BLACK);
			btn_z1.setTag(null);
			btn_z2.setTag(null);
			MainService.getInstance().zu = 3;
			gvselect(v, dblist);

		}
		if (v == btn_call) {
			Log.i("XUANZ",
					"======================================================");
			if (MainService.getInstance().getAudioUserBeans().size() == 0
					&& type == 0) {
				showTextToast(getText(R.string.selete_lxr), 0);
			} else {
				for (UserBean us : MainService.getInstance()
						.getAudioUserBeans()) {
					Log.i("XUANZ", us.getUserIp() + "---");
				}
			}
		}
		if(v==iv_setting_audio){
//			startVideo2();
//			audioManager.adjustStreamVolume (AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
			startActivity(intent);
		}
		if(v==btn_pingdao){
			final EditText et_id = new EditText(PrepareCallCheckUser.this);
			et_id.setText(pindaoID);
			et_id.setFilters(new InputFilter[]{new MaxTextLengthFilter(6)});
			new AlertDialog.Builder(PrepareCallCheckUser.this)
			.setTitle("请输入频道ID号")
			.setIcon(android.R.drawable.ic_dialog_info)
			.setView(et_id)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO 频道聊天
					if( et_id.getText().toString().length()==0){
						Toast.makeText(getApplicationContext(), "频道号不能为空", 0).show();
						return;
					}
					pindaoID = et_id.getText().toString();
					db.updatePingdaoid(pindaoID);
					btn_pingdao.setText(pindaoID);
					db.deleteALL(1);
					setListAdapter();
				}
			})
			.setNegativeButton("取消", null)
			.show();
		}
		if (v == btn_qiehuan) {

			if (btn_qiehuan.getText().equals("单工")) {// 切换双工
				btn_qiehuan.setText("双工");
				audioManager.setMicrophoneMute(false);// 开启mic
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				MainService.SHUAXIN = false;// 不刷新
				MainService.getInstance().sendSoundsThread(true);
				SendSoundsThread_oldSpeex.shuanggong = true;

			} else {// 切换单工
				btn_qiehuan.setText("单工");
				audioManager.setMicrophoneMute(true);// 关闭mic
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				MainService.getInstance().sendSoundsThread(false);
				SendSoundsThread_oldSpeex.shuanggong = false;

			}

		}
		if (v == tv_name||v==tv_myip) {
//			startActivity(new Intent(this, AddressManagerActivity.class));
		}

		if(v == btn_menu){
			String mac = MainService.getInstance().getMacAddressLastSix();
			Log.i("MAC", mac);
			openOptionsMenu();
		}
		//		if (v == btn_online) {
		//			showTextToast("正在刷新列表", 1);
		//			new Thread(new Runnable() {
		//
		//				@Override
		//				public void run() {
		//					MainService.getInstance().getReceiveSoundsThread().clearOnline();
		//					DatagramSocket clientSocket = null;
		//					List<UserBean> list = db.findUserTable(MainService
		//							.getInstance().zu);
		//					try {
		//						clientSocket = new DatagramSocket();
		//						StringBuffer str = new StringBuffer(
		//								"QuestlongitudeAndlatitude:"
		//										+ MainService.getInstance().getIPadd());
		//						// 构建数据包 头+体
		//						DataPacket dataPacket = new DataPacket(str.toString()
		//								.getBytes(), new byte[] { 01, 01, 01 });
		//						// // 构建数据报 +发送
		//						for (int i = 0; i < list.size(); i++) {
		//							if (list.get(i).getUserIp().equals(deviceIp)) {
		//								continue;
		//							}
		//							clientSocket.send(new DatagramPacket(dataPacket
		//									.getAllData(),
		//									dataPacket.getAllData().length,
		//									InetAddress.getByName(list.get(i)
		//											.getUserIp()), AppConfig.PortAudio));
		//							Log.i("111", "send:" + list.get(i).getUserIp());
		//						}
		//						Log.i("111", "===============");
		//					} catch (SocketException e) {
		//						e.printStackTrace();
		//					} catch (UnknownHostException e) {
		//						e.printStackTrace();
		//					} catch (IOException e) {
		//						e.printStackTrace();
		//					} finally {
		//						if (clientSocket != null) {
		//							clientSocket.close();
		//						}
		//					}
		//					try {
		//						Thread.sleep(3000);
		//					} catch (InterruptedException e) {
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//					Message msg = Message.obtain();
		//					msg.obj = MainService.getInstance()
		//							.getReceiveSoundsThread().getOnlineList();
		//					msg.what = 1;
		//					mHandler.sendMessage(msg);
		//				}
		//			}).start();
		//		}
	}

	private void gvselect(View v, List<UserBean> dblist) {
		try {
			if (type == 0) {
				for (int i = 0; i < dblist.size(); i++) {
					PrepareCallCheckUserAdapter.getIsSelected().put(i, true);
					UserBean userBean = new UserBean(dblist.get(i)
							.getUsername(), dblist.get(i).getUserIp());
					userBean.setMacAddress(dblist.get(i).getMacAddress());
					instances.setAudioCheckStatu(userBean);
				}
				v.setTag(1);

			}
			callAdapter.notifyDataSetChanged();
			if (type == 0) {
				tv_show.setText(String.format(
						getResources().getString(R.string.selete_people), instances
						.getAudioCheckStatu().size()));
			} else {
				tv_show.setText(String.format(
						getResources().getString(R.string.selete_people), instances
						.getVideoCheckStatu().size()));
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}



	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == btn_call) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
					onActionUp();
				 
			}
			if(event.getAction()==MotionEvent.ACTION_DOWN){
//				if(dialog_button!=null){
//					if(dialog_button.isShowing()){
//						showTextToast("您操作的太快", 0);
//						return true;
//					}
//				}
					onActionDown();
					btn_call.setBackgroundResource(R.drawable.call_blue_down);

				return true;

			}
			return false;
		}
		return false;
	}

	private void onActionUp() {	
		if(call_btn_statu==false)
			return;
		call_btn_statu = false;
		if(MainService.getInstance().model.equals("组")){
			tv_speak_info_btn.setText("按住讲话");
			btn_call.setBackgroundResource(R.drawable.call_blue_up);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(300);
						MainService.getInstance().sendSoundsThread(false);
						MainService.getInstance().getsendSoundsThread().endPackage();	
						MainService.getInstance().sendSoundsThread.setMacStatu(false);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						audioManager.setMicrophoneMute(true);// 关闭mic
//						Thread.sleep(300);
//						MainService.getInstance().sendSoundsThread(false);
//						Thread.sleep(100);
//						MainService.getInstance().getsendSoundsThread().endPackage();	
//						isDown = false;
//						Thread.sleep(200);
//						mHandler.sendEmptyMessage(34);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					//					VibratorUtil.Vibrate(PrepareCallCheckUser.this, 100); // 震动100ms
//				}
//			}).start();
//			MainService.getInstance().sendSoundsThread(false);
//			MainService.getInstance().getsendSoundsThread().endPackage();	
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						audioManager.setMicrophoneMute(true);// 关闭mic
						Thread.sleep(300);
						MainService.getInstance().sendSoundsThread(false,MainService.getInstance().pindao);
						Thread.sleep(100);
						MainService.getInstance().getsendSoundsThread().endPackage();	
						isDown = false;
						Thread.sleep(200);
						mHandler.sendEmptyMessage(34);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//					VibratorUtil.Vibrate(PrepareCallCheckUser.this, 100); // 震动100ms
				}
			}).start();
			tv_speak_info_btn.setText("按住频道讲话");
		}
		


	}
	boolean isDown = false;
	private void onActionDown() {
		call_btn_statu = true;
		
		
		MainService.getInstance().sendSoundsThread.setMacStatu(true);//开启mac风
		btn_call.setBackgroundResource(R.drawable.call_blue_down);
		if (type == 0) {//语言对讲
			if(MainService.getInstance().model.equals("组")){
				MainService.getInstance().sendSoundsThread(true);
			}else{
				MainService.getInstance().sendSoundsThread(true,MainService.getInstance().pindao);//频道未实现	
			}
			tv_speak_info_btn.setText("请讲话");
			AudioRecordDemo.DB = 0;
			audioManager.setMicrophoneMute(false);// 不禁用mic
//			showVoiceDialog();
			if(isDown==false){
				isDown = true;
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							while (isDown) {
////								mHandler.sendEmptyMessage(33);
//								Thread.sleep(10);
//
//							}
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}).start();
			}
		} else {
			if (MainService.getInstance().getVideoUserBeans().size() != 1) {
				showTextToast("只能与1人进行视频对讲", 1);
			} else {
				sendUDP("Video", MainService.getInstance()
						.getVideoUserBeans().get(0).getUserIp());
				showTextToast("正在连接...", 1);
				new Thread(new Runnable() {

					@Override
					public void run() {
						try { 
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (CameraActivity.instances == null) {
							mHandler.sendEmptyMessage(3);
						}
					}
				}).start();
			}
		}

	}

	// 进入等待界面
	public void startVideo() {
		if (null == CameraActivity.instances) {
			Intent mIntent1 = new Intent(getApplicationContext(),
					CameraActivity.class);
			UserBean user = new UserBean(MainService.getInstance()
					.getVideoUserBeans().get(0).getUsername(), MainService
					.getInstance().getVideoUserBeans().get(0).getUserIp());
			List<UserBean> list = new ArrayList<UserBean>();
			list.add(user);
			MainService.getInstance().setAudioUserBeans(list);
			mIntent1.putExtra("UserBean", user);
			startActivity(mIntent1);
		}
	}
	
	// 进入等待界面
	public void startVideo2() {
			Intent mIntent1 = new Intent(getApplicationContext(),
					CameraActivity.class);
			UserBean user = new UserBean("TEST", MainService
					.getInstance().getIPadd());
			List<UserBean> list = new ArrayList<UserBean>();
			list.add(user);
			MainService.getInstance().setAudioUserBeans(list);
			mIntent1.putExtra("UserBean", user);
			startActivity(mIntent1);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {

		final List<UserBean> listData = callAdapter.getList();
		if (listData.get(position).getUsername().equals("")) {
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.add_call_alert2, null);
			final AlertDialog dialog = new AlertDialog.Builder(
					PrepareCallCheckUser.this)
			.setTitle(
					String.format(
							getResources().getString(
									R.string.new_lxr_info),
									String.format(getResources().getString(
											R.string.group)
											+ "("
											+ MainService.getInstance().zu
											+ ")")))
											.setIcon(android.R.drawable.ic_menu_add).setView(layout// ic_menu_edit
													).show();
			final EditText ip = (EditText) layout.findViewById(R.id.et_ip);
			final EditText name = (EditText) layout.findViewById(R.id.et_name);
			final EditText tv_mac = (EditText) layout
					.findViewById(R.id.tv_mac);
			try {
				tv_mac.setText(listData.get(position).getMacAddress());
				tv_mac.setEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			}			
			Button ok = (Button) layout.findViewById(R.id.btn_ok);
			ip.setText(listData.get(position).getUserIp());
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String userIp = ip.getText().toString();
					String userName = name.getText().toString();
					if (userName.isEmpty()) {
						Toast.makeText(PrepareCallCheckUser.this,
								R.string.name_not_null, Toast.LENGTH_SHORT)
								.show();
					} else if (userIp.isEmpty()) {
						Toast.makeText(PrepareCallCheckUser.this,
								R.string.ip_not_null, Toast.LENGTH_SHORT)
								.show();
					} else if (IPCheck.isboolIP(userIp) == false) {
						Toast.makeText(PrepareCallCheckUser.this,
								R.string.ip_error, Toast.LENGTH_SHORT).show();
					} else if (userName.length() > 6) {
						Toast.makeText(PrepareCallCheckUser.this,
								R.string.name_len_error, Toast.LENGTH_SHORT)
								.show();
					} else {
						UserBean user = new UserBean(userName, userIp);
						user.setMacAddress(listData.get(position).getMacAddress());
						boolean result = db.saveUserTable(user,
								MainService.getInstance().zu);
						if(result){
							Toast.makeText(PrepareCallCheckUser.this,
									R.string.save_succ, Toast.LENGTH_SHORT).show();
							callAdapter = new PrepareCallCheckUserAdapter(
									getApplicationContext(), type, db
									.findUserTable(MainService
											.getInstance().zu),
											PrepareCallCheckUser.instances);
							PrepareCallCheckUser.instances
							.deleteAudioAllCheckStatu();
							callAdapter.notifyDataSetChanged();
							lv_users.setAdapter(callAdapter);
							dialog.dismiss();
						}else{
							Toast.makeText(PrepareCallCheckUser.this,
									"添加失败,改设备已存在", Toast.LENGTH_SHORT).show();
						}

					}
				}
			});

			return false;

		}
		//		if (listData.get(position).getId() == null) {
		//			showTextToast("临时处理:该用户已存在,无须添加!", 0);
		//			return false;
		//		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				PrepareCallCheckUser.this);
		builder.setItems(getResources().getStringArray(R.array.ItemArray),
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					List<UserBean> list = new ArrayList<UserBean>();
					list.add(listData.get(position));
					MainService.getInstance().setVideoUserBeans(list);
					MainService.getInstance().setAudioUserBeans(list);
					sendUDP("Video", MainService.getInstance().getVideoUserBeans().get(0).getUserIp());
					showTextToast("正在连接...", 1);
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (CameraActivity.instances == null) {
								mHandler.sendEmptyMessage(3);
							}
						}
					}).start();



				}

				if(which==1){
					Intent intent = new Intent(getApplicationContext(), com.example.tst.MainActivity.class);
					intent.putExtra("UserBean",listData.get(position));
					startActivity(intent);  
				}
				if (which == 2) {
					if (listData.get(position).getId() == null) {
						showTextToast("临时处理:该用户无法编辑删除!", 0);
						return;
					}
					String sFinal1 = String.format(getResources()
							.getString(R.string.title_what_delete),
							listData.get(position).getUsername() + "?");
					new AlertDialog.Builder(PrepareCallCheckUser.this)
					.setTitle(R.string.title_tishi)
					.setPositiveButton(
							R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									List<UserBean> lll = db
											.findUserTableByIdOrName(
													listData.get(position),
													MainService
													.getInstance().zu);
									db.deleteUserID(
											Integer.valueOf(lll
													.get(0)
													.getId()),
													MainService
													.getInstance().zu);
									Toast.makeText(
											getApplicationContext(),
											String.format(
													getResources()
													.getString(
															R.string.title_delete),
															lll.get(0)
															.getUsername()),
															Toast.LENGTH_LONG)
															.show();
									callAdapter = new PrepareCallCheckUserAdapter(
											getApplicationContext(),
											type,
											db.findUserTable(MainService
													.getInstance().zu),
													instances);
									List<UserBean> bean = new ArrayList<UserBean>();
									MainService.getInstance()
									.setAudioUserBeans(
											bean);
									callAdapter
									.notifyDataSetChanged();
									lv_users.setAdapter(callAdapter);
									dialog.dismiss();

								}
							})
							.setNegativeButton(R.string.cancel, null)
							.setMessage(sFinal1).show();
				} else if (which == 3) {
					if (listData.get(position).getId() == null) {
						showTextToast("临时处理:该用户无法编辑删除!", 0);
						return;
					}
					Toast.makeText(getApplicationContext(),R.string.btn_bianji, Toast.LENGTH_LONG).show();
					dialog.dismiss();
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.add_call_alert2, null);
					final AlertDialog editdialog = new AlertDialog.Builder(PrepareCallCheckUser.this).setTitle(R.string.title_update)
							.setIcon(android.R.drawable.ic_menu_edit)
							.setView(layout).show();
					final EditText ip = (EditText) layout.findViewById(R.id.et_ip);
					final EditText name = (EditText) layout.findViewById(R.id.et_name);
					final TextView tv_mac = (TextView) layout.findViewById(R.id.tv_mac);
					tv_mac.setEnabled(false);
					tv_mac.setText(listData.get(position).getMacAddress());
					final String id = listData.get(position).getId();
					ip.setText(listData.get(position).getUserIp());
					name.setText(listData.get(position).getUsername());
					Button ok = (Button) layout.findViewById(R.id.btn_ok);
					final String historyName = listData.get(position).getUsername();
					ok.setText(R.string.update);
					ok.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String userIp = ip.getText().toString();
							String userName = name.getText().toString();
							if (userName.isEmpty()) {
								Toast.makeText(PrepareCallCheckUser.this,R.string.name_not_null,Toast.LENGTH_SHORT).show();
							} else if (userIp.isEmpty()) {
								Toast.makeText(PrepareCallCheckUser.this,R.string.ip_not_null,Toast.LENGTH_SHORT).show();
							} else if (IPCheck.isboolIP(userIp) == false) {
								Toast.makeText(PrepareCallCheckUser.this,R.string.ip_error,Toast.LENGTH_SHORT).show();
							} else if (userName.length() > 6) {
								Toast.makeText(PrepareCallCheckUser.this,R.string.name_len_error,Toast.LENGTH_SHORT).show();
							} else if (db.findUserTableByIdOrNameAndZU(new UserBean(userName, userIp),MainService.getInstance().zu).size() > 0) {
								Toast.makeText(PrepareCallCheckUser.this,R.string.witre_update_info,Toast.LENGTH_SHORT).show();
							} else if (db.findUserTableByIdOrName(userIp,MainService.getInstance().zu).size() > 0) {
								showTextToast(getText(R.string.ip_exist), 0);
								if (!userName.equals(historyName)) {
									UserBean userBean = new UserBean(userName,userIp, id);
									userBean.setMacAddress(tv_mac.getText().toString());
									if (db.updateUserTableName(userBean,MainService.getInstance().zu) == true) {
										callAdapter = new PrepareCallCheckUserAdapter(getApplicationContext(),type,db.findUserTable(MainService.getInstance().zu),PrepareCallCheckUser.instances);
										PrepareCallCheckUser.instances.deleteAudioAllCheckStatu();
										callAdapter.notifyDataSetChanged();
										lv_users.setAdapter(callAdapter);
										showTextToast(getText(R.string.name_upadte_succ),0);
									} else {
										showTextToast(getText(R.string.name_upadte_error),0);
									}
								}
								editdialog.dismiss();
							} else {
								UserBean userBean = new UserBean(userName,userIp, id);
								userBean.setMacAddress(tv_mac.getText().toString());
								db.updateUserTable(userBean,
										MainService.getInstance().zu);
								showTextToast(
										getText(R.string.upadte_succ),
										0);
								callAdapter = new PrepareCallCheckUserAdapter(
										getApplicationContext(), type,
										db.findUserTable(MainService
												.getInstance().zu),
												instances);
								PrepareCallCheckUser.instances
								.deleteAudioAllCheckStatu();
								callAdapter.notifyDataSetChanged();
								lv_users.setAdapter(callAdapter);
								editdialog.dismiss();
							}
						}
					});

				} else if (which == 4) {
					// Toast.makeText(getApplicationContext(), "取消",
					// Toast.LENGTH_LONG).show();
					dialog.dismiss();

				}
			}

		});
		builder.show();
		return false;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		try {
			MyHolder myHolder = (MyHolder) view.getTag();
			myHolder.cb.toggle();
			PrepareCallCheckUserAdapter.getIsSelected().put(position,
					myHolder.cb.isChecked());
			if (myHolder.cb.isChecked() == true) {
				//			myHolder.ll_bg.setBackgroundColor(Color.GREEN);
				myHolder.tv_userName.setTextColor(Color.GREEN);
				myHolder.tv_ip.setTextColor(Color.GREEN);
				checkNum++;
				if (type == 0) {
					if (!deviceIp.equals(myHolder.tv_ip.getText())) {// 不是本机就添加
						UserBean userBean =new UserBean(
								myHolder.tv_userName.getText().toString(),
								myHolder.tv_ip.getText().toString());
						userBean.setMacAddress(myHolder.tv_mac.getText().toString());
						setAudioCheckStatu(userBean);
					}
				} else {
					if (!deviceIp.equals(myHolder.tv_ip.getText())) {// 不是本机就添加
						UserBean userBean =new UserBean(
								myHolder.tv_userName.getText().toString(),
								myHolder.tv_ip.getText().toString());
						userBean.setMacAddress(myHolder.tv_mac.getText().toString());
						setVideoCheckStatu(userBean);
					}
				}
			} else {
				//			myHolder.ll_bg.setBackgroundColor(Color.BLACK);
				myHolder.tv_userName.setTextColor(Color.GRAY);
				myHolder.tv_ip.setTextColor(Color.GRAY);
				checkNum--;
				try {
					if (type == 0) {
						instances.deleteAudioPositionCheckStatu(new UserBean(
								myHolder.tv_userName.getText().toString(),
								myHolder.tv_ip.getText().toString()));
					} else {
						instances.deleteVideoPositionCheckStatu(new UserBean(
								myHolder.tv_userName.getText().toString(),
								myHolder.tv_ip.getText().toString()));
					}
				} catch (Exception e) {
				}
			}
			if (type == 0) {

				tv_show.setText(String.format(
						getResources().getString(R.string.selete_people), instances
						.getAudioCheckStatu().size()));
			} else {
				tv_show.setText(String.format(
						getResources().getString(R.string.selete_people), instances
						.getVideoCheckStatu().size()));
			}

			for(OnLineBean ls :listmsg){//查询列表IP是否属于在线
				if(ls.getIp().equals(myHolder.tv_ip.getText())){
					myHolder.tv_userName.setTextColor(Color.RED);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (v == btn_z1) {
			new AlertDialog.Builder(PrepareCallCheckUser.this)
			.setTitle("是否清空组1联系人？")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					db.deleteALL(1);
					callAdapter.setList(new ArrayList<UserBean>());
					callAdapter.notifyDataSetChanged();
				}
			}).setNegativeButton("取消", null).create().show();
		}
		if (v == btn_z2) {
			new AlertDialog.Builder(PrepareCallCheckUser.this)
			.setTitle("是否清空组2联系人？")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					db.deleteALL(2);
					callAdapter.setList(new ArrayList<UserBean>());
					callAdapter.notifyDataSetChanged();
				}
			}).setNegativeButton("取消", null).create().show();
		}
		if (v == btn_z3) {
			new AlertDialog.Builder(PrepareCallCheckUser.this)
			.setTitle("是否清空组3联系人？")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					db.deleteALL(3);
					callAdapter.setList(new ArrayList<UserBean>());
					callAdapter.notifyDataSetChanged();
				}
			}).setNegativeButton("取消", null).create().show();
		}
		return false;
	}

	@Override
	public void OnButtonChangeListener(boolean statu, String info) {
		setSpeakWifi(statu, info);
	}






	public void setList(UserBean user) {
		for(int i = 0 ; i <callAdapter.listView_List.size();i++ ){
			if(callAdapter.listView_List.get(i).getMacAddress().equals(user.getMacAddress())){
				callAdapter.listView_List.get(i).setLocation(user.getLocation());
				callAdapter.listView_List.get(i).setUserIp(user.getUserIp());
				callAdapter.notifyDataSetChanged();
				break;
			}
		}
		
		List<UserBean>  list = getAudioCheckStatu();
		for(int j = 0 ; j < list.size();j++){
			if(list.get(j).getMacAddress().equals(user.getMacAddress())){
				list.get(j).setUserIp(user.getUserIp());
				break;
			}
		}
	}
	
	
	public void addList(UserBean user){
		callAdapter.listView_List.add(user);
//		callAdapter.getIsSelected().put(callAdapter.listView_List.size()-1, true);
//		callAdapter.notifyDataSetChanged();
		setListAdapter();
	}

}
