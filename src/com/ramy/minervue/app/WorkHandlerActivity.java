package com.ramy.minervue.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ramy.minervue.R;
import com.ramy.minervue.adapter.WorkHandlerAdapter;
import com.ramy.minervue.bean.GasData;
import com.ramy.minervue.bean.MyHandler;
import com.ramy.minervue.bean.WorkBean;
import com.ramy.minervue.bean.WorkContent;
import com.ramy.minervue.control.ControlManager;
import com.ramy.minervue.control.QueryTask;
import com.ramy.minervue.control.ControlManager.PacketListener;
import com.ramy.minervue.dao.PubDao;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.util.FileUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WorkHandlerActivity extends Activity implements OnClickListener{
	private ListView lv_list;
	private FrameLayout tv_myHandler;//待我处理
	private TextView tv_file;
	private WorkHandlerAdapter adapter;
	private File[] list;
	private File rootFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workhandler_activity);
		lv_list = (ListView) findViewById(R.id.lv_list);
		tv_file = (TextView) findViewById(R.id.tv_file);
		tv_myHandler = (FrameLayout) findViewById(R.id.tv_myHandler);
		tv_myHandler.setOnClickListener(this);
		rootFile = (File) getIntent().getSerializableExtra(
				getPackageName() + ".ListFile");///storage/sdcard0/Android/data/com.ramy.minervue/files
		if (rootFile == null || !rootFile.isDirectory()) {
			return;
		}
		if(!rootFile.exists()){
			tv_file.setVisibility(View.VISIBLE);
		}else{
			File[] list = rootFile.listFiles();
			adapter = new WorkHandlerAdapter(this);//file.getPath()+"/"+
			adapter.setFiles(list);
			lv_list.setAdapter(adapter);
		}
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File file =adapter.getItem(position);
				
				PubDao pubDao = new PubDao(WorkHandlerActivity.this);
				int unread = MainService.getInstance().getPreferenceUtil().getWorkDownload(null);
				if (!pubDao.isExistWork(file.getPath()) && unread > 0) {
					pubDao.addWork(file.getPath());
					MainService.getInstance().getPreferenceUtil().setWorkDownload(unread - 1);
					adapter.notifyDataSetChanged();
				}
		 
				
				File [] content = file.listFiles();//读取SD卡文件
				int i=isMyFile(content);//是否含有list.txt文件
				if(i>-1){
					String value = FileUtils.readFile(content[i].getPath());//解析JSon
					try {
						JSONObject jsobj = new JSONObject(value);
						workBean = new WorkBean();
						workBean.setWorkName(jsobj.getString("WorkName"));
						workBean.setDescribe(jsobj.getString("describe"));
						workBean.setCreateTime(jsobj.getString("CreateTime"));
						workBean.setEndTime(jsobj.getString("EndTime"));
						List<String> sendName = new ArrayList<String>();
						JSONArray sendNameArray =jsobj.getJSONArray("SENDName"); 
						for(int s = 0 ; s < sendNameArray.length();s++){
							sendName.add(sendNameArray.getString(s));
						}
						workBean.setSENDName(sendName);	
						JSONArray array = jsobj.getJSONArray("WorkContent");
						List<WorkContent> workContent = new ArrayList<WorkContent>();
						for(int K = 0 ; K< array.length();K++){
							JSONObject obj = (JSONObject) array.get(K);

							WorkContent c = new WorkContent();
							c.setImagePath(obj.getString("image"));
							c.setDescribe(obj.getString("describe"));
							workContent.add(c );
						}
						workBean.setContent(workContent);
						workBean.setPath(file.getPath()+"/");

						List<WorkContent> myWorkContents = new ArrayList<WorkContent>();
						JSONObject obj = jsobj.getJSONObject("Handle");
						JSONArray jsonArray =obj.getJSONArray("WorkContent");
						MyHandler handler = new MyHandler();
						handler.setDescribe(obj.getString("describe"));
						for(int j = 0 ; j < jsonArray.length();j++){	
							WorkContent c = new WorkContent();
							JSONObject object = (JSONObject) jsonArray.get(j);
							c.setImagePath(object.getString("image"));
							c.setDescribe(object.getString("describe"));
							myWorkContents.add(c );
						}
						handler.setContent(myWorkContents);
						workBean.setHandler(handler);
						Intent intent = new Intent(getApplicationContext(), WorkHandlerInfoActivity.class);
						sendRequest(file.getName());
						startActivity(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}
	private void sendRequest(String workName) {
		JSONObject json = new JSONObject();
		try {
			json.put("uuid", MainService.getInstance().getUUID());
			json.put("action", "sync-file-work-name");
			json.put("result", workName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ControlManager control = MainService.getInstance().getControlManager();
		QueryTask queryTask = control.createQueryTask(json, listener, -1);
		queryTask.start();
	}
	private ControlManager.PacketListener listener = new PacketListener() {

		@Override
		public void onPacketArrival(JSONObject packet) {

		}

		@Override
		public String getPacketType() {
			// TODO Auto-generated method stub
			return "";
		}
	};
	
	public static WorkBean workBean;
	
	public static WorkBean getData(){
		
		return workBean;
	}

	public int isMyFile(File [] fileNames){
		for(int i= 0 ; i < fileNames.length;i++){
			if(fileNames[i].getName().equals("list.txt")){
				return i;
			}
		}
		return -1;
	}




	@Override
	public void onClick(View v) {
		if(v==tv_myHandler){

		}
	}



}
