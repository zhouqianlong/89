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
//	private SurfaceView oSurfaceview = null; // SurfaceView����(��ͼ���)��Ƶ��ʾ
//	private SurfaceView mSurfaceview = null; // SurfaceView����(��ͼ���)��Ƶ��ʾ
//	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder����(����ӿ�)SurfaceView֧����
//	private SurfaceHolder  sfh;   //�Է��Ļ���
//	private Camera mCamera = null; // Camera�������Ԥ�� 
//	private Bitmap bitmap;
//	/**��������ַ*/
//	private String pUsername="XZY";
//	/**��������ַ*/
//	private String serverUrl="192.168.1.100";
//	/**�������˿�*/
//	private int serverPort=8888;
//	/**��Ƶˢ�¼��*/
//	private int VideoPreRate=1;
//	/**��ǰ��Ƶ���*/
//	private int tempPreRate=0;
//	/**��Ƶ����*/
//	private int VideoQuality=85;
//
//	/**������Ƶ��ȱ���*/
//	private float VideoWidthRatio=1;
//	/**������Ƶ�߶ȱ���*/
//	private float VideoHeightRatio=1;
//
//	/**������Ƶ���*/
//	private int VideoWidth=320;
//	/**������Ƶ�߶�*/
//	private int VideoHeight=240;
//	/**��Ƶ��ʽ����*/
//	private int VideoFormatIndex=0;
//	/**�Ƿ�����Ƶ*/
//	private boolean startSendVideo=false;
//	/**�Ƿ���������*/
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
//		//��ֹ��Ļ����
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//		mSurfaceview = (SurfaceView) findViewById(R.id.camera_preview);
//		oSurfaceview = (SurfaceView) findViewById(R.id.duifang);
//		sfh = oSurfaceview.getHolder();  
//		//�� surfaceView ���в���  
//		doThings = new DoThings();
//
//		sfh.addCallback(doThings);// �Զ�����surfaceCreated�Լ�surfaceChanged  
//
//		myBtn02=(Button)findViewById(R.id.button2);
//
//		//��ʼ����������ť
//		myBtn02.setOnClickListener(new OnClickListener(){
//			public void onClick(View v) {
//				if(startSendVideo)//ֹͣ������Ƶ
//				{
//					startSendVideo=false;
//					myBtn02.setText("��ʼ����");
//				}
//				else{ // ��ʼ������Ƶ
//					startSendVideo=true;
//					myBtn02.setText("ֹͣ����");
//				}
//			}});
//	}
//
//	@Override
//	public void onStart()//����������ʱ��
//	{	
//		mSurfaceHolder = mSurfaceview.getHolder(); // ��SurfaceView��ȡ��SurfaceHolder����
//		mSurfaceHolder.addCallback(this); // SurfaceHolder����ص��ӿ�       
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// ������ʾ�����ͣ�setType��������
//		//��ȡ�����ļ�
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
//	/**��ʼ������ͷ*/
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
//				mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
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
//		mCamera.setDisplayOrientation(90); //���ú���¼��
//		//��ȡ����ͷ����
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
//			mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
//			mCamera.stopPreview();
//			mCamera.release();
//			mCamera = null;
//		}
//	}
//
//	@Override
//	public void onPreviewFrame(byte[] data, Camera camera) {
//		// TODO Auto-generated method stub
//		//���û��ָ�����Ƶ�����Ȳ���
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
//					//�ڴ�����ͼƬ�ĳߴ������ 
//					image.compressToJpeg(new Rect(0, 0, (int)(VideoWidthRatio*VideoWidth), 
//							(int)(VideoHeightRatio*VideoHeight)), VideoQuality, outstream);  
//					outstream.flush();
//					//�����߳̽�ͼ�����ݷ��ͳ�ȥ
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
//	/**�����˵�*/    
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		menu.add(0,0,0,"ϵͳ����");
//		menu.add(0,1,1,"���ڳ���"); 
//		menu.add(0,2,2,"�˳�����"); 
//		return super.onCreateOptionsMenu(menu);
//	}
//	/**�˵�ѡ��ʱ��������Ӧ�¼�*/  
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		super.onOptionsItemSelected(item);//��ȡ�˵�
//		switch(item.getItemId())//�˵����
//		{
//		case 0:
//			//ϵͳ����
//		{
//			Intent intent=new Intent(this,SettingActivity.class);
//			startActivity(intent);  
//		}
//		break;  
//		case 1://���ڳ���
//		{
////			new AlertDialog.Builder(this)
////			.setTitle("���ڱ�����")
////			.setMessage("���������人��ѧˮ��ˮ��ѧԺФ������ơ���д��\nEmail��xwebsite@163.com")
////			.setPositiveButton
////			(
////					"��֪����",
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
//		case 2://�˳�����
//		{
//			//ɱ���߳�ǿ���˳�
//			android.os.Process.killProcess(android.os.Process.myPid());
//		}
//		break;
//		}    	
//		return true;
//	}
//
//	/**�����ļ��߳�*/
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
////				Log.i(TAG, "������"+jxsize);
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
//			//��surface�Ĵ�С�����ı�ʱ����  
//		}  
//
//		@Override  
//		public void surfaceCreated(SurfaceHolder holder){  
//			new Thread(){  
//				public void run() {  
//					while(true){  
//						try {
//							//1.������Ǻ����ˣ� �õ����� ��Ȼ������Ļ����ϻ���Ҫ��ʾ������  
//							Canvas c = sfh.lockCanvas();  
//							//2.����  
//							Paint  p =new Paint();  
//							
//							p.setColor(Color.rgb( (int)(Math.random() * 255),   
//									(int)(Math.random() * 255) ,  (int)(Math.random() * 255)));  
//							 
//								c.drawBitmap(bitmap, 0, 0, p);
//							//3. ��������   �����ύ��Ļ��ʾ����  
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
//			//����ʱ������һ�������ｫ��ͼ���߳�ֹͣ���ͷš�  
//		}     
//
//
//
//	}  
//}