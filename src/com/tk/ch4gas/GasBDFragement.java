package com.tk.ch4gas;

import com.ramy.minervue.R;
import com.tk.ch4gas.db.DBHelper;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android_serialport_api.GasDetector;
import android_serialport_api.SerialCmdUtils;

public class GasBDFragement extends Fragment implements OnCheckedChangeListener,OnClickListener{
	private CheckBox cb_1,cb_2,cb_3;
	private Button btn_ok,btn_back;
	private DBHelper db;
	private EditText gasData1,gasData2,gasData3;
	public GasDetector detector;
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	public GasBDFragement() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		detector = GasMainActivity.getIntances().getGasDetector();
		db = new DBHelper(getActivity());
		float [] list = 	db.findGAS_CALIBRATION();
		View rootView = inflater.inflate(R.layout.fragment_bd, container, false);
		cb_1 = (CheckBox) rootView.findViewById(R.id.cb_1);
		cb_2 = (CheckBox) rootView.findViewById(R.id.cb_2);
		cb_3 = (CheckBox) rootView.findViewById(R.id.cb_3);
		btn_ok = (Button) rootView.findViewById(R.id.btn_ok);
		btn_back = (Button) rootView.findViewById(R.id.btn_back);
		cb_1.setOnCheckedChangeListener(this);
		cb_2.setOnCheckedChangeListener(this);
		cb_3.setOnCheckedChangeListener(this);
		cb_1.setChecked(true);
		gasData1 = (EditText) rootView.findViewById(R.id.gasData1);
		gasData2 = (EditText) rootView.findViewById(R.id.gasData2);
		gasData3 = (EditText) rootView.findViewById(R.id.gasData3);
		gasData1.setText(list[0]+"");
		gasData2.setText(list[1]+"");
		gasData3.setText(list[2]+"");
		btn_ok.setOnClickListener(this);
		btn_back.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if(btn_ok==v){
			if(cb_1.isChecked()){
				new Thread(new Runnable() {
					@Override
					public void run() {
						detector.open();
						detector.SerialPointCommunication(SerialCmdUtils.READ_CH4_PPM);
						if(detector.getLastReplyCmd()==GasDetector.CMD_READ_CH4_SUCCE){
							Message msg = Message.obtain();
							msg.arg1 = (int)detector.getLastResponseGasData();
							msg.what=1;
							mHandler.sendMessage(msg);
						} 
					}
				}).start();
			}
			if(cb_2.isChecked()){
				new Thread(new Runnable() {
					@Override
					public void run() {
						detector.open();
						detector.SerialPointCommunication(SerialCmdUtils.READ_CH4_PPM);
						if(detector.getLastReplyCmd()==GasDetector.CMD_READ_CH4_SUCCE){
							Message msg = Message.obtain();
							msg.arg1 = (int) detector.getLastResponseGasData();
							msg.what=2;
							mHandler.sendMessage(msg);
						} 
					}
				}).start();
			}
			if(cb_3.isChecked()){
				db.updateGAS_CALIBRATION3(gasData3.getText().toString());
				Toast.makeText(getActivity().getApplicationContext(), "甲烷报警点标定成功", 0).show();
			}
		}
		if(v==btn_back){
			//					 期望值    ,实际值
			db.updateGAS_CALIBRATION1(""+0,""+29978);//零点
			db.updateGAS_CALIBRATION2(""+2,""+37775);//标气
			db.updateGAS_CALIBRATION3(""+0);
			Toast.makeText(getActivity().getApplicationContext(), "重置成功", 0).show();
			gasData1.setText("0");
			gasData2.setText("0");
			gasData3.setText("0");
			GasMainActivity.getIntances().initXSGASValue();
		}
	}

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1){
				if(msg.arg1<65535){
					db.updateGAS_CALIBRATION1(gasData1.getText().toString(),msg.arg1+"");
					Toast.makeText(getActivity().getApplicationContext(), "甲烷零点标定成功", 0).show();
					GasMainActivity.getIntances().initXSGASValue();
				}else{
					Toast.makeText(getActivity().getApplicationContext(), "甲烷零点标定失败,当前电压大于最大值", 0).show();
				}
			}
			if(msg.what==2){
				if(msg.arg1<65535){
					db.updateGAS_CALIBRATION2(gasData2.getText().toString(),msg.arg1+"");
					Toast.makeText(getActivity().getApplicationContext(), "甲烷标气标定成功", 0).show();
					GasMainActivity.getIntances().initXSGASValue();
				}else{
					Toast.makeText(getActivity().getApplicationContext(), "甲烷标气标定失败,当前电压大于最大值", 0).show();	
				}
			}
		};
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView==cb_1){
			if(isChecked){
				cb_2.setChecked(false);
				cb_3.setChecked(false);
			}
		}
		if(buttonView==cb_2){
			if(isChecked){
				cb_1.setChecked(false);
				cb_3.setChecked(false);
			}
		}
		if(buttonView==cb_3){
			if(isChecked){
				cb_1.setChecked(false);
				cb_2.setChecked(false);
			}
		}
	}


}
