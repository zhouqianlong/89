package com.ramy.minervue.app;

import com.ramy.minervue.R;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.RtspCamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RtspEditActivity extends Activity implements OnClickListener{
	TextView tv_title;
	EditText et_address,et_port,et_name;
	private Button btn_ok,btn_canle;
	private RtspCamera camera;
	private DBHelper dbHelper = new DBHelper(this); 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rtsp_add_camera);
		camera = (RtspCamera) getIntent().getExtras().getSerializable("RtspEditActivity");
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("修改摄像头");
		et_address = (EditText) findViewById(R.id.et_address);
		et_name = (EditText) findViewById(R.id.et_name);
		et_port = (EditText) findViewById(R.id.et_port);
		btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_canle = (Button) findViewById(R.id.btn_canle);
		btn_ok.setText("修改");
		btn_ok.setOnClickListener(this);
		btn_canle.setOnClickListener(this);
		
		et_name.setText(camera.getCameraName());
		et_port.setText(camera.getCameraPort());
		et_address.setText(camera.getCameraAddress());
		
		
		
	}
	@Override
	public void onClick(View v) {
		if(btn_ok == v){
			String cameraName = et_name.getText().toString().trim();
			String cameraAddress = et_address.getText().toString().trim();
			String cameraPort = et_port.getText().toString().trim();
			if(et_name.getText().toString().trim().equals("")){
				showTextToast("摄像头名称不能为空", 0);
			}else if(et_address.getText().toString().trim().equals("")){
				showTextToast("摄像头地址不能为空", 0);
			}else{
				dbHelper.updateRtspCamera(new RtspCamera(camera.getId(),cameraName, cameraAddress, cameraPort));
				showTextToast("修改摄像头成功!", 0);
				finish();
			}
		}
	}

	private Toast toast = null;
	/**
	 * 
	 * @param msg 内容
	 * @param i  显示时间   0 短时间   | 1 长时间
	 */
	private void showTextToast(String msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}
}
