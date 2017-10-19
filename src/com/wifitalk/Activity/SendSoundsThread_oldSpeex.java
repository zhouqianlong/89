package com.wifitalk.Activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Message;
import android.util.Log;

import com.example.music.MusicMainActivity;
import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.PrepareCallCheckUser;
import com.ramy.minervue.db.UserBean;
import com.sinaapp.bashell.sayhi.Speex;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.DataPacket;

public class SendSoundsThread_oldSpeex extends Thread
{
	private Object object = new Object();
	public static boolean shuanggong = false;
	public AudioRecord recorder = null;
	private boolean isRunning = false;
	private boolean isPDRunning = false;
	private int pd = 1;
	private String ipAddrss = MainService.getInstance().getIPadd();
	private int bufferSize;
	private AcousticEchoCanceler canceler;
	public static double volume = 0;

	public boolean initAEC(int audioSession)
	{
		if (canceler != null)
		{
			return false;
		}
		canceler = AcousticEchoCanceler.create(audioSession);
		canceler.setEnabled(true);
		return canceler.getEnabled();
	}
	//使能/去使能AEC。

	public boolean setAECEnabled(boolean enable)
	{
		if (null == canceler){
			return false;
		}
		canceler.setEnabled(enable);
		return canceler.getEnabled();
	}
	//释放AEC。
	public boolean release(){
		if (null == canceler){
			return false;
		}
		
		canceler.setEnabled(false);
		canceler.release();
		return true;
	}

	public void destorySendSoundsThread(){
		//		recorder.stop();
		//		recorder.release();
		setMacStatu(false);
		isRunning = false;
	}
	public void setMacStatu(boolean statu){
		if(statu){
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AppConfig.AUDIO_CAI_YANG_LV, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			recorder.startRecording();
		}else{
			try {
				recorder.stop();
				speexBufferPostion = 0;
				recorder.release();
				recorder = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public SendSoundsThread_oldSpeex()
	{
		bufferSize = AudioRecord.getMinBufferSize(AppConfig.AUDIO_CAI_YANG_LV, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
	}
	byte [] speexaBuffer = new byte[DataPacket.bodyLen];//语音数据buffer 
	public int speexBufferPostion = 0;//语音数据buffer 当前下标
	public boolean isPasue = false;
	public DatagramSocket clientSocket = null;
	Speex publishSpeex;
	@Override
	public synchronized void run()
	{

		super.run();
		publishSpeex = new Speex();
		int frameSize;
		short[] mAudioRecordBuffer = new short[bufferSize];
		byte[] processedData;
		frameSize = publishSpeex.getFrameSize();
		processedData = new byte[frameSize];
		int spexxLen;

		DataPacket dataPacket ;
		StringBuffer str = null;
		List<UserBean> checkStatu;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while (true){
		
			if (isRunning){
				try
				{
					if(MainActivity.getInstance.isSendWifiData()==false){
						Thread.sleep(100);
						Log.i("asd", "信号差,不可以发送");
						continue;
					}
					if(frameSize==0){
						frameSize = publishSpeex.getFrameSize();
					}
 					int len = recorder.read(mAudioRecordBuffer, 0, frameSize);
					Log.d("AAA", "frameSize:" + frameSize+"| len:"+len);  
					if(len>0){



//						long v = 0;  
//						// 将 buffer 内容取出，进行平方和运算  
//						for (int i = 0; i < mAudioRecordBuffer.length; i++) {  
//							v += mAudioRecordBuffer[i] * mAudioRecordBuffer[i];  
//						}  
//						// 平方和除以数据总长度，得到音量大小。  
//						double mean = v / (double) len;  
//						volume = 10 * Math.log10(mean); 
						spexxLen =  publishSpeex.encode(mAudioRecordBuffer, 0, processedData, AppConfig.AUDIO_BPS);
//						Log.d("AAA", "分贝值:" + volume+"| spexxLen:"+spexxLen);  
						byte[] speexData = new byte[spexxLen];
						System.arraycopy(processedData, 0, speexData, 0, spexxLen);
						StringBuffer sb = new StringBuffer("audio:");
						for(int i = 0 ; i < 10 ; i ++){
							sb.append(speexData[i]+",");
						}
						Log.i("WifiConfig", "encode: succe len:"+spexxLen +"//"+sb.toString());
						
						checkStatu =MainService.getInstance().getAudioUserBeans();
						if(ipAddrss.equals("未联网")){
							ipAddrss = MainService.getInstance().getIPadd();
						}
						if(isPDRunning){//广播包  已取消设计  
							str = new StringBuffer("P:"+pd+":"+ipAddrss);//频道号/ip 地址
							dataPacket = new DataPacket( str.toString().getBytes(), speexData);		// 构建数据包 头+体
							List<UserBean>  pdlist = MainService.getInstance().getReceiveSoundsThread().pd_list;
							for(int i=0;i<pdlist.size();i++){
								if(pdlist.get(i).getId().equals(String.valueOf(pd))){
									clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length,InetAddress.getByName(pdlist.get(i).getUserIp()), AppConfig.PortAudio));
									Log.i("UDP-Send-Audio","sendlen:"+dataPacket.getAllData().length+",readlen:"+len);
									Thread.sleep(10);
								}
							}
						}else if(checkStatu.size()>0){ 
							str = new StringBuffer("S:"+MainService.getInstance().getMacAddressLastSix()+":"+PrepareCallCheckUser.pindaoID);//传递自己的mac
							for(UserBean sh :checkStatu){
								String mac = sh.getMacAddress();
								if(mac.isEmpty()){
									mac = "123456";
								}
								str.append(":"+mac+sh.getUserIp().split("\\.")[3]);//设置mac地址Ip地址最后一位
							}
							if(speexBufferPostion<AppConfig.AUDIO_BUFFER){
								System.arraycopy(speexData, 0, speexaBuffer, speexBufferPostion, speexData.length);
								speexBufferPostion+=speexData.length;
							}else{
								speexBufferPostion=0;
								dataPacket = new DataPacket( str.toString().getBytes(), speexaBuffer);		// 构建数据包 头+体
								//// 构建数据报 +发送
								for(int i = 0 ; i < checkStatu.size();i++){
									if(ipAddrss.equals(checkStatu.get(i).getUserIp())){
										continue;
									}
									clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length,InetAddress.getByName(checkStatu.get(i).getUserIp()), AppConfig.PortAudio));
									Log.i("UDP-Send-Audio","sendlen:"+dataPacket.getAllData().length+",readlen:"+speexBufferPostion);
								}
//								System.arraycopy(speexData, 0, speexaBuffer, speexBufferPostion, speexData.length);
//								speexBufferPostion+=speexData.length;
//								Thread.sleep(10);
							}
						}
					} 
				}catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch(NullPointerException e){
					
				}catch (Exception e) {
					publishSpeex.close();
					publishSpeex = new Speex(); 
					frameSize = publishSpeex.getFrameSize();
					processedData = new byte[frameSize];
					Log.e("AAA", "error"+""+e.toString());  
				}
			}else{
				
				if(speexBufferPostion>0){
					try {
						checkStatu =MainService.getInstance().getAudioUserBeans();
						byte [] otherData = new byte[DataPacket.bodyLen];//语音数据buffer 
						System.arraycopy(speexaBuffer, 0, otherData, 0, speexBufferPostion);
						speexaBuffer  = new byte[DataPacket.bodyLen];//语音数据buffer
						dataPacket = new DataPacket( str.toString().getBytes(), otherData);		// 构建数据包 头+体
						//// 构建数据报 +发送
						for(int i = 0 ; i < checkStatu.size();i++){
							if(ipAddrss.equals(checkStatu.get(i).getUserIp())){
								continue;
							}
//							clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length,InetAddress.getByName(checkStatu.get(i).getUserIp()), AppConfig.PortAudio));
							Log.i("UDP-Send-Audio","还有数据没法完：sendlen:"+dataPacket.getAllData().length+",readlen:"+speexBufferPostion);
						}
						MainService.getInstance().getsendSoundsThread().endPackage();	
						speexBufferPostion=0;
					} catch (Exception e) {
						e.printStackTrace();
					}
				
				}
				try {
					if(MainService.getInstance().system_Action==false){
						myWait();
					}else{
						Thread.sleep(100);
						//						Log.i("asd","按键等待再次按下...");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setRunning(boolean isRunning)
	{
		if(isRunning==false){
			ipAddrss = MainService.getInstance().getIPadd();
		}
		this.isRunning = isRunning;
	}
	public void setpdRunning(boolean isRunning,int pd)
	{
		if(isRunning==false){
			ipAddrss = MainService.getInstance().getIPadd();
		}
		this.isRunning = isRunning;
		this.isPDRunning = isRunning;
		this.pd = pd;
	}

	//发送结束包
	public void endPackage(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<UserBean>	checkStatu =MainService.getInstance().getAudioUserBeans();
					if(checkStatu.size()>0){
						StringBuffer str = new StringBuffer("E:"+MainService.getInstance().getMacAddressLastSix());//传递自己的mac
						for(UserBean sh :checkStatu){
							String mac = sh.getMacAddress();
							if(mac.isEmpty()){
								mac = "123456";
							}
							str.append(":"+mac+sh.getUserIp().split("\\.")[3]);//设置mac地址Ip地址最后一位
						}
						DataPacket dataPacket = new DataPacket( str.toString().getBytes(), new byte[]{});		// 构建数据包 头+体
						DatagramSocket	clientSocket = new DatagramSocket();
						//// 构建数据报 +发送
						for(int i = 0 ; i < checkStatu.size();i++){
							if(ipAddrss.equals(checkStatu.get(i).getUserIp())){
								continue;
							}
							if(MainActivity.getInstance.isSendWifiData()){
								clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length,InetAddress.getByName(checkStatu.get(i).getUserIp()), AppConfig.PortAudio));
							}
							Log.i("UDP-Send-Audio","sendlen:"+dataPacket.getAllData().length);
						}
						Thread.sleep(10);
					}  
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
	public void myNotify() {
		synchronized (object) {
			object.notify();
			Log.i("asd","系统取消休眠,发送线程开始工作=======");
		}
	}
	public void myWait() {
		synchronized (object) {
			try {
				Log.i("asd","系统休眠发送线程需要等待");
				object.wait();
				Log.i("asd","系统休眠发送线程已经被唤醒了");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}