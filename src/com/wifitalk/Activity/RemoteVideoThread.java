package com.wifitalk.Activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.wifitalk.Config.AppConfig;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class RemoteVideoThread extends Thread{

	private boolean isRunning = false;
	private byte[] recordBytes = new byte[1024*30];
	private Context instances;
	private long receivreData =0;
	private DatagramPacket receivePacket =null;
	byte [] data = null;
	private static UdpFrameCallback Frameback;
	public static  void setUdpFrameback(UdpFrameCallback udpFrameback) {
		if(udpFrameback!=null){
			Frameback = udpFrameback;
		}
	}
	public RemoteVideoThread(Context getInstances) {
		this.instances = getInstances;    
	}
	@Override
	public synchronized void run()
	{
		super.run();
		try
		{  
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					while (isRunning) {
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						long kb = receivreData/1024;
//						Log.i("SUDU", "接受:"+kb+" KB/s");
//						receivreData = 0;
//					}
//				}
//			}).start();
			
			DatagramSocket serverSocket = new DatagramSocket(5000);
			receivePacket =   new DatagramPacket(recordBytes, recordBytes.length);
			Log.i("ReceiveThread", "开始监听");
			while (true)
			{ 
					try {
						if(serverSocket==null){
							serverSocket = new DatagramSocket(5000);
						}
						
						serverSocket.receive(receivePacket);
						//Log.i("ReceiveThread", "PortVideo:"+receivePacket.getLength());
						if(Frameback!=null){
//						byte [] data = new byte[receivePacket.getLength()];
//						System.arraycopy(recordBytes, 0, data, 0, data.length);
//						Frameback.onUdpFrame(data);
							data = new byte[receivePacket.getLength()];
							int jxsize = ((recordBytes[0]&0x00ff)<<24)+((recordBytes[1]&0x00ff)<<16)+((recordBytes[2]&0x00ff)<<8)+(recordBytes[3]&0x00ff);
							System.arraycopy(recordBytes, 4, data, 0, data.length-4);
							Frameback.onUdpFrame(data,jxsize);
							receivreData +=data.length;
						
						}
						Log.i("RemoteVideoThread", "摄像头线程在运行..."+System.currentTimeMillis());
					} catch (Exception e) {
						serverSocket.disconnect();
						serverSocket.close();
						serverSocket = null;
						try {
							Thread.sleep(6000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						Log.e("RemoteVideoThread", "摄像头线程出现错误..."+System.currentTimeMillis()+e.toString());
					}
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}

	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			if(msg.what==200){ }
		};
	};
 
	long beginTime = -1;
 
	public interface UdpFrameCallback {
		void onUdpFrame(byte[] data,int direction);
	}

}