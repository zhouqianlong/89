package com.ramy.minervue.app;

import h264.com.H264Android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.app.MainActivity.WifiInfoViewListener;
import com.ramy.minervue.camera.MyCamera;
import com.ramy.minervue.camera.color.ColorUtil;
import com.ramy.minervue.camera.color.YUV420Converter;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.media.VideoFrameConsumer;
import com.ramy.minervue.media.VideoCodec.FrameFetcher;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.VibratorUtil;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex;
import com.wifitalk.Activity.SendSoundsThread_oldSpeex;
import com.wifitalk.Activity.ReceiveSoundsThread_oldSpeex.PrepareCallLintener;
import com.wifitalk.Activity.RemoteVideoThread;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.Coordinates;
import com.wifitalk.Utils.DataPacket;
import com.wifitalk.Utils.ProgressDialogUtils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodec.CryptoException;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint({ "NewApi", "HandlerLeak" }) 
public class CameraActivity extends Activity implements SurfaceHolder.Callback,Camera.PreviewCallback,OnClickListener,RemoteVideoThread.UdpFrameCallback,WifiInfoViewListener,PrepareCallLintener, SurfaceTextureListener{
	private int direction = 0;//摄像头方向 
	private static final int TIME_OUT = 100000;
	private TextureView textureView;
	private SurfaceTexture surfaceTexture;
	private Surface textureSurface; 
	public AudioManager audioManager = MainService.getInstance().getAudioManager();
	MediaCodec.BufferInfo bufferInfo = null;
	private boolean isCapturing = false;
//	private MyFrameFetcher frameFetcher = null;
	public static CameraActivity instances;
	private static final String TAG = "StudyCamera";
	private static final int FRAME_RATE = 30;
	public String host= "192.168.1.141";
	private byte [] desBuf;//摄像头旋转90°数据
	private static final short REMOTE_HOST_PORT = 5000;
	private static final String H264FILE = "/sdcard/test.h264";
	private boolean bOpening = false;
	private SurfaceView surfaceView ;
	private SurfaceHolder surfaceHolder;
	private SurfaceView remotsurfaceView ;
	private SurfaceHolder remotsurfaceHolder;
	private boolean isTask = false;
	private Surface	mSurface;
	private Surface	remortSurface;
	private Camera 	mCamera;
	private long sendData = 0;
			private int		mCameraWidth = 352 ,mCameraHeight = 288;
//	private int		mCameraWidth = 640 ,mCameraHeight = 480;
	//	private int		mCameraWidth = 320 ,mCameraHeight = 240;
	//	private int		mSurfaceWidth,mSurfaceHeight;
	private MediaCodec mMediaEncoder; 
	private MediaCodec mMediaDecoder;
	private Paint 		paint;
	private Camera.Parameters parameters = null;
	private InetAddress address;
	private InetAddress addressHost;
	private DatagramSocket socket;
	private UdpSendTask netSendTask;
	private int colorFormat; 
	private int mFrameIndex = 0;
	private byte[] mEncoderH264Buf;
	private byte[] mMediaHead = null;  
	private MediaRecorder mediarecorder;
	//	private byte[] mYuvBuffer = new byte[mCameraWidth*mCameraHeight*3/2];
	private Bitmap bitmap;
	public H264Android h264Android = H264Android.getInstances();
	Button btn_sendOrClose;
	public UserBean userBean = null;
	EditText et_ip;
	com.wifitalk.Utils.Coordinates coordinates;
	private YUV420Converter colorConverter;
	private TextView tv_wifi_info,tv_call_info,tv_speak_info;
	private Button btn_qiehuan,btn_end,btn_call,btn_swtich;
	private boolean isSend;
	private boolean lastSHUAXIN; 
	private int bottonWidth = 0;
	private int bottonHeight = 0;
	private boolean call_btn_statu = false;
	private TextView tv_speak_info_btn;
	private Button sw_led;
	private ImageView iv_setting_audio;
	
	public Matrix getMatrixFor(int targetWidth, int targetHeight) {
		Matrix transform = new Matrix();
		float fromWHR = (float) currentSize.width / currentSize.height;
		float toWHR = (float) targetWidth / targetHeight;
		if (fromWHR > toWHR) {
			transform.setScale(1, toWHR / fromWHR, targetWidth / 2, targetHeight / 2);
		} else {
			transform.setScale(fromWHR / toWHR, 1, targetWidth / 2, targetHeight / 2);
		}
		return transform;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ReceiveSoundsThread_oldSpeex.videoStatu = true;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ReceiveSoundsThread_oldSpeex.count = 0;
		instances = this;
		try {
			MainService.getInstance().getReceiveSoundsThread().setGPSCallBackListener(this);
			MainActivity.getInstance.setwifiInfoViewListener(this);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camera);
		tv_speak_info_btn = (TextView) findViewById(R.id.tv_speak_info_btn);
		tv_wifi_info = (TextView) findViewById(R.id.tv_wifi_info);
		tv_call_info = (TextView) findViewById(R.id.tv_call_info);
		btn_qiehuan = (Button) findViewById(R.id.btn_qiehuan);
		btn_call = (Button) findViewById(R.id.btn_call);
		btn_end = (Button) findViewById(R.id.btn_end);
		btn_swtich = (Button) findViewById(R.id.btn_swtich);
		sw_led = (Button) findViewById(R.id.sw_led);
		iv_setting_audio = (ImageView) findViewById(R.id.iv_setting_audio);
		iv_setting_audio.setOnClickListener(this);
		sw_led.setVisibility(View.GONE);
		sw_led.setOnClickListener(this);
		btn_qiehuan.setOnClickListener(this);
		btn_end.setOnClickListener(this);
		btn_swtich.setOnClickListener(this);
		Chronometer  timer = (Chronometer)this.findViewById(R.id.chronometer);  
		timer.start();
		et_ip = (EditText) findViewById(R.id.et_ip);
		tv_speak_info = (TextView) findViewById(R.id.tv_speak_info);
		lastSHUAXIN = MainService.SHUAXIN;
		textureView = (TextureView) findViewById(R.id.textureview);
		textureView.setRotation(-90);
		textureView.setSurfaceTextureListener(this);
		coordinates = (Coordinates) findViewById(R.id.coordinates);
		//		coordinates.setRotation(180);
		btn_call.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					MainService.getInstance().sendSoundsThread.setMacStatu(true);//开启mac风
					MainService.getInstance().sendSoundsThread(true);
					audioManager.setMicrophoneMute(false);//开启mic
					btn_call.setBackgroundResource(R.drawable.call_blue_down);
					tv_speak_info_btn.setText("请讲话");
					call_btn_statu=true;
					btn_qiehuan.setText("单工");
				}
				else if (event.getAction() == MotionEvent.ACTION_UP)
				{
//					call_btn_statu=false;
//					tv_speak_info_btn.setText("按住讲话");
//					MainService.getInstance().sendSoundsThread.setMacStatu(false);
//					btn_call.setBackgroundResource(R.drawable.call_blue_up);
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							try {
//								audioManager.setMicrophoneMute(true);// 关闭mic
//								Thread.sleep(300);
//								MainService.getInstance().sendSoundsThread(false,MainService.getInstance().pindao);
//								Thread.sleep(100);
//								MainService.getInstance().getsendSoundsThread().endPackage();	
//								Thread.sleep(200);
//								audioManager.setMicrophoneMute(false);// 开启mic
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							//					VibratorUtil.Vibrate(PrepareCallCheckUser.this, 100); // 震动100ms
//						}
//					}).start();
					tv_speak_info_btn.setText("按住讲话");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(300);
								call_btn_statu=false;
								MainService.getInstance().sendSoundsThread.setMacStatu(false);
								MainService.getInstance().sendSoundsThread(false);
								MainService.getInstance().getsendSoundsThread().endPackage();	
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
					
					
			
				
					
				}
				return false;
			}
		});
		try {
			userBean = (UserBean) getIntent().getExtras().getSerializable("UserBean");
			isTask = getIntent().getExtras().getBoolean("isTask");
			if(userBean!=null){
				host = userBean.getUserIp();
				et_ip.setText(host);
				tv_call_info.setText("与"+userBean.getUsername()+"通话中");
			}else{
				et_ip.setText(MainService.getInstance().getIPadd());
				host = et_ip.getText().toString();
			}
		} catch (Exception e1) {
			et_ip.setText(MainService.getInstance().getIPadd());
			host = et_ip.getText().toString();
			isTask = false;
		}

		if(!setupView()){
			Log.e(TAG, "failed to setupView");
			return;
		}	
		btn_sendOrClose = (Button) findViewById(R.id.btnOpen);
		btn_sendOrClose.setOnClickListener(this);
		RemoteVideoThread.setUdpFrameback(this);


		bitmap = Bitmap.createBitmap(mCameraWidth, mCameraHeight, Bitmap.Config.RGB_565);		
		h264Android.JInitDecoder(mCameraWidth, mCameraHeight);  

		paint = new Paint();
		mMediaEncoder = null;
		mMediaDecoder = null;
		mSurface = null;
		remortSurface = null;

		mEncoderH264Buf = new byte[10240];

		netSendTask = new UdpSendTask();
		netSendTask.init();
		netSendTask.start();
		netSendTask.isSend=true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mHander.sendEmptyMessage(1);
			}
		}).start();


	}

	Handler mHander = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==200){
				textureView.setRotation(0);
				textureView.setRotation(getDirection(dirver));
				return;
			}
			try {
				if(!setupCamera(CameraInfo.CAMERA_FACING_FRONT)){
					Log.e(TAG, "failed to setupCamera");
					return;
				}
			} catch (Exception e2) {
				finish();
			}
			if(!startCamera()){
				Log.e(TAG, "failed to openCamera");
				return;
			}
			if(isCapturing==false){
				 
				for( int i=0; i <300;i++){
					try {
						setupEncoder();
						myFrameFetcher.start();
						ProgressDialogUtils.dismissProgressDialog();
						break;
					} catch (Exception e) {
						try {
							MainActivity.frameIng = false;
							Log.e(TAG, e.toString()+"==重新尝试  setupEncoder ");
							ProgressDialogUtils.showProgressDialog(CameraActivity.this, "初始化编码出现异常,重新尝试"+i);
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
				}
			}


		}; 
	};

	@Override
	public void onClick(View view)
	{  
		if(view==sw_led){


			new Thread(new Runnable() {
				@Override
				public void run() {
					if(sw_led.getTag()==null){
						try{

							Camera.Parameters mParameters;
							mParameters = mCamera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							mCamera.setParameters(mParameters);
							sw_led.setTag(this);
							sw_led.setTextColor(Color.RED);
						} catch(Exception ex){}
					}else{
						try{
							Camera.Parameters mParameters;
							mParameters = mCamera.getParameters();
							mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							mCamera.setParameters(mParameters);
							sw_led.setTag(null);
							sw_led.setTextColor(Color.WHITE);
						} catch(Exception ex){}
					}					
				}
			}).start();




		}

		 
		
		if(view ==btn_qiehuan){
			if(btn_qiehuan.getText().equals("单工")){//切换双工
				btn_qiehuan.setText("双工");
				audioManager.setMicrophoneMute(false);//开启mic
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {  
					e.printStackTrace();
				}
				MainService.SHUAXIN = false;//不刷新
				MainService.getInstance().sendSoundsThread(true);
				SendSoundsThread_oldSpeex.shuanggong = true;
				MainService.getInstance().getsendSoundsThread().setMacStatu(true);

			}else{//切换单工
				btn_qiehuan.setText("单工");
				audioManager.setMicrophoneMute(true);//关闭mic
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				MainService.getInstance().sendSoundsThread(false);
				MainService.getInstance().getsendSoundsThread().setMacStatu(false);
				SendSoundsThread_oldSpeex.shuanggong= false;

			}
		}

		if(btn_end==view){
			finish();
		}



		if(iv_setting_audio == view){
			//			if (!MainService.JINYIN) {
			//				MainService.JINYIN = true;
			//				showTextToast("已开启声音", 1);
			//			} else {
			//				showTextToast("已关闭声音", 1);
			//				MainService.JINYIN = false;
			//			}

			//			audioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			audioManager.adjustStreamVolume (AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);

		}


		if(btn_swtich == view){
			stopCamera();
			if(mCamera!=null){
				mCamera.release();
			}
			if(swtichcount%2==0){
				try {
					setupCamera(CameraInfo.CAMERA_FACING_BACK);
				} catch (Exception e1) {
					finish();
				}
				sw_led.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {

					@Override
					public void run() {
						DatagramSocket clientSocket = null;
						try {
							clientSocket = new DatagramSocket();
							StringBuffer str = new StringBuffer("VideoRoat:"
									+ MainService.getInstance().getIPadd());
							// 构建数据包 头+体
							DataPacket dataPacket = new DataPacket(str.toString()
									.getBytes(), new byte[] { 01, 01, 01 });
							// // 构建数据报 +发送
							clientSocket.send(new DatagramPacket(dataPacket
									.getAllData(), dataPacket.getAllData().length,
									InetAddress.getByName(userBean.getUserIp()), AppConfig.PortAudio));
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (clientSocket != null) {
								clientSocket.close();
							}
						}
					}
				}).start();
			}else{
				sw_led.setVisibility(View.GONE);
				try {
					setupCamera(CameraInfo.CAMERA_FACING_FRONT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
			startCamera();
			swtichcount++;
		}
	}
	int swtichcount = 0;

	private Toast toast = null;
	private byte[] mYuvBuffer;

	/**
	 * 
	 * @param msg
	 *            内容
	 * @param i
	 *            显示时间 0 短时间 | 1 长时间
	 */
	private void showTextToast(String msg, int i) {
		try {
			if (toast == null) {
				toast = Toast.makeText(getApplicationContext(), msg, i);
			} else {
				toast.setText(msg);
			}
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {  
		Log.i(TAG, "surfaceCreated.");  
	}  

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,  
			int height) {  
		Log.i(TAG,"surfaceChanged w:"+width+" h:"+height);
		mSurface = surfaceHolder.getSurface();
	}  

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {  
		Log.i(TAG,"surfaceDestroyed");
		mSurface = null;

	}  

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instances = null;
		netSendTask.setSend(false);
		try {
			outputStream.flush();
			outputStream.close();
			ReceiveSoundsThread_oldSpeex.videoStatu = false;
			PrepareCallCheckUser.sendUDP("bye", host);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			if(isTask){
				MainService.getInstance().wakeAndUnlock(false);
			}
			MainService.SHUAXIN=lastSHUAXIN;
			MainActivity.getInstance.setwifiInfoViewListener(null);
			audioManager.setMicrophoneMute(true);//关闭mic
			MainService.getInstance().sendSoundsThread(false);
			MainService.getInstance().getReceiveSoundsThread().destoryLintener(this);
			SendSoundsThread_oldSpeex.shuanggong = false;
			ReceiveSoundsThread_oldSpeex.videoStatu =false;
			closePerview();
			releaseCamera();
			mMediaDecoder.stop();
			mMediaDecoder.release();
//			frameFetcher.cancel(true);
			
			MainActivity.frameIng = false;
//			myFrameFetcher.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ReceiveSoundsThread_oldSpeex.videoStatu = false;
		Log.i(TAG, "ondestory SUccess");
	}  

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	//	摄像头旋转90°
	public static void rotateYUV240SP(byte[] src,byte[] des,int width,int height)  
	{  

		int wh = width * height;  
		//旋转Y  
		int k = 0;  
		for(int i=0;i<width;i++) {  
			for(int j=0;j<height;j++)   
			{  
				des[k] = src[width*j + i];              
				k++;  
			}  
		}  

		for(int i=0;i<width;i+=2) {  
			for(int j=0;j<height/2;j++)   
			{     
				des[k] = src[wh+ width*j + i];      
				des[k+1]=src[wh + width*j + i+1];  
				k+=2;  
			}  
		}  

	}  
	@Override
	public void onPreviewFrame(final byte[] rawData, Camera camera){

		//		rotateYUV240SP(rawData, desBuf,currentSize.width, currentSize.height);

		if (isCapturing&&encoderIng) { 
			try {
				if(colorFormat==19){
					final int index = mMediaEncoder.dequeueInputBuffer(TIME_OUT);
					if (isCapturing) {
						if (index >= 0) {
							try {
								inputBuffers[index].clear();
								colorConverter.convert(rawData);
								inputBuffers[index].put(rawData, 0, getCurrentFrameSize());
								mMediaEncoder.queueInputBuffer(index, 0, getCurrentFrameSize(),
										System.nanoTime() / 1000, 0);
							} catch (CryptoException e) {
								e.printStackTrace();
							}
						}
					}
				}else if(colorFormat==21){
					final int index = mMediaEncoder.dequeueInputBuffer(TIME_OUT);
					if(index<0)return;
					try {
						swapYV12toYUV420SemiPlanar(rawData, mYuvBuffer, currentSize.width, currentSize.height);
						inputBuffers[index].clear();
						inputBuffers[index].put(mYuvBuffer, 0, mYuvBuffer.length);
						mMediaEncoder.queueInputBuffer(index, 0, mYuvBuffer.length,
								System.nanoTime() / 1000, 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		camera.addCallbackBuffer(rawData);


	}

	private void swapYV12toYUV420SemiPlanar(byte[] yv12bytes, byte[] i420bytes, int width, int height){
		System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
		int startPos = width*height;
		int yv_start_pos_v =  width*height+width;
		int yv_start_pos_u =  width*height+width*height/4;
		for(int i = 0; i < width*height/4; i++){
			i420bytes[startPos + 2 * i + 0] = yv12bytes[yv_start_pos_u + i];
			i420bytes[startPos + 2 * i + 1] = yv12bytes[yv_start_pos_v + i];
		}
	}
	/**
	 * 
	 */
	private boolean setupView()
	{
		//		Log.d(TAG,"fall in setupView");


		closeRemote();
		closePerview();
		remotsurfaceView = (SurfaceView) findViewById(R.id.remotsurfaceView);
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);

		surfaceHolder = surfaceView.getHolder();
		remotsurfaceHolder = remotsurfaceView.getHolder();

		remotsurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		remotsurfaceHolder.addCallback(remoteCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		return true;
	}

	private void closePerview() {
		if (null != surfaceHolder) {
			surfaceHolder.removeCallback(this);
			surfaceView = null;
		}
		if (null != surfaceView) {
			surfaceView = null;
		}
	}

	private void closeRemote() {
		if (null != remotsurfaceHolder) {
			remotsurfaceHolder.removeCallback(this);
			surfaceView = null;
		}
		if (null != remotsurfaceView) {
			remotsurfaceView = null;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(!stopCamera()){
			//			Log.e(TAG, "failed to stopCamera");
			return;
		}
		finish();
	}
	//	public int[] getFpsRange() {
	//		return fpsRange;
	//	}

	private class MatchComparator implements Comparator<Camera.Size> {

		private int preferredWidth;
		private int preferredHeight;

		private int getDelta(Camera.Size size) {
			return Math.abs(preferredWidth - size.width)
					+ Math.abs(preferredHeight - size.height);
		}

		public MatchComparator(int preferredWidth, int preferredHeight) {
			this.preferredWidth = preferredWidth;
			this.preferredHeight = preferredHeight;
		}

		@Override
		public int compare(Camera.Size lhs, Camera.Size rhs) {
			return getDelta(rhs) - getDelta(lhs);
		}

	}

	private class MaxComparator implements Comparator<Camera.Size> {

		@Override
		public int compare(Camera.Size lhs, Camera.Size rhs) {
			return lhs.width * lhs.height - rhs.width * rhs.height;
		}

	}
	private int fpsRange[] = new int[2];
	//	private int fpsRange[] = new int[2];
	private Camera.Size currentSize;
	private int currentFrameSize;

	public Camera.Size getCurrentPreviewSize() {
		return currentSize;
	}
	public int getCurrentFrameSize() {
		return currentFrameSize;
	}
	public static int openCamera(int camera_Ori) { 
		Camera.CameraInfo info = new Camera.CameraInfo();
		int count = Camera.getNumberOfCameras();
		for (int i = 0; i < count; ++i) {
			Camera.getCameraInfo(i, info);
			if (info.facing == camera_Ori) {
				try {
					return i;
				} catch (RuntimeException e) {
					return -1;
				}
			}
		}
		return -1;
	}
	private boolean setupCamera(int cameraFacing) throws Exception{
		direction = cameraFacing;

		if (null != mCamera) {
			mCamera.release();
			mCamera = null;
		}
		if(openCamera(cameraFacing)>-1){
			mCamera = Camera.open(openCamera(cameraFacing)); // Turn on the camera  0代表前置
		} 
		Comparator<Camera.Size> comparator;
		comparator = new MatchComparator(mCameraWidth, mCameraHeight);
		parameters = mCamera.getParameters(); // Camera parameters to obtain
//		parameters.getPreviewFpsRange(fpsRange);
		//		parameters.setPreviewFpsRange(fpsRange[0], fpsRange[0]);
		//		parameters.setPreviewFrameRate(10);
		List<Size> listSize = parameters.getSupportedVideoSizes();
		for(int i=0;listSize != null && i<listSize.size();i++){
			Size size = listSize.get(i);
			Log.d(TAG, "supportedSize:"+size.width+"-"+size.height);
		}
		int width = mCameraWidth,height = mCameraHeight;
		parameters.setPreviewFormat(ImageFormat.YV12);
		parameters.setPreviewSize(width, height);
//		parameters.setPictureSize(width, height);
//		parameters.setJpegQuality(100);
		currentSize = Collections.max(parameters.getSupportedPreviewSizes(),comparator);
		currentFrameSize = ColorUtil.getFrameSize(currentSize, ImageFormat.YV12);
		mYuvBuffer = new byte[currentFrameSize];
		mCamera.setParameters(parameters); // Setting camera parameters
		mCamera.setPreviewCallbackWithBuffer(this);
		//alloc buffer for camera callback data with once.
		byte[] rawBuf = new byte[mCameraWidth * mCameraHeight * 3 / 2];  
		desBuf = new byte[currentFrameSize];  
		mCamera.addCallbackBuffer(desBuf);
		//		Matrix m = getMatrixFor(textureView.getWidth(), textureView.getHeight());
		//		textureView.setTransform(m);

		//unused preview display to surface view by first;
		try {
			mCamera.setPreviewDisplay(surfaceHolder); // Set Preview
			mCamera.setDisplayOrientation(90);
		} catch (IOException e) {
			Log.e(TAG,"failed to setPreviewDisplay");
			mCamera.release();// release camera  
			mCamera = null; 
			return false;
		}

		Log.d(TAG,"fall in setupCamera");
		return true;
	}

	private boolean startCamera(){
		try {
			if(bOpening)return false;
			mCamera.startPreview(); // Start Preview   
			bOpening = true;
			Log.d(TAG,"fall in startCamera");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	private boolean stopCamera(){
		Log.d(TAG,"fall in stop Camera");
		if(!bOpening)return false;

		mCamera.stopPreview();// stop preview 
		bOpening = false;
		return true;
	}

	private boolean releaseCamera()
	{
		try {
			mCamera.stopPreview();
			mCamera.release(); // Release camera resources  
			mCamera = null;
			Log.d(TAG,"fall in release Camera");
		} catch (Exception e) {
			Log.e(TAG,"fall in release Camera error EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
			e.printStackTrace();
		} 
		return true;
	}

	boolean encoderIng = false;
	private boolean setupEncoder() throws Exception{
		colorFormat = selectColorFormat(selectCodec("video/avc"), "video/avc");//高通返回21
		mYuvBuffer = new byte[currentSize.width*currentSize.height*3/2];
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", currentSize.width, currentSize.height);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);    
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, currentSize.width*currentSize.height*4);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
		mMediaEncoder = MediaCodec.createEncoderByType("video/avc");
		mMediaEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mMediaEncoder.start();
		inputBuffers = mMediaEncoder.getInputBuffers();
		outputBuffers = mMediaEncoder.getOutputBuffers();
		bufferInfo = new MediaCodec.BufferInfo();
		createfile();
		colorConverter = YUV420Converter.create(MyCamera.PREVIEW_FORMAT, currentSize);
		isCapturing = true;
		encoderIng = true;
		MainActivity.frameIng = true;
//		frameFetcher = new MyFrameFetcher();
//		frameFetcher.start();

		return true;

	} 



	private boolean setupDecoder(Surface surface,String mime,int width, int height){
		Log.d(TAG,"setupDecoder surface:"+surface+" mime:"+mime+" w:"+width+" h:"+height);
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime,width,height);		
		mMediaDecoder = MediaCodec.createDecoderByType(mime);
		if (mMediaDecoder == null) {
			Log.e("DecodeActivity", "createDecoderByType fail!");
			return false;
		}
		mMediaDecoder.configure(mediaFormat, textureSurface, null, 0);  
		//		mMediaDecoder.configure(mediaFormat, remortSurface, null, 0);  
		mMediaDecoder.start();
		return true;
	}

	ByteBuffer[] inputBuffers =null;
	ByteBuffer[] outputBuffers =null;
	public byte[] configbyte; 
	private BufferedOutputStream outputStream;
	private void createfile(){
		File file = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + "/zql2.h264");
		if(file.exists()){
			file.delete();
		}
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file));
		} catch (Exception e){ 
			e.printStackTrace();
		}
	}
//	private int offerEncoder(byte[] input,byte[] output) {
//		int pos = 0;          
//		try {
//			inputBuffers= mMediaEncoder.getInputBuffers();
//			outputBuffers = mMediaEncoder.getOutputBuffers();
//			int inputBufferIndex = mMediaEncoder.dequeueInputBuffer(-1);
//			if (inputBufferIndex >= 0) {
//				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//				Log.d(TAG,"offerEncoder InputBufSize: " +inputBuffer.capacity()+" inputSize: "+input.length + " bytes");
//				inputBuffer.clear();
//				inputBuffer.put(input);
//				mMediaEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, System.nanoTime() / 1000, 0);
//			} 
//			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//			int outputBufferIndex = mMediaEncoder.dequeueOutputBuffer(bufferInfo,-1);
//			while (outputBufferIndex >= 0) {
//				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//				byte[] data = new byte[bufferInfo.size];
//				outputBuffer.get(data);
//				Log.d(TAG,"offerEncoder InputBufSize:"+outputBuffer.capacity()+" outputSize:"+ data.length + " bytes written");
//				if(mMediaHead != null)  
//				{                 
//					System.arraycopy(data, 0,  output, pos, data.length);  
//					pos += data.length;  
//				} else // 保存pps sps 只有开始时 第一个帧里有， 保存起来后面用
//				{
//					ByteBuffer spsPpsBuffer = ByteBuffer.wrap(data);
//					if (spsPpsBuffer.getInt() == 0x00000001) {
//						mMediaHead = new byte[data.length];
//						System.arraycopy(data, 0, mMediaHead, 0, data.length);
//					} else {
//						//						Log.e(TAG,"not found media head.");
//						return -1;
//					}
//				}
//
//				mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
//				outputBufferIndex = mMediaEncoder.dequeueOutputBuffer(bufferInfo, 0);
//
//			} 
//			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//				outputBuffers = mMediaEncoder.getOutputBuffers();
//			}
//			if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//				mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
//			}
//			if(output[4] == 0x65) //key frame   编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上  
//			{
//				System.arraycopy(output, 0,  input, 0, pos);  
//				System.arraycopy(mMediaHead, 0,  output, 0, mMediaHead.length);  
//				System.arraycopy(input, 0,  output, mMediaHead.length, pos);  
//				pos += mMediaHead.length;  
//			}  
//
//		} catch (Exception t) {
//			t.printStackTrace();
//			Log.e(TAG,"error :"+t.getMessage()); 
//			mMediaEncoder = null;
//		}
//
//		Log.d(TAG,"offerEncoder 结束  pos:"+pos); 
//		return pos;
//
//	}


	//	硬解码功能  XML布局文件取消对应的注释
	private  boolean offerDecoder(byte[] input,int length) {
		try {
			ByteBuffer[] inputBuffers = mMediaDecoder.getInputBuffers();
			int inputBufferIndex = mMediaDecoder.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				long timestamp = mFrameIndex++ * 1000000 / FRAME_RATE;
				inputBuffer.clear();
				inputBuffer.put(input,0,length);
				mMediaDecoder.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = mMediaDecoder.dequeueOutputBuffer(bufferInfo,0);
			while (outputBufferIndex >= 0) {
				mMediaDecoder.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = mMediaDecoder.dequeueOutputBuffer(bufferInfo, 0);
			}
		} catch (Exception e) {
			mMediaDecoder = null;
			e.printStackTrace();
			return false;
		}
		return true;

	}


	//	private void startPlayH264File()
	//	{
	//		assert(mSurface != null);
	//		if(mMediaDecoder == null){
	//			if(!setupDecoder(mSurface,"video/avc",mSurfaceWidth,mSurfaceHeight)){
	//					Log.e(TAG, "failed to setupDecoder");
	//				return;
	//			}
	//		}
	//
	//		h264FileTask = new H264FileTask();
	//		h264FileTask.start();
	//	}


	private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)   
	{        
		System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
		System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);  
		System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);    
	}    



	/**
	 * Returns the first codec capable of encoding the specified MIME type, or null if no
	 * match was found.
	 */
	private static MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

			if (!codecInfo.isEncoder()) {
				continue;
			}

			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a color format that is supported by the codec and by this test code.  If no
	 * match is found, this throws a test failure -- the set of formats known to the test
	 * should be expanded for new platforms.
	 */
	private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
		for (int i = 0; i < capabilities.colorFormats.length; i++) {
			int colorFormat = capabilities.colorFormats[i];
			if (isRecognizedFormat(colorFormat)) {
				return colorFormat;
			}
		}
		Log.e(TAG,"couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
		return 0;   // not reached
	}

	/**
	 * Returns true if this is a color format that this test code understands (i.e. we know how
	 * to read and generate frames in this format).
	 */
	private static boolean isRecognizedFormat(int colorFormat) {
		switch (colorFormat) {
		// these are the formats we know how to handle for this test
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			return true;
		default:
			return false;
		}
	}
	class UdpSendTask extends Thread{
		private ArrayList<ByteBuffer> mList;
		public void init()
		{
			try {  
				socket = new DatagramSocket();  
				address = InetAddress.getByName(host);  
				addressHost = InetAddress.getByName("192.168.1.73");  
			} catch (SocketException e) {   
				e.printStackTrace();  
			} catch (UnknownHostException e) {
				e.printStackTrace();  
			}catch (Exception e) {
				finish();
			}
			mList = new ArrayList<ByteBuffer>();

		}
		public void pushBuf(byte[] buf,int len)
		{
			ByteBuffer buffer = ByteBuffer.allocate(len);
			buffer.put(buf,0,len);
			mList.add(buffer);
		}
		public boolean isSend = false;


		public void setSend(boolean isSend) {	
			this.isSend = isSend;
		}

		public  byte[] intToByteArray(int a) {  
			return new byte[] {  
					(byte) ((a >> 24) & 0xFF),  
					(byte) ((a >> 16) & 0xFF),     
					(byte) ((a >> 8) & 0xFF),     
					(byte) (a & 0xFF)  
			};  
		} 
		int count = 0 ;;
		@Override  
		public void run() {
			Log.d(TAG,"fall in udp send thread");
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isSend) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						long kb = sendData/1024;
						sendData = 0;
						Log.i("SUDU", "视频发送:"+kb+" KB/s");
					}
				}
			}).start();

			while(isSend){
				if(mList.size() <= 0){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				while(mList.size() > 0){
					try {         
						ByteBuffer sendBuf = mList.get(0);
						byte[] dire = intToByteArray(direction);//摄像头方向
						//						int jxsize = ((len[0]&0x00ff)<<24)+((len[1]&0x00ff)<<16)+((len[2]&0x00ff)<<8)+(len[3]&0x00ff);
						//						Log.i(TAG, "包长："+jxsize);
						byte[] lenPack = new byte[sendBuf.capacity()+4];
						System.arraycopy(dire, 0, lenPack, 0, 4);
						System.arraycopy(sendBuf.array(), 0, lenPack, 4, sendBuf.capacity());
						socket.send(new DatagramPacket(lenPack, lenPack.length,address, REMOTE_HOST_PORT));
						mList.remove(0);
						sendData+=lenPack.length;
						//						Log.d(TAG,"send udp packet len:"+sendBuf.capacity());
						//						 						DatagramPacket packet=new DatagramPacket(sendBuf.array(),sendBuf.capacity(), addressHost,REMOTE_HOST_PORT);  
						//						 						socket.send(packet);  
					} catch (Throwable t) {
						mList = new ArrayList<ByteBuffer>();
						t.printStackTrace();
					}
				}	
			}
		}  
	}


	Callback remoteCallback = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			closeRemote();
			remortSurface = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			remortSurface = remotsurfaceHolder.getSurface();

		}
	};
	byte [] h264out = new byte[mCameraWidth*mCameraHeight*2];
	Buffer buffer = ByteBuffer.wrap(h264out);
	int i = 0;
	int			mTrans		= 0x0F0F0F0F;
	boolean bFirst = true;
	boolean bFindPPS = true;
	int NalBufUsed = 0;
	int SockBufUsed = 0;
	int nalLen;
	int bytesRead = 0;
	byte[] NalBuf = new byte[1024*80]; //80k 
	int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain) {
		int i = 0;
		byte Temp;
		for (i = 0; i < SockRemain; i++) {
			Temp = SockBuf[i + SockBufUsed];
			NalBuf[i + NalBufUsed] = Temp;
			mTrans <<= 8;
			mTrans |= Temp;
			if (mTrans == 1) // 找到一个开始字
			{
				i++;
				break;
			}
		}
		return i;
	}
	private int getDirection(int cameraId){
		if(CameraInfo.CAMERA_FACING_FRONT == cameraId){
			return -90;
		}else if((CameraInfo.CAMERA_FACING_BACK == cameraId)){
			return 90;
		}else{
			return 0;
		}
	}
	 	/**
	 * Check if is H264 frame head
	 * 
	 * @param buffer
	 * @param offset
	 * @return whether the src buffer is frame head
	 */
	static boolean checkHead(byte[] buffer) {
		 int offset = 0;
		// 00 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 0 && buffer[3] == 1)
			return true;
		// 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 1)
			return true;
		return false;
	}
	int frameOffset = 0;
	int count = 0;
	int countSucc = 0;
	byte[] framebuffer = new byte[200000];
	int dirver =-1;//摄像头方向
	@Override
	public void onUdpFrame(byte[] data,int direction) {
		if(dirver==-1){
			dirver = direction;//第一次的加载方向
		}

		if(dirver!=direction){//方向发生变化
			dirver=direction;
			mHander.sendEmptyMessage(200);
		}

		if(mMediaDecoder == null&&textureSurface!=null){
			if(!setupDecoder(mSurface,"video/avc",mCameraWidth,mCameraHeight)){
				Log.e(TAG, "failed to setupDecoder");
				return;
			}
		}
		
//		count = data.length;
//		
//		// Fill frameBuffer
//		if (frameOffset + count < 200000) {
//			System.arraycopy(data, 0, framebuffer,
//					frameOffset, count);
//			frameOffset += count;
//		} else {
//			frameOffset = 0;
//			System.arraycopy(data, 0, framebuffer,
//					frameOffset, count);
//			frameOffset += count;
//		}
//		int offset = findHead(framebuffer, frameOffset);
//		while (offset > 0) {
//			if (checkHead(framebuffer, 0)) {
//				// Fill decoder
//				boolean flag = offerDecoder(framebuffer, offset);
//				if (flag) {
//					byte[] temp = framebuffer;
//					framebuffer = new byte[200000];
//					System.arraycopy(temp, offset, framebuffer,
//							0, frameOffset - offset);
//					frameOffset -= offset;
//					Log.e("Check", "is Head:" + offset);
//					// Continue finding head
//					offset = findHead(framebuffer, frameOffset);
//				}
//			
//			}else {
//				offset = 0;
//				frameOffset = 0;
//			}
//			
//		}
		
		if(checkHead(data)){
			offerDecoder(data, data.length);
		}

		//		bytesRead = data.length;
		//		SockBufUsed = 0;
		//		if (bytesRead <= 0)
		//			return;
		//		byte[] SockBuf = data;
		//		//		int i = 0;
		//		while (bytesRead - SockBufUsed > 0) {
		//			nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf , SockBufUsed, bytesRead-SockBufUsed);
		//			NalBufUsed += nalLen;
		//			SockBufUsed += nalLen;
		//			while(mTrans == 1)
		//			{
		//				mTrans = 0xFFFFFFFF;
		//				if(bFirst==true) // the first start flag
		//				{
		//					bFirst = false;
		//				}
		//				else  // a complete NAL data, include 0x00000001 trail.
		//				{
		//					if(bFindPPS==true) // true
		//					{
		//						if( (NalBuf[4]&0x1F) == 7 )
		//						{
		//							bFindPPS = false;
		//						}
		//						else
		//						{
		//							NalBuf[0]=0;
		//							NalBuf[1]=0;
		//							NalBuf[2]=0;
		//							NalBuf[3]=1;
		//							NalBufUsed=4;
		//
		//							break;
		//						}
		//					}
		//					//	decode nal
		//		int iTemp=h264Android.JDecoderNal(data, data.length, h264out);   
		//		if(iTemp>0){
		//			bitmap.copyPixelsFromBuffer(buffer);// makeBuffer(data565, N));
		//			buffer.position(0);
		//			Coordinates.getInstances.Draw(bitmap,System.currentTimeMillis());
		//		}
		//				}
		//				NalBuf[0]=0;
		//				NalBuf[1]=0;
		//				NalBuf[2]=0;
		//				NalBuf[3]=1;
		//				NalBufUsed=4;
		//			}		
		//		Log.d("TEST", "Ok"+iTemp);
		//		}

	}

	@Override
	public void wifiInfoChange(String msg, int color) {
		tv_wifi_info.setText(msg);
		tv_wifi_info.setTextColor(color);
	}

	@Override
	public void OnButtonChangeListener(boolean statu, String info) {
		setSpeakWifi(statu, info);
	}
	public void setSpeakWifi(boolean isClose, String speakInfo) {
		if (isClose == true) {
			btn_call.setBackgroundResource(R.drawable.call_red);
			//			btn_call.setEnabled(false);
			tv_speak_info_btn.setVisibility(View.INVISIBLE);
			//			MainService.getInstance().getsendSoundsThread().setRunning(false);
			tv_speak_info.setText(speakInfo);
			tv_speak_info.setVisibility(View.VISIBLE);
		} else {
			//			btn_call.setBackgroundResource(R.drawable.call_blue_up);
			//			btn_call.setEnabled(true);
			if(call_btn_statu ){
				btn_call.setBackgroundResource(R.drawable.call_blue_down);
			}else{
				btn_call.setBackgroundResource(R.drawable.call_blue_up);
			}
			tv_speak_info.setText("");
			tv_speak_info.setVisibility(View.VISIBLE);
			tv_speak_info_btn.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		surfaceTexture = textureView.getSurfaceTexture();
		textureSurface = new Surface(surfaceTexture);
	}
	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub

	}


	public Thread myFrameFetcher = new Thread(new Runnable() {
		private InetAddress addressHost;
		@Override
		public void run() {
			try {
				MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
				while (MainActivity.frameIng) {
					try {
						int pos = 0;         
						int index = mMediaEncoder.dequeueOutputBuffer(info, TIME_OUT);
						if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							mMediaEncoder.releaseOutputBuffer(index, false);
							break;
						}
						if (index >= 0) {
							try {

								ByteBuffer buffer = outputBuffers[index];
								buffer.position(info.offset);//设置此缓冲区的位置：0
								buffer.limit(info.size + info.offset);//设置缓冲区大小
								byte[] data = new byte[info.size + info.offset];
								byte[] output = new byte[info.size + info.offset];
								buffer.get(data);
								outputStream.write(data);//本地写文件


								/*******************添加 pps sps***************/
								//							if(mMediaHead != null){                 
								//								System.arraycopy(data, 0,  output, pos, data.length);  
								//								pos += data.length;  
								//							}else{
								//								ByteBuffer spsPpsBuffer = ByteBuffer.wrap(data);
								//								if (spsPpsBuffer.getInt() == 0x00000001) {
								//									mMediaHead = new byte[data.length];
								//									System.arraycopy(data, 0, mMediaHead, 0, data.length);
								//								} 
								//							} 
								//							if(output[4] == 0x65) //key frame   编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上  
								//							{
								//								System.arraycopy(output, 0,  data, 0, pos);  
								//								System.arraycopy(mMediaHead, 0,  output, 0, mMediaHead.length);  
								//								System.arraycopy(data, 0,  output, mMediaHead.length, pos);  
								//								pos += mMediaHead.length;  
								//							} 
								if(pos>0){
									/*******************添加 pps sps***************/
									netSendTask.pushBuf(output,pos);
								}else{
									netSendTask.pushBuf(data,data.length);
									//								DatagramPacket packet=new DatagramPacket(data,data.length, addressHost,5000);//发送vlc测试
									//								socket.send(packet);
								}


							} catch (Exception e) {
								e.printStackTrace();
							} 
							mMediaEncoder.releaseOutputBuffer(index, false);

						} else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
							outputBuffers = mMediaEncoder.getOutputBuffers();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				
				}
				Log.i(TAG, "frameIng stop success");
				mMediaEncoder.stop();
				mMediaEncoder.release();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
	});
		
		
	
//	public class MyFrameFetcher extends ATask<Void, Void, Void> {
//		
//		
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//			try {
//				socket = new DatagramSocket(); 
//				addressHost = InetAddress.getByName("192.168.1.73");
//			} catch (UnknownHostException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (SocketException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			while (frameIng) {
//				try {
//					if(encoderIng==false){
//						Thread.sleep(100);
//						continue;
//					}
//					int pos = 0;         
//					int index = mMediaEncoder.dequeueOutputBuffer(info, TIME_OUT);
//					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//						mMediaEncoder.releaseOutputBuffer(index, false);
//						break;
//					}
//					if (index >= 0) {
//						try {
//
//							ByteBuffer buffer = outputBuffers[index];
//							buffer.position(info.offset);//设置此缓冲区的位置：0
//							buffer.limit(info.size + info.offset);//设置缓冲区大小
//							byte[] data = new byte[info.size + info.offset];
//							byte[] output = new byte[info.size + info.offset];
//							buffer.get(data);
//							outputStream.write(data);//本地写文件
//
//
//							/*******************添加 pps sps***************/
//							//							if(mMediaHead != null){                 
//							//								System.arraycopy(data, 0,  output, pos, data.length);  
//							//								pos += data.length;  
//							//							}else{
//							//								ByteBuffer spsPpsBuffer = ByteBuffer.wrap(data);
//							//								if (spsPpsBuffer.getInt() == 0x00000001) {
//							//									mMediaHead = new byte[data.length];
//							//									System.arraycopy(data, 0, mMediaHead, 0, data.length);
//							//								} 
//							//							} 
//							//							if(output[4] == 0x65) //key frame   编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上  
//							//							{
//							//								System.arraycopy(output, 0,  data, 0, pos);  
//							//								System.arraycopy(mMediaHead, 0,  output, 0, mMediaHead.length);  
//							//								System.arraycopy(data, 0,  output, mMediaHead.length, pos);  
//							//								pos += mMediaHead.length;  
//							//							} 
//							if(pos>0){
//								/*******************添加 pps sps***************/
//								netSendTask.pushBuf(output,pos);
//							}else{
//								netSendTask.pushBuf(data,data.length);
//								//								DatagramPacket packet=new DatagramPacket(data,data.length, addressHost,5000);//发送vlc测试
//								//								socket.send(packet);
//							}
//
//
//						} catch (Exception e) {
//							e.printStackTrace();
//						} 
//						mMediaEncoder.releaseOutputBuffer(index, false);
//
//					} else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//						outputBuffers = mMediaEncoder.getOutputBuffers();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			return null;
//		}
//
//	}

}
