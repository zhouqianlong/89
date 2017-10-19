package com.ramy.minervue.adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.bean.OnLineBean;
import com.ramy.minervue.db.UserBean;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class CallAdapter extends BaseAdapter{
	private static HashMap<Integer, Boolean> isSelected;
	private boolean[] checks; //用于保存checkBox的选择状态  
	private int type = 0 ;//type为0代表当前是语音对讲                       1代表视频对讲
	public CallAdapter(Context context,int type,
			List<UserBean> list, PrepareCallCheckUser instances) {
		this.mContent = context;
		this.listView_List = list;
		this.instances = instances;
		checks = new boolean[list.size()];  
		myIp = MainService.getInstance().getIPadd();
		this.type = type;
		initDate();
	}


	public CallAdapter(Context context,List<UserBean> list, PrepareCallCheckUser instances,int zu,int type) {
		this.mContent = context;
		this.listView_List = list;
		this.instances = instances;
		this.zu = zu;
		checks = new boolean[list.size()];  
		myIp = MainService.getInstance().getIPadd();
		this.type = type;
		initDate();
	}

	public synchronized void setOnlineList(List<OnLineBean> list){
		onLinelist = list;
		notifyDataSetChanged();
	}
	public synchronized void setList(List<UserBean> list){
		if(MainService.SHUAXIN){
			checks = new boolean[list.size()];  
			for(int i = 0 ; i < checks.length;i++){
				checks[i] = true;
			}
			MainService.getInstance().setAudioUserBeans(list);
			PrepareCallCheckUser.instances.tv_list_info.setVisibility(View.VISIBLE);
			this.listView_List =  new ArrayList<UserBean>();
			for(int i = 0 ;  i < list.size();i++){
				listView_List.add(list.get(i));
			}
			notifyDataSetChanged();
			PrepareCallCheckUser.instances.btn_z1.setBackgroundColor(Color.TRANSPARENT);
			PrepareCallCheckUser.instances.btn_z2.setBackgroundColor(Color.TRANSPARENT);
			PrepareCallCheckUser.instances.btn_z3.setBackgroundColor(Color.TRANSPARENT);
			PrepareCallCheckUser.instances.btn_z1.setTextColor(Color.WHITE);
			PrepareCallCheckUser.instances.btn_z2.setTextColor(Color.WHITE);
			PrepareCallCheckUser.instances.btn_z3.setTextColor(Color.WHITE);
			//			PrepareCallCheckUser.instances.zu = 0;
		}
	}
	private int zu = 0;
	private PrepareCallCheckUser instances;
	private List<UserBean> listView_List;
	private List<OnLineBean> onLinelist= new ArrayList<OnLineBean>();//在线列表
	private Context mContent;
	private String myIp;
	public List<UserBean> getList(){
		return this.listView_List;
	}
	@Override
	public int getCount() {
		return listView_List.size();
	}
	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder =null;
		if(convertView==null){
			mHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContent).inflate(R.layout.prepare_call_check_user_item, null);
			mHolder.tv_userName = (TextView) convertView.findViewById(R.id.tv_userName);
			mHolder.tv_ip = (TextView) convertView.findViewById(R.id.tv_ip);
			mHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_state);
			convertView.setTag(mHolder);
		}else{
			mHolder  = (ViewHolder) convertView.getTag();
		}
		mHolder.tv_userName.setText(listView_List.get(position).getUsername());
		mHolder.tv_ip.setText(listView_List.get(position).getUserIp());
		for(int k = 0;k<onLinelist.size();k++){
			if(listView_List.get(position).getUserIp().equals(onLinelist.get(k).getIp())){
				mHolder.tv_userName.setText("在线："+listView_List.get(position).getUsername());
			}
		}
		mHolder.cb.setChecked(getIsSelected().get(position));
		return convertView;
	}
	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}
	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		CallAdapter.isSelected = isSelected;
	}
	private void initDate() {
		for (int i = 0; i < listView_List.size(); i++) {
			getIsSelected().put(i, false);
		}
	}
}
