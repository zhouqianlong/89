package com.ramy.minervue.camera;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.ramy.minervue.app.BaseSurfaceActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.VideoActivity;
import com.ramy.minervue.camera.color.ColorUtil;
import com.ramy.minervue.util.Util;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.CameraService;
import com.serenegiant.usb.CameraService.USBFrameCallBack;
import com.wifitalk.Utils.ProgressDialogUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by peter on 11/30/13.
 */
public class MyCamera implements Camera.PreviewCallback,USBFrameCallBack{

	public static final int PREVIEW_FORMAT = ImageFormat.YV12;//NV21 | YV12

	private static final String TAG = "RAMY-MyCamera";

	public int id;
	private Camera camera = null;
	private CameraZoomer zoomer;
	private boolean isPreviewing = false;
	private boolean isSavingPicture = false;
	private int fpsRange[] = new int[2];
	private Camera.Size currentSize;
	private int currentFrameSize;
	public PreviewCallback previewConsumer;

	public static MyCamera getInstances;
	public static MyCamera openDefaultCamera(int preferredWidth,
			int preferredHeight) {
		MyCamera myCamera = openBackCamera(preferredWidth, preferredHeight);
		if (myCamera == null) {
			myCamera = openFrontCamera(preferredWidth, preferredHeight);
		}
		return myCamera;
	}

	public static MyCamera openFrontCamera(int preferredWidth,
			int preferredHeight) { 
		Camera.CameraInfo info = new Camera.CameraInfo();
		int count = Camera.getNumberOfCameras();
		for (int i = 0; i < count; ++i) {
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					return new MyCamera(i, preferredWidth, preferredHeight);
				} catch (RuntimeException e) {
					return null;
				}
			}
		}
		return null;
	}

	public static MyCamera openBackCamera(int preferredWidth,
			int preferredHeight) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		int count = Camera.getNumberOfCameras();
		for (int i = 0; i < count; ++i) {
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				try {
					return new MyCamera(i, preferredWidth, preferredHeight);
				} catch (RuntimeException e) {
					return null;
				}
			}
		}
		return null;
	}



	public static MyCamera reopenCamera(MyCamera last, int preferredWidth,
			int preferredHeight) {
		int lastId = last.id;
		last.release();
		try {
			return new MyCamera(lastId, preferredWidth, preferredHeight);
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static MyCamera nextCamera(MyCamera last, int preferredWidth,
			int preferredHeight) {
		int newId = (last.id + 1) % Camera.getNumberOfCameras();
		last.release();
		try {
			return new MyCamera(newId, preferredWidth, preferredHeight);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return null;
		}
	}
	
 
	public int getCurrentFrameSize() {
		return currentFrameSize;
	}

	//	private MyCamera(int id, int preferredWidth, int preferredHeight)throws RuntimeException {
	//		this.id = id;
	//		camera = Camera.open(id);
	//		zoomer = new CameraZoomer(camera);
	//		Comparator<Camera.Size> comparator;
	//		if (preferredWidth != 0 && preferredHeight != 0) {
	//			comparator = new MatchComparator(preferredWidth, preferredHeight);
	//		} else {
	//			comparator = new MaxComparator();
	//		}
	//		Camera.Parameters param = camera.getParameters();
	//		param.getPreviewFpsRange(fpsRange);
	//		fpsRange[0] /= 1000;
	//		fpsRange[1] /= 1000;
	//		int format = param.getPreviewFormat();
	//		Log.i(TAG, "当前支持PreviewFormat:"+format);
	//		param.setPreviewFormat(PREVIEW_FORMAT);
	//		param.setPictureSize(preferredHeight, preferredWidth);
	//		param.setJpegQuality(100);
	//		param.set("orientation", "portrait"); //
	//		param.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍） 
	//		camera.setDisplayOrientation(90); // 在2.2以上可以使用
	//		currentSize = Collections.max(param.getSupportedPreviewSizes(),
	//				comparator);
	//		currentFrameSize = ColorUtil.getFrameSize(currentSize, PREVIEW_FORMAT);
	////		param.setPreviewSize(currentSize.width, currentSize.height);
	//		// Now configure.
	//		camera.setParameters(param);
	//
	//		// Get ready for buffer.
	//		camera.setPreviewCallbackWithBuffer(this);
	//		for (int i = 0; i < 4; ++i) {
	//			camera.addCallbackBuffer(new byte[currentFrameSize]);
	//		}
	//	}


	private MyCamera(int id, int preferredWidth, int preferredHeight)throws RuntimeException {
		this.id = id;
		camera = Camera.open(id);
		zoomer = new CameraZoomer(camera);
		Comparator<Camera.Size> comparator;
		if (preferredWidth != 0 && preferredHeight != 0) {
			comparator = new MatchComparator(preferredWidth, preferredHeight);
		} else {
			comparator = new MaxComparator();
		}
		Camera.Parameters param = camera.getParameters();
		// First, some info we need.
		param.getPreviewFpsRange(fpsRange);
		fpsRange[0] /= 1000;
		fpsRange[1] /= 1000;
		// Second, some configuration.
		int format = param.getPreviewFormat();
		Log.i(TAG, "当前支持PreviewFormat:"+format);
		param.setPreviewFormat(PREVIEW_FORMAT);
		//			param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//		param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦  
		//			param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
		// Third, best jpeg quality.
		Camera.Size size = Collections.max(param.getSupportedPictureSizes(),
				new MaxComparator());
		//		param.setPictureSize(size.height, size.width);
		param.setPictureSize(size.width, size.height);
		param.setPreviewSize(640, 480);
//		param.setPictureSize(640, 480);
		param.setJpegQuality(100);
		param.set("orientation", "portrait"); //
		if(isFront()){
			param.set("rotation", 270); // 镜头角度转90度（默认摄像头是横拍）
		}else{
			param.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍） 
		}
		camera.setDisplayOrientation(90); // 在2.2以上可以使用
		// Fourth, video quality.
		currentSize = Collections.max(param.getSupportedPreviewSizes(),
				comparator);
		currentFrameSize = ColorUtil.getFrameSize(currentSize, PREVIEW_FORMAT);
//		param.setPreviewSize(currentSize.width, currentSize.height);
	
		//			param.setPreviewSize(1152, 720);
		camera.cancelAutoFocus();
		// Now configure.
		camera.setParameters(param);
		// Get ready for buffer.
		camera.setPreviewCallbackWithBuffer(this);
		CameraService.setUsbFrameCallBack(this);
		for (int i = 0; i < 4; ++i) {
			camera.addCallbackBuffer(new byte[currentFrameSize]);
		}
	}

	public void setPreviewCallback(PreviewCallback callback) {
		if (camera != null) {
			previewConsumer = callback;
		}
	}



	public boolean isFront() {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(id, info);
		return info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	public boolean startPreview(SurfaceTexture surface) {
		if (camera != null) {
			try {
				getInstances = this;
				camera.setPreviewTexture(surface);
				camera.startPreview();
				camera.cancelAutoFocus();
				isPreviewing = true;
				return true;
			} catch (IOException e) {
				Log.e(TAG, "Error setting texture.");
			}
		}
		return false;
	}

	public void cancelAutoFocus(){
		try {
			List<Camera.Area> areas = new ArrayList<Camera.Area>();
			Camera.Parameters param = camera.getParameters();
			param.setFocusMode(Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式  
			camera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能  
			areas.add(new Camera.Area(new Rect(-500, -500, 500, 500), 1000));
			param.setFocusAreas(areas);
			camera.setParameters(param);
			camera.autoFocus(callback);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private final Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//聚焦之后根据结果修改图片
			if (success) {
				Log.i("CameraZQL", "对焦 success");
			} else {
				//聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
				Log.i("CameraZQL", "对焦 error");
			}
		}
	};

	//	public void cancelAutoFocus(){
	//		camera.takePicture(null, null, new Camera.PictureCallback() {
	//			@Override
	//			public void onPictureTaken(byte[] data, Camera camera) {
	//				camera.startPreview();
	//			}
	//		});
	////		camera.stopPreview();
	//		camera.cancelAutoFocus();
	//	}

	public int[] getFpsRange() {
		return fpsRange;
	}

	public List<Camera.Size> getPreviewSizes() {
		Camera.Size myS = null;
		Camera.Size myM = null;
		Camera.Size myL = null;
		if (camera != null) {
			List<Camera.Size> list  = camera.getParameters().getSupportedPreviewSizes();
			List<Camera.Size> listSML  = new ArrayList<Camera.Size>();
			for (Camera.Size size : list) {
				if(size.width==320&&size.height==240){
					myS = size;
				}
				if(size.width==640&&size.height==480){
					myM = size;
				}
				if(size.width==1280&&size.height==720){
					myL = size;
				}
			}
			if(myS!=null) 
				listSML.add(myS);
			if(myM!=null)
				listSML.add(myM);
			if(myL!=null)
				listSML.add(myL);
			if(listSML.size()==3){
				return listSML;
			}
			return list;
		} else {
			return null;
		}
	}

	public Camera.Size getCurrentPreviewSize() {
		return currentSize;
	}

	public void release() {
		if (camera == null) {
			return;
		}
		if (isPreviewing) {
			camera.stopPreview();
			try {
				camera.setPreviewTexture(null);
			} catch (IOException e) {
				Log.e(TAG, "Error releasing texture.");
			}
			camera.setPreviewCallbackWithBuffer(null);
			isPreviewing = false;
		}
		camera.release();
		getInstances = null;
		camera = null;
	}

	public boolean isPreviewing() {
		return isPreviewing;
	}

	public void zoom(int percent) {
		if (camera != null) {
			zoomer.zoomTo(percent);
		}
	}

	public void takePicture(final PictureCallback callback) {
		if(VideoActivity.instances.usbCount==100){
			if(myFrame!=null){
				callback.onPictureTaken(myFrame);
				Log.i("0090", "+++++++++++USB-onPictureTaken++++++++++++++++++++");
				isSavingPicture = false;
			}
			return;
		}
		
		if (camera != null && !isSavingPicture) {
			isSavingPicture = true;
			camera.takePicture(null, null, new Camera.PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					//					if(isFront()){
					//						data = Util.rotateYv12Degree90(data,currentSize.width, currentSize.height,true);//顺时针
					//					}

					callback.onPictureTaken(data);
					isSavingPicture = false;
					camera.startPreview();
				}
			});
			
		}
	}

	public void setLED(boolean isChecked) {
		try {
			if (camera != null) {
				Camera.Parameters param = camera.getParameters();
				if (isChecked) {
					param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				} else {
					param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				}
				camera.setParameters(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		if(BaseSurfaceActivity.usbCount==100){
			return;
		}
			if (previewConsumer != null) {
				previewConsumer.onPreviewFrame(data);
				camera.addCallbackBuffer(data);
			}	
	}

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

	public interface PreviewCallback {

		void onPreviewFrame(byte[] data);

	}

	public interface PictureCallback {

		public void onPictureTaken(byte[] data);

	}
	
	byte[] myFrame = null;
	@Override
	public void onUsbFrame(byte[] frame) {
		myFrame = frame;
	}

}
