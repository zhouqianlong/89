
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
 * @date : 2011-10-9 ����09:06:38
 * 
 */
public class GpsView extends View{
	private SelectPicPopupWindow selectPicPopupWindow;
	//	private int multiple = 131072;//�Ŵ���
	private int multiple = 1;//�Ŵ���
	public static GpsView getInstances = null;
	private Paint mPaint;
	private Paint zPaint;
	public Context mContext;
	private double center_x_width =0;//��Ļ����X����
	private double center_y_width =0;//��Ļ����Y����
	private double dst_x =0;//ƫ������X����
	private double dst_y =0;//ƫ������Y����
	private float y_height = 0;//�����곤��
	private float x_width = 0;//�����곤��
	private  List<XY> xyList;//������Ϣ
	private float y_px =0;//������̶ȸ߶�
	private float x_px =0;//������̶ȳ���
	private float x_px_s=0;// ����̶�/5   ���ı��εĳ���
	private float y_px_s=0;// ����̶�/5  �ı��εĸ߶�
	/*
	 * �Զ���ؼ�һ��д�������췽�� CoordinatesView(Context context)����javaӲ���봴���ؼ�
	 * �����Ҫ���Լ��Ŀؼ��ܹ�ͨ��xml�������ͱ����е�2�����췽�� CoordinatesView(Context context,
	 * AttributeSet attrs) ��Ϊ��ܻ��Զ����þ���AttributeSet������������췽���������̳���View�Ŀؼ�
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
		// ��������
		mPaint = new Paint();
		zPaint = new Paint();
		zPaint.setColor(Color.GREEN);
		zPaint.setTextSize(30f);
		//�����
		zPaint.setAntiAlias(true);  
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(30f);
		//�����
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
				//				xyList.add(new XY(113.275797,29.494165, "�����"));
				//				xyList.add(new XY(113.52222,22.353818, "�Լ�"));//����
				//				xyList.add(new XY(114.071416,22.325579, "�޺���"));
				//				xyList.add(new XY(126.39362, 45.32434, "��������45,126�� "));
				xyList.add(new XY(MainActivity.getInstance.getLongitude(),MainActivity.getInstance.getLatitude(),MainService.getInstance().getIPadd(),"�Լ�"));

			}   
		});

	}
	//	double mx = 87.5989548588;
	//	double my = 43.432506;

	/** 
	 * �����������������(��γ��)���� 
	 *  
	 * @param long1 
	 *            ��һ�㾭�� 
	 * @param lat1 
	 *            ��һ��γ�� 
	 * @param long2 
	 *            �ڶ��㾭�� 
	 * @param lat2 
	 *            �ڶ���γ�� 
	 * @return ���ؾ��� ��λ���� 
	 */  
	public static double Distance(double long1, double lat1, double long2,  
			double lat2) {  
		double a, b, R;  
		R = 6378137; // ����뾶  
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
			canvas.drawLine(x_px*i, 0,x_px*i, y_height , mPaint);   //5������
		}
		for(int i = 0; i < 6;i++){
			canvas.drawLine(0, y_px*i, x_width, y_px*i, mPaint);   //5������
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
		RectF rect = new RectF(pointX-x_px_s, pointY-y_px_s, pointX+x_px_s, pointY+y_px_s);   //��������
		if(ip.equals(MainService.getInstance().getIPadd())){
			canvas.drawRect(rect, zPaint);//���Ʒ���
			canvas.drawText("�Լ�", pointX-x_px_s*2, pointY+y_px_s*2.5f, mPaint);//д����    �������ƫ��2����λ
		}else{
			canvas.drawRect(rect, mPaint);//���Ʒ���
			if(ip!=null){
				canvas.drawText(ip, pointX-x_px_s*2, pointY+y_px_s*2.5f, mPaint);//д����    �������ƫ��2����λ
				canvas.drawText(name, pointX-x_px_s*2, pointY+y_px_s*3.5f, mPaint);//д����    �������ƫ��2����λ
			}
		}
	}
	Bitmap bitmap;

	/**
	 * ����
	 */
	public void Refresh(){

		postInvalidate();
	}



	@Override  
	public boolean onTouchEvent(MotionEvent event) {   
		// ��ȡ�����Ļʱ�ĵ������   
		float x = event.getX();   
		float y = event.getY();   
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			whichCircle(x, y);   
		}
		return super.onTouchEvent(event);   
	}   

	/**  
	 * ȷ������ĵ����ĸ�������  
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
				//				showTextToast("�������꣺x:"+x+",y"+y+xyList.get(i).getMsg()+"��γ�ȣ�j:"+xyList.get(i).getY()+",w"+xyList.get(i).getX(), 0);
				//				double mi = Distance(xyList.get(i).getX(), xyList.get(i).getY(), MainActivity.getInstance.getLongitude(), MainActivity.getInstance.getLatitude());
				String distance= "";
				double mi = Distance(xyList.get(i).getX(), xyList.get(i).getY(), MainActivity.getInstance.getLongitude(), MainActivity.getInstance.getLatitude());
				double gl = Math.round(mi/100d)/10d;//����
				if(mi>1000){
					distance = gl+"����";
				}else{
					BigDecimal   b   =   new   BigDecimal(mi);  
					double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();  
					distance = f1+"��";
				}
				if(!xyList.get(i).getName().equals("�Լ�")){
					selectPicPopupWindow = new SelectPicPopupWindow(GpsActivity.instances, itemsOnClick,xyList.get(i).getName(),xyList.get(i).getIpaddress());
					selectPicPopupWindow.showAtLocation(GpsActivity.instances.findViewById(R.id.gpsview), Gravity.TOP|Gravity.RIGHT, 10, 230); //����layout��PopupWindow����ʾ��λ��
				}
				//+"N:"+xyList.get(i).getY()+",E"+xyList.get(i).getX()
				sb.append(xyList.get(i).getName()+"������"+distance+"!\n");
//				showTextToast(xyList.get(i).getIpaddress()+"N:"+xyList.get(i).getY()+",E"+xyList.get(i).getX()+"������"+distance+"!", 1);
//				Log.i("111", "add");
			} 

			if(size>0){
				showTextToast(sb.toString(), 1);
			}

		}

	}   
	//Ϊ��������ʵ�ּ�����
	private OnClickListener  itemsOnClick = new OnClickListener(){

		public void onClick(View v) {
			selectPicPopupWindow.dismiss();
		}
	};
	private Toast toast = null;
	/**
	 * 
	 * @param msg ����
	 * @param i  ��ʾʱ��   0 ��ʱ��   | 1 ��ʱ��
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
	 * ���ؾ���ֵ
	 * @return
	 */
	public double getAbs(double number){
		return Math.abs(number);
	}

	public void gpsChange(double x,double y){
		if(xyList.get(0).getName().equals("�Լ�")){
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
	 * �Ŵ�
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
	 * ��С
	 */
	public void narrow() {
		if(multiple-multiple/2>0){
			multiple = multiple-multiple/2;
		}
		Refresh();
	}





}



