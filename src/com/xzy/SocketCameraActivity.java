package com.xzy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.ramy.minervue.R;
import com.wifitalk.Utils.Coordinates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

//public class SocketCameraActivity extends Activity implements SurfaceHolder.Callback,
//Camera.PreviewCallback,GPGReceiveVideosThread.UdpFrameCallback{		
//	private SurfaceView oSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
//	private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
//	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
//	private SurfaceHolder  sfh;   //对方的画面
//	private Camera mCamera = null; // Camera对象，相机预览 
//	private Bitmap bitmap;
//	/**服务器地址*/
//	private String pUsername="XZY";
//	/**服务器地址*/
//	private String serverUrl="192.168.1.100";
//	/**服务器端口*/
//	private int serverPort=8888;
//	/**视频刷新间隔*/
//	private int VideoPreRate=1;
//	/**当前视频序号*/
//	private int tempPreRate=0;
//	/**视频质量*/
//	private int VideoQuality=85;
//
//	/**发送视频宽度比例*/
//	private float VideoWidthRatio=1;
//	/**发送视频高度比例*/
//	private float VideoHeightRatio=1;
//
//	/**发送视频宽度*/
//	private int VideoWidth=320;
//	/**发送视频高度*/
//	private int VideoHeight=240;
//	/**视频格式索引*/
//	private int VideoFormatIndex=0;
//	/**是否发送视频*/
//	private boolean startSendVideo=false;
//	/**是否连接主机*/
//	private boolean connectedServer=false;
//
//	private Button  myBtn02;
//	private String TAG= "SocketCameraActivity";
//	/** Called when the activity is first created. */
//	DoThings doThings;
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.videomain);
//		GPGReceiveVideosThread.setUdpFrameback(this);
//		//禁止屏幕休眠
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//		mSurfaceview = (SurfaceView) findViewById(R.id.camera_preview);
//		oSurfaceview = (SurfaceView) findViewById(R.id.duifang);
//		sfh = oSurfaceview.getHolder();  
//		//对 surfaceView 进行操作  
//		doThings = new DoThings();
//
//		sfh.addCallback(doThings);// 自动运行surfaceCreated以及surfaceChanged  
//
//		myBtn02=(Button)findViewById(R.id.button2);
//
//		//开始连接主机按钮
//		myBtn02.setOnClickListener(new OnClickListener(){
//			public void onClick(View v) {
//				if(startSendVideo)//停止传输视频
//				{
//					startSendVideo=false;
//					myBtn02.setText("开始传输");
//				}
//				else{ // 开始传输视频
//					startSendVideo=true;
//					myBtn02.setText("停止传输");
//				}
//			}});
//	}
//
//	@Override
//	public void onStart()//重新启动的时候
//	{	
//		mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
//		mSurfaceHolder.addCallback(this); // SurfaceHolder加入回调接口       
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 设置显示器类型，setType必须设置
//		//读取配置文件
//		SharedPreferences preParas = PreferenceManager.getDefaultSharedPreferences(SocketCameraActivity.this);
//		pUsername=preParas.getString("Username", "XZY");
//		serverUrl=preParas.getString("ServerUrl", "192.168.0.100");
//		String tempStr=preParas.getString("ServerPort", "8888");
//		serverPort=Integer.parseInt(tempStr);
//		tempStr=preParas.getString("VideoPreRate", "1");
//		VideoPreRate=Integer.parseInt(tempStr);	            
//		tempStr=preParas.getString("VideoQuality", "85");
//		VideoQuality=Integer.parseInt(tempStr);
//		tempStr=preParas.getString("VideoWidthRatio", "100");
//		VideoWidthRatio=Integer.parseInt(tempStr);
//		tempStr=preParas.getString("VideoHeightRatio", "100");
//		VideoHeightRatio=Integer.parseInt(tempStr);
//		VideoWidthRatio=VideoWidthRatio/100f;
//		VideoHeightRatio=VideoHeightRatio/100f;
//		this.bitmap = Bitmap.createBitmap(VideoWidth, VideoHeight, Bitmap.Config.RGB_565);
//		super.onStart();
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();        
//		InitCamera();
//	}
//
//	/**初始化摄像头*/
//	private void InitCamera(){
//		try{
//			mCamera = Camera.open();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		try{
//			if (mCamera != null) {
//				mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
//				mCamera.stopPreview();
//				mCamera.release();
//				mCamera = null;
//			} 
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//	}
//	@Override
//	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
//		// TODO Auto-generated method stub
//		if (mCamera == null) {
//			return;
//		}
//		mCamera.stopPreview();
//		mCamera.setPreviewCallback(this);
//		mCamera.setDisplayOrientation(90); //设置横行录制
//		//获取摄像头参数
//		Camera.Parameters parameters = mCamera.getParameters();
//		Size size = parameters.getPreviewSize();
//		VideoWidth=size.width;
//		VideoHeight=size.height;
//		VideoFormatIndex=parameters.getPreviewFormat();
//
//		mCamera.startPreview();
//	}
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		try {
//			if (mCamera != null) {
//				mCamera.setPreviewDisplay(mSurfaceHolder);
//				mCamera.startPreview();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		if (null != mCamera) {
//			mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
//			mCamera.stopPreview();
//			mCamera.release();
//			mCamera = null;
//		}
//	}
//
//	@Override
//	public void onPreviewFrame(byte[] data, Camera camera) {
//		// TODO Auto-generated method stub
//		//如果没有指令传输视频，就先不传
//		if(!startSendVideo)
//			return;
//		if(tempPreRate<VideoPreRate){
//			tempPreRate++;
//			return;
//		}
//		tempPreRate=0;		
//		try {
//			if(data!=null)
//			{
//				YuvImage image = new YuvImage(data,VideoFormatIndex, VideoWidth, VideoHeight,null);
//				if(image!=null)
//				{
//					ByteArrayOutputStream outstream = new ByteArrayOutputStream();
//					//在此设置图片的尺寸和质量 
//					image.compressToJpeg(new Rect(0, 0, (int)(VideoWidthRatio*VideoWidth), 
//							(int)(VideoHeightRatio*VideoHeight)), VideoQuality, outstream);  
//					outstream.flush();
//					//启用线程将图像数据发送出去
//					Log.i(TAG, "ImageSize:"+outstream.size());
//					Thread th = new MySendFileThread(outstream,pUsername,serverUrl,serverPort);
//					th.start();  
//					
//					
////					 bitmap = BitmapFactory.decodeByteArray(outstream.toByteArray(), 0, outstream.size());
//					 
////					Buffer buffer = ByteBuffer.wrap(outstream.toByteArray());;
////					try {
////						bitmap.copyPixelsFromBuffer(buffer);// makeBuffer(data565, N));
////					} catch (Exception e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
////					buffer.position(0);
////					outstream.close();
//					
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void decodeToBitMap(byte[] data, Camera _camera) {
//		Size size = mCamera.getParameters().getPreviewSize();
//		try {
//		YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
//		size.height, null);
//		if (image != null) {
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		image.compressToJpeg(new Rect(0, 0, size.width, size.height),
//		80, stream);
//		Bitmap bmp = BitmapFactory.decodeByteArray(
//		stream.toByteArray(), 0, stream.size());
//		stream.close();
//		}
//		} catch (Exception ex) {
//		Log.e("Sys", "Error:" + ex.getMessage());
//		}
//		}
//	/**创建菜单*/    
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		menu.add(0,0,0,"系统设置");
//		menu.add(0,1,1,"关于程序"); 
//		menu.add(0,2,2,"退出程序"); 
//		return super.onCreateOptionsMenu(menu);
//	}
//	/**菜单选中时发生的相应事件*/  
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		super.onOptionsItemSelected(item);//获取菜单
//		switch(item.getItemId())//菜单序号
//		{
//		case 0:
//			//系统设置
//		{
//			Intent intent=new Intent(this,SettingActivity.class);
//			startActivity(intent);  
//		}
//		break;  
//		case 1://关于程序
//		{
////			new AlertDialog.Builder(this)
////			.setTitle("关于本程序")
////			.setMessage("本程序由武汉大学水利水电学院肖泽云设计、编写。\nEmail：xwebsite@163.com")
////			.setPositiveButton
////			(
////					"我知道了",
////					new DialogInterface.OnClickListener()
////					{						
////						@Override
////						public void onClick(DialogInterface dialog, int which) 
////						{
////						}
////					}
////					)
////					.show();
//		}
//		break;
//		case 2://退出程序
//		{
//			//杀掉线程强制退出
//			android.os.Process.killProcess(android.os.Process.myPid());
//		}
//		break;
//		}    	
//		return true;
//	}
//
//	/**发送文件线程*/
//	class MySendFileThread extends Thread{	
//		private String username;
//		private String ipname;
//		private int port;
//		private byte byteBuffer[] = new byte[1024];
//		//    	private OutputStream outsocket;	
//		private ByteArrayOutputStream myoutputstream;
//
//		public MySendFileThread(ByteArrayOutputStream myoutputstream,String username,String ipname,int port){
//			this.myoutputstream = myoutputstream;
//			this.username=username;
//			this.ipname = ipname;
//			this.port=port;
//			try {
//				myoutputstream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		public void run() {
//			try{
// 
//				DatagramSocket clientSocket = new DatagramSocket();
//				byte[] pack = myoutputstream.toByteArray();
//				int packLen = pack.length;
//				byte[] len = intToByteArray(packLen);
////				int jxsize = ((len[0]&0x00ff)<<24)+((len[1]&0x00ff)<<16)+((len[2]&0x00ff)<<8)+(len[3]&0x00ff);
////				Log.i(TAG, "包长："+jxsize);
//				byte[] lenPack = new byte[packLen+4];
//				System.arraycopy(len, 0, lenPack, 0, 4);
//				System.arraycopy(pack, 0, lenPack, 4, packLen);
//				clientSocket.send(new DatagramPacket(lenPack, lenPack.length,InetAddress.getByName(serverUrl), serverPort));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		public  byte[] intToByteArray(int a) {  
//		    return new byte[] {  
//		        (byte) ((a >> 24) & 0xFF),  
//		        (byte) ((a >> 16) & 0xFF),     
//		        (byte) ((a >> 8) & 0xFF),     
//		        (byte) (a & 0xFF)  
//		    };  
//		}  
//	}
//
//	@Override
//	public void onUdpFrame(byte[] data) {
//		bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//	}
//	public class DoThings implements SurfaceHolder.Callback{  
//		@Override  
//		public void surfaceChanged(SurfaceHolder holder, int format, int width,  
//				int height) {  
//			//在surface的大小发生改变时激发  
//		}  
//
//		@Override  
//		public void surfaceCreated(SurfaceHolder holder){  
//			new Thread(){  
//				public void run() {  
//					while(true){  
//						try {
//							//1.这里就是核心了， 得到画布 ，然后在你的画布上画出要显示的内容  
//							Canvas c = sfh.lockCanvas();  
//							//2.开画  
//							Paint  p =new Paint();  
//							
//							p.setColor(Color.rgb( (int)(Math.random() * 255),   
//									(int)(Math.random() * 255) ,  (int)(Math.random() * 255)));  
//							 
//								c.drawBitmap(bitmap, 0, 0, p);
//							//3. 解锁画布   更新提交屏幕显示内容  
//							sfh.unlockCanvasAndPost(c);
//						} catch (Exception e) {
//						}  
//					 
//					}  
//				};  
//			}.start();  
//
//		}  
//
//		@Override  
//		public void surfaceDestroyed(SurfaceHolder holder) {  
//			//销毁时激发，一般在这里将画图的线程停止、释放。  
//		}     
//
//
//
//	}  
//}