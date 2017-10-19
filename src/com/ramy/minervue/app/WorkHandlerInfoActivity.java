package com.ramy.minervue.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagescan.ChildAdapter;
import com.example.imagescan.MainActivity;
import com.ramy.minervue.R;
import com.ramy.minervue.bean.WorkBean;
import com.ramy.minervue.bean.WorkContent;
import com.ramy.minervue.util.FileUtils;
import com.ramy.minervue.util.JsonUtils;

public class WorkHandlerInfoActivity extends Activity implements OnClickListener{

	public TextView tv_q1,tv_miaoshu,tv_statrtime,tv_endtime;
	private GridView gview,gview2;
	private SimpleAdapter simpleAdapter;
	private ChildAdapter adapter;
	private ImageView iv_back;
	private Button btn_upload_image,btn_upload_txt;
	private EditText et_miaoshu;
	private TextView tv_hostay_content ,tv_content;
	public static List<String> selectItemPaths = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workhandler_info_activity);
		tv_q1 = (TextView) findViewById(R.id.tv_q1);
		tv_miaoshu = (TextView) findViewById(R.id.tv_miaoshu);
		tv_statrtime = (TextView) findViewById(R.id.tv_statrtime);
		tv_endtime = (TextView) findViewById(R.id.tv_endtime);
		gview = (GridView) findViewById(R.id.gview);
		gview2 = (GridView) findViewById(R.id.gview2);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		btn_upload_image = (Button) findViewById(R.id.btn_upload_image);
		btn_upload_txt = (Button) findViewById(R.id.btn_upload_txt);
		et_miaoshu = (EditText) findViewById(R.id.et_miaoshu);
		tv_content = (TextView) findViewById(R.id.tv_content);
		tv_hostay_content = (TextView) findViewById(R.id.tv_hostay_content);
		iv_back.setOnClickListener(this);
		btn_upload_image.setOnClickListener(this);
		btn_upload_txt.setOnClickListener(this);
		WorkBean bean = WorkHandlerActivity.getData();
		tv_q1.setText(bean.getWorkName());//名称
		tv_miaoshu.setText(bean.getDescribe());//描述
		tv_statrtime.setText(bean.getCreateTime());//截止时间
		tv_endtime.setText(bean.getEndTime());//处置时间
		if(!bean.getHandler().getDescribe().equals("")){
			tv_hostay_content.setVisibility(View.VISIBLE);
			tv_content.setText(bean.getHandler().getDescribe());
		}
		
		List<String> list = new ArrayList<String>();
		for(int i = 0 ; i < bean.getContent().size();i++){
			list.add(bean.getPath()+""+bean.getContent().get(i).getImagePath());
		}//
		
		selectItemPaths = new ArrayList<String>();
		for(int i = 0 ; i < bean.getHandler().getContent().size();i++){
			selectItemPaths.add(bean.getPath()+""+bean.getHandler().getContent().get(i).getImagePath());
		}
		adapter = new ChildAdapter(this, list, gview);
		adapter.image_GONE = true;
		gview.setAdapter(adapter);
		//		Toast.makeText(getApplicationContext(), WorkHandlerActivity.getData().getDescribe(), 0).show();
	}

	@Override
	public void onClick(View v) {
		if(v==iv_back){
			finish();
		}
		if(v==btn_upload_image){
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
		}
		if(v==btn_upload_txt){//发送给服务器
			WorkBean data = WorkHandlerActivity.getData();
//							FileUtils.copyFile(selectItemPaths.get(i), data.getPath()+content.getImagePath());//复制文件
			try {
				if(!et_miaoshu.getText().toString().equals("")){
					data.getHandler().setDescribe(data.getHandler().getDescribe()+"\n"+et_miaoshu.getText().toString());
				}
				String result = JsonUtils.workBean_jsonBean_ToString(data);
				Toast.makeText(getApplicationContext(), result, 0).show();
				FileUtils.writeFile(data.getPath()+"list.txt", result);
				String path = data.getPath().replace("/up_work/", "/pub_work/");
				try {
					FileUtils.copyFolder(data.getPath(), path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 
		ChildAdapter adapter2  = new ChildAdapter(this, selectItemPaths, gview2);
		adapter2.image_GONE = true;
		gview2.setAdapter(adapter2);
		//		Toast.makeText(getApplicationContext(), "选中了"+selectItemPaths.size()+"", 0).show();
	}


	class Holder{
		ImageView imageView;
	}
}
