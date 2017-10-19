package com.ramy.minervue.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.adapter.SimpleSpinnerAdapter;
import com.ramy.minervue.bean.UsersBean;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.util.ConfigUtil;

public class AddressManagerActivity extends Activity  implements OnClickListener,OnItemSelectedListener{
	public int position;
	public int dbPostion;
	private TextView et_server_address;//服务器地址
	private TextView et_server_port;//端口
	private TextView et_mac;//mac
	private TextView et_server_video_port;//视频端口
	private TextView et_server_audio_port;//音频端口
	private TextView et_driver_name;//设备名称
	private TextView et_wifidb;//信号强度保护
	private TextView tv_version;
	private RadioGroup rg_FTP=null;//FTP上传
	private Spinner sp_users;
	Button btn_add;
	RadioButton rbtn_upload,rbtn_noUpload;
	Button btn_result,btn_delete,btn_confim;
	Button btn_update;
	
	private DBHelper dbHelper;
	private List<UsersBean> userList;
	private List<String>  userStrings;
	private SimpleSpinnerAdapter user_adapter;
	private SharedPreferences settings;	

	public List<String> getUserStrs(){
		userStrings = new ArrayList<String>();
		if(userList==null)
			return userStrings;
		for(int i = 0 ; i< userList.size();i++){
			userStrings.add(userList.get(i).username);
		}
		return userStrings;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.address_manager);
		settings = getSharedPreferences("AddressManagerActivity", 0);
		this.et_server_address = (TextView) findViewById(R.id.et_server_address);
		this.et_server_port = (TextView) findViewById(R.id.et_server_port);
		this.et_server_video_port = (TextView) findViewById(R.id.et_server_video_port);
		this.et_server_audio_port = (TextView) findViewById(R.id.et_server_audio_port);
		this.et_mac = (TextView) findViewById(R.id.et_mac);
		this.et_mac.setEnabled(false);
		this.et_mac.setText(MainService.getInstance().getMacAddressLastSix());
		this.et_driver_name = (TextView) findViewById(R.id.et_driver_name);
		this.et_wifidb = (TextView) findViewById(R.id.et_wifidb);
		this.rg_FTP = (RadioGroup) findViewById(R.id.rg_FTP);
		this.rbtn_upload= (RadioButton) findViewById(R.id.rbtn_upload);
		this.btn_update = (Button) findViewById(R.id.btn_update);
		this.btn_update.setOnClickListener(this);
		this.rbtn_noUpload= (RadioButton) findViewById(R.id.rbtn_noUpload);
		this.tv_version = (TextView) findViewById(R.id.tv_version);
		this.btn_add = (Button) findViewById(R.id.btn_add);
		this.btn_delete = (Button) findViewById(R.id.btn_delete);
		this.sp_users = (Spinner) findViewById(R.id.sp_users);
		this.btn_add.setOnClickListener(this);
		this.btn_delete.setOnClickListener(this);
		this.btn_confim = (Button) findViewById(R.id.btn_confim);
		this.btn_confim.setOnClickListener(this);
		dbHelper =new DBHelper(getApplicationContext());

		sp_users.setOnItemSelectedListener(this);
		setAdapter();


		//		this.btn_init = (Button) findViewById(R.id.btn_init);
		PackageManager manager = getPackageManager();
		String version = "";
		try {
			version = manager.getPackageInfo(getPackageName(), 0).versionName;
			String title = "v" + version+"" ;
			tv_version.setText(title);
		} catch (Exception e) {
			// Ignore.
		}
		//		rg_FTP.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		//			@Override
		//			public void onCheckedChanged(RadioGroup group, int checkedId) {
		//				if(rbtn_upload.getId()==checkedId){//上传
		//					settings.edit().putString("rg_FTP", "上传").commit();  
		////					MainService.rg_FTP ="上传";
		//					//					showTextToast("设置成功：上传数据", 0);
		//				}else{//不上传
		//					settings.edit().putString("rg_FTP", "不上传").commit();  
		////					MainService.rg_FTP ="不上传";
		//					showTextToast("设置成功：不上传数据", 0);
		//				}
		//
		//			}
		//		});

		//		initData(settings);
		//		btn_save = (Button) findViewById(R.id.btn_save);
		//		btn_result = (Button) findViewById(R.id.btn_result);
		//		btn_init.setOnClickListener(new OnClickListener() {
		//			
		//			@Override
		//			public void onClick(View v) {
		//				startActivity(new Intent(getApplicationContext(), WelecomeActivity.class));
		//			}
		//		});
		//		btn_result.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				settings.edit().putString("et_server_address", MainService.getInstance().getServerAddr()).commit();  //服务器地址
		////				settings.edit().putString("et_server_port", "8000").commit();  //端口
		////				settings.edit().putString("et_server_video_port", "8100").commit();  //视频端口
		////				settings.edit().putString("et_server_audio_port", "8200").commit();  //音频端口
		//				settings.edit().putString("et_driver_name", MainService.getInstance().getUUID()+getRanDom()).commit();  //设备名称
		////				method1("8001&&8101&&8201");
		//				showTextToast("重置成功！", 0);
		//				initData(settings);
		//			}
		//		});
		//		btn_save.setOnClickListener(new OnClickListener() {
		//			@Override
		//			public void onClick(View v) {
		//				if(et_server_address.getText().toString().trim().equals("")){
		//					showTextToast("服务器地址不能为空!", 0);
		//					return;
		//				}
		//				if(et_server_port.getText().toString().trim().equals("")){
		//					showTextToast("端口不能为空!", 0);
		//					return;
		//				}
		//				if(et_server_video_port.getText().toString().trim().equals("")){
		//					showTextToast("视频端口不能为空!", 0);
		//					return;
		//				}
		//				if(et_server_audio_port.getText().toString().trim().equals("")){
		//					showTextToast("音频端口不能为空!", 0);
		//					return;
		//				}
		//				if(et_driver_name.getText().toString().trim().equals("")){
		//					showTextToast("设备名称不能为空!", 0);
		//					return;
		//				}
		//				if(et_wifidb.getText().toString().trim().equals("")){
		//					showTextToast("信号强度保护不能为空!", 0);
		//					return;
		//				}else if(isNumberSrc(et_wifidb.getText().toString())==false){
		//					showTextToast("信号强度保护数据格式必须为数字!", 0);
		//					return;
		//				}
		//				settings.edit().putString("et_server_address", et_server_address.getText().toString()).commit();  //服务器地址
		////				settings.edit().putString("et_server_port", et_server_port.getText().toString()).commit();  //端口
		////				settings.edit().putString("et_server_video_port", et_server_video_port.getText().toString()).commit();  //视频端口
		////				settings.edit().putString("et_server_audio_port", et_server_audio_port.getText().toString()).commit();  //音频端口
		//				settings.edit().putString("et_driver_name", et_driver_name.getText().toString()).commit();  //设备名称
		//				settings.edit().putString("et_wifidb", et_wifidb.getText().toString()).commit();  //设备名称
		//				MainActivity.DBERROR = Integer.valueOf(et_wifidb.getText().toString());
		//				showTextToast(getText(R.string.ok)+"！", 0);
		//				
		//				ConfigUtil.changeFile( et_server_address.getText().toString(),et_driver_name.getText().toString());
		//				finish();
		//			}
		//		});

	}

	private void setAdapter() {
		userList = dbHelper.selectUsers();
		user_adapter = new SimpleSpinnerAdapter(getUserStrs(),getApplicationContext());
		sp_users.setAdapter(user_adapter);
		String username  =  settings.getString("et_driver_name", MainService.getInstance().getUUID());
		String serverIp  =  settings.getString("et_server_address", MainService.getInstance().getServerAddr());
		
		for(int i= 0 ; i< userList.size();i++){
			if(username.equals(userList.get(i).username)&&serverIp.equals(userList.get(i).serverIp)){
				sp_users.setSelection(i);
			}
		}
	}

	public boolean isNumberSrc(String num){
		try {
			Integer.valueOf(num);
		} catch (Exception e) {
			return false;
		}
		return true;

	}
	private void initData(final SharedPreferences settings) {
		//设置初始化值
		et_server_address.setText(settings.getString("et_server_address",  MainService.getInstance().getServerAddr()));
		HashMap<String, String> map = getString();
		String serverPort  = map.get("serverPort");
		String videoPort  = map.get("videoPort");
		String audioPort  = map.get("audioPort");
		if(serverPort==null||videoPort==null||audioPort==null){
			serverPort ="6000";
			videoPort ="6100";
			audioPort ="6200";
		}else{

		}
		et_server_port.setText(serverPort);
		et_server_video_port.setText(videoPort);
		et_server_audio_port.setText(audioPort);
		et_driver_name.setText(settings.getString("et_driver_name", MainService.getInstance().getUUID()));
		et_wifidb.setText(settings.getString("et_wifidb", "-80"));

		if(settings.getString("rg_FTP", "上传").endsWith("不上传")){
			rbtn_noUpload.setChecked(true);
		}else{
			rbtn_upload.setChecked(true);
		}
	}

	public int  getRanDom(){
		return (int)( Math.random()*1000);
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode==67){
			//			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	public static void method1(String conent) {  
		BufferedWriter out = null;  
		try {  
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("sdcard/TK.txt", false)));  
			out.write(conent);  
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

	@SuppressWarnings("resource")
	public static HashMap<String, String> getString() {  
		HashMap<String, String> map = new HashMap<String, String>();
		InputStreamReader inputStreamReader = null;  
		try {  
			InputStream ism =  new FileInputStream(new File("sdcard/TK.txt")); 
			inputStreamReader = new InputStreamReader(ism, "gbk");  
			StringBuffer sb = new StringBuffer("");  
			String line;  
			BufferedReader reader = new BufferedReader(inputStreamReader);  
			while ((line = reader.readLine()) != null) {  
				sb.append(line);  
			}  
			String [] conent  = sb.toString().split("&&");
			if(conent.length==3){
				map.put("serverPort", conent[0]);
				map.put("videoPort", conent[1]);
				map.put("audioPort", conent[2]);
			}
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
		return map;  
	}

	@Override
	public void onClick(View v) {
		if(v==btn_update){
			Intent intent = new Intent(getApplicationContext(), UpdateUserActivity.class);
			intent.putExtra("dbPostion",dbPostion);
			startActivity(intent);
		
		}
		if(v==btn_add){
			startActivity(new Intent(getApplicationContext(), AddUserActivity.class));
		}
		
		if(v==btn_delete){

			 AlertDialog.Builder builder = new AlertDialog.Builder(AddressManagerActivity.this);
			 builder.setTitle("警告");
			 builder.setMessage("您确定删除该用户?");
			 builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
			  }
			 });
			 builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
					 
					dbHelper.deleteUsers(dbPostion);
					setAdapter();
			  }
			 });
			 AlertDialog dialog = builder.create();
			 dialog.show();
			 
		
			
		}
		if(btn_confim ==v ){
			try {
				Intent intent = new Intent(getApplicationContext(), StaticIpActivity.class);
				intent.putExtra("xinhaobaohu", et_wifidb.getText().toString());
				intent.putExtra("deriverid", userList.get(position).driverId);
				String[] addresss = userList.get(position).serverIp.split("\\.");
				intent.putExtra("ip", addresss[0]+"."+addresss[1]+"."+addresss[2]+"."+userList.get(position).driverId);
				intent.putExtra("serverip", userList.get(position).serverIp);
				intent.putExtra("username", userList.get(position).username);
				intent.putExtra("wangguan", userList.get(position).wangguan);
				intent.putExtra("yuming", userList.get(position).yuming);
				SharedPreferences settings = getSharedPreferences("AddressManagerActivity", 0);
				settings.edit().putString("et_server_address", userList.get(position).serverIp).commit();  //服务器地址
				settings.edit().putString("et_driver_name", userList.get(position).username);  //设备名称
				settings.edit().putBoolean("init", false).commit();  //是否是第一次
				ConfigUtil.changeFile( userList.get(position).serverIp,userList.get(position).username);
				settings.edit().putString("et_wifidb",et_wifidb.getText().toString()).commit();  
				MainActivity.DBERROR = Integer.valueOf(et_wifidb.getText().toString());
				startActivity(intent);
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setAdapter();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		this.position = position;
		dbPostion = userList.get(position).id;
		et_driver_name.setText(userList.get(position).username);
		et_wifidb.setText(userList.get(position).xinhaobaohu);
		et_server_address.setText(userList.get(position).serverIp);
		et_mac.setText(MainService.getInstance().getMacAddressLastSix());
		

	 
	
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}  



}
