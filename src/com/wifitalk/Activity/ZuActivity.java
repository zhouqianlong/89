package com.wifitalk.Activity;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;
import com.wifitalk.adapter.PrepareCallCheckUserAdapter;
import com.wifitalk.adapter.PrepareCallCheckUserAdapter.MyHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ZuActivity extends Activity implements OnClickListener{
	private PrepareCallCheckUserAdapter zu1Adapter;
	private Button btn_queding;
	private DBHelper db = new DBHelper(this);
	private int zu = 0;
	private List<UserBean> checkStatu; 
	private TextView tv_refresh;
	private GridView  gv_zu1;
	private int type =0;//类型  默认语音
	private  List<UserBean> list = new ArrayList<UserBean>();
	public static ZuActivity getInstances;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getInstances = this;
		setContentView(R.layout.bj_prepare_call_check_user_activity);
		tv_refresh = (TextView) findViewById(R.id.tv_refresh);
		tv_refresh.setOnClickListener(this);
		zu = getIntent().getExtras().getInt("user_zu", -1);
		btn_queding = (Button) findViewById(R.id.btn_queding);
		gv_zu1 = (GridView) findViewById(R.id.gv_zu1);
		PrepareCallCheckUser.instances.deleteAudioAllCheckStatu();
		btn_queding.setOnClickListener(this);
		type = getIntent().getExtras().getInt("type");
		if(type == 0){
			setTitle(getString(R.string.app_name)+"-语音对讲");
		}else{
			setTitle(getString(R.string.app_name)+"-视频对讲");
		}
		gv_zu1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				MyHolder myHolder = (MyHolder) view.getTag();
				myHolder.cb.toggle();
				PrepareCallCheckUserAdapter.getIsSelected().put(position,myHolder.cb.isChecked());
				if (myHolder.cb.isChecked() == true) {
					myHolder.tv_userName.setTextColor(Color.GREEN);
					myHolder.tv_ip.setTextColor(Color.GREEN);
					UserBean user = new UserBean(myHolder.tv_userName.getText().toString(), myHolder.tv_ip.getText().toString());
					user.setMacAddress(myHolder.tv_mac.getText().toString());
					PrepareCallCheckUser.instances.setAudioCheckStatu(user);
				}else{
					myHolder.tv_userName.setTextColor(Color.WHITE);
					myHolder.tv_ip.setTextColor(Color.WHITE);
					PrepareCallCheckUser.instances.deleteAudioPositionCheckStatu(new UserBean(myHolder.tv_userName.getText().toString(), myHolder.tv_ip.getText().toString()));
				}
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(zu>=1&&zu<=3){
			refersh();
		}else{
			Toast.makeText(getApplicationContext(), "用户组不存在！", 0).show();
			finish();
		}
	}
	@Override 
	public void onClick(View v) {
		if(v==btn_queding){//确定添加
			checkStatu = PrepareCallCheckUser.instances.getAudioCheckStatu();
			if(checkStatu.size()==0){
				showTextToast( "请选择联系人", 0);
			}else{
				new AlertDialog.Builder(ZuActivity.this).setTitle("是否添加"+checkStatu.size()+"位联系人？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkStatu = PrepareCallCheckUser.instances.getAudioCheckStatu();
						StringBuffer sb = new StringBuffer();
						boolean isFail = false;
						for(UserBean sh :checkStatu){
							if(db.saveUserTable(sh,zu)==false){
								sb.append(sh.getUserIp()+"\n");
								isFail = true;
							}
						}
						if(isFail){
							showTextToast( sb.toString()+"设备已存在,无法添加", 1);
						}
						PrepareCallCheckUser.instances.deleteAudioAllCheckStatu();
						finish();
					}
				}).setNegativeButton("取消", null).create().show();
			}

		}
		if(v== tv_refresh){

			refersh();
		}


	}

	private void refersh() {
		Toast.makeText(getApplicationContext(), "正在刷新...", 0).show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramSocket clientSocket=null;
				try {
					clientSocket = new DatagramSocket();
					StringBuffer str = new StringBuffer("online:"+MainService.getInstance().getUUID());
					DataPacket dataPacket = new DataPacket(str.toString().getBytes(), new byte[]{01,01,01});
					String [] ips = MainService.getInstance().getReceiveSoundsThread().deviceIps;
					clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length, InetAddress.getByName("255.255.255.255"), AppConfig.PortAudio));
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(clientSocket!=null){
						clientSocket.close();
					}
				}
			}
		}).start();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getInstances = null;
		setResult(zu);   
		finish();   

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

	public void addOnlineList(String ip ,String name,String mac){
		for(int i = 0 ; i < list.size();i++){
			if(list.get(i).getUserIp().equals(ip)){
				return;
			}
		}
		UserBean user =new UserBean( name,ip);
		user.setMacAddress(mac);
		list.add(user);
		if(zu1Adapter==null){
			zu1Adapter = new PrepareCallCheckUserAdapter(getApplicationContext(), type, list,PrepareCallCheckUser.instances);
			gv_zu1.setAdapter(zu1Adapter);
		}else{
			zu1Adapter.setOnline(list);
		}
		Log.i("WifiConfig", "name："+name);
	}
}
