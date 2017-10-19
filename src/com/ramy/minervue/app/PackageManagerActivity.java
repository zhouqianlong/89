package com.ramy.minervue.app;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.camera.ApplicationAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class PackageManagerActivity extends Activity{
	GridView gv;
	PackageManager pm;
	int maxCount;
	private List<ResolveInfo> listAll;
	private List<ResolveInfo> listShow = new ArrayList<ResolveInfo>();
	//写入需要显示应用的包名~
	private String[] name = new String[300];
	LinearLayout lly_manager;
	TextView tv_title;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		gv = (GridView) findViewById(R.id.gv);
		tv_title =(TextView) findViewById(R.id.tv_title);
		tv_title.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		pm = getPackageManager();
		// 数据
		// 搜索整个操作系统，
		// 将所有的可以罗列的主运行程序都检索出来

		List<ApplicationInfo> listApplications = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		int appCount = listApplications.size();
		name = new String[appCount];
		for (int i = 0; i < appCount; i++) {
			ApplicationInfo ai = listApplications.get(i);
			//			if(ai.packageName.equals(""))
			name[i] = ai.packageName;
			
		}

		Intent in= new Intent(Intent.ACTION_MAIN);
		in.addCategory(Intent.CATEGORY_LAUNCHER);
		if (listAll != null) {
			listAll.clear();
		}
		if (listShow != null) {
			listShow.clear();
		}
//		05-18 10:13:21.089: I/MYDB(4927): 拨号

		listAll = pm.queryIntentActivities(in, 0);
		Log.i("list1.size())", "list1.size() =" + listAll.size());
		for (int i = 0; i < listAll.size(); i++) {
			Log.i("list1", "position"+i+" name" + listAll.get(i).loadLabel(pm)+listAll.get(i).activityInfo.packageName);
			
				if(listAll.get(i).loadLabel(pm).equals("拨号")
					||listAll.get(i).loadLabel(pm).equals("信息")
						||listAll.get(i).loadLabel(pm).equals("资源管理器")
						||listAll.get(i).loadLabel(pm).equals("录音机")
						||listAll.get(i).loadLabel(pm).equals("日历")
//						||listAll.get(i).loadLabel(pm).equals("浏览器")
//						||listAll.get(i).loadLabel(pm).equals("音乐")
					||listAll.get(i).loadLabel(pm).equals("文件管理")){
				Log.i("MYDB", ""+listAll.get(i).loadLabel(pm));
				String packagerName = listAll.get(i).activityInfo.packageName;

				for (int j = 0; j < name.length; j++) {
					//把需要的放入listShow中去显示
					if (packagerName.equals(name[j])) {
						listShow.add(listAll.get(i));
					}

				}
			}
			
			if(listAll.get(i).activityInfo.packageName.equals("com.unionbroad.app")
//					||listAll.get(i).activityInfo.packageName.equals("com.android.settings")
					){
				Log.i("MYDB", ""+listAll.get(i).loadLabel(pm));
				String packagerName = listAll.get(i).activityInfo.packageName;
				
				for (int j = 0; j < name.length; j++) {
					//把需要的放入listShow中去显示
					if (packagerName.equals(name[j])) {
						listShow.add(listAll.get(i));
					}
					
				}
			}
			if(listAll.get(i).loadLabel(pm).equals("Phone")
					||listAll.get(i).loadLabel(pm).equals("Messaging")||listAll.get(i).loadLabel(pm).equals("File Manager")){
				Log.i("MYDB", ""+listAll.get(i).loadLabel(pm));
				String packagerName = listAll.get(i).activityInfo.packageName;
				
				for (int j = 0; j < name.length; j++) {
					//把需要的放入listShow中去显示
					if (packagerName.equals(name[j])) {
						
						
						listShow.add(listAll.get(i));
						
					}
					
				}
			}

		}
		// 每一个ResolveInfo对象，对应一个应用程序的信息
		gv.setAdapter(new ApplicationAdapter(this, listShow, pm));
		gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ResolveInfo info = listShow.get(position);
				Intent in = new Intent();

				ComponentName cn = new ComponentName(
						info.activityInfo.packageName, info.activityInfo.name);
				Log.i("packageName", "packageName=" + info.activityInfo.packageName);//com.android.settings
				Log.i("name", "name=" + info.activityInfo.name);//com.android.settings.Settings
				in.setComponent(cn);
				if(info.activityInfo.name.endsWith("com.android.soundrecorder.SoundRecorder")){//录音机比较特殊
//					Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION); 
//					startActivity(intent); 
					startActivity(in);
				}else{
					startActivity(in);
				}

			}
		});

		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ResolveInfo info = listShow.get(position);
				Intent in = new Intent();
				ComponentName cn = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
				in.setComponent(cn);
				if(info.loadLabel(pm).equals("WLAN")){
					startActivity(new Intent(getApplicationContext(), PackageManagerActivity.class));
				}else{
					//					if("音视频联络".equals(info.loadLabel(pm))){
					//						in.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP );
					//						startActivity(in);
					////						finish();
					//					}

					Uri packageURI = Uri.parse("package:"+info.activityInfo.packageName);           
					Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);           
					startActivity(uninstallIntent);   
				}
				return false;
			}
		});
		maxCount = listShow.size();
	}
	int selectCount  = 0 ;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==8){
			gv.setSelection(selectCount);
			if(selectCount == maxCount){
				selectCount = 0;
			}else{
				selectCount ++;
			}
		}
		if(keyCode==12){
			//确定
			if(selectCount-1 <=maxCount&&selectCount-1>=0){
				ResolveInfo info = listShow.get(selectCount-1);
				Intent in = new Intent();
				ComponentName cn = new ComponentName(
						info.activityInfo.packageName, info.activityInfo.name);
				Log.i("packageName", "packageName=" + info.activityInfo.packageName);
				Log.i("name", "name=" + info.activityInfo.name);
				in.setComponent(cn);
				if(info.loadLabel(pm).equals("WLAN")){
					startActivity(new Intent(getApplicationContext(), PackageManagerActivity.class));
					Toast.makeText(getApplicationContext(), "查询更多", 0).show();
				}else{
					startActivity(in);
				}
				return super.onKeyDown(66, event);
			}
		}
		if(keyCode==67){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	//	@Override
	//	public void onClick(View v) {
	//		// TODO Auto-generated method stub
	//		
	//	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
}
