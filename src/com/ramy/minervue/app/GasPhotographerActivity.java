package com.ramy.minervue.app;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ramy.minervue.R;
import com.ramy.minervue.camera.MyCamera;
import com.ramy.minervue.media.Recorder;
import com.ramy.minervue.media.VideoCodec;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.util.ATask;

import java.io.FileOutputStream;

/**
 * Created by peter on 6/7/14.
 */
public class GasPhotographerActivity extends BaseSurfaceActivity {
	private static final String TAG = "RAMY-GasPhotographerActivity";
	private Button actionButton;
	private Recorder recorder = new Recorder();
	private String gasData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gasData = getIntent().getStringExtra(getPackageName() + ".GasData");
		setContentView(R.layout.gas_grapher_activity);
		if (TextUtils.isEmpty(gasData)) {
			finish();
		}
	}

	@Override
	public void onContentChanged() {
		actionButton = (Button) findViewById(R.id.bt_video_action);
		super.onContentChanged();
		timeText.setText(gasData);
	}

	@Override
	protected void setUILevel(int level) {
		actionButton.setEnabled(level >= UI_LEVEL_BUSY);
		super.setUILevel(level);
	}

	@Override
	protected VideoCodec getVideoCodec() {
		return recorder.getVideoCodec();
	}

	public void onRecord(View view) {
		MainService service=MainService.getInstance();
		final String filename = service.getSyncManager().getLocalFileUtil()
				.generateSensorFilename();
		MyCamera camera = getVideoCodec().getCurrentCamera();
		camera.takePicture(new MyCamera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data) {
				new WatermarkSaver(data, gasData, filename).start();
			}
		});

		setUILevel(UI_LEVEL_DISABLE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getVideoCodec().stopPreview();
		finish();
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		VideoCodec codec = getVideoCodec();
		if (codec.startPreview(surface, 0, 0)) {
			StatusManager.setGasPhoto(true);
			setUILevel(UI_LEVEL_NORMAL);
			updateUIForCamera(codec.getCurrentCamera());
		} else {
			toast(getString(R.string.unknown_error));
		}
	}

	private class WatermarkSaver extends ATask<Void, Void, Void> {
		private byte[] picData;
		private String gasData;
		private String filePath;

		public WatermarkSaver(byte[] picData, String gasData, String filePath) {
			this.picData = picData;
			this.gasData = gasData;
			this.filePath = filePath;
		}

		@Override
		protected void onPreExecute() {
			String processing = getString(R.string.processing);
			ProgressDialog.show(GasPhotographerActivity.this, "", processing,
					true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap jpg = BitmapFactory.decodeByteArray(picData, 0,
					picData.length);
			// �����ƶ���ʽ��ͼƬ
			Bitmap bitmap = jpg.copy(Bitmap.Config.ARGB_8888, true);
			// �����µ�ͼƬ���ͷ���ͼƬ
			jpg.recycle();
			// ��������
			Canvas canvas = new Canvas(bitmap);
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			TextPaint textPaint = new TextPaint();
			textPaint.setARGB(0xFF, 0xFF, 0, 0);
			textPaint.setTextSize(width / 20);
			textPaint.setAntiAlias(true);
			canvas.save();
			StaticLayout layout = new StaticLayout(gasData, textPaint, width,
					Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
			canvas.translate(20, 80);
			layout.draw(canvas);
			canvas.restore();
			try {
				FileOutputStream fout = new FileOutputStream(filePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
				bitmap.recycle();
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return super.doInBackground();
		}
		@Override
		protected void onPostExecute(Void aVoid) {
			toast(getString(R.string.saved) + filePath);
			MainService.getInstance().getSyncManager().startSync();
			finish();
		}

	}

	@Override
	public int onBatteryError() {
		return 0;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		StatusManager.setGasPhoto(true);
		super.onDestroy();
	}

	@Override
	public void onTimerFresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLog(String log) {
		// TODO Auto-generated method stub
		
	}
}
