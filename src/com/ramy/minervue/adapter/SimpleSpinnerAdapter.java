package com.ramy.minervue.adapter;


import java.util.List;





import android.R;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SimpleSpinnerAdapter extends BaseAdapter {

	private List<String> nameList;
	private Context context;

	public SimpleSpinnerAdapter(List<String> nameList,Context context) {
		this.nameList = nameList;
		this.context = context;
	}
	public void setList(List<String> nameList){
		this.nameList = nameList;
	}
	@Override
	public int getCount() {
		return nameList.size();
	}
	@Override
	public String getItem(int position) {
		return nameList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public int getItemPosition(String name) {
		return nameList.indexOf(name);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.simple_list_item_1, null);
		}
		TextView tv = (TextView) convertView;
		tv.setTextColor(Color.BLACK);
		String name = (String) getItem(position);
		tv.setText(name);
		return tv;
	}

}
