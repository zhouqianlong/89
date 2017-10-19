package com.wifitalk.adapter;


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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrepareCallCheckUserAdapter extends BaseAdapter{
	private int type = 0 ;//type为0代表当前是语音对讲                       1代表视频对讲
	public PrepareCallCheckUserAdapter(Context context,int type,
			List<UserBean> list, PrepareCallCheckUser instances) {
		this.mContent = context;
		this.listView_List = list;
		this.instances = instances;
		myIp = MainService.getInstance().getIPadd();
		this.type = type;
		isSelected = new HashMap<Integer, Boolean>();
		initDate();
	}


	public PrepareCallCheckUserAdapter(Context context,List<UserBean> list, PrepareCallCheckUser instances,int zu,int type) {
		this.mContent = context;
		this.listView_List = list;
		this.instances = instances;
		this.zu = zu;
		myIp = MainService.getInstance().getIPadd();
		this.type = type;
		isSelected = new HashMap<Integer, Boolean>();
		initDate();
	}

	public synchronized void setOnlineList(List<OnLineBean> list){
		onLinelist = list;
	}
	
	public void setOnline(List<UserBean> list){
		listView_List = list;
		initDate();
		notifyDataSetChanged();
	}
	public synchronized void setList(List<UserBean> list){
		if(MainService.SHUAXIN){
			MainService.getInstance().setAudioUserBeans(list);
			PrepareCallCheckUser.instances.tv_list_info.setVisibility(View.VISIBLE);
			Log.i("XUANZ", "setlist:"+list.size());
			this.listView_List =  new ArrayList<UserBean>();
			for(int i = 0 ;  i < list.size();i++){
				listView_List.add(list.get(i));
			}
			notifyDataSetChanged();
//			PrepareCallCheckUser.instances.btn_z1.setBackgroundColor(Color.TRANSPARENT);
//			PrepareCallCheckUser.instances.btn_z2.setBackgroundColor(Color.TRANSPARENT);
//			PrepareCallCheckUser.instances.btn_z3.setBackgroundColor(Color.TRANSPARENT);
//			PrepareCallCheckUser.instances.btn_z1.setTextColor(Color.WHITE);
//			PrepareCallCheckUser.instances.btn_z2.setTextColor(Color.WHITE);
//			PrepareCallCheckUser.instances.btn_z3.setTextColor(Color.WHITE);
			isSelected = new HashMap<Integer, Boolean>();
			initDate();
			for (int i = 0; i < listView_List.size(); i++) {
				PrepareCallCheckUserAdapter.getIsSelected().put(i, true);
			} 
			if(type==0){
				instances.tv_show.setText("已选中" + instances.getAudioCheckStatu().size() + "人");
			}else{
				instances.tv_show.setText("已选中" + instances.getVideoCheckStatu().size() + "人");
			}
		}
	}
	private int zu = 0;
	private PrepareCallCheckUser instances;
	public List<UserBean> listView_List;
	private List<OnLineBean> onLinelist= new ArrayList<OnLineBean>();//在线列表
	private Context mContent;
	private String myIp;
	public List<UserBean> getList(){
		return this.listView_List;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
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
		MyHolder mHolder =null;
		Log.i("TEST", "YM89getView："+position);
		if(convertView==null){
			mHolder = new MyHolder();
			convertView = LayoutInflater.from(mContent).inflate(R.layout.prepare_call_check_user_item, null);
			mHolder.tv_userName = (TextView) convertView.findViewById(R.id.tv_userName);
			mHolder.tv_mac = (TextView) convertView.findViewById(R.id.tv_mac);
			mHolder.tv_ip = (TextView) convertView.findViewById(R.id.tv_ip);
			mHolder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
			mHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_state);
//			mHolder.ll_bg = (LinearLayout) convertView.findViewById(R.id.ll_bg);
			convertView.setTag(mHolder);
		}else{
			mHolder  = (MyHolder) convertView.getTag();
		}

		mHolder.tv_userName.setText(listView_List.get(position).getUsername());
		if(listView_List.get(position).getLocation()==null){
			
			mHolder.tv_location.setText("无位置");
		}else if(listView_List.get(position).getLocation().equals("")){
			mHolder.tv_location.setText("无位置");
		}else{
			mHolder.tv_location.setText(listView_List.get(position).getLocation());
		}
		
		mHolder.tv_ip.setText(listView_List.get(position).getUserIp());
		mHolder.tv_mac.setText(listView_List.get(position).getMacAddress());
		if(listView_List.get(position).getUsername().equals("")){
			mHolder.tv_userName.setText(listView_List.get(position).getUserIp());
		}
		
		// 根据isSelected来设置checkbox的选中状况
		mHolder.cb.setChecked(getIsSelected().get(position));
		if(getIsSelected().get(position)){
			mHolder.tv_userName.setTextColor(Color.GREEN);
			mHolder.tv_ip.setTextColor(Color.GREEN);
		}else{
			mHolder.tv_userName.setTextColor(Color.GRAY);
			mHolder.tv_ip.setTextColor(Color.GRAY);
		}
		for(OnLineBean ls :onLinelist){//查询列表IP是否属于在线
			if(ls.getIp().equals(listView_List.get(position).getUserIp())){
				mHolder.tv_userName.setTextColor(Color.RED);
				Log.i("TEST", listView_List.get(position).getUserIp()+" is：online  for index:"+position);
			}
		}
		Log.i("MYTEST", "position:"+position+"--"+getIsSelected().get(position)+"");
		if(zu>0){
			if(type==0){
			}else{
				for(int i = 0 ; i <instances.getVideoCheckStatu().size();i++ ){
					if(listView_List.get(position).getUserIp().equals(instances.getVideoCheckStatu().get(i))){
						mHolder.cb.setChecked(true);
					}else{
						mHolder.cb.setChecked(false);
					}
				}
			}
		} 
		if(myIp.equals(listView_List.get(position).getUserIp())){
			if(type==0){
				instances.deleteAudioPositionCheckStatu(new UserBean(listView_List.get(position).getUsername(), listView_List.get(position).getUserIp()));
			}else{
				instances.deleteVideoPositionCheckStatu(new UserBean(listView_List.get(position).getUsername(), listView_List.get(position).getUserIp()));
			}
			mHolder.tv_userName.setText("本机");
		}
		return convertView;
	}
	public  class MyHolder{
//		public LinearLayout ll_bg;
		public TextView tv_userName;
		public TextView tv_mac;
		public TextView tv_ip;
		public TextView tv_location;
		public CheckBox cb;
	}

	/**
	 * 查看该IP在音频发送列表中是否存在
	 * @param ip
	 * @return
	 */
	public boolean isAudioEmpty(String ip){
		for(int i =0;i < instances.getAudioCheckStatu().size();i++){
			if(ip.equals(instances.getAudioCheckStatu().get(i).getUserIp())){	 
				return true;
			}
		}
		return false;
	}
	private static HashMap<Integer, Boolean> isSelected;
	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		PrepareCallCheckUserAdapter.isSelected = isSelected;
	}
	private void initDate() {
	
		for (int i = 0; i < listView_List.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

}
