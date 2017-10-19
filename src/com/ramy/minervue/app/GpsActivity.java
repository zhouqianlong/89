package com.ramy.minervue.app;  


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import we_smart.com.data.PollingInfo;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.bean.BlueBean;
import com.ramy.minervue.bean.Dervic;
import com.ramy.minervue.bean.XY;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.util.FileUtils;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;
import com.wifitalk.Utils.GpsView;

public class GpsActivity extends Activity {  



	private Button button,button_add,button_delete;  
	public TextView textview;  
	public static boolean statu = true;
	//	private LocationManager manager;  
	private EditText et_x,et_y;
	//	private Location location;  
	private DBHelper db = new DBHelper(this);
	private List<UserBean> list = null;
	Timer timer = new Timer();
	String jsonData;
	public static GpsActivity instances = null;
	private RadioGroup radio_group_music;
	private RadioButton rb_open;
	private RadioButton rb_close;
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		instances = this;
		setContentView(R.layout.gpsmain);  
		radio_group_music = (RadioGroup) findViewById(R.id.radio_group_music);
		rb_open = (RadioButton) findViewById(R.id.rb_open);
		rb_close = (RadioButton) findViewById(R.id.rb_close);
		radio_group_music.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(rb_open.getId()==checkedId){
					statu = true;
				}else{
					statu = false;
				}
			}
		});
		if(statu){
			rb_open.setChecked(true);
		}else{
			rb_close.setChecked(true);
		}

		list = MainService.getInstance().getDB_ZU_ALL_List();
		if(list==null){
			finish();
			return;
		}

		timer.schedule(QuestlongitudeAndlatitudeTask, 0, 3000);//延时0ms后执行，100ms执行一次  Task
		textview=(TextView)findViewById(R.id.textview);  
		textview.setText("经度:"+MainActivity.getInstance.getLongitude()+"\n"+"纬度:"+MainActivity.getInstance.getLatitude());

		button=(Button)findViewById(R.id.button);  
		button_add=(Button)findViewById(R.id.button_add);  
		button_delete=(Button)findViewById(R.id.button_delete);  
		button_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GpsView.getInstances.amplify();
			}
		});
		button_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GpsView.getInstances.narrow();				
			}
		});
		et_x=(EditText)findViewById(R.id.et_x);  
		et_y=(EditText)findViewById(R.id.et_y); 
		button.setOnClickListener(new OnClickListener() {  

			@Override  
			public void onClick(View v) {  

				Toast.makeText(getApplicationContext(), "add", 0).show();
				if(!et_x.getText().toString().trim().equals("")&&!et_y.getText().toString().trim().equals("")){
					XY xy = new XY(Float.valueOf(et_x.getText().toString()),Float.valueOf(et_y.getText().toString()),"192.168.1.1","测试");
					GpsView.getInstances.addDrivice(xy);
				}

			}  
		});  
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {

						mHandler.post(new Runnable() {

							@Override
							public void run() {
								List<PollingInfo> blueBeans = MainService.getInstance().mBeaconList; 
								if(blueBeans==null){
									textview.setText("无定位点");
									return;
								}
								if(blueBeans.size()==0){
									textview.setText("无定位点");
								}
								StringBuffer sb = new StringBuffer();
								for(int i = 0 ; i < blueBeans.size();i++){
									MainService.getInstance().mBeaconList.get(i).lastPollingTime = MainService.getInstance().getDesByMac(blueBeans.get(i).mac);
									sb.append(blueBeans.get(i).mac+"="+ MainService.getInstance().getDesByMac(blueBeans.get(i).mac)+"="+blueBeans.get(i).risi+"db="+blueBeans.get(i).battery+"% \n");
								}
								textview.setText(sb.toString()+"\n");
							}
						});
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		}).start();

	}  


	Handler mHandler = new Handler(){};

	private TimerTask QuestlongitudeAndlatitudeTask = new TimerTask(){

		public void run() {	
			DatagramSocket clientSocket=null;
			try {
				clientSocket = new DatagramSocket();
				StringBuffer str = new StringBuffer("QuestlongitudeAndlatitude:"+MainService.getInstance().getIPadd());
				// 构建数据包 头+体
				DataPacket dataPacket = new DataPacket(str.toString().getBytes(), new byte[]{01,01,01});
				//// 构建数据报 +发送
				for(int i = 0 ; i < list.size();i++){
					clientSocket.send(new DatagramPacket(dataPacket.getAllData(),
							dataPacket.getAllData().length,InetAddress.getByName(list.get(i).getUserIp()), AppConfig.PortAudio));
					Log.i("111", "send:"+list.get(i).getUserIp());
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(clientSocket!=null){
					clientSocket.close();
				}
			}
		}
	};



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MainActivity.getInstance.setGPSCallBackListener(null);
		timer.cancel();
		QuestlongitudeAndlatitudeTask.cancel();
		instances = null;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}





}  