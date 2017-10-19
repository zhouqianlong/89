package com.ramy.minervue.adapter;

import java.util.List;

import we_smart.com.data.PollingInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ramy.minervue.R;
import com.ramy.minervue.app.MainService;

public class BlueToothAdapter extends BaseAdapter {
	public Context mContext;
	public List<PollingInfo> mData;
	public BlueToothAdapter(Context context, List<PollingInfo> data) {
		this.mData = data;
		this.mContext = context;
	}
	public void setList(List<PollingInfo> data){
		this.mData = data;
	}


	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			  LayoutInflater inflater = LayoutInflater.from(mContext);
	            convertView = inflater.inflate(R.layout.bluetooth_manager_adapter, parent, false);
			holder = new ViewHolder();
			holder.tv_mac = (TextView) convertView.findViewById(R.id.tv_mac);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_db = (TextView) convertView.findViewById(R.id.tv_db);
			holder.btn_update = (Button) convertView.findViewById(R.id.btn_update);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tv_name.setText(mData.get(position).name+"");
		holder.tv_mac.setText(mData.get(position).mac);
		holder.tv_db.setText(mData.get(position).risi+"");

		if (holder.btn_update != null) {
			holder.btn_update.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					  final EditText inputServer = new EditText(mContext);
					  inputServer.setText(mData.get(position).name+"");
				        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				        builder.setTitle("修改名称").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
				                .setNegativeButton("取消", null);
				        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				            public void onClick(DialogInterface dialog, int which) {
				               inputServer.getText().toString();
				               mData.get(position).name =  inputServer.getText().toString();
				               Toast.makeText(mContext,  inputServer.getText().toString()+"", Toast.LENGTH_SHORT).show();
				             }
				        });
				        builder.show();
				}
			});
		}
		return convertView;
	}

	class ViewHolder {
		TextView tv_db;
		TextView tv_name;
		TextView tv_mac;
		Button btn_update;
	}

}