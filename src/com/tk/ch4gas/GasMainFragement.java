package com.tk.ch4gas;


import java.text.SimpleDateFormat;
import java.util.Date;

import com.ramy.minervue.R;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.GasDetector;
import android_serialport_api.SerialCmdUtils;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class GasMainFragement extends Fragment implements OnClickListener{
	public GasDetector detector;
	ProgressDialog dialog = null;
	private Button btn_test,btn_add,btn_minus;
	private TextView tv_gasdata;
	private SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss");
	public static float gasInitGAS = 0;
//	public static float gasDATA1 = 0;//ת��
	public static float gasDATA_CHECK = 0;//У���

	private boolean threadRunning = false; 

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	public GasMainFragement() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		list = GasMainActivity.getIntances().list;
		detector = GasMainActivity.getIntances().getGasDetector();
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		btn_add = (Button) rootView.findViewById(R.id.btn_add);
		btn_minus = (Button) rootView.findViewById(R.id.btn_minus);
		btn_test = (Button) rootView.findViewById(R.id.btn_test);
		btn_test.setOnClickListener(this);
		btn_minus.setOnClickListener(this);
		btn_add.setOnClickListener(this);
		tv_gasdata = (TextView) rootView.findViewById(R.id.tv_gasdata);
		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		threadRunning = false;
		detector.close();
	}


	@Override
	public void onClick(View v) {
		if(v==btn_test){
			if(threadRunning){
				btn_test.setText("����ȡ�����....");
				btn_test.setEnabled(false);
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							threadRunning=false;//�ر�ѭ��
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mHandler.sendEmptyMessage(3);
					}
				}).start();
			
			}else{
				dialog = ProgressDialog.show(getActivity(), "��ʾ", "���ڶ�ȡ������...���Ժ�", false, false);  
				threadRunning=true;
				btn_test.setText("�����������....");
				startSeriaCMD();
			}
		}
		
		if(btn_minus==v){
			count-=1000;
		}
		if(btn_add==v){
			count+=1000;
		}
			
	}
	int count=0;
	private void startSeriaCMD() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				detector.open();
				int errorCount = 0 ; 
				while (threadRunning) {
					detector.SerialPointCommunication(SerialCmdUtils.READ_CH4_PPM);
//					detector.setTestData(3000+count);
					if(detector.getLastReplyCmd()==GasDetector.CMD_READ_CH4_SUCCE){
						errorCount = 0;
						Message msg = Message.obtain();
						msg.obj =  detector.getLastResponseGasData();
						msg.what=1;
						mHandler.sendMessage(msg);
					}else{
						errorCount++;
						if(errorCount>2){
							Message msg = Message.obtain();
							msg.what=2;  
							mHandler.sendMessage(msg);
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				detector.close();
			}
		}).start();
	}
	public float [] list;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			try {
				if(msg.what==1){
					if(GasMainActivity.getIntances()!=null){
						tv_gasdata.setText(
								"\n��ʼAD:"+gasInitGAS+
								"\nУ��Ũ��:"+gasDATA_CHECK+"%" +
								"\n	���궨Ũ��ֵ:"+SerialCmdUtils.getPercentageByPPM(GasMainActivity.getIntances().zero)+"%"+
								"\n	�����궨Ũ��ֵ:"+SerialCmdUtils.getPercentageByPPM(GasMainActivity.getIntances().biaoqi)+"%"+
								"\n	���ʵ��ֵADֵ:"+GasMainActivity.getIntances().zero_+
								"\n	����ʵ��ֵADֵ:"+GasMainActivity.getIntances().biaoqi_+
								"\nϵ��:"+GasMainActivity.getIntances().getBJ()+
								"\n���ʱ��:"+sdf.format(new Date()) 
								);
						if(dialog!=null){
							dialog.dismiss();
							dialog = null;
						}
					}
				}
				if(msg.what==2){
					if(dialog!=null){
						dialog.dismiss();
					}
					tv_gasdata.setText("���ʧ��,������·�Ƿ���������!");	
				}
				if(msg.what==3){
					btn_test.setText("������");
					tv_gasdata.setText("�������水ť���м��\n����Ũ������:��0-4%��");
					btn_test.setEnabled(true);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	};

}