package com.example.music;

import com.example.myapp.AudioRecordDemoTest;
import com.ramy.minervue.R;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MusicMainActivity extends Activity implements OnClickListener {
	public static int db_values = 0;
	SharedPreferences sp;
	TextView tv_db;
	Button btn_1;
	EditText et_1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_avtivity);
		sp = getSharedPreferences("db_info", 0);
		db_values = Integer.valueOf(sp.getString("db", "50"));
		tv_db = (TextView) findViewById(R.id.tv_db);
		et_1 = (EditText) findViewById(R.id.et_1);
		et_1.setText(db_values+"");
		btn_1 =  (Button) findViewById(R.id.btn_1);
		btn_1.setOnClickListener(this);
		AudioRecordDemoTest demo = new AudioRecordDemoTest(handler,this);
		demo.getNoiseLevel();
	}
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1){
				tv_db.setText((String)msg.obj);
			}
		};
	};

	@Override
	public void onClick(View v) {
		if(v==btn_1){
			String value = et_1.getText().toString();
			try {
				if(Integer.valueOf(value)>40&&Integer.valueOf(value)<80){
					sp.edit().putString("db", value).commit();
					db_values = Integer.valueOf(value);
					Toast.makeText(getApplicationContext(), "设置成功:"+value, 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "DB 参数必须是40至80之间", 0).show();
				}
			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(), "DB 参数必须是40至80之间,并且为整数", 0).show();
			}
		}
	}



}
