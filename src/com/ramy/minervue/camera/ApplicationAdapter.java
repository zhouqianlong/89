package com.ramy.minervue.camera;

import java.util.List;

import com.ramy.minervue.R;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationAdapter extends BaseAdapter {

	private Activity context;
	private List<ResolveInfo> list;
	private PackageManager pm;;

	public ApplicationAdapter(Activity context, List<ResolveInfo> list,PackageManager pm) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
		this.pm = pm;
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		if(convertView==null){
			mViewHolder = new ViewHolder();
			convertView = context.getLayoutInflater().inflate(R.layout.piitem, null);
			mViewHolder.iv = (ImageView) convertView.findViewById(R.id.icon);
			mViewHolder.tv = (TextView) convertView.findViewById(R.id.appName);
			convertView.setTag(mViewHolder);
		}else{
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		ResolveInfo info = list.get(position);
		Drawable d = info.loadIcon(pm);
//		if(info.loadLabel(pm).equals("WLAN")){
//			mViewHolder.tv.setText("更多");
//			mViewHolder.iv.setImageDrawable(dh);
//		}else{
			mViewHolder.iv.setImageDrawable(d);
			mViewHolder.tv.setText(info.loadLabel(pm));
			Log.i("ZZZ", info.loadLabel(pm)+"..............");
//		}
		return convertView;
	}
	private  class ViewHolder{
		ImageView iv;
		TextView tv;
	}
}
