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
 * @date : 2011-10-9 上午09:06:38
 * 
 */
public class Coordinates extends View {
	public static Coordinates getInstances = null;
	long i= 0;
	/*
	 * 颜料
	 */
	private Paint mPaint;
	/*
	 * 数据集合
	 */
	private List<PointF[]> mPointsList;
	private List<Paint> mPaintList;
	/*
	 * 标题
	 */
	private boolean mHasTitle;
	private String mTitle;
	private int mTitleHeight;
	private PointF mTitlePoint;
	/*
	 * 边距
	 */
	private int mLeftPad, mRightPad, mBottomPad, mTopPad;
	/*
	 * 横轴纵轴密度、长度和比例。
	 */
	private float mXValuePerPix, mYValuePerPix;
	private int mXLen, mYLen;
	private float mXScale, mYScale;
	/*
	 * 横轴纵轴标识和单位
	 */
	private String mXAxisPrickle, mYAxisPrickle;
	private String mXAxisName = "X", mYAxisName = "Y";
	/*
	 * 圆心（坐标值是相对与控件的左上角的）
	 */
	// private PointF mPointZero = new PointF();
	/*
	 * 参考坐标
	 */
	private PointF mPointBase = new PointF();
	private PointF mPointBaseValue = new PointF();
	/*
	 * 交叉点坐标中心点
	 */
	private PointF mPointOrigin = new PointF();

	/*
	 * 自定义控件一般写两个构造方法 CoordinatesView(Context context)用于java硬编码创建控件
	 * 如果想要让自己的控件能够通过xml来产生就必须有第2个构造方法 CoordinatesView(Context context,
	 * AttributeSet attrs) 因为框架会自动调用具有AttributeSet参数的这个构造方法来创建继承自View的控件
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
		// 设置颜料
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(20f);
		//抗锯齿
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
	 * 设置标题高度
	 */
	public void setTitleHeight(int height) {
		mTitleHeight = height;
	}

	/**
	 * 设置图表标题
	 */
	public void setTitleName(String titleName) {
		mTitle = titleName;
	}

	/**
	 * 设置放大缩小倍数
	 */
	public void setScaleXY(float xScale, float yScale) {
		mXScale = xScale;
		mYScale = yScale;
	}

	/**
	 * 设置边距
	 */
	public void setCoordinatesPadding(int leftPad, int rightPad, int topPad,
			int bottomPad) {
		mLeftPad = leftPad + 40;
		mRightPad = rightPad + 20;
		mTopPad = topPad + 10;
		mBottomPad = bottomPad + 40;
	}

	/**
	 * 添加一条曲线
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

	/**	绘制坐标
	 * 坐标固定为  高80 像素   宽200像素
	 * 根据图中的刻度  20   100  做出比例调整       
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
	 * 设置坐标名称和单位
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
	 * 控件创建完成之后，在显示之前都会调用这个方法，此时可以获取控件的大小 并得到中心坐标和坐标轴圆心所在的点。
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
	 * 自定义控件一般都会重载onDraw(Canvas canvas)方法，来绘制自己想要的图形
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
		//		 * 画背景色
		//		 */
		//		canvas.drawColor(Color.WHITE);
		/*				
		 * 画标题
		 */
		//		canvas.drawText(i+""+mTitle, mTitlePoint.x, mTitlePoint.y, mPaint);
		try {

			if(rectF==null){
				rectF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高  
			}
			canvas.drawBitmap(FitTheScreenSizeImage(bitmap, bitmap.getWidth(), bitmap.getHeight()), null, rectF, null);
		} catch (Exception e) {
			//			Log.i("RAMY-UDPVideoCodec", "画图错误"+e.getMessage());
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
