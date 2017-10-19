package com.ramy.minervue.app;

import com.ramy.minervue.R;
import com.ramy.minervue.bean.UsersBean;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.util.ConfigUtil;
import com.wifitalk.Utils.IPCheck;

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
import android.widget.Spinner;
import android.widget.Toast;

public class AddUserActivity extends Activity implements OnClickListener{
	EditText et_username,et_address,et_id,et_address2,et_address3;
	Button btn_complete,btn_connwifi,btn_gaoji;
	SharedPreferences settings;
	int rundomID = 0;
	private EditText et_wifidb;
	private LinearLayout ll_gaoji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_user_activty);
		settings = getSharedPreferences("AddressManagerActivity", 0);
		et_username = (EditText) findViewById(R.id.et_username);
		ll_gaoji = (LinearLayout) findViewById(R.id.ll_gaoji);
		et_id = (EditText) findViewById(R.id.et_id);
		et_address = (EditText) findViewById(R.id.et_address);
		et_address2 = (EditText) findViewById(R.id.et_address2);
		et_address3 = (EditText) findViewById(R.id.et_address3);
		et_wifidb = (EditText) findViewById(R.id.et_wifidb);
		btn_complete = (Button) findViewById(R.id.btn_complete);
		btn_gaoji = (Button) findViewById(R.id.btn_gaoji);
		ll_gaoji.setVisibility(View.GONE);
		btn_complete.setOnClickListener(this);
		btn_gaoji.setOnClickListener(this);
		rundomID = (int) ((Math.random()*255)+1);
		et_id.setText(""+rundomID);
		et_username.setText("TK_"+rundomID);
//		et_address.setText("10.10.10.255");
		
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
		
		if(btn_gaoji==v){
			btn_gaoji.setVisibility(View.GONE);
			ll_gaoji.setVisibility(View.VISIBLE);
		}
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
								
								Intent intent = new Intent(getApplicationContext(), StaticIpActivity.class);
								intent.putExtra("xinhaobaohu", et_wifidb.getText().toString());
								intent.putExtra("deriverid", et_id.getText().toString());
								intent.putExtra("ip", addresss[0]+"."+addresss[1]+"."+addresss[2]+"."+et_id.getText().toString());
								intent.putExtra("serverip", et_address.getText().toString());
								intent.putExtra("username", et_username.getText().toString());
								intent.putExtra("wangguan", et_address2.getText().toString());
								intent.putExtra("yuming", et_address3.getText().toString());
								
								DBHelper dbHelper = new DBHelper(getApplicationContext());
								UsersBean usersBean = new UsersBean();
								usersBean.driverId = deriverid;
								usersBean.serverIp = serverip;
								usersBean.username = username;
								usersBean.wangguan = wangguan;
								usersBean.yuming = yuming;
								usersBean.xinhaobaohu = xinhaobaohu;//信号保护
								dbHelper.AddUsers(usersBean);
								
								startActivity(intent);
								
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
