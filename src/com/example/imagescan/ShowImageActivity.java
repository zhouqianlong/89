package com.example.imagescan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.GasPhotographerActivity;
import com.ramy.minervue.app.WorkHandlerActivity;
import com.ramy.minervue.app.WorkHandlerInfoActivity;
import com.ramy.minervue.bean.WorkContent;
import com.ramy.minervue.util.FileUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class ShowImageActivity extends Activity implements OnClickListener{
	private GridView mGridView;
	private List<String> list;
	private ChildAdapter adapter;
	private Button btn_upload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image_activity);

		mGridView = (GridView) findViewById(R.id.child_grid);
		btn_upload = (Button) findViewById(R.id.btn_upload);
		list = getIntent().getStringArrayListExtra("data");

		adapter = new ChildAdapter(this, list, mGridView);
		mGridView.setAdapter(adapter);
		btn_upload.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if(btn_upload==v){
			ProgressDialog.show(ShowImageActivity.this, "", "正在处理..",
					true, false);
			new Thread( new Runnable() {
				@Override
				public void run() {
					List<Integer>positions = adapter.getSelectItems();
					List<String> paths     = adapter.getSelectListItems();
					for( int i= 0 ; i< adapter.getSelectItems().size();i++){
						addPaths(paths.get(positions.get(i)));
					}
					mHandler.sendEmptyMessage(1);//通知线程完成
				}
			}).start();
			
			
		}

	}

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1){
				finish();
			}
		};
	};
	
	public void addPaths(String path){
		int count = 0 ;
		for(int i = 0 ; i< WorkHandlerInfoActivity.selectItemPaths.size();i++){
			if(WorkHandlerInfoActivity.selectItemPaths.get(i).indexOf(FileUtils.getPathFileName(path))>0){
				count ++;
			}
		}
		if(count==0){
			WorkHandlerInfoActivity.selectItemPaths.add(path);
			WorkHandlerActivity.workBean.getHandler().getContent().add(new WorkContent(FileUtils.getPathFileName(path), ""));
			try {
				FileUtils.copyFile(path, WorkHandlerActivity.workBean.getPath()+FileUtils.getPathFileName(path));
			} catch (IOException e) {
				e.printStackTrace();
			}//复制文件
		}
	}



}
