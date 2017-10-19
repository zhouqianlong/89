package com.ramy.minervue.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tst.MainActivity;
import com.ramy.minervue.R;
import com.ramy.minervue.adapter.QuestionAdapter;
import com.ramy.minervue.bean.QuestionInfo;
import com.ramy.minervue.camera.MyCamera;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.media.Recorder;
import com.ramy.minervue.media.VideoCodec;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.sync.SyncManager;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.PreferenceUtil;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.CameraService;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.TimerService;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.wifitalk.Utils.ProgressDialogUtils;

public class VideoActivity extends BaseSurfaceActivity implements OnClickListener {

	private static final String TAG = "RAMY-VideoActivity";
	private DBHelper dbHelper ;
	private List<QuestionInfo> questionList= new ArrayList<QuestionInfo>();
	private Button recordTypeSwitch;//    android:textOn="@string/record_video"     
	private Button bt_video_action,bt_switch_camera,bt_add_content;
	private int count = 1;
	private int mcount = 1;
	private String type;
	private boolean isPassive = false;
	private Recorder recorder = new Recorder();
	private Spinner sp_resolution;
	private Spinner sp_hostory;
	private TextView tv_miaoshu_info;
	private QuestionAdapter adapter;
	private ImageView iv_photo;
	private boolean flag = false;
	boolean mFlag = false;//状态为 true 结束后处理

	protected String gasDataStr="";//描述类容

	public static  VideoActivity instances;
	public TextureView tv_video_preview;
	public SurfaceView tv_SurfaceView;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = new DBHelper(this);
		instances = this;
		setContentView(R.layout.video_activity);
		MainService.getInstance().setActivityVideoActivity(this);
		tv_video_preview= (TextureView) findViewById(R.id.tv_video_preview);
		tv_miaoshu_info = (TextView) findViewById(R.id.tv_miaoshu_info);
		bt_video_action = (Button) findViewById(R.id.bt_video_action);
		bt_switch_camera = (Button) findViewById(R.id.bt_switch_camera);
		bt_add_content = (Button) findViewById(R.id.bt_add_content);
		bt_add_content.setOnClickListener(this);
		sp_resolution = (Spinner) findViewById(R.id.sp_resolution);
		iv_photo = (ImageView) findViewById(R.id.iv_photo);
		tv_SurfaceView = (SurfaceView) findViewById(R.id.tv_SurfaceView);
		try {
			mFlag = getIntent().getExtras().getBoolean("flag");
		} catch (Exception e) {
			e.printStackTrace();
		}


	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		tv_miaoshu_info.setText(gasDataStr);
	}

	@Override
	public void onContentChanged() {
		recordTypeSwitch = (Button) findViewById(R.id.sw_photo_or_video);
		recordTypeSwitch.setTag("photo");
		recordTypeSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(recordTypeSwitch.getTag().equals("photo")){
					recordTypeSwitch.setTag("video");
					recordTypeSwitch.setText(R.string.record_video);
					iv_photo.setBackgroundResource(R.anim.movie);
				}else{
					recordTypeSwitch.setTag("photo");
					recordTypeSwitch.setText(R.string.take_photo);
					iv_photo.setBackgroundResource(R.anim.photo);
					//					bt_video_action.setText(R.string.record);
				}

			}
		});
		bt_video_action = (Button) findViewById(R.id.bt_video_action);
		count = getIntent().getIntExtra("Phtot_count_int", 0);
		type = getIntent().getStringExtra("type");
		if(null==type){
			type = "内置";
		}
		isPassive = getIntent().getBooleanExtra(getPackageName() + ".StartNow",
				false);
		super.onContentChanged();
	}

	@Override
	protected void setUILevel(int level) {
		// recordTypeSwitch.setEnabled(level >= UI_LEVEL_NORMAL);
		if (level == UI_LEVEL_BUSY) {
			//			bt_video_action.setText(R.string.stop);
		} else {
			//			bt_video_action.setText(R.string.record);
		}
		bt_video_action.setEnabled(level >= UI_LEVEL_BUSY);
		super.setUILevel(level);
	}

	@Override
	protected VideoCodec getVideoCodec() {
		return recorder.getVideoCodec();
	}

	private void setPrioritys(String path, int priority) {
		LocalFileUtil util = MainService.getInstance().getSyncManager().getLocalFileUtil();
		util.setPriority(path, priority);
		toast(getString(R.string.saved) + util.setPriority(path, priority));
		MainService.getInstance().getSyncManager().startSync();
		if(mFlag == true){
			finish();
			MainActivity.instances.refersh("TK2", util.setPriority(path, priority));
		}
	}

	private void setPriority(String path, int priority) {
		LocalFileUtil util = MainService.getInstance().getSyncManager().getLocalFileUtil();
		toast(getString(R.string.saved) + util.setPriority(path, priority));
		MainService.getInstance().getSyncManager().startSync();
		if(mFlag == true){
			finish();
			MainActivity.instances.refersh("TK2", util.setPriority(path, priority));
		}
	}

	private void choosePriority(final String path) {
		setPriority(path, 1);
		//		String[] priority = getResources().getStringArray(R.array.priority);
		//		new AlertDialog.Builder(this).setItems(priority, new DialogInterface.OnClickListener() {
		//			@Override
		//			public void onClick(DialogInterface dialog, int which) {
		//				setPriority(path, which + 1);
		//			}
		//		}).setTitle(R.string.choose_priority).setCancelable(false).show();
	}


	public void onRecord2(View view){
		bt_video_action.performClick();
	}


	public void onRecord(View view) {
		SyncManager syncManager = MainService.getInstance().getSyncManager();
		if (recordTypeSwitch.getTag().toString().equals("video")) {
			if (recorder.isCapturing()) {//
				//停止视频 并且保存视频
				recorder.stop();
				String filePath = recorder.getMuxer().getFilePath();
				setUILevel(UI_LEVEL_NORMAL);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				choosePriority(filePath);
				sp_resolution.setEnabled(true);
				StatusManager.setVideo(false);
			} else {
				//录制视频                 录制视频                
				//视频生成的文件名称
				String recordFile = syncManager.getLocalFileUtil().generateVideoFilename();
				recorder.start(recordFile,this);
				//				recorder.getMuxer().setRecordTimeListener(this);
				//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				StatusManager.setVideo(true);
				sp_resolution.setEnabled(false);
				//				bt_video_action.setText(getString(R.string.stop));
			}
		} else {


			PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
			util.playSounds(5, 0);



			//拍照功能
			/*如果是监控拍照自动选择存储*/
			if(isPassive == true&&type.equals("内置")){
				StatusManager.setRemotePhoto(true);
				MyCamera camera = getVideoCodec().getCurrentCamera();
				camera.takePicture(new MyCamera.PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data) {
						try {
							String path  = MainService.getInstance().getSyncManager().getLocalFileUtil().generateImageFilename();
							FileOutputStream fos = new FileOutputStream(MainService.getInstance().getSyncManager().getLocalFileUtil().generateImageFilename());
							fos.write(data);
							fos.close();
							setPrioritys(path, 1);
							bt_video_action.requestFocus();//切换摄像头
							if(mcount==count){
								MyDestory();
								return;
							}
							handler.sendEmptyMessage(1);   
							mcount++;
						} catch (FileNotFoundException e) {
							MyDestory();
							e.printStackTrace();
						} catch (IOException e) {
							MyDestory();
							e.printStackTrace();
						} 
					}
				});
			}else if(type.equals("外置拍摄")){
				StatusManager.setRemotePhoto(true);
				MyCamera camera = getVideoCodec().getCurrentCamera();
				if (camera != null) {
					camera.takePicture(new MyCamera.PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data) {
							try {
								String path  = MainService.getInstance().getSyncManager().getLocalFileUtil().generateImageFilename();
								FileOutputStream fos = new FileOutputStream(MainService.getInstance().getSyncManager().getLocalFileUtil().generateImageFilename());
								fos.write(data);
								fos.close();
								setPrioritys(path, 1);
								bt_video_action.requestFocus();
								if(mcount==count){
									MyDestory();
									return;
								}
								handler.sendEmptyMessage(1);   
								mcount++;
							} catch (FileNotFoundException e) {
								MyDestory();
								e.printStackTrace();
							} catch (IOException e) {
								MyDestory();
								e.printStackTrace();
							} 
						}
					});
				}
			}else{
				final String filename = MainService.getInstance().getSyncManager().getLocalFileUtil().generateImageFilename();
				MyCamera camera = getVideoCodec().getCurrentCamera();
				camera.takePicture(new MyCamera.PictureCallback() {//普通拍照
					@Override
					public void onPictureTaken(byte[] data) {
						if(gasDataStr.length()!=0){
							new WatermarkSaver(data, gasDataStr, filename).start();
						}else{




							//							
							//							FileOutputStream outStream = null;  
							//	                        try {  
							//	                            YuvImage yuvimage = new YuvImage(data,ImageFormat.YUY2,640,480,null);  
							//	                            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
							//	                            yuvimage.compressToJpeg(new Rect(0,0,640,480), 80, baos);  
							//	  
							//	                            outStream = new FileOutputStream(filename);  
							//	                            outStream.write(baos.toByteArray());  
							//	                            outStream.flush();  
							//	                            outStream.close();  
							//	  
							//	  
							//	                        } catch (FileNotFoundException e) {  
							//	                            e.printStackTrace();  
							//	                            Log.i("yy", "1111111111111111111");  
							//	                        } catch (IOException e) {  
							//	                            e.printStackTrace();  
							//	                        } finally {  
							//	                        }  
							//							
							//							int[] rgb = new int[data.length];
							//							decodeYUV420SPrgb565(rgb, data, 640, 480);
							//							Bitmap bitmap = Bitmap.createBitmap(rgb, 640, 480,
							//									Config.RGB_565);
							//							Bitmap bitmap  = decodeToBitMap(data);
							//							File file2 = new File(filename);
							//							try {
							//								FileOutputStream out = new FileOutputStream(file2);
							//								if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
							//									out.flush();
							//									out.close();
							//								}
							//							} catch (Exception e) {
							//							}



							if(usbCount==100){
								

								Bitmap   bitmap = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT,Bitmap.Config.RGB_565);
						            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
						            bitmap = comp(bitmap);
								
								
//								  int [] argb=I420toARGB(data,640, 480);  
//							    int []  bitmapData = new int[640 * 480];  
//								   decodeYUV420SPrgb565(bitmapData,data,640,480);  
//				                    Bitmap bitmap = Bitmap.createBitmap(bitmapData,640, 480, Bitmap.Config.ARGB_8888);  
//				  
//				  
//				  
				                    FileOutputStream fileOutputStream;  
				                    try {  
				                        fileOutputStream = new FileOutputStream(filename);  
				                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);  
				                        fileOutputStream.close();  
				                    } catch (IOException e) {  
				  
				                    }  
				  
								
							}else{
								try {
									FileOutputStream fos = new FileOutputStream(filename);
									fos.write(data);
									fos.close();
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}



						
							choosePriority(filename);
						}

					}
				});

			}

		}
	}
	
	
	
	   private Bitmap comp(Bitmap image) {

	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	        if (baos.toByteArray().length / 1024 >
	                1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
	            baos.reset();//重置baos即清空baos
	            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
	        }
	        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
	        BitmapFactory.Options newOpts = new BitmapFactory.Options();
	        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
	        newOpts.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(isBm, null, newOpts);
	        newOpts.inJustDecodeBounds = false;
	        int w = newOpts.outWidth;
	        int h = newOpts.outHeight;
	        float hh = 720f;
	        float ww = 1280f;
	        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
	        int be = 1;//be=1表示不缩放
	        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
	            be = (int) (newOpts.outWidth / ww);
	        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
	            be = (int) (newOpts.outHeight / hh);
	        }
	        if (be <= 0) {
	            be = 1;
	        }
	        newOpts.inSampleSize = be;//设置缩放比例
	        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
	        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
	        isBm = new ByteArrayInputStream(baos.toByteArray());
	        Bitmap  bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
	        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	    }

	    private Bitmap compressImage(Bitmap image) {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
	        int options = 100;
	        while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
	            baos.reset();//重置baos即清空baos
	            options -= 10;//每次都减少10
	            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

	        }
	        ByteArrayInputStream isBm = new ByteArrayInputStream(
	                baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
	        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
	        return bitmap;
	    }

	    

	public Bitmap decodeToBitMap(byte[] data) {
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21,640,
					480, null);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, 640,480),
						80, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(
						stream.toByteArray(), 0, stream.size());
				stream.close();
				return bmp;
			}
		} catch (Exception ex) {
			return null;
		}
		return null;
	}

	public static int[] I420toARGB(byte[] yuv, int width, int height)  
	{  

		boolean invertHeight=false;  
		if (height<0)  
		{  
			height=-height;  
			invertHeight=true;  
		}  

		boolean invertWidth=false;  
		if (width<0)  
		{  
			width=-width;  
			invertWidth=true;  
		}  



		int iterations=width*height;  
		//if ((iterations*3)/2 > yuv.length){throw new IllegalArgumentException();}  
		int[] rgb = new int[iterations];  

		for (int i = 0; i<iterations;i++)  
		{  
			/*int y = yuv[i] & 0x000000ff; 
    int u = yuv[iterations+(i/4)] & 0x000000ff; 
    int v = yuv[iterations + iterations/4 + (i/4)] & 0x000000ff;*/  
			int nearest = (i/width)/2 * (width/2) + (i%width)/2;  

			int y = yuv[i] & 0x000000ff;  
			int u = yuv[iterations+nearest] & 0x000000ff;  


			int v = yuv[iterations + iterations/4 + nearest] & 0x000000ff;  

			//int b = (int)(1.164*(y-16) + 2.018*(u-128));  
			//int g = (int)(1.164*(y-16) - 0.813*(v-128) - 0.391*(u-128));  
			//int r = (int)(1.164*(y-16) + 1.596*(v-128));  

			//double Y = (y/255.0);  
			//double Pr = (u/255.0-0.5);  
			//double Pb = (v/255.0-0.5);  



			/*int b = (int)(1.164*(y-16)+1.8556*(u-128)); 

    int g = (int)(1.164*(y-16) - (0.4681*(v-128) + 0.1872*(u-128))); 
    int r = (int)(1.164*(y-16)+1.5748*(v-128));*/  

			int b = (int)(y+1.8556*(u-128));  

			int g = (int)(y - (0.4681*(v-128) + 0.1872*(u-128)));  

			int r = (int)(y+1.5748*(v-128));  


			/*double B = Y+1.8556*Pb; 

    double G = Y - (0.4681*Pr + 0.1872*Pb); 
    double R = Y+1.5748*Pr;*/  

			//int b = (int)B*255;  
			//int g = (int)G*255;  
			//int r = (int)R*255;  





			if (b>255){b=255;}  
			else if (b<0 ){b = 0;}  
			if (g>255){g=255;}  
			else if (g<0 ){g = 0;}  
			if (r>255){r=255;}  
			else if (r<0 ){r = 0;}  

			/*rgb[i]=(byte)b; 
    rgb[i+1]=(byte)g; 
    rgb[i+2]=(byte)r;*/  
			int targetPosition=i;  

			if (invertHeight)  
			{  
				targetPosition=((height-1)-targetPosition/width)*width   +   (targetPosition%width);  
			}  
			if (invertWidth)  
			{  
				targetPosition=(targetPosition/width)*width    +    (width-1)-(targetPosition%width);  
			}  


			rgb[targetPosition] =  (0xff000000) | (0x00ff0000 & r << 16) | (0x0000ff00 & g << 8) | (0x000000ff & b);  
		}  
		return rgb;  

	}  

	public static void decodeYUV420SPrgb565(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);
				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;
				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private class WatermarkSaver extends ATask<Void, Void, Void> {
		private byte[] picData;
		private String gasData;
		private String filePath;
		private ProgressDialog myDialog;
		public WatermarkSaver(byte[] picData, String gasData, String filePath) {
			this.picData = picData;
			this.gasData = gasData;
			this.filePath = filePath;
		}

		@Override
		protected void onPreExecute() {
			String processing = getString(R.string.processing);
			myDialog = ProgressDialog.show(VideoActivity.this, "", processing,
					true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap jpg = BitmapFactory.decodeByteArray(picData, 0,
					picData.length);
			// 产生制定格式的图片
			Bitmap bitmap = jpg.copy(Bitmap.Config.ARGB_8888, true);
			// 产生新的图片后，释放新图片
			jpg.recycle();
			// 创建画布
			Canvas canvas = new Canvas(bitmap);
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			TextPaint textPaint = new TextPaint();
			textPaint.setARGB(0xFF, 0xFF, 0, 0);
			textPaint.setTextSize(width / 40);
			textPaint.setAntiAlias(true);
			canvas.save();
			StaticLayout layout = new StaticLayout(gasData, textPaint, width,
					Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
			canvas.translate(20, 80);
			layout.draw(canvas);
			canvas.restore();
			try {
				FileOutputStream fout = new FileOutputStream(filePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
				bitmap.recycle();
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return super.doInBackground();
		}
		@Override
		protected void onPostExecute(Void aVoid) {
			myDialog.dismiss();
			choosePriority(filePath);
		}

	}

	@Override
	public void onBackPressed() {
		if (recordTypeSwitch.getTag().toString().equals("video") && recorder.isCapturing()) {
			recorder.stop();
			String filePath = recorder.getMuxer().getFilePath();
			setUILevel(UI_LEVEL_NORMAL);
			choosePriority(filePath);
			StatusManager.setVideo(false);
		} else {
			super.onBackPressed();
		}
	}

	Handler handler = new Handler(){  



		public void handleMessage(Message msg) {
			if(msg.what==1){
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(1000);
							handler.sendEmptyMessage(2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}else if(msg.what==2){
				bt_video_action.performClick();  
			}else{
				if(msg.arg1 == 100){
					//					textView.setText((String)msg.obj);
				}else{
					//					textView.setText(timerService.getTimeStr());
				}
			}
			super.handleMessage(msg);  
		}

	};





	@Override
	protected void onPause() {
		super.onPause();

		if (recordTypeSwitch.getTag().toString().equals("video") && recorder.isCapturing()) {
			recorder.stop();
			String filePath = recorder.getMuxer().getFilePath();
			setUILevel(UI_LEVEL_NORMAL);
			setPriority(filePath, 1);
		}
		StatusManager.setVideo(false);
		try {
			getVideoCodec().getCurrentCamera().setLED(false);
			getVideoCodec().stopPreview();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instances = null;
		StatusManager.setVideo(false);
		StatusManager.setRemotePhoto(false);
		MainService.getInstance().setActivityVideoActivity(null);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		flag  = false;

	}

	public void MyDestory(){
		StatusManager.setRemotePhoto(false);
		this.finish();
	}

	@Override
	public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
		final VideoCodec codec = getVideoCodec();
		ProgressDialogUtils.showProgressDialog(VideoActivity.this, "加载摄像头中");
		new Thread(new Runnable() {

			@Override
			public void run() {
				//开启预览
				if (codec.startPreview(surface, 640, 480)) {
					mhander.post(new Runnable() {

						@Override
						public void run() {
							ProgressDialogUtils.dismissProgressDialog();
							autoFocus();
							setUILevel(UI_LEVEL_NORMAL);
							updateUIForCamera(codec.getCurrentCamera());
							if(type.equals("外置拍摄")){
								bt_switch_camera.performClick();
							}
							if(isPassive){
								startPassively(count, type, isPassive);
								//				bt_video_action.performClick();
							}

						}


					});

				} else {
					mhander.post(new Runnable() {
						@Override
						public void run() {
							ProgressDialogUtils.dismissProgressDialog();
							getVideoCodec().stopPreview();
							codec.stopCapture();
							toast(getString(R.string.unknown_error));
						}
					});
				}
			}
		}).start();
	}

	private void autoFocus() {
		if(flag==false){
			flag = true;
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mhander.post(new Runnable() {

						@Override
						public void run() {
							try {
								getVideoCodec().getCurrentCamera().cancelAutoFocus();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				}
			}).start();

		}
	}
	int pathLength = 0;
	@Override
	public int onUpdateTimeForSave(int count, final String path) {
		BaseSurfaceActivity.time=1;
		if(pathLength==0){
			pathLength = path.length();
		}
		if (recorder.isCapturing()) {
			//停止视频 并且保存视频
			recorder.stop();
			setUILevel(UI_LEVEL_NORMAL);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			setPrioritys(recorder.getMuxer().getFilePath(), 3);
			try {
				if(MainService.getInstance().getmLeve()<=5)
					mhander.sendEmptyMessage(101);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			//录制视频
			//视频生成的文件名称
			String paths = path.substring(0, pathLength-4)+"_"+count+".mp4";
			recorder.start(paths,this);
			//			recorder.getMuxer().setRecordTimeListener(this);
			setUILevel(UI_LEVEL_BUSY);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		return count;
	}

	private void closeMp4(){
		if (recordTypeSwitch.getTag().toString().equals("video") && recorder.isCapturing()) {
			recorder.stop();
			String filePath = recorder.getMuxer().getFilePath();
			setUILevel(UI_LEVEL_NORMAL);
			// If the activity is destroyed due to unknown reasons, the priority of the ongoing
			// record is set to 1 by default.
			setPriority(filePath, 1);
		}
		getVideoCodec().stopPreview();
	}
	Handler mhander = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==101){
				Toast.makeText(VideoActivity.this,R.string.dianliangbuzu, Toast.LENGTH_LONG).show();
				closeMp4();
				finish();
			}
			if(msg.what==102){
				onRecord(bt_video_action);
			}
		};
	};

	@Override
	public int onBatteryError() {
		finish();
		return 0;
	}

	public void startPassively(int mcount,String mtype,boolean mispassive) {
		count = mcount;
		type =mtype;
		isPassive = mispassive;
		if(type.equals("外置拍摄")){
			bt_switch_camera.performClick();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					mhander.sendEmptyMessage(102);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		if(bt_add_content ==v){
			View layout = LayoutInflater.from(VideoActivity.this).inflate(R.layout.add_call_alert,null);
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoActivity.this);
			alertDialog.setView(layout);
			final AlertDialog dialog = alertDialog.show();
			Button btn_ok = (Button) layout.findViewById(R.id.btn_ok);
			Button btn_cancle = (Button) layout.findViewById(R.id.btn_cancle);
			Button btn_clear = (Button) layout.findViewById(R.id.btn_clear);
			final EditText et_quesname = (EditText) layout.findViewById(R.id.et_quesname);
			final EditText et_quesmessage = (EditText) layout.findViewById(R.id.et_quesmessage);
			sp_hostory = (Spinner)layout.findViewById(R.id.sp_hostory);
			findQuestion();
			sp_hostory.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {

					et_quesname.setText(questionList.get(position).getQuestionName());
					et_quesmessage.setText(questionList.get(position).getQuestionContent());

				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			btn_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String questionName = et_quesname.getText().toString();
					String questionMessage = et_quesmessage.getText().toString();
					if(!questionName.isEmpty()&&!questionMessage.isEmpty()){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						StringBuffer strBuffer = new StringBuffer();
						strBuffer.append("问题名称:"+questionName+"\n");
						strBuffer.append("描述内容:"+questionMessage+"\n");
						strBuffer.append("发现时间:"+sdf.format(new Date())+"\n");
						dbHelper.insertQuestion(questionName, questionMessage);
						gasDataStr = strBuffer.toString();
						tv_miaoshu_info.setText(gasDataStr);
						if(gasDataStr.length()!=0){
							bt_add_content.setText("已描述");
						}
						Log.i("AAA", gasDataStr);
					}
					dialog.dismiss();
				}
			});

			btn_cancle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gasDataStr="";
					tv_miaoshu_info.setText(gasDataStr);
					if(gasDataStr.length()!=0){
						bt_add_content.setText("添加描述");
					}
					dialog.dismiss();
				}
			});

			btn_clear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					new AlertDialog.Builder(VideoActivity.this).setTitle("是否删除历史描述记录").setPositiveButton("是", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dbHelper.clearQuestion();
							findQuestion();
						}
					}).setNegativeButton("否", null).show();
					dialog.dismiss();
				}
			});


		}
	}

	public void findQuestion() {
		questionList = dbHelper.findQuestion();
		adapter = new QuestionAdapter(questionList,VideoActivity.this);
		sp_hostory.setAdapter(adapter);
	}




	@Override
	public void onTimerFresh() {
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onLog(String log) {
		Message msg = new Message();
		msg.arg1 = 100;
		msg.obj = log;
		handler.sendMessage(msg);
	}

}
