package com.ramy.minervue.app;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.bean.UsersBean;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.util.ConfigUtil;
import com.wifitalk.Utils.IPCheck;

public class UpdateUserActivity extends Activity implements OnClickListener{
	EditText et_username,et_address,et_id,et_address2,et_address3;
	Button btn_complete,btn_connwifi;
	SharedPreferences settings;
	private EditText et_wifidb;
	private DBHelper dbHelper;
	int dbPostion;
	private UsersBean userbean = null;
	private List<UsersBean> userList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_user_activty);
		dbPostion = getIntent().getExtras().getInt("dbPostion");
		dbHelper = new DBHelper(getApplicationContext());
//		userbean =  dbHelper.selectUsers();
		userList =  dbHelper.selectUsers();

		for(int i= 0 ; i< userList.size();i++){
			if(userList.get(i).id == dbPostion){
				userbean = userList.get(i);
			}
		}
		if(userbean==null){
			Toast.makeText(getApplicationContext(), "无法修改改用户", 0).show();
			finish();
			return;
		}

		settings = getSharedPreferences("AddressManagerActivity", 0);
		et_username = (EditText) findViewById(R.id.et_username);
		et_id = (EditText) findViewById(R.id.et_id);
		et_address = (EditText) findViewById(R.id.et_address);
		et_address2 = (EditText) findViewById(R.id.et_address2);
		et_address3 = (EditText) findViewById(R.id.et_address3);
		et_wifidb = (EditText) findViewById(R.id.et_wifidb);
		btn_complete = (Button) findViewById(R.id.btn_complete);
		btn_complete.setOnClickListener(this);
		et_id.setText(userbean.driverId+"");
		et_username.setText(userbean.username);
		et_address.setText(userbean.serverIp);
		et_address2.setText(userbean.wangguan);
		et_address3.setText(userbean.yuming);
		et_wifidb.setText(userbean.xinhaobaohu);
		et_address.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				if(StaticIpActivity.isboolIP(s.toString())==true){
					String[] ips = s.toString().split("\\.");
					et_address2.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
					et_address3.setText(ips[0]+"."+ips[1]+"."+ips[2]+".1");
				}else{
					et_address2.setHint("请输入网关");
					et_address3.setHint("请输入域名");
				}
			}
		});
	}
	@Override
	public void onClick(View v) {

		if(btn_complete==v){

			if(et_id.getText().toString().equals("")){
				Toast.makeText(getApplicationContext(), "设备ID范围0-255", 0).show();
				return;
			}
			boolean checkId = false;
			try {
				checkId = Integer.valueOf(et_id.getText().toString())>=0&&Integer.valueOf(et_id.getText().toString())<=255;
			} catch (Exception e) {
			}
			if(checkId==false){
				Toast.makeText(getApplicationContext(), "设备ID范围0-255", 0).show();
				return;
			}
			if(et_username.getText().toString().equals("")){
				Toast.makeText(getApplicationContext(), "设备名不能为空", 0).show();
				return;
			}
			if(et_address.getText().toString().equals("")){
				Toast.makeText(getApplicationContext(), "服务器地址不能为空", 0).show();
				return;
			}else if(IPCheck.isboolIP(et_address.getText().toString())==false){
				Toast.makeText(getApplicationContext(),
						R.string.ip_error, Toast.LENGTH_SHORT).show();
				return;
			}else if(IPCheck.isboolIP(et_address2.getText().toString())==false){

				Toast.makeText(getApplicationContext(),
						"网关地址错误", Toast.LENGTH_SHORT).show();
				return;


			}else if(IPCheck.isboolIP(et_address3.getText().toString())==false){

				Toast.makeText(getApplicationContext(),
						"域名地址错误", Toast.LENGTH_SHORT).show();
				return;


			}else	if(et_wifidb.getText().toString().trim().equals("")){
				Toast.makeText(getApplicationContext(),
						"信号强度保护不能为空!", Toast.LENGTH_SHORT).show();
				return;
			}else if(isNumberSrc(et_wifidb.getText().toString())==false){
				Toast.makeText(getApplicationContext(),
						"信号强度保护数据格式必须为数字!", Toast.LENGTH_SHORT).show();
				return;
			}else{
				String[] addresss = et_address.getText().toString().split("\\.");
				if(et_id.getText().toString().equals(addresss[3])){
					Toast.makeText(getApplicationContext(),
							addresss[3]+"是服务器ID,请修改后确定", Toast.LENGTH_SHORT).show();
				}
				//								settings.edit().putString("et_server_address", et_address.getText().toString()).commit();  //服务器地址
				//								settings.edit().putString("et_driver_name", et_username.getText().toString()).commit();  //设备名称
				//								settings.edit().putBoolean("init", false).commit();  //是否是第一次
				//								ConfigUtil.changeFile( et_address.getText().toString(),et_username.getText().toString());
				//								Toast.makeText(getApplicationContext(), "保存成功", 0).show();
				//								finish();

				String deriverid = et_id.getText().toString();
				String serverip = et_address.getText().toString();
				String username =et_username.getText().toString();
				String wangguan = et_address2.getText().toString();
				String yuming = et_address3.getText().toString();
				String xinhaobaohu = et_wifidb.getText().toString();
				userbean.driverId = deriverid;
				userbean.serverIp = serverip;
				userbean.username = username;
				userbean.wangguan = wangguan;
				userbean.yuming = yuming;
				userbean.xinhaobaohu = xinhaobaohu;
				DBHelper dbHelper = new DBHelper(getApplicationContext());
				dbHelper.updateUser(userbean);
				
				SharedPreferences settings = getSharedPreferences("AddressManagerActivity", 0);
				settings.edit().putString("et_server_address", serverip).commit();  //服务器地址
				settings.edit().putString("et_driver_name",username);  //设备名称
				settings.edit().putBoolean("init", false).commit();  //是否是第一次
				ConfigUtil.changeFile(serverip,username);
				settings.edit().putString("et_wifidb",et_wifidb.getText().toString()).commit();  
				MainActivity.DBERROR = Integer.valueOf(et_wifidb.getText().toString());
				finish();
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




}
