package com.tk.ch4gas;

import com.ramy.minervue.R;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android_serialport_api.GasDetector;
import android_serialport_api.SerialCmdUtils;

public class GasAboutFragement extends Fragment implements OnClickListener{
	private TextView tv_version;
    public static final String ARG_SECTION_NUMBER = "section_number";
    public GasDetector gasDetector;
    public GasAboutFragement() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	gasDetector = GasMainActivity.getIntances().getGasDetector();
    	gasDetector.open();
    	
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        tv_version = (TextView) rootView.findViewById(R.id.tv_version);
        String version = "";
        try {
			PackageManager manager = getActivity().getPackageManager();
			version = manager.getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        tv_version.setText(tv_version.getText()+""+version);
        readVersion();
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
		
	}

	public void readVersion(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte [] cmd = new byte[]{(byte) 0xfc,0x09,0x05,0x10,0x00};
				cmd[4] = SerialCmdUtils.addBuffer(cmd);
				gasDetector.SerialPointCommunication(cmd);
			}
		}).start();
	}

}
