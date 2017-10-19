package h264.com;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class H264AndroidTest extends Activity {
	VViewV	vv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		vv = new VViewV(this);
		setContentView(vv);
	}

	// Menu item Ids
	public static final int	PLAY_ID	= Menu.FIRST;
	public static final int	EXIT_ID	= Menu.FIRST + 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PLAY_ID, 0, "播放");
		menu.add(0, EXIT_ID, 1, "停止");
		menu.add(0, 3, 1, "修改");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case PLAY_ID: {
				// 此处设定不同分辨率的码流文件
				String file = "/sdcard/352x288.264"; // 352x288.264"; //240x320.264";
				vv.PlayVideo(file);
				return true;
			}
			case EXIT_ID: {
				finish();
				return true;
			}
			case 3:{
				final EditText text = new EditText(this);
				new AlertDialog.Builder(this).setTitle("请输入跳过的帧:"+vv.getIndex()).setIcon(
						android.R.drawable.ic_dialog_info).setView(
								text).setPositiveButton("确定", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										vv.setIndex(Integer.parseInt(text.getText().toString()));
										Toast.makeText(getApplicationContext(),text.getText().toString(),0).show();
									}
								})
						.setNegativeButton("取消", null).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}
}

class VViewV extends View implements Runnable {
	H264Android h264 = H264Android.getInstances();
	Bitmap		mBitQQ		= null;
	Paint		mPaint		= null;
	Bitmap		mSCBitmap	= null;
	int			width		= 352;													// 此处设定不同的分辨率
	int			height		= 288;
	byte[]		mPixel		= new byte[width * height * 2];
	ByteBuffer	buffer		= ByteBuffer.wrap(mPixel);
	Bitmap		VideoBit	= Bitmap.createBitmap(width, height, Config.RGB_565);
	int			mTrans		= 0x0F0F0F0F;
	String		PathFileName;
	int index = 0;
	int count = 0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public VViewV(Context context) {
		super(context);
		setFocusable(true);
		int i = mPixel.length;
		for (i = 0; i < mPixel.length; i++) {
			mPixel[i] = (byte) 0x00;
		}
	}

	public void PlayVideo(String file) {
		PathFileName = file;
		new Thread(this).start();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Bitmap tmpBit = Bitmap.createBitmap(mPixel, 320, 480,
		// Bitmap.Config.RGB_565);//.ARGB_8888);
		VideoBit.copyPixelsFromBuffer(buffer);// makeBuffer(data565, N));
		buffer.position(0);
		canvas.drawBitmap(VideoBit, 0, 0, null);
	}

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
		StringBuffer sb = new StringBuffer();
		for(int k = 0 ; k  <  30 ; k++ ){
			sb.append(NalBuf[k]+",");
		}
		Log.d("H264Android", "MergeBuffer  mTrans=="+mTrans+"/NalBuf=="+sb.toString());
		return i;
	}
	public void run() {
		InputStream is = null;
		FileInputStream fileIS = null;
		int iTemp = 0;
		int nalLen;
		boolean bFirst = true;
		boolean bFindPPS = true;
		int bytesRead = 0;
		int NalBufUsed = 0;
		int SockBufUsed = 0;
		byte[] NalBuf = new byte[40980]; // 40k 
		byte[] SockBuf = new byte[2048];
		try {
			fileIS = new FileInputStream(PathFileName);
		} catch (IOException e) {
			return;
		} 
		InitDecoder(width, height);
		while (!Thread.currentThread().isInterrupted()) {
			try {
				bytesRead = fileIS.read(SockBuf, 0, 2048);
				StringBuffer sb = new StringBuffer();
				for(int k = 0 ; k  <  2048 ; k++ ){
					sb.append(SockBuf[k]+",");
				}
				Log.i("H264",sb.toString());
			} catch (IOException e) {
			}
			if (bytesRead <= 0)
				break;
			SockBufUsed = 0;
			while (bytesRead - SockBufUsed > 0) {
				nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf, SockBufUsed, bytesRead- SockBufUsed);
				NalBufUsed += nalLen;
				SockBufUsed += nalLen;
				while (mTrans == 1) {
					mTrans = 0xFFFFFFFF;
					if (bFirst == true) // the first start flag
					{
						bFirst = false;
					} else // a complete NAL data, include 0x00000001 trail.
					{
						if (bFindPPS == true) // true
						{
							if ((NalBuf[4] & 0x1F) == 7) {
								bFindPPS = false;
							} else {
								NalBuf[0] = 0;
								NalBuf[1] = 0;
								NalBuf[2] = 0;
								NalBuf[3] = 1;
								NalBufUsed = 4;
								break;
							}
						}
						// decode nal
						StringBuffer sb = new StringBuffer();
						for(int k = 0 ; k  <  30 ; k++ ){
							sb.append(NalBuf[k]+",");
						}
						count ++;
						if(index==count){
							
						}else{
							iTemp = DecoderNal(NalBuf, NalBufUsed - 4, mPixel);
							if (iTemp > 0){
								postInvalidate(); // 使用postInvalidate可以直接在线程中更新界面 //
								Log.v("iTemp", index+"/iTemp:"+iTemp+"NalBuf=="+sb.toString());
							}else{
								Log.e("iTemp", index+"/iTemp:"+iTemp+"NalBuf=="+sb.toString());
							}
						}
					
												// postInvalidate();
					}
					NalBuf[0] = 0;
					NalBuf[1] = 0;
					NalBuf[2] = 0;
					NalBuf[3] = 1;
					NalBufUsed = 4;
				}
			}
		}
		try {
			if (fileIS != null)
				fileIS.close();
			if (is != null)
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		count=0;
		UninitDecoder();
	}

	private int DecoderNal(byte[] nalBuf, int i, byte[] mPixel2) {
		return h264.JDecoderNal(nalBuf,i,mPixel2);
	}

	private int UninitDecoder() {
		Log.e("iTemp", "==============UninitDecoder============");
		return h264.JUninitDecoder();
		
	}

	private int InitDecoder(int width2, int height2) {
		return h264.JInitDecoder(width2, height2);
	}
}
