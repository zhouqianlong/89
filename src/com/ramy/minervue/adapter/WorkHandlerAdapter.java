package com.ramy.minervue.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ramy.minervue.R;
import com.ramy.minervue.app.FileListActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.VideoActivity;
import com.ramy.minervue.bean.QuestionInfo;
import com.ramy.minervue.dao.PubDao;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.media.VideoCodec;
import com.ramy.minervue.util.PreferenceUtil;

import java.io.File;
import java.util.List;
import java.util.jar.Attributes;

/**
 * Created by peter on 11/30/13.
 */
public class WorkHandlerAdapter extends BaseAdapter implements PreferenceUtil.PreferenceListener {
	private File[] files;
	private Activity activity;
	private PubDao pubDao ;
	public WorkHandlerAdapter(Activity activity) {
		this.files = new File[0];
		this.activity = activity;
		pubDao = new PubDao(activity);;
	}

	public void setFiles(File[] files) {
		this.files = files;
		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		return files.length;
	}

	@Override
	public File getItem(int position) {
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	Holder mHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.work_handler_item, parent, false);
        	mHolder = new Holder();
        	mHolder.tv = (TextView) convertView.findViewById(R.id.tv);
        	mHolder.tv_unread_download = (TextView) convertView.findViewById(R.id.tv_unread_download);
        	convertView.setTag(mHolder);
        }else{
        	mHolder = (Holder) convertView.getTag();
        }
        mHolder.tv.setText(files[position].getName());
         
		int unread = MainService.getInstance().getPreferenceUtil().getWorkDownload(
				WorkHandlerAdapter.this);
		if (unread > 0 && !pubDao.isExistWork(files[position].getPath())) {
			mHolder.tv_unread_download.setText(R.string.tv_new);
			mHolder.tv_unread_download.setTextSize(20);
			mHolder.tv_unread_download.setTextColor(Color.parseColor("#CC0033"));
			mHolder.tv_unread_download.setVisibility(View.VISIBLE);
		}else{
			mHolder.tv_unread_download.setVisibility(View.INVISIBLE);
		}
        return convertView;
    }

    
    
    class Holder{
    	TextView tv;
    	TextView tv_unread_download;
    	Button ibtn_remove;
    }



	@Override
	public void onPreferenceChanged() {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}
}
