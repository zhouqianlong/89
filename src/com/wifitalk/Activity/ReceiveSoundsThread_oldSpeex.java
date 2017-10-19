package com.wifitalk.Activity;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.tst.bean.ChatInfo;
import com.ramy.minervue.R;
import com.ramy.minervue.app.CameraActivity;
import com.ramy.minervue.app.GpsActivity;
import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.app.MainActivity.GPSCallBack;
import com.ramy.minervue.bean.Dervic;
import com.ramy.minervue.bean.OnLineBean;
import com.ramy.minervue.bean.XY;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.ramy.minervue.sync.LocalFileUtil;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.util.PreferenceUtil;
import com.sinaapp.bashell.sayhi.Speex;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;
import com.wifitalk.Utils.GpsView;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class ReceiveSoundsThread_oldSpeex extends Thread
{

	public static final String TAG ="ReceiveSoundsThread_oldSpeex";
	private AudioTrack player = null;
	private boolean isRunning = false;
	private byte[] recordBytes = new byte[2048];
	private Context instances;
	public static boolean videoStatu = false;//视频状态
	private List<OnLineBean> listOnLine = new ArrayList<OnLineBean>();
	private DBHelper dbHelper;
	public List<UserBean> pd_list = new ArrayList<UserBean>();
	public void destoryReceiveSoundsThread(){
		player.pause();
		player.flush();
		player.release();
	}
	public String ipAddress;
	public String[] deviceIps;
	public static final int TK1 = 120001;
	public static final int TK2 = 120002;
	public static int SOUND_STATU = 0;
	public static String  speakInfo = "";

	public ReceiveSoundsThread_oldSpeex(String ip, Context getInstances) {
		this.ipAddress = ip;
		this.deviceIps = ip.split("\\.");
		// 播放器
		int bufferSizeInBytes = AudioTrack.getMinBufferSize(AppConfig.AUDIO_CAI_YANG_LV, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		player = new AudioTrack(AudioManager.STREAM_SYSTEM, AppConfig.AUDIO_CAI_YANG_LV, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
		player.setStereoVolume(1.0f, 1.0f);
		//		player.setStereoVolume(0.5f, 0.5f);


		this.instances = getInstances;
		SOUND_STATU_Thread();
		dbHelper = MainService.getInstance().dbHelper; 
		HeartbeatThread();
	}


	public void initIp(){
		ipAddress = MainService.getInstance().getIPadd(); 
		deviceIps = ipAddress.split("\\.");
	}


	/**
	 * 心跳包线程
	 */
	public void HeartbeatThread(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					playBaojinMethod();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				//每2秒发送一次心跳包
				while (true) {
					DatagramSocket clientSocket=null;
					try {
						if(MainService.getInstance().system_Action==false){
							Thread.sleep(2000);
						}else{
							clientSocket = new DatagramSocket();
							StringBuffer str;
							if(CameraActivity.instances==null){
								 str = new StringBuffer("online_1:"+MainService.getInstance().getUUID()+":"+PrepareCallCheckUser.pindaoID+":"+MainService.getInstance().getLocation()+":"+MainService.getInstance().getMacAddressLastSix()+":0");								
							}else{
								 str = new StringBuffer("online_1:"+MainService.getInstance().getUUID()+":"+PrepareCallCheckUser.pindaoID+":"+MainService.getInstance().getLocation()+":"+MainService.getInstance().getMacAddressLastSix()+":1");																
							}
							DataPacket dataPacket = new DataPacket(str.toString().getBytes(), new byte[]{01,01,01});
							clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length, InetAddress.getByName("255.255.255.255"), AppConfig.PortAudio));
							Log.e("WifiConfig", str.toString());
						}
						Thread.sleep(2000);
					} catch (SocketException e) {
						e.printStackTrace();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						if(clientSocket!=null){
							clientSocket.close();
						}
					}
				}
			}

		}).start();



	}

	MediaPlayer mediaPlayer=new MediaPlayer();
	AlertDialog mDialog = null;
	public static int count;
	private void playBaojinMethod() {
		try {
			if(GpsActivity.statu==false){
				return;
			}
			final List<Dervic>  list = MainService.getInstance().dervicList;
			if(list!=null){
				for(int i = 0 ; i < list.size();i++){
					if(MainService.getInstance().mBeaconList.get(0).mac.equals(list.get(i).getMac())){
						if(list.get(i).getStatu().equals("报警")){
							final int position = i;
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									if(GpsActivity.statu==false){
										return;
									}
									if(list.get(position).getUrl().equals("默认")){
										PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
										util.playSounds(4, 0);
									}else{
										try {
											String[] musicPath =  list.get(position).getUrl().split("\\\\");
											if(!mediaPlayer.isPlaying()){
												mediaPlayer=new MediaPlayer();
												mediaPlayer.setDataSource(	LocalFileUtil.getPubPath(musicPath[musicPath.length-1]));
												mediaPlayer.prepare();//准备播放
												mediaPlayer.start();//播放
											}
										} catch (Exception e) {
											PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
											util.playSounds(4, 0);

										}

									}
								}
							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<UserBean> getHead(String[] head){
		List<UserBean> userBeans = new ArrayList<UserBean>();
		for(int i = 2 ;i < head.length;i++){try {
			//跳过 S
			UserBean userBean = new UserBean();
			userBean.setMacAddress(head[i].substring(0, 6));
			userBean.setUserIp(head[i].substring(6));
			userBeans.add(userBean);
		} catch (Exception e) {
			return userBeans;
		}
		}
		return userBeans;
	}


	@Override
	public synchronized void run(){
		super.run();
		
		Speex playSpeex = new Speex();
		short[] decData = new short[256];
		DatagramSocket serverSocket = null;
		
		while (true) {
			if (isRunning) {
				try {
					if(serverSocket ==null)
						serverSocket = new DatagramSocket(AppConfig.PortAudio);
				} catch (SocketException e2) {
					serverSocket = null;
				}
				
				DatagramPacket receivePacket = new DatagramPacket(recordBytes, recordBytes.length);
				try {
					if(serverSocket==null){
						serverSocket = new DatagramSocket(AppConfig.PortAudio);
					}
					serverSocket.receive(receivePacket);
				} catch (Exception e) {
					try {
						Log.e("WifiConfig", "ReceiveSoundsThread err");
						Thread.sleep(6000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					return;
				}
				try {
					byte[] data = receivePacket.getData();
					byte[] head = new byte[DataPacket.headLen];
					byte[] body = new byte[DataPacket.bodyLen];
					System.arraycopy(data, 0, head, 0, head.length);					// 获得包头
					System.arraycopy(data, head.length, body, 0, body.length);			// 获得包体
					String thisDevInfo = new String(head).trim();
					String[] strarray=thisDevInfo.split(":");//分割包头 
					List<UserBean> listPeople = MainService.getInstance().getDB_ZU_ALL_List();//获取全组成员
					String macAddress = strarray[1];
					Log.i("WifiConfig", "strarray[0]:"+strarray[0]+" ipsrc:"+receivePacket.getAddress().toString());
					if(strarray[0].equals("S")){
						List<UserBean> userBeans = new ArrayList<UserBean>();
						String userName = "";
						List<UserBean>  heards = getHead(strarray);//解析包头  （内容为组员）
						macAddress = strarray[1].substring(0, 6);//mac地址
						speakInfo = macAddress+"正在说话";//按钮名称
						for(int i = 0 ; i < listPeople.size();i++){
							if(listPeople.get(i).getMacAddress().equals(macAddress)){
								userName = listPeople.get(i).getUsername();
								speakInfo = userName+"正在说话";
								break;
							}
						}
						UserBean userBean = new UserBean(userName, receivePacket.getAddress().getHostAddress());
						userBean.setMacAddress(macAddress);
						userBeans.add(userBean);//呼叫方
						for(int i =0 ; i < heards.size();i++){
							userName="";
							macAddress = heards.get(i).getMacAddress();//默认值
							for(int k =0 ; k < listPeople.size();k++){//与列表人员比较
								if(heards.get(i).getMacAddress().equals(listPeople.get(k).getMacAddress())){
									userName = listPeople.get(k).getUsername();
									continue;
								}
							}
							if(heards.get(i).getUserIp().equals(deviceIps[3])){
								continue;
							}
							UserBean userbean = new UserBean(userName, deviceIps[0]+"."+deviceIps[1]+"."+deviceIps[2]+"."+heards.get(i).getUserIp());
							userbean.setMacAddress(macAddress);
							userBeans.add(userbean);
						}
						if(StatusManager.isVideo()==false&&StatusManager.isMonitor()==false&&StatusManager.isGasPhoto()==false){
							byte[] speexDate = new byte[AppConfig.AUDIO_BPS];
							for(int i = 0 ; i < body.length/AppConfig.AUDIO_BPS;i++){
								System.arraycopy(body, speexDate.length*i, speexDate, 0, speexDate.length);
								int dec = playSpeex.decode(speexDate, decData, speexDate.length);
								if(!strarray[2].equals(PrepareCallCheckUser.pindaoID)){
									continue;
								}
								if(MainService.JINYIN){//没有静音
									if (dec > 0) {
//										if(videoStatu == true){
//											try {
//												if(receivePacket.getAddress().getHostAddress().equals(CameraActivity.instances.userBean.getUserIp())){
//													player.write(decData, 0, dec);
//												}
//											} catch (Exception e) {
//												continue;
//											}
//										}else{
//											setDate(System.currentTimeMillis(), userBeans);
//											player.write(decData, 0, dec);
//										}
										
										setDate(System.currentTimeMillis(), userBeans);
										player.write(decData, 0, dec);
										player.play();
									}
									SOUND_STATU  = 30;
//									if(player.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING){
//									}
								}
							}
							
						}
						continue;
					} 
					if(strarray[0].equals("TK1")){//聊天包
						byte[] content = new byte[1024];
						System.arraycopy(data, 100, content, 0, 1024);
						Log.i("WifiConfig", receivePacket.getAddress().toString()+"("+strarray[0]+")"+strarray[1]);
						Message msg = Message.obtain();
						ChatInfo chatInfo = new ChatInfo();
						chatInfo.content = new String(content).trim();;
						chatInfo.fromOrTo = 0;
						chatInfo.toMac = strarray[4]+strarray[5]+strarray[6];
						chatInfo.mac = MainService.getInstance().getMacAddress();
						chatInfo.type = 1;

						UserBean bean = new UserBean(strarray[1],receivePacket.getAddress().getHostAddress());
						bean.setUsername(dbHelper.findNameByMac(strarray[4]+strarray[5]+strarray[6]));
						bean.setMacAddress(chatInfo.toMac);
						bean.setChatInfo(chatInfo);
						msg.obj = bean;
						msg.what = TK1 ;
						mHandler.sendMessage(msg);
					}
					if(strarray[0].equals("TK2")){//聊天文件包
						byte[] content = new byte[1024];
						System.arraycopy(data, 100, content, 0, 1024);
						Log.i("WifiConfig", receivePacket.getAddress().toString()+"("+strarray[0]+")"+strarray[1]);
						Message msg = Message.obtain();
						ChatInfo chatInfo = new ChatInfo();
						chatInfo.content = new String(content).trim();;
						chatInfo.type = 2;
						chatInfo.toMac = strarray[4]+strarray[5]+strarray[6];
						chatInfo.mac = MainService.getInstance().getMacAddress();
						chatInfo.fromOrTo = 0;
						UserBean bean = new UserBean(strarray[1],receivePacket.getAddress().getHostAddress());
						bean.setUsername(dbHelper.findNameByMac(strarray[4]+strarray[5]+strarray[6]));
						bean.setMacAddress(chatInfo.toMac);
						bean.setChatInfo(chatInfo);
						msg.obj = bean;
						msg.what = TK2 ;

						mHandler.sendMessage(msg);
					}
					if(strarray[0].equals("P")){
						if(StatusManager.isVideo()==false&&StatusManager.isMonitor()==false&&StatusManager.isGasPhoto()==false&&strarray[1].equals(MainService.getInstance().pindao+"")){
							byte[] speexDate = new byte[20];
							System.arraycopy(body, 0, speexDate, 0, speexDate.length);
							int dec = playSpeex.decode(speexDate, decData, speexDate.length);
							if(MainService.JINYIN){//没有静音
								if (dec > 0) {
									speakInfo="频道讲话";
									player.write(decData, 0, dec);
								}
								SOUND_STATU  = 30;
								player.play();
							}
						}
						continue;
					} 
					if(strarray[0].equals("E")){
						SOUND_STATU = 0;
						continue;
					}
					if(strarray[0].equals("NOVieo")){
						mHandler.sendEmptyMessage(800);
						continue;
					}
					if(strarray[0].equals("YesVieo")){
						mHandler.sendEmptyMessage(801);
						continue;
					}
					if(strarray[0].equals("NOVieo-Monitor")){
						mHandler.sendEmptyMessage(802);
						continue;
					}
					if(strarray[0].equals("Video")){
						if(videoStatu == true){
							sendUDP("NOVieo",strarray[1]);//本地已经在通话中
							Log.i("WifiConfig", "NOVieo");
							continue;
						}
						if(StatusManager.isMonitor()){
							sendUDP("NOVieo-Monitor",strarray[1]);//本地正在被服务器监控
							Log.i("WifiConfig", "本地正在被服务器监控");
							continue;
						} 
						try {
							videoStatu = true;
							sendUDP("YesVieo",strarray[1]);//本地已经在通话中
							Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//给定类型的Uri的默认铃声。
							Ringtone r = RingtoneManager.getRingtone(MainService.getInstance(), uri);
							r.play();
							MainService.getInstance().wakeAndUnlock(true);
							Log.i("WifiConfig", "对方呼叫："+strarray[1]);
							Intent mIntent = new Intent(MainService.getInstance(), CameraActivity.class);
							macAddress = "陌生人";
							for(int i =0 ; i < listPeople.size();i++){
								if(strarray[1].equals(listPeople.get(i).getUserIp())){
									macAddress = listPeople.get(i).getUsername();
									break;
								}
							}
							UserBean bean = new UserBean(macAddress, strarray[1]);
							mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mIntent.putExtra("UserBean",bean);
							mIntent.putExtra("isTask", true);
							MainService.getInstance().setAudioUserBeans(null);
							for(int i= 0 ; i < MainService.getInstance().getAudioUserBeans().size();i++){
								if(MainService.getInstance().getAudioUserBeans().get(i).getUserIp().equals(bean.getUserIp())){
									break;//终止添加
								}
							}
							MainService.getInstance().getAudioUserBeans().add(bean);
							MainService.getInstance().startActivity(mIntent);
							Log.i("WifiConfig", "对方开启摄像头");
						} catch (Exception e1) {
							Log.e("WifiConfig", "Cannot play notification ringtone.");
						}
						continue;
					}
					if(strarray[0].equals("bye")){
						if(CameraActivity.instances!=null&&CameraActivity.instances.host.equals(strarray[1])){
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									try {
										CameraActivity.instances.finish();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
							Log.i("WifiConfig", "对方bye："+strarray[1]);
						}
						continue;
					}
					if(strarray[0].equals("QuestlongitudeAndlatitude")){
						Log.i("WifiConfig", "对方询问经纬度："+strarray[1]);
						if(MainActivity.getGetInstance()!=null){
							double jingdu = MainActivity.getGetInstance().getLongitude();
							double weidu = MainActivity.getGetInstance().getLatitude();
							sendUDP("AnswerlongitudeAndlatitude:"+MainService.getInstance().getIPadd()+":"+jingdu+":"+weidu, strarray[1]);
						}
						continue;
					}
					if(strarray[0].equals("AnswerlongitudeAndlatitude")){
						if(GpsView.getInstances!=null){
							macAddress = "陌生人";
							for(int i =0 ; i < listPeople.size();i++){
								if(strarray[1].equals(listPeople.get(i).getUserIp())){
									macAddress = listPeople.get(i).getUsername();
									break;
								}
							}
							GpsView.getInstances.addDrivice(new XY(strarray[2], strarray[3], strarray[1], macAddress));
						}
						addListOnLine(new OnLineBean(strarray[1], strarray[2], strarray[3],0));
						Log.i("WifiConfig", "解析对面的内容   IP："+strarray[1]+"经度是"+strarray[2]+"维度是"+strarray[3]);
						continue;
					} 
					if(strarray[0].equals("online_1")){
						Log.i("WifiConfig","在线："+strarray[1]);
						String ipaddress =receivePacket.getAddress().getHostAddress();
						if(PrepareCallCheckUser.pindaoID.equals(strarray[2])&&!ipaddress.equals(MainService.getInstance().getIPadd())){
							//							sendOnlineUDP("answeronline", MainService.getInstance().getUUID()+":"+MainService.getInstance().getMacAddressLastSix(), receivePacket.getAddress());

							//直接添加
							//							[online, TK_143, 888,loaction,mac6]
							Message msg = Message.obtain();
							String name = strarray[1];
							String loacation = strarray[3];

							String mac6 = strarray[4];
							UserBean userBean = new UserBean();
							try {
								userBean.setUsername(name);
								userBean.setUserIp(ipaddress);
								userBean.setLocation(loacation);
								userBean.setMacAddress(mac6);
							} catch (Exception e) {
								userBean.setMacAddress("123456");
							}
							msg.obj = userBean;
							msg.what = 804;
							mHandler.sendMessage(msg);
						}
						
						try {
							Log.i("WifiConfig","状态："+strarray[5]);
							if(strarray[5].equals("0")){
								if(CameraActivity.instances!=null){
									if(CameraActivity.instances.userBean.getUserIp().equals(ipaddress)){
										count++;
										if(count==3){
											CameraActivity.instances.finish();
										}
									}
								}
							}
						} catch (Exception e) {
						}
						continue;
					} 
					if(strarray[0].equals("answeronline")){
						String address = receivePacket.getAddress().toString();
						address= address.replace("/", "");
						if(ZuActivity.getInstances!=null&&!address.equals(MainService.getInstance().getIPadd())){
							Message msg = Message.obtain();
							UserBean userBean = new UserBean(strarray[1],address);
							try {
								userBean.setMacAddress(strarray[2]);
							} catch (Exception e) {
								userBean.setMacAddress("123456");
								e.printStackTrace();
							}
							msg.obj = userBean;
							msg.what = 803;
							mHandler.sendMessage(msg);
						}else{
							Message msg = Message.obtain();
							UserBean userBean = new UserBean(strarray[1],address);
							try {
								userBean.setMacAddress(strarray[2]);
							} catch (Exception e) {
								userBean.setMacAddress("123456");
							}
							msg.obj = userBean;
							msg.what = 804;
							mHandler.sendMessage(msg);
						}
						continue;
					}
				} catch (Exception e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	//根据mac修改数据表的ip地址
	private void updateDB_IpByMac(String distanceIp, String distanceMac) {
		List<UserBean> list = MainService.getInstance().getDB_ZU_ALL_List();//获取数据表（组1组2组3）
		for(int i = 0;i<list.size();i++){
			if(distanceMac.equals(list.get(i).getMacAddress())){//MAC地址相同
				if(distanceIp.equals(list.get(i).getUserIp())){ //IP地址相同
					Log.i(TAG,"IP地址相同 i:"+i+" mac:"+list.get(i).getMacAddress()+" ipaddress:"+list.get(i).getUserIp()+" list.size:"+list.size());
					break;
				}else{//IP地址不相同
					//根据MAC修改数据表
					Log.i(TAG,"修改method: IP地址不相同 i:"+i+" mac:"+list.get(i).getMacAddress()+" src.ip:"+list.get(i).getUserIp()+" now.ip:"+distanceIp);
					dbHelper.updateUserIpByMac(distanceMac, distanceIp, 1);
					dbHelper.updateUserIpByMac(distanceMac, distanceIp, 2);
					dbHelper.updateUserIpByMac(distanceMac, distanceIp, 3);
					break;
				}
			}
		}
	}


	public boolean addListOnLine(OnLineBean bean){
		if(listOnLine==null)
			return false;

		for(int i = 0 ; i < listOnLine.size(); i ++){
			if(listOnLine.get(i).getIp().equals(bean.getIp())){
				return true;
			}
		}
		listOnLine.add(bean);
		if(listOnLine.size()==0)
			return false;
		return true;
	}

	public  List<OnLineBean> getOnlineList(){
		return listOnLine;
	}
	/**
	 * 清空在线列表
	 */
	public  void  clearOnline(){
		listOnLine = new  ArrayList<OnLineBean>();
	}







	public void sendUDP(final String heard,final String ip) {
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
							dataPacket.getAllData().length,InetAddress.getByName(ip), AppConfig.PortAudio));
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
	public void sendOnlineUDP(final String heard,final String name,final InetAddress address ) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket clientSocket=null;
				try {
					clientSocket = new DatagramSocket();
					StringBuffer str = new StringBuffer(heard+":"+name);
					// 构建数据包 头+体
					DataPacket dataPacket = new DataPacket(str.toString().getBytes(), new byte[]{01,01,01});
					//// 构建数据报 +发送
					clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length,address, AppConfig.PortAudio));
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

	public void SOUND_STATU_Thread(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
						if(SOUND_STATU>0){
							SOUND_STATU--;

						}
						if(SOUND_STATU==0){
							//								Log.i("WifiConfig", "PrepareCallCheckUser.instances.setSpeakWifi(false)");
							Message msg = Message.obtain();
							msg.what=SPEAK_FALSE;
							mHandler.sendMessage(msg);
						}else{
							Message msg = Message.obtain();
							msg.what=SPEAK_TRUE;
							mHandler.sendMessage(msg);
							//								Log.i("WifiConfig", "PrepareCallCheckUser.instances.setSpeakWifi(true)");

						}
					} catch (Exception e) {
						//							Log.i("WifiConfig", "error");
					}
				}
			}
		}).start();
	}



	public void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}
	/**
	 * 网络对讲机按钮监听
	 * @author Administrator
	 *
	 */
	public interface PrepareCallLintener {  
		public void OnButtonChangeListener(boolean statu,String info);  
	} 

	private List<PrepareCallLintener> lintener = new ArrayList<ReceiveSoundsThread_oldSpeex.PrepareCallLintener>();
	public void setGPSCallBackListener(PrepareCallLintener lintener){
		this.lintener.add(lintener);
	}
	public void destoryLintener(PrepareCallLintener ls){
		this.lintener.remove(ls);

	}

	boolean flag = false; 
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			if(msg.what==START_ACTIVITY){
				List<UserBean> msgData = (List<UserBean>) msg.obj;
				if(msgData!=null){
					try {
						//						PrepareCallCheckUser.instances.getAdapter().setList(msgData);

					} catch (Exception e) {
						Log.e("SHUAXIN", "刷新数据Exception");
					}
				}
			}
			if(msg.what==TK1){
				if(com.example.tst.MainActivity.instances !=null){
					UserBean info = (UserBean) msg.obj;
					com.example.tst.MainActivity.instances.infos.add(info.getChatInfo());
					com.example.tst.MainActivity.instances.mLvAdapter.notifyDataSetChanged();
					com.example.tst.MainActivity.instances.mListView.setSelection(com.example.tst.MainActivity.instances.infos.size() - 1);
					dbHelper.AddLiaoTian(info.getChatInfo());
				}else{
					UserBean info = (UserBean) msg.obj;
					normalRegular(info, info.getChatInfo().content);
					dbHelper.AddLiaoTian(info.getChatInfo());
				}
			}
			if(msg.what==TK2){//收到文件
				if(com.example.tst.MainActivity.instances !=null){
					UserBean info = (UserBean) msg.obj;
					com.example.tst.MainActivity.instances.infos.add(info.getChatInfo());
					com.example.tst.MainActivity.instances.mLvAdapter.notifyDataSetChanged();
					com.example.tst.MainActivity.instances.mListView.setSelection(com.example.tst.MainActivity.instances.infos.size() - 1);
					com.example.tst.MainActivity.instances.mLvAdapter.listPath = new ArrayList<String>();
					com.example.tst.MainActivity.instances.mLvAdapter.listPath.add(info.getChatInfo().content);
					com.example.tst.MainActivity.instances.mLvAdapter.downIng= false;
					com.example.tst.MainActivity.instances.mLvAdapter.downFile();
					dbHelper.AddLiaoTian(info.getChatInfo());
				}else{
					UserBean info = (UserBean) msg.obj;
					normalRegular(info, info.getChatInfo().content);
					dbHelper.AddLiaoTian(info.getChatInfo());
					//					Intent mIntent = new Intent(MainService.getInstance(), com.example.tst.MainActivity.class);
					//					mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//					mIntent.putExtra("UserBean", info);
					//					MainService.getInstance().startActivity(mIntent);
				}
			}
			if(msg.what==SPEAK_TRUE){
				if(SendSoundsThread_oldSpeex.shuanggong==false){// 双工就不进行按钮控制
//					MainService.getInstance().getsendSoundsThread().setRunning(false);
					if(lintener!=null){
						for(PrepareCallLintener ls:lintener){
							ls.OnButtonChangeListener(true,speakInfo);
						}
					}
					if(flag==false){
						PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
						util.playSounds(2, 0);
						flag = true;
					}
				}else{
					for(PrepareCallLintener ls:lintener){
						ls.OnButtonChangeListener(false,"");
					}
				}
			}
			if(msg.what==SPEAK_FALSE){
				if(lintener!=null){
					for(PrepareCallLintener ls:lintener){
						ls.OnButtonChangeListener(false,"");
					}
				}	
				if(flag==true){
					PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
					util.playSounds(3, 0); 
					flag = false;
				}
			}
			if(msg.what==200){
				showTextToast(System.currentTimeMillis()+"--", 0);
			}
			if(msg.what==800){
				showTextToast("对方正在通话中", 0);
			}
			if(msg.what==802){
				showTextToast("对方正在被服务器监控,无法呼叫", 0);
			}
			if(msg.what==801){
				try {
					PrepareCallCheckUser.instances.startVideo();
				} catch (Exception e) {
				}
			}
			if(msg.what==803){
				UserBean  user = (UserBean) msg.obj;
				ZuActivity.getInstances.addOnlineList(user.getUserIp(), user.getUsername(),user.getMacAddress());
			}
			if(msg.what==804){
				if(PrepareCallCheckUser.instances!=null){
					UserBean  user = (UserBean) msg.obj;
					boolean statu = PrepareCallCheckUser.instances.db.saveUserTable(user, 1);// true 添加    false代表更新
					if(statu==true){
						PrepareCallCheckUser.instances.addList(user);
					}
					PrepareCallCheckUser.instances.setList(user);
				}

			}
		};
	};
	private Toast toast = null;
	private void showTextToast(String msg,int i) {
		if (toast == null) {
			toast = Toast.makeText(PrepareCallCheckUser.instances, msg, i);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}
	long beginTime = -1;
	public synchronized void setDate(long nowTime,List<UserBean> list){
		if(beginTime==-1)
			beginTime =System.currentTimeMillis();//初始化
		if((nowTime - beginTime)>=1000){//每隔
			beginTime = nowTime;
			Message msg = Message.obtain();
			msg.what=START_ACTIVITY;
			msg.obj = list;
			mHandler.sendMessage(msg);
			Log.i("SHUAXIN", "刷新数据setDate");
		}
	}



	//启动对讲界面
	public static final int START_ACTIVITY = 1; 
	//结束对讲界面
	public static final int END_ACTIVITY = 2; 

	protected static final int SPEAK_FALSE = 3;

	protected static final int SPEAK_TRUE = 4;


	public void setPlayVolume(){
		//		player.set
	}





	private void normalRegular(UserBean userBean,String content) {
		NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(
				MainActivity.getInstance);
		// 下面三个参数是必须要设定的
		notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifyBuilder.setContentTitle(userBean.getUsername());
		notifyBuilder.setContentText(content);

		// 如果不设置LargeIcon，那么系统会默认将上面的SmallIcon显示在通知选项的最左侧，右下角的小图标将不再显示
		Bitmap bitmap = BitmapFactory.decodeResource(instances.getResources(),R.drawable.ic_menu_friendslist);
		notifyBuilder.setLargeIcon(bitmap);
		// 这里用来显示右下角的数字
		//		notifyBuilder.setNumber(10);
		notifyBuilder.setWhen(System.currentTimeMillis());
		Intent notifyIntent = new Intent(MainActivity.getInstance, com.example.tst.MainActivity.class);
		notifyIntent.putExtra("UserBean", userBean);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(instances);
		stackBuilder.addNextIntent(notifyIntent);
		// 当设置下面PendingIntent.FLAG_UPDATE_CURRENT这个参数的时候，常常使得点击通知栏没效果，你需要给notification设置一个独一无二的requestCode
		int requestCode = (int) SystemClock.uptimeMillis();
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
				requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
		notifyBuilder.setContentIntent(resultPendingIntent);

		// 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失
		notifyBuilder.setAutoCancel(true);
		// 将Ongoing设为true 那么notification将不能滑动删除
		// notifyBuilder.setOngoing(true);
		// 从Android4.1开始，可以通过以下方法，设置notification的优先级，优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
		notifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		// notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);

		notifyBuilder.setTicker(userBean.getUsername()+"发送一条消息");

		// Uri uri =
		// Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.cat);
		// Uri uri = Uri.parse("file:///mnt/sdcard/cat.mp3");
		// notifyBuilder.setSound(uri);

		// Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
		// Notification.DEFAULT_SOUND：系统默认铃声。
		// Notification.DEFAULT_VIBRATE：系统默认震动。
		//		 Notification.DEFAULT_LIGHTS：系统默认闪光。
		notifyBuilder.setDefaults(Notification.DEFAULT_ALL);

		NotificationManager mNotificationManager = (NotificationManager) MainActivity.getInstance.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(10, notifyBuilder.build());
	}

}