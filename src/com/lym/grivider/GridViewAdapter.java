package com.lym.grivider;

import java.util.List;

import com.ramy.minervue.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class GridViewAdapter extends BaseAdapter{
	List<String> fileList =null;
	LayoutInflater flater = null;
	FilePerate filePerate = null;
	Context context = null;
	
	int fileNum = 0;//�ļ���
	int folderNum = 0;//Ŀ¼��
	
	/**
	 * @param path �ļ�·��
	 * @param context
	 */
	public GridViewAdapter(FilePerate filePerate,List<String> fileList,Context context) {
		// TODO Auto-generated constructor stub
		flater = LayoutInflater.from(context);
		this.context = context;
		this.fileList = fileList;
		this.filePerate = filePerate;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fileList.size();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		fileNum = filePerate.getFileNum();//��ȡ�ļ�����
		folderNum = filePerate.getFolderNum();
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = flater.inflate(R.layout.gird_item, null);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView)convertView.findViewById(R.id.fileIcon);
			viewHolder.title = (TextView)convertView.findViewById(R.id.fileName);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		if(position >= folderNum){
			viewHolder.image.setImageResource(R.drawable.file);//�ļ���ͼ��
		}else{
			viewHolder.image.setImageResource(R.drawable.folder);//Ŀ¼��ͼ��
		}
		viewHolder.title.setText(fileList.get(position));//�ļ���
		return convertView;
	}
}

class ViewHolder{
	public ImageView image;
	public TextView title;
}
