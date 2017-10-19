package com.ramy.minervue.media;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.MediaCodec;
import android.os.Handler;
import android.util.Log;

import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.MonitorActivity;
import com.ramy.minervue.ffmpeg.MP4Muxer.RecordTimeListener;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.ByteBufferPool;
import com.ramy.minervue.util.ConfigUtil;
import com.ramy.minervue.util.NetUtil;
import com.wifitalk.Config.AppConfig;

/**
 * Created by peter on 12/29/13.
 */
//public class MonitorLocal extends Recorder {
//	private static final String TAG = "RAMY-MonitorLocal";
//	//	public static boolean THREAD_STATU = true;//线程状态  默认： true
//	private VideoSender vSender;
//	private OnAbortListener listener = null;
//	private VideoAudioAvtivity avtivity;
//	public MonitorLocal(VideoAudioAvtivity avtivity) {
//		this.avtivity = avtivity;
//	}
//	public void setOnAbortListener(OnAbortListener listener) {
//		this.listener = listener;
//	}
//
//
//	@Override
//	public void start() {
//		Log.i(TAG, "MoitorLocal:51"+getVideoCodec().getSize().height+"\""+getVideoCodec().getSize().width);
//		try {
//			vSender = new VideoSender(MainService.getInstance().getIPadd(), AppConfig.PortVideo);
////			vSender = new VideoSender("192.168.1.74", 6100);
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
//		if(!vSender.isCancelled()){
//			vSender.start();
//			videoCodec.addConsumer(vSender);
//		}
//		Log.i(TAG, "MoitorLocal:65"+getVideoCodec().getSize().height+"\""+getVideoCodec().getSize().width);
//		super.start();
//	}
//
//	@Override
//	public void stop() {
//		super.stopVideo();
//		vSender.cancelAndClear(true);//video数据停止发送
//	}
//
//	public void monitorstop(){
//		//			vSender.cancelAndClear(true);//取消网络发送
//		//				aSender.cancelAndClear(true);//取消网络发送
//		super.stop();//关闭解码器
//
//	}
//	public void monitorstart(String recordFile,RecordTimeListener  listener){
//		videoCodec.addConsumer(vSender);
//		super.start(recordFile,avtivity);
//	}
//
//	public void abort() {
//		stop();
//		if (listener != null) {
//			listener.onAbort();
//		}
//	}
//
//
//	public interface OnAbortListener {
//		public void onAbort();
//
//	}
//	public static void printStatu(String content){
//		BufferedWriter out = null;  
//		try {  
//			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/不要删除ERRORTEST.txt", true)));  //false表示覆盖内容
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
//			out.write(sdf.format(new Date())+"\r\n");
//			out.write(content+"\r\n");  
//		} catch (Exception e) {  
//			e.printStackTrace();  
//		} finally {  
//			try {  
//				out.close();  
//			} catch (IOException e) {  
//				e.printStackTrace();  
//			}  
//		} 
//	}
//	private byte[] FLAG = { 0x54, 0x4b, 0x4c, 0x59, 0x54, 0x4b, 0x4c, 0x59};
//	private byte[] FLAGs = { 84, 75, 76, 89, 84, 75, 76, 89};
//	private abstract class AbstractMediaSender extends ATask<Void, Void, Void> {
//		private static final String TAG = "RAMY-AbstractMediaSender";
//		protected ByteBufferPool pool = new ByteBufferPool(64);
//		protected final LinkedBlockingQueue<ByteBuffer> list = new LinkedBlockingQueue<ByteBuffer>(256);
//		private DatagramSocket clientSocket;
//		private String remoteAddr;
//		private int port;
//		public AbstractMediaSender(String remoteAddr, int port) throws SocketException, UnknownHostException {
//			this.remoteAddr = remoteAddr;
//			this.port = port;
//			clientSocket = new DatagramSocket();
//		}
//		MainService service = MainService.getInstance();
//		@Override
//		protected Void doInBackground(Void... params) {
//			pool.fillPool(8, getInitBufferSize());
//			try {
//				//				socket = SocketChannel.open(remoteAddr);
//			}catch (Exception e) {
//				StringWriter writer = new StringWriter();
//				e.printStackTrace(new PrintWriter(writer));
//				printStatu(writer.toString());
//				return super.doInBackground();
//			}
//			ByteBuffer buffer = null;
//			while (!isCancelled()) {//线程被  设置取消状态  ， isCancelled 为  false  
//				synchronized (list) {
//					try {
//						buffer = list.take();
//						if(buffer==null){
//							continue;	
//						}
//						byte [] senddata = buffer.array();
//						//						Log.i(TAG, senddata[0]+"/"+senddata[1]+"/"+senddata[2]+"/"+senddata[3]+"/"+senddata[4]+"/"+senddata[5]+"/"+senddata[6]+"/");
//						clientSocket.send(new DatagramPacket(senddata, senddata.length,InetAddress.getByName(remoteAddr), port));
//						pool.queue(buffer);//清空数据
//					} catch (InterruptedException e) {
//						Log.i(TAG, "..................InterruptedException视频对讲发送错误   ................");
//						continue;
//					} catch (IOException e) {
//						e.printStackTrace();
//						Log.i(TAG, "..................IOException视频对讲发送错误  ................");
//					}
//				}
//			}
//			clientSocket.close();
//			return super.doInBackground();
//		}
//
//
//		@Override
//		protected void onPostExecute(Void aVoid) {
//			Log.i(TAG, ".................. onPostExecute  abort   ................");
//			MonitorLocal.printStatu("AbstractMediaSender 线程结束，关闭监控");
//			abort();
//		}
//		public abstract int getInitBufferSize();
//	}
//	public class VideoSender extends AbstractMediaSender implements VideoFrameConsumer {
//		public VideoSender(String remoteAddr, int port) throws SocketException, UnknownHostException {
//			super(remoteAddr, port);
//		}
//		@Override
//		public void addVideoFrame(ByteBuffer data, MediaCodec.BufferInfo info) {
//			ByteBuffer buffer = pool.dequeue(info.size +2+4);//分配缓存大小    ------ 视频数据（摄像头采样决定  已被H264编码过）+ int（视频校验长度，4位）
//			if (buffer == null) {//分配失败
//				return;
//			}
//			data.position(info.offset);//设置此缓冲区的位置：0
//			data.limit(info.size + info.offset);//设置缓冲区大小
//			buffer.put(new byte[]{0x54, 0x4b});//8  TK    同科   包头
//			buffer.putInt(info.size);//写入帧的长度  （4字节）
//			buffer.put(data);//写入视频采集到的数据   （info.size字节）
//			Log.i(TAG, info.size+":Video"); 
//			buffer.flip();//为下次ByteBuffer写入操作做好准备
//			list.offer(buffer);//将这个ByteBuffer装入到  队列尾部
//		}
//		@Override
//		public int getInitBufferSize() {
//			return 128 * 1024;
//		}
//	}
//
//}
