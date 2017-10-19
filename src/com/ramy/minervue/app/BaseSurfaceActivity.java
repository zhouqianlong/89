package com.ramy.minervue.app;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.ramy.minervue.R;
import com.ramy.minervue.camera.MyCamera;
import com.ramy.minervue.camera.PreviewSizeAdapter;
import com.ramy.minervue.ffmpeg.MP4Muxer;
import com.ramy.minervue.media.VideoCodec;
import com.ramy.minervue.util.MySwitch;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.CameraService;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.TimerService;
import com.serenegiant.usb.USBMonitor;
import com.wifitalk.Utils.ProgressDialogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by peter on 3/29/14.
 */
public abstract class BaseSurfaceActivity extends Activity implements TextureView.SurfaceTextureListener, MP4Muxer.RecordTimeListener , CameraDialog.CameraDialogParent,  CameraService.OnTimerOutListener{

    private static final String TAG = "RAMY-BaseSurfaceActivity";

    protected static final int UI_LEVEL_DISABLE = 0;
    protected static final int UI_LEVEL_BUSY = 1;
    protected static final int UI_LEVEL_NORMAL = 2;
    
    public USBMonitor mUSBMonitor;//USB摄像头监听
	public CameraService camerService;//摄像头服务
	public TimerService timerService;//timer服务
	public static int usbCount =0;
//	
	public  int count = 0;
	public static boolean audioswitch = true;
    protected TextView timeText;
    protected TextureView textureView;
    protected SeekBar zoomSeek;
    protected TextView cameraFacingText;
//    protected MySwitch ledSwitch;
    protected Button ledSwitch;
    protected Button switchCameraButton;
    protected Spinner sizeSpinner;
    public SurfaceView tv_SurfaceView;

    protected CameraSizeListener sizeListener = new CameraSizeListener();
    protected PreviewSizeAdapter spinnerAdapter;

    protected Date recordTime = new Date();
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    
    @Override
    public void onContentChanged() {
    	timerService = new TimerService();
		camerService = new CameraService(this,getApplicationContext(),timerService);
		mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
		usbCount= 	mUSBMonitor.getDeviceList(DeviceFilter.getDeviceFilters(this, R.xml.device_filter).get(0)).size();
		tv_SurfaceView = (SurfaceView) findViewById(R.id.tv_SurfaceView);
        timeText = (TextView) findViewById(R.id.tv_video_info);
        timeText.setTextSize(20f);
        textureView = (TextureView) findViewById(R.id.tv_video_preview);
        textureView.setSurfaceTextureListener(this);
        textureView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				showProgressDialog();
				try {
					getVideoCodec().getCurrentCamera().cancelAutoFocus();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        zoomSeek = (SeekBar) findViewById(R.id.sb_zoom_control);
        zoomSeek.setProgress(0);
        zoomSeek.setOnSeekBarChangeListener(new ZoomListener());
        cameraFacingText = (TextView) findViewById(R.id.tv_camera_info);
//        ledSwitch = (MySwitch) findViewById(R.id.sw_led);
        ledSwitch = (Button) findViewById(R.id.sw_led);
        ledSwitch.setTag("false");//默认关闭
//        ledSwitch.setOnCheckedChangeListener(new LEDListener());
        ledSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if(ledSwitch.getTag().toString().equals("true")){
						getVideoCodec().getCurrentCamera().setLED(false);
						ledSwitch.setTag("false");
						ledSwitch.setTextColor(Color.WHITE);
					}else{
						getVideoCodec().getCurrentCamera().setLED(true);
						ledSwitch.setTag("true");
						ledSwitch.setTextColor(Color.RED);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}); 
        switchCameraButton = (Button) findViewById(R.id.bt_switch_camera);
        sizeSpinner = (Spinner) findViewById(R.id.sp_resolution);
        sizeSpinner.setOnItemSelectedListener(sizeListener);
        setUILevel(UI_LEVEL_DISABLE);
    }

    protected abstract VideoCodec getVideoCodec();

    protected void updateUIForCamera(MyCamera camera) {
        spinnerAdapter = new PreviewSizeAdapter(camera.getPreviewSizes());
        sizeSpinner.setAdapter(spinnerAdapter);
        int pos = spinnerAdapter.getItemPosition(camera.getCurrentPreviewSize());
        sizeListener.setLastPos(pos);
        sizeSpinner.setSelection(pos);
        boolean front = camera.isFront();
        cameraFacingText.setText(front ? R.string.front_camera : R.string.back_camera);
//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：480px）
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕宽（像素，如：480px）
//        Matrix m = getVideoCodec().getMatrixFor(textureView.getHeight(),textureView.getWidth());
        Matrix m = getVideoCodec().getMatrixFor(640,480);
        textureView.setTransform(m);
    }
    
 

    boolean isUsb =false;//是否切换到usb
    
    public void onSwitchCamera(View view) {
    	if(isUsb==false&&getVideoCodec().getCurrentCamera().id==1&&usbCount>0){
    		mUSBMonitor.register();
    		isUsb = true;
    		CameraDialog.showDialog(BaseSurfaceActivity.this);
    		new Thread(){
    			@Override
    			public void run() {
    				runOnUiThread(new Runnable() {
    					@Override
    					public void run() {
    						camerService.initService(BaseSurfaceActivity.this);
    						cameraFacingText.setText("USB摄像头");
    					}
    				});
    			}
    		}.start();
    	}else{
    		isUsb = false;
    		camerService.stopPreview();
    		mUSBMonitor.unregister();
    		tv_SurfaceView.setVisibility(View.GONE);
    		VideoCodec codec = getVideoCodec();
    		MyCamera camera = codec.getCurrentCamera();
    		if (camera != null) {
    			Camera.Size size = camera.getCurrentPreviewSize();
    			if (!codec.switchCamera(textureView.getSurfaceTexture(), size.width, size.height)) {
    				setUILevel(UI_LEVEL_DISABLE);
    				toast(getString(R.string.unknown_error));
    			} else {
    				updateUIForCamera(codec.getCurrentCamera());
    			}
    		}
    	}
//    	
    	

//		VideoCodec codec = getVideoCodec();
//		MyCamera camera = codec.getCurrentCamera();
//		if (camera != null) {
//			Camera.Size size = camera.getCurrentPreviewSize();
//			if (!codec.switchCamera(textureView.getSurfaceTexture(), size.width, size.height)) {
//				setUILevel(UI_LEVEL_DISABLE);
//				toast(getString(R.string.unknown_error));
//			} else {
//				updateUIForCamera(codec.getCurrentCamera());
//			}
//		}
	
    }

	static int time =0;
	@Override
	public void onUpdateTime(long recordMilli,final String path) {
		recordTime.setTime(recordMilli);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeText.post(new Runnable() {
			@Override
			public void run() {
				String record_time  = dateFormat.format(recordTime);
				if(MainService.MONITOR_STATU){
//					timeText.setText(dateFormat.format(recordTime)+"(当前电量："+MainService.getInstance().getmLeve()+"%)");
					timeText.setText(dateFormat.format(recordTime));
				}else{
					timeText.setText(dateFormat.format(recordTime)+"");
				}
//				if(MainService.battery==false){
//					Log.i("INFOs", "无电");
//					onBatteryError();
//					return;
//				}
//				Log.i("INFOs", "还有电");			
				if(record_time.equals("00:00:10")){
					time=0;
//					onBatteryError();
				}else{
					if(record_time.equals("00:30:00")||record_time.equals("00:32:00")||record_time.equals("00:34:00")){
						time++;
						try {
							if(time==1){
								Thread.sleep(500);
								count++;
								onUpdateTimeForSave(count, path);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				}
			});
		}


	@Override
	public int onUpdateTimeForSave(int count, String path) {
		return 0;
	}
    
    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void setUILevel(int level) {
        //ledSwitch.setEnabled(level >= UI_LEVEL_BUSY);
        switchCameraButton.setEnabled(level >= UI_LEVEL_BUSY);
        sizeSpinner.setEnabled(level >= UI_LEVEL_NORMAL);
        zoomSeek.setEnabled(level >= UI_LEVEL_BUSY);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // Default implementation: do nothing.
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Default implementation: do nothing.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // Default implementation: do nothing.
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    
    }

    protected class ZoomListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
				getVideoCodec().getCurrentCamera().zoom(progress);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    protected class LEDListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            getVideoCodec().getCurrentCamera().setLED(isChecked);
        }

    }

    protected class CameraSizeListener implements AdapterView.OnItemSelectedListener {

        private int lastPos = 0;

        public void setLastPos(int lastPos) {
            this.lastPos = lastPos;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (lastPos == position) {
                return;
            }
            Camera.Size size = spinnerAdapter.getItem(position);
            Log.i(TAG, "Selecting " + size.width + "x" + size.height + ".");
            SurfaceTexture surface = textureView.getSurfaceTexture();
            if (!getVideoCodec().startPreview(surface, size.width, size.height)) {
                setUILevel(UI_LEVEL_DISABLE);
                toast(getString(R.string.unknown_error));
            } else {
                updateUIForCamera(getVideoCodec().getCurrentCamera());
            }
            
         
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
		private boolean DEBUG = true;

		@Override
		public void onAttach(final UsbDevice device) {
			if (DEBUG ) Log.v(TAG, "onAttach:");
//			Toast.makeText(this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "onConnect:");
//			Toast.makeText(this, "onConnect", Toast.LENGTH_LONG).show();
			camerService.SwitchUSBCamera(device,ctrlBlock,createNew);
		}

		@Override
		public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			camerService.CloseUSBCamera();
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "onDettach:");
//			Toast.makeText(this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel() {
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		mUSBMonitor.register();
	};
	@Override
	protected void onPause() {
		super.onPause();
		mUSBMonitor.unregister();
		
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mUSBMonitor.unregister();
		if (mUSBMonitor != null) {
			mUSBMonitor.destroy();
			mUSBMonitor = null;
		}
	}
	@Override
	public USBMonitor getUSBMonitor() {
		// TODO Auto-generated method stub
		return mUSBMonitor;
	}
}
