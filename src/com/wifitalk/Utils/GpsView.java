
package com.wifitalk.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.GpsActivity;
import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainActivity.GPSCallBack;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.bean.XY;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Activity.SelectPicPopupWindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

/**
 * @ClassName : CoordinatesView
 * @Description : TODO
 * @author : ZGX zhangguoxiao_happy@163.com
 * @date : 2011-10-9 上午09:06:38
 * 
 */
public class GpsView extends View{
	private SelectPicPopupWindow selectPicPopupWindow;
	//	private int multiple = 131072;//放大倍数
	private int multiple = 1;//放大倍数
	public static GpsView getInstances = null;
	private Paint mPaint;
	private Paint zPaint;
	public Context mContext;
	private double center_x_width =0;//屏幕中心X坐标
	private double center_y_width =0;//屏幕中心Y坐标
	private double dst_x =0;//偏移中心X坐标
	private double dst_y =0;//偏移中心Y坐标
	private float y_height = 0;//纵坐标长度
	private float x_width = 0;//横坐标长度
	private  List<XY> xyList;//参数信息
	private float y_px =0;//纵坐标刻度高度
	private float x_px =0;//横坐标刻度长度
	private float x_px_s=0;// 坐标刻度/5   画四边形的长度
	private float y_px_s=0;// 坐标刻度/5  四边形的高度
	/*
	 * 自定义控件一般写两个构造方法 CoordinatesView(Context context)用于java硬编码创建控件
	 * 如果想要让自己的控件能够通过xml来产生就必须有第2个构造方法 CoordinatesView(Context context,
	 * AttributeSet attrs) 因为框架会自动调用具有AttributeSet参数的这个构造方法来创建继承自View的控件
	 */
	public GpsView(Context context) {
		super(context, null);
		init(context);
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				postInvalidate();
			}
		});
	}

	public GpsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				postInvalidate();
			}
		});
	}

	private void init(Context context) {
		mContext = context;
		getInstances = this;
		// 设置颜料
		mPaint = new Paint();
		zPaint = new Paint();
		zPaint.setColor(Color.GREEN);
		zPaint.setTextSize(30f);
		//抗锯齿
		zPaint.setAntiAlias(true);  
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(30f);
		//抗锯齿
		mPaint.setAntiAlias(true);  
		ViewTreeObserver vto = this.getViewTreeObserver();   
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
			@Override  
			public void onGlobalLayout() { 
				getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				y_height=getHeight();
				x_width=	getWidth();
				y_px = y_height /5;
				x_px = x_width /5;
				x_px_s = x_px/3;
				y_px_s = x_px_s; 
				center_x_width  = x_width/2;
				center_y_width = y_height/2;
				Log.i("111", "height:"+y_height); 
				Log.i("111", "width:"+x_width);
				xyList = new ArrayList<XY>();
				//				dst_x  =  center_x_width- MainActivity.getInstance.getLongitude()*multiple;
				//				dst_y  =  center_y_width- (180-MainActivity.getInstance.getLatitude()*multiple);
				dst_x  =  center_x_width- MainActivity.getInstance.getLongitude()*multiple;
				dst_y  =  center_y_width- (180-MainActivity.getInstance.getLatitude()*multiple);
				//				xyList.add(new XY(113.275797,29.494165, "洪湖市"));
				//				xyList.add(new XY(113.52222,22.353818, "自己"));//保安
				//				xyList.add(new XY(114.071416,22.325579, "罗湖区"));
				//				xyList.add(new XY(126.39362, 45.32434, "哈尔滨（45,126） "));
				xyList.add(new XY(MainActivity.getInstance.getLongitude(),MainActivity.getInstance.getLatitude(),MainService.getInstance().getIPadd(),"自己"));

			}   
		});

	}
	//	double mx = 87.5989548588;
	//	double my = 43.432506;

	/** 
	 * 计算地球上任意两点(经纬度)距离 
	 *  
	 * @param long1 
	 *            第一点经度 
	 * @param lat1 
	 *            第一点纬度 
	 * @param long2 
	 *            第二点经度 
	 * @param lat2 
	 *            第二点纬度 
	 * @return 返回距离 单位：米 
	 */  
	public static double Distance(double long1, double lat1, double long2,  
			double lat2) {  
		double a, b, R;  
		R = 6378137; // 地球半径  
		lat1 = lat1 * Math.PI / 180.0;  
		lat2 = lat2 * Math.PI / 180.0;  
		a = lat1 - lat2;  
		b = (long1 - long2) * Math.PI / 180.0;  
		double d;  
		double sa2, sb2;  
		sa2 = Math.sin(a / 2.0);  
		sb2 = Math.sin(b / 2.0);  
		d = 2  
				* R  
				* Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)  
						* Math.cos(lat2) * sb2 * sb2));  
		return d;  
	} 

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		dst_x  =  center_x_width- MainActivity.getInstance.getLongitude()*multiple;
		dst_y  =  center_y_width- (180-MainActivity.getInstance.getLatitude()*multiple);
		//		dst_x  =  center_x_width- MainActivity.getInstance.getLongitude()*multiple;
		//		dst_y  =  center_y_width- (180-MainActivity.getInstance.getLatitude()*multiple);
		if (canvas == null) {
			return;
		}

		for(int i = 0; i < 6;i++){
			canvas.drawLine(x_px*i, 0,x_px*i, y_height , mPaint);   //5条竖线
		}
		for(int i = 0; i < 6;i++){
			canvas.drawLine(0, y_px*i, x_width, y_px*i, mPaint);   //5条横线
		}
		if(xyList!=null){
			for(int i = 0 ; i < xyList.size();i++){
				double X = xyList.get(i).getX()*multiple+dst_x;
				double Y = (180-xyList.get(i).getY()*multiple)+dst_y;
				drawRectPoint(canvas,X,Y,xyList.get(i).getIpaddress(),xyList.get(i).getName());
			}
		}
	}

	private void drawRectPoint(Canvas canvas,double X,double Y,String ip,String name) {
		float pointX = (float) X;
		float pointY = (float) Y;
		RectF rect = new RectF(pointX-x_px_s, pointY-y_px_s, pointX+x_px_s, pointY+y_px_s);   //方块的面积
		if(ip.equals(MainService.getInstance().getIPadd())){
			canvas.drawRect(rect, zPaint);//绘制方块
			canvas.drawText("自己", pointX-x_px_s*2, pointY+y_px_s*2.5f, mPaint);//写文字    光标左下偏移2个单位
		}else{
			canvas.drawRect(rect, mPaint);//绘制方块
			if(ip!=null){
				canvas.drawText(ip, pointX-x_px_s*2, pointY+y_px_s*2.5f, mPaint);//写文字    光标左下偏移2个单位
				canvas.drawText(name, pointX-x_px_s*2, pointY+y_px_s*3.5f, mPaint);//写文字    光标左下偏移2个单位
			}
		}
	}
	Bitmap bitmap;

	/**
	 * 绘制
	 */
	public void Refresh(){

		postInvalidate();
	}



	@Override  
	public boolean onTouchEvent(MotionEvent event) {   
		// 获取点击屏幕时的点的坐标   
		float x = event.getX();   
		float y = event.getY();   
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			whichCircle(x, y);   
		}
		return super.onTouchEvent(event);   
	}   

	/**  
	 * 确定点击的点在哪个区域内  
	 * @param x  
	 * @param y  
	 */  
	private void whichCircle(float x, float y) {   
		int count  = xyList.size();
		int size = 0;
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < count;i++){
			if(getAbs(x-xyList.get(i).getX()*multiple-dst_x)<=x_px_s&&getAbs(y-(180-xyList.get(i).getY()*multiple)-dst_y)<=y_px_s){
				size++;
				//				showTextToast("区域坐标：x:"+x+",y"+y+xyList.get(i).getMsg()+"经纬度：j:"+xyList.get(i).getY()+",w"+xyList.get(i).getX(), 0);
				//				double mi = Distance(xyList.get(i).getX(), xyList.get(i).getY(), MainActivity.getInstance.getLongitude(), MainActivity.getInstance.getLatitude());
				String distance= "";
				double mi = Distance(xyList.get(i).getX(), xyList.get(i).getY(), MainActivity.getInstance.getLongitude(), MainActivity.getInstance.getLatitude());
				double gl = Math.round(mi/100d)/10d;//公里
				if(mi>1000){
					distance = gl+"公里";
				}else{
					BigDecimal   b   =   new   BigDecimal(mi);  
					double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();  
					distance = f1+"米";
				}
				if(!xyList.get(i).getName().equals("自己")){
					selectPicPopupWindow = new SelectPicPopupWindow(GpsActivity.instances, itemsOnClick,xyList.get(i).getName(),xyList.get(i).getIpaddress());
					selectPicPopupWindow.showAtLocation(GpsActivity.instances.findViewById(R.id.gpsview), Gravity.TOP|Gravity.RIGHT, 10, 230); //设置layout在PopupWindow中显示的位置
				}
				//+"N:"+xyList.get(i).getY()+",E"+xyList.get(i).getX()
				sb.append(xyList.get(i).getName()+"距离您"+distance+"!\n");
//				showTextToast(xyList.get(i).getIpaddress()+"N:"+xyList.get(i).getY()+",E"+xyList.get(i).getX()+"距离您"+distance+"!", 1);
//				Log.i("111", "add");
			} 

			if(size>0){
				showTextToast(sb.toString(), 1);
			}

		}

	}   
	//为弹出窗口实现监听类
	private OnClickListener  itemsOnClick = new OnClickListener(){

		public void onClick(View v) {
			selectPicPopupWindow.dismiss();
		}
	};
	private Toast toast = null;
	/**
	 * 
	 * @param msg 内容
	 * @param i  显示时间   0 短时间   | 1 长时间
	 */
	private void showTextToast(String msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(mContext, msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}
	/**
	 * 返回绝对值
	 * @return
	 */
	public double getAbs(double number){
		return Math.abs(number);
	}

	public void gpsChange(double x,double y){
		if(xyList.get(0).getName().equals("自己")){
			xyList.get(0).setX(x);
			xyList.get(0).setY(y);
			Refresh();
		}
	}

	public  void addDrivice(XY xy){
		if(xyList==null)
			return;
		if(xy.getIpaddress().equals(MainService.getInstance().getIPadd()))
			return;
		for(int i = 0 ; i < xyList.size();i++){
			if(xyList.get(i).getIpaddress().equals(xy.getIpaddress())){
				xyList.get(i).setX(xy.getX());
				xyList.get(i).setY(xy.getY());
				Refresh();
				return;
			}
		}
		xyList.add(xy);
		Refresh();
	}

	/**
	 * 放大
	 */
	public void amplify() {
		if(multiple==0){
			multiple++;
		}else{
			multiple=multiple+multiple;
		}
		Refresh();
	}

	/**
	 * 缩小
	 */
	public void narrow() {
		if(multiple-multiple/2>0){
			multiple = multiple-multiple/2;
		}
		Refresh();
	}





}



