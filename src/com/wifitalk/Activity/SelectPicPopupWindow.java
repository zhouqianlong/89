package com.wifitalk.Activity;





import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.app.CameraActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class SelectPicPopupWindow extends PopupWindow implements OnClickListener{
	public static final String TAG = "SF6";

	private boolean isopen = false;
	private Button  Read1,Read2,Read3;
	private View mMenuView;
	public Context context;
	private TextView tv_info;

	public SelectPicPopupWindow(final Activity context,OnClickListener itemsOnClick,String name,String ip) {
		super(context);
		setIPaddress(ip);
		setDistance(name);
		this.context =context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.bottomdialog, null);
		tv_info = (TextView) mMenuView.findViewById(R.id.tv_info);
		Read1 =  (Button) mMenuView.findViewById(R.id.Read1);
		Read2 =  (Button) mMenuView.findViewById(R.id.Read2);
		Read3 =  (Button) mMenuView.findViewById(R.id.Read3);
		tv_info.setText(name+"\n"+ip);
		Read1.setOnClickListener(this);
		Read2.setOnClickListener(this);
		Read3.setOnClickListener(this);
		int h = context.getWindowManager().getDefaultDisplay().getHeight();
		int w = context.getWindowManager().getDefaultDisplay().getWidth();
		//设置按钮监听
		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(w/2+50);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.mystyle);
		//实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		//mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mMenuView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
		
		

	}
	
	 
	@Override
	public void onClick(View v) { 
		if(v==Read1){
			Toast.makeText(context, getIPaddress()+""+getDistance(), 1).show();
			Intent mIntent = new Intent(context, PrepareCallCheckUser.class);
			List<UserBean> list  = new ArrayList<UserBean>();
			list.add(new UserBean("", getIPaddress()));
			MainService.getInstance().setAudioUserBeans(list);
			UserBean userBean = new UserBean(getDistance(), getIPaddress());
			mIntent.putExtra("UserBean", userBean);
			context.startActivity(mIntent);
		}
		if(v==Read2){
			sendUDP("Video",getIPaddress());
			Intent mIntent = new Intent(context, CameraActivity.class);
			List<UserBean> list  = new ArrayList<UserBean>();
			list.add(new UserBean("", getIPaddress()));
			MainService.getInstance().setAudioUserBeans(list);
			UserBean userBean = new UserBean("", getIPaddress());
			mIntent.putExtra("UserBean", userBean);
			context.startActivity(mIntent);
			dismiss();
			
		}
		if(v==Read3){
			dismiss();
		}
	}
	public static void sendUDP(final String heard,final String remoteip) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramSocket clientSocket=null;
				try {
					clientSocket = new DatagramSocket();
					StringBuffer str = new StringBuffer(heard+":"+MainService.getInstance().getIPadd());
					// 构建数据包 头+体
					DataPacket dataPacket = new DataPacket(str.toString().getBytes(), new byte[]{01,01,01});
					//// 构建数据报 +发送
					clientSocket.send(new DatagramPacket(dataPacket.getAllData(),
							dataPacket.getAllData().length,InetAddress.getByName(remoteip), AppConfig.PortAudio));
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if(clientSocket!=null){
						clientSocket.close();
					}
				}
			}
		}).start();
	}
	public String IPaddress;
	public String distance;

	public String getIPaddress() {
		return IPaddress;
	}


	public void setIPaddress(String iPaddress) {
		IPaddress = iPaddress;
	}


	public String getDistance() {
		return distance;
	}


	public void setDistance(String distance) {
		this.distance = distance;
	}
	
}
