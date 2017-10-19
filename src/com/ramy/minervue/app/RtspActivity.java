package com.ramy.minervue.app;

import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.RtspCamera;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Utils.IPCheck;
import com.wifitalk.adapter.PrepareCallCheckUserAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RtspActivity extends Activity implements OnItemClickListener,OnClickListener,OnItemLongClickListener{
	ListView lv_listcamera;
	private ArrayList<String> mArrayList = new ArrayList<String>();
	private Button btn_add;
	private ListAdapter listAdapter;
	private DBHelper dbHelper = new DBHelper(this);
	private int size = 0 ;
	private  List<RtspCamera> list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rtsp_main_camera);

		lv_listcamera = (ListView) findViewById(R.id.lv_listcamera);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);
		lv_listcamera.setOnItemClickListener(this);
		lv_listcamera.setOnItemLongClickListener(this);
	}

	private ArrayList<String> getData() {
		mArrayList=  new ArrayList<String>();
		for(int i= 0 ; i< size; i ++ ){
			mArrayList.add(list.get(i).getCameraName()+":"+list.get(i).getCameraAddress());
		}
		return mArrayList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(RtspActivity.this, RtspPlayActivity.class);
		//   rtsp://192.168.1.32:554
		if(list.get(position).getCameraPort().equals("")){
			intent.putExtra("url", list.get(position).getCameraAddress());
		}else{
			intent.putExtra("url", "rtsp://"+list.get(position).getCameraAddress()+":"+list.get(position).getCameraPort());
		}
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		if(v==btn_add){
			startActivity(new Intent(getApplicationContext(), RtspAddActivity.class));
		}

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		notifily();
		Log.i("MYDB", "onResume--findRtspCameraAll size:"+size);
	}

	private void notifily() {
		list = dbHelper.findRtspCameraAll();
		size= list.size();
		listAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,getData());
		lv_listcamera.setAdapter(listAdapter);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		
		AlertDialog.Builder builder=new AlertDialog.Builder(RtspActivity.this);
		builder.setItems(getResources().getStringArray(R.array.rtsp),  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if(which==0){
					new AlertDialog.Builder(RtspActivity.this).setTitle(R.string.title_tishi).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dbHelper.deleteRtspCamera(list.get(position));
							notifily();
							dialog.dismiss();
						}
					} ).setNegativeButton(R.string.cancel, null).setMessage("ÊÇ·ñÉ¾³ý?").show();
				}else if(which==1){
					Intent mIntent = new Intent(getApplicationContext(), RtspEditActivity.class); 
					mIntent.putExtra("RtspEditActivity", list.get(position));
					startActivity(new Intent(mIntent));
					
					dialog.dismiss();
				}else if(which==2){
					dialog.dismiss();
				}
				
			}
		});
		builder.show();
		
		return false;
	}

}
