package com.ramy.minervue.adapter;

import java.util.List;

import com.ramy.minervue.R;



import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.provider.CalendarContract.Colors;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingAdapter extends BaseAdapter {
	public Context context;
	private List<ScanResult> list;  
	private int selectPosition = -1;
	private String connPosition = "";
	private String mac = "";
	public SettingAdapter(List<ScanResult> list,Context context){
		this.context = context;
		this.list = list;
	}
	public void setSelectPosition(int selectPosition){
		this.selectPosition = selectPosition;
	}
	public void setConnPosition(String connPosition,String mac){
		this.connPosition = connPosition;
		this.mac = mac;
	}
	public void setList(List<ScanResult> list){
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		WifiHolder myHolder = null;
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.setting_item, null);
			myHolder = new WifiHolder();
			myHolder.iv_signal = (ImageView) convertView.findViewById(R.id.iv_signal);
			myHolder.iv_conn = (ImageView) convertView.findViewById(R.id.iv_conn);
			myHolder.iv_suo = (ImageView) convertView.findViewById(R.id.iv_suo);
			myHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			myHolder.tv_mac = (TextView) convertView.findViewById(R.id.tv_mac);
			myHolder.ll_bg = (LinearLayout) convertView.findViewById(R.id.ll_bg);
			convertView.setTag(myHolder);
		}else{
			myHolder =  (WifiHolder) convertView.getTag();
		}
		ScanResult scanResult = list.get(position);  

		if (Math.abs(scanResult.level) > 100) {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_0));  
		} else if (Math.abs(scanResult.level) > 80) {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_1));  
		} else if (Math.abs(scanResult.level) > 70) {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_2));  
		} else if (Math.abs(scanResult.level) > 60) {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_3));  
		} else if (Math.abs(scanResult.level) > 50) {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_4));  
		} else {  
			myHolder.iv_signal.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_power_level_4));  
		}  
		myHolder.tv_name.setText(scanResult.SSID);
		myHolder.tv_mac.setText(scanResult.BSSID);
		//		if(position==selectPosition){
		//			myHolder.ll_bg.setBackgroundColor(Color.GREEN);
		//		}else{
		//			myHolder.ll_bg.setBackgroundColor(Color.WHITE);
		//		}
		if(connPosition.equals("\""+scanResult.SSID+"\"")&&mac.equals(scanResult.BSSID)){
			myHolder.iv_conn.setVisibility(View.VISIBLE);
		}else{
			myHolder.iv_conn.setVisibility(View.INVISIBLE);
		} 


		if (!TextUtils.isEmpty(scanResult.capabilities)) {

			if (scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("wpa")) {
				myHolder.iv_suo.setVisibility(View.VISIBLE);
			} else if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("wep")) {
				myHolder.iv_suo.setVisibility(View.VISIBLE);
			} else {
				myHolder.iv_suo.setVisibility(View.INVISIBLE);
			}
		}


		return convertView;
	}
}
class WifiHolder{
	ImageView iv_signal;
	ImageView iv_conn;
	ImageView iv_suo;
	TextView tv_name;
	TextView tv_mac;
	LinearLayout ll_bg;
}

