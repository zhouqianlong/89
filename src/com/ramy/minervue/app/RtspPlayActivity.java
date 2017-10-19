package com.ramy.minervue.app;

import com.ramy.minervue.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

public class RtspPlayActivity extends Activity implements OnClickListener {
	Button btn_playButton ;
	VideoView videoView ;
	String rtspUrl  = "rtsp://192.168.1.32:554";
//	String rtspUrl  = "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rtsp_play_activity);
		rtspUrl = getIntent().getExtras().getString("url");

		videoView = (VideoView)this.findViewById(R.id.videoView);
		videoView.setVideoURI(Uri.parse(rtspUrl));
		videoView.requestFocus(); 
		videoView.start();
	}

	//play rtsp stream
	private void PlayRtspStream(String rtspUrl){
		videoView.setVideoURI(Uri.parse(rtspUrl));
		videoView.requestFocus(); 
		videoView.start();
	}

	@Override
	public void onClick(View v) {
		PlayRtspStream(rtspUrl);
	}
}
