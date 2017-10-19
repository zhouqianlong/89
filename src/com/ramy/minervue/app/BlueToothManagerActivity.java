package com.ramy.minervue.app;

import java.io.File;
import java.util.List;

import we_smart.com.data.PollingInfo;

import com.ramy.minervue.R;
import com.ramy.minervue.adapter.BlueToothAdapter;
import com.ramy.minervue.util.FileUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BlueToothManagerActivity extends Activity implements OnClickListener{

	private ListView lv_1;
	private Button button1;
	public BlueToothAdapter mAdapter;
	private boolean flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_manager_activity);
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
		mAdapter = new BlueToothAdapter(this, MainService.getInstance().mBeaconList);
		lv_1 = (ListView) findViewById(R.id.lv_1);
		lv_1.setAdapter(mAdapter);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (flag) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							mAdapter.setList(MainService.getInstance().mBeaconList);
							mAdapter.notifyDataSetChanged();
						}
					});
				}
			}
		}).start();
	}
	Handler mHandler = new Handler(){
		
	};
	@Override
	public void onClick(View v) {
		if(v == button1){
			List<PollingInfo> data = mAdapter.mData;
			StringBuffer sb = new StringBuffer();
			for(int i = 0 ; i <data.size();i++){
				sb.append(data.get(i).name+":"+data.get(i).mac+"\n");
			}
			Toast.makeText(getApplicationContext(), sb.toString(), 0).show();
			FileUtils.writeFile("/storage/sdcard0/Android/data/cfg/"+MainService.getInstance().getUUID()+"bl_cfg.log", FileUtils.enCodeJson(MainService.getInstance().mBeaconList));
			
			
			
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		flag = false;
	}
}
