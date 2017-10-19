package com.ramy.minervue.adapter;

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
import com.ramy.minervue.app.VideoActivity;
import com.ramy.minervue.bean.QuestionInfo;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.media.VideoCodec;

import java.util.List;
import java.util.jar.Attributes;

/**
 * Created by peter on 11/30/13.
 */
public class QuestionAdapter extends BaseAdapter {

    private List<QuestionInfo> list;
    private VideoActivity mContext;
    private DBHelper dbHelper;
    public QuestionAdapter(List<QuestionInfo> list,VideoActivity mContext) {
        this.list = list;
        this.mContext = mContext;
        dbHelper = new DBHelper(mContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public QuestionInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	Holder mHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.question_item, parent, false);
        	mHolder = new Holder();
        	mHolder.tv = (TextView) convertView.findViewById(R.id.tv);
//        	mHolder.ibtn_remove = (Button) convertView.findViewById(R.id.ibtn_remove);
        	convertView.setTag(mHolder);
        }else{
        	mHolder = (Holder) convertView.getTag();
        }
        mHolder.tv.setTextColor(Color.WHITE);
        mHolder.tv.setText(list.get(position).getQuestionName());
//        mHolder.ibtn_remove.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				new AlertDialog.Builder(mContext).setTitle("是否删除该条记录").setPositiveButton("是", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dbHelper.clearQuestion(list.get(position).getId());
//						Log.i("AAA", "list.get(position).getId():"+list.get(position).getId());
//						mContext.findQuestion();
//					}
//				}).setNegativeButton("否", null).show();
//				
//			}
//		});
        return convertView;
    }

    
    
    class Holder{
    	TextView tv;
    	Button ibtn_remove;
    }
}
