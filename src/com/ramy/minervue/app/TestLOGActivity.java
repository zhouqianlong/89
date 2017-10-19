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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.ramy.minervue.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestLOGActivity extends Activity {
	TextView tv_logmsg;
	Button btn_deletelog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testlog_activity);
		tv_logmsg = (TextView) findViewById(R.id.tv_logmsg);
		btn_deletelog = (Button) findViewById(R.id.btn_deletelog);
		tv_logmsg.setText(getLog());
		btn_deletelog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				File file = new File("/sdcard/不要删除ERRORTEST.txt");
				file.delete();
				tv_logmsg.setText(getLog());
			}
		});
	}
 
	@SuppressWarnings("resource")
	public  String getLog() {  
		InputStreamReader inputStreamReader = null;  
		try {  
			InputStream ism =  new FileInputStream(new File("/sdcard/不要删除ERRORTEST.txt")); 
			inputStreamReader = new InputStreamReader(ism, "UTF-8");  //编码gbk
			StringBuffer sb = new StringBuffer("");  //接收buferr
			String line;  //一次读一行
			BufferedReader reader = new BufferedReader(inputStreamReader);  
			while ((line = reader.readLine()) != null) {  
				sb.append(line+"\n");  //一次拼一行
			}  
			return sb.toString();  
		} catch (IOException e) {  
			e.printStackTrace();  
			return "";
		}  
	} 


}
