package com.wifitalk.Utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * @ClassName : CoordinatesView
 * @Description : TODO
 * @author : ZGX zhangguoxiao_happy@163.com
 * @date : 2011-10-9 ����09:06:38
 * 
 */
public class Coordinates extends View {
	public static Coordinates getInstances = null;
	long i= 0;
	/*
	 * ����
	 */
	private Paint mPaint;
	/*
	 * ���ݼ���
	 */
	private List<PointF[]> mPointsList;
	private List<Paint> mPaintList;
	/*
	 * ����
	 */
	private boolean mHasTitle;
	private String mTitle;
	private int mTitleHeight;
	private PointF mTitlePoint;
	/*
	 * �߾�
	 */
	private int mLeftPad, mRightPad, mBottomPad, mTopPad;
	/*
	 * ���������ܶȡ����Ⱥͱ�����
	 */
	private float mXValuePerPix, mYValuePerPix;
	private int mXLen, mYLen;
	private float mXScale, mYScale;
	/*
	 * ���������ʶ�͵�λ
	 */
	private String mXAxisPrickle, mYAxisPrickle;
	private String mXAxisName = "X", mYAxisName = "Y";
	/*
	 * Բ�ģ�����ֵ�������ؼ������Ͻǵģ�
	 */
	// private PointF mPointZero = new PointF();
	/*
	 * �ο�����
	 */
	private PointF mPointBase = new PointF();
	private PointF mPointBaseValue = new PointF();
	/*
	 * ������������ĵ�
	 */
	private PointF mPointOrigin = new PointF();

	/*
	 * �Զ���ؼ�һ��д�������췽�� CoordinatesView(Context context)����javaӲ���봴���ؼ�
	 * �����Ҫ���Լ��Ŀؼ��ܹ�ͨ��xml�������ͱ����е�2�����췽�� CoordinatesView(Context context,
	 * AttributeSet attrs) ��Ϊ��ܻ��Զ����þ���AttributeSet������������췽���������̳���View�Ŀؼ�
	 */
	public Coordinates(Context context) {
		super(context, null);
		init();
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				i++;
				postInvalidate();
			}
		});
	}

	public Coordinates(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				i++;
				postInvalidate();
			}
		});
	}
	int height = 0;
	int width = 0;
	private void init() {
		getInstances = this;
		// ��������
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(20f);
		//�����
		mPaint.setAntiAlias(true);  

		ViewTreeObserver vto = this.getViewTreeObserver();   
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
			@Override  
			public void onGlobalLayout() { 
				getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				height=getHeight();
				width=	getWidth();
				Log.i("111", "height:"+height);
				Log.i("111", "width:"+width);
			}   
		});
	}

	/**
	 * ���ñ���߶�
	 */
	public void setTitleHeight(int height) {
		mTitleHeight = height;
	}

	/**
	 * ����ͼ�����
	 */
	public void setTitleName(String titleName) {
		mTitle = titleName;
	}

	/**
	 * ���÷Ŵ���С����
	 */
	public void setScaleXY(float xScale, float yScale) {
		mXScale = xScale;
		mYScale = yScale;
	}

	/**
	 * ���ñ߾�
	 */
	public void setCoordinatesPadding(int leftPad, int rightPad, int topPad,
			int bottomPad) {
		mLeftPad = leftPad + 40;
		mRightPad = rightPad + 20;
		mTopPad = topPad + 10;
		mBottomPad = bottomPad + 40;
	}

	/**
	 * ���һ������
	 */
	public void addPoints(PointF[] points, Paint paint) {
		if (points == null)
			return;
		if (mPointsList == null)
			mPointsList = new ArrayList<PointF[]>();
			mPointsList.add(points);
			if (mPaintList == null)
				mPaintList = new ArrayList<Paint>();
			if (paint != null)
				mPaintList.add(paint);
			else {
				mPaintList.add(mPaint);
			}
	}
	public float x1, y1, x2, y2, x3, y3,px,py,sx,sy;

	/**	��������
	 * ����̶�Ϊ  ��80 ����   ��200����
	 * ����ͼ�еĿ̶�  20   100  ������������       
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param px
	 * @param py
	 */
	public void addDrawText(float x1,float y1,float x2,float y2,float x3,float y3,float px,float py,float sx,float sy){
		this.x1 = x1*2;
		this.y1 = y1*4;
		this.x2 = x2*2;
		this.y2 = y2*4;
		this.x3 = x3*2;
		this.y3 = y3*4;
		this.px = px*2;
		this.py = py*4;
		this.sx = sx*2;
		this.sy = sx*4;
	}
	/**
	 * �����������ƺ͵�λ
	 */
	public void setAxisNamePrickleXY(String xName, String xPrickle,
			String yName, String yPrickle) {
		mXAxisName = xName;
		mXAxisPrickle = xPrickle;
		mYAxisName = yName;
		mYAxisPrickle = yPrickle;
	}

	// private int centerX, centerY;
	/*
	 * �ؼ��������֮������ʾ֮ǰ������������������ʱ���Ի�ȡ�ؼ��Ĵ�С ���õ����������������Բ�����ڵĵ㡣
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		// centerX = w / 2;
		// centerY = h / 2;
		mXLen = w - mLeftPad - mRightPad;
		mYLen = h - mBottomPad - mTopPad - (mHasTitle ? mTitleHeight : 0);
		mPointOrigin.set(mLeftPad, h - mBottomPad);
		mPointBase.set(mXLen / 2 + mPointOrigin.x, mPointOrigin.y - mYLen / 2);
		mPointBaseValue.set(mXLen / 2 * mXValuePerPix / mXScale, mYLen / 2
				* mYValuePerPix / mYScale);
		// mPointZero.set(mLeftPad, h - mBottomPad);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/*
	 * �Զ���ؼ�һ�㶼������onDraw(Canvas canvas)�������������Լ���Ҫ��ͼ��
	 */	
	RectF rectF =null;

	Matrix matrix = null;
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (canvas == null) {
			return;
		}
		//		/*
		//		 * ������ɫ
		//		 */
		//		canvas.drawColor(Color.WHITE);
		/*				
		 * ������
		 */
		//		canvas.drawText(i+""+mTitle, mTitlePoint.x, mTitlePoint.y, mPaint);
		try {

			if(rectF==null){
				rectF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());   //w��h�ֱ�����Ļ�Ŀ�͸ߣ�Ҳ����������ͼƬ��ʾ�Ŀ�͸�  
			}
			canvas.drawBitmap(FitTheScreenSizeImage(bitmap, bitmap.getWidth(), bitmap.getHeight()), null, rectF, null);
		} catch (Exception e) {
			//			Log.i("RAMY-UDPVideoCodec", "��ͼ����"+e.getMessage());
		}

	}
	Bitmap bitmap;

	public void Draw(Bitmap bitmap,long i){
		this.i = i;
		this.bitmap = bitmap;
		postInvalidate();
	}


	public  Bitmap FitTheScreenSizeImage(Bitmap m,int ScreenWidth, int ScreenHeight)
	{
		float width  = (float)ScreenWidth/m.getWidth()*2;
		float height = (float)ScreenHeight/m.getHeight()*2;
		if(matrix==null){
			matrix = new Matrix();
			matrix.postScale(width,height);
			matrix.setRotate(-90);
		}
		return Bitmap.createBitmap(m, 0, 0, m.getWidth(), m.getHeight(), matrix, true);
	} 
}
