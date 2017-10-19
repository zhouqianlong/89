package com.ramy.minervue.media;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.MediaCodec;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.MonitorActivity;
import com.ramy.minervue.ffmpeg.MP4Muxer.RecordTimeListener;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.ByteBufferPool;
import com.ramy.minervue.util.ConfigUtil;
import com.ramy.minervue.util.NetUtil;

/**
 * Created by peter on 12/29/13.
 */
public class Monitor extends Recorder {
	public static boolean MONITOR_SEND_STATU = false;
	private static final String TAG = "RAMY-Monitor";
	//	public static boolean THREAD_STATU = true;//线程状态  默认： true
	private RemoteAudioPlayer audioPlayer = new RemoteAudioPlayer();
	private VideoSender vSender;
	private AudioSender aSender;
	private OnAbortListener listener = null;
	private Handler mhander;
	private MonitorActivity monitorActivity;
	public Monitor(Handler monitorHander,MonitorActivity monitorActivity) {
		this.mhander = monitorHander;
		this.monitorActivity = monitorActivity;
	}

	public void prepare(int type) {
		int port = MainService.getInstance().getConfigUtil().getPortAudio();
		audioPlayer.startPlayback(port,type);
	}
	/**
	 *  关声音端口
	 */
	public void reset() {
		audioPlayer.stopPlayback();
	}

	public void setOnAbortListener(OnAbortListener listener) {
		this.listener = listener;
	}


	@Override
	public void start(String recordFile,RecordTimeListener  listener) {
		ConfigUtil util = MainService.getInstance().getConfigUtil();
		String server = util.getServerAddr();
		int aPort = util.getPortAudio();
		int vPort = util.getPortVideo();
		try {
			if(aSender!=null){
				aSender.cleanCloseSocket();
			}
			if(vSender!=null){
				vSender.cleanCloseSocket();
			}
		} catch (Exception e) {
		}
		if(recordFile.equals("RESULT")){
			aSender = new AudioSender(server, aPort);//音频线程
			vSender = new VideoSender(server, vPort);//视频线程
			if(!aSender.isCancelled()){
				aSender.start();
				audioCodec.addConsumer(aSender);
			}
			if(!vSender.isCancelled()){
				vSender.start();
				videoCodec.addConsumer(vSender);
			}
			return;
		}
		aSender = new AudioSender(server, aPort);//音频线程
		vSender = new VideoSender(server, vPort);//视频线程
		if(!aSender.isCancelled()){
			aSender.start();
			audioCodec.addConsumer(aSender);
		}
		if(!vSender.isCancelled()){
			vSender.start();
			videoCodec.addConsumer(vSender);
		}

		super.start(recordFile,monitorActivity);
	}

	@Override
	public void stop() {
		super.stop();
		audioPlayer.stopPlayback();//本地接收声音端口停止
		vSender.cancelAndClear(true);//video数据停止发送
		aSender.cancelAndClear(true);//audio数据停止发送
	}

	public void monitorstop(){
		//			vSender.cancelAndClear(true);//取消网络发送
		//				aSender.cancelAndClear(true);//取消网络发送
		super.stop();//关闭解码器

	}
	public void monitorstart(String recordFile,RecordTimeListener  listener){
		audioCodec.addConsumer(aSender);
		videoCodec.addConsumer(vSender);
		super.start(recordFile,monitorActivity);
	}

	public void abort() {
		stop();
		if (listener != null) {
			listener.onAbort();
		}
	}


	public interface OnAbortListener {
		public void onAbort();

	}
	public static void printStatu(String content){
		BufferedWriter out = null;  
		try {  
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/不要删除ERRORTEST.txt", true)));  //false表示覆盖内容
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
			out.write(sdf.format(new Date())+"\r\n");
			out.write(content+"\r\n");  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				out.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		} 
	}
	private abstract class AbstractMediaSender extends ATask<Void, Void, Void> {

		public void cleanCloseSocket(){
			try {
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			NetUtil.cleanSocket(socket);
		}
		private static final String TAG = "RAMY-AbstractMediaSender";
		protected SocketChannel socket;
		protected SocketAddress remoteAddr;
		protected ByteBufferPool pool = new ByteBufferPool(64);
		protected final LinkedBlockingQueue<ByteBuffer> list = new LinkedBlockingQueue<ByteBuffer>(256);
		public AbstractMediaSender(String remoteAddr, int port) {
			if(outputStream ==null)
				createfile();
			this.remoteAddr = new InetSocketAddress(remoteAddr, port);
		}
		MainService service = MainService.getInstance();
		@Override
		protected Void doInBackground(Void... params) {
			pool.fillPool(8, getInitBufferSize());
			try {
				socket = SocketChannel.open(remoteAddr);
			}catch (SocketTimeoutException etx){
				NetUtil.cleanSocket(socket);
			} catch (IOException e1) {
				return super.doInBackground();
			}catch (Exception e) {

				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				printStatu(writer.toString());
				return super.doInBackground();

			}
			ByteBuffer buffer = null;
			//			int c = 0;
			while (!isCancelled()) {//线程被  设置取消状态  ， isCancelled 为  false  
				//				if(MyThreadManager.vSender“Flag==true&&MyThreadManager.aSenderFlag==true){
				synchronized (list) {
					try {
						buffer = list.take();
						if(buffer==null){
							continue;	
						}
						socket.write(buffer);//远程写入数据到服务器。	
						if(getClass().getSimpleName().equals("VideoSender")){
							StringBuffer sb = new StringBuffer();
							for(int i = 0 ; i < buffer.position();i++){
								if(i>50)
									break;
								sb.append(buffer.get(i)+",");
							}
//							printStatu(sb.toString());
							
							Log.i(TAG, sb.toString());
						}
						MONITOR_SEND_STATU = true;
						pool.queue(buffer);//清空数据
						MONITOR_WIFI = true;
					} catch (InterruptedException e) {
						Log.i(TAG, "..................InterruptedException  ................");
						MONITOR_COUNT++;
						if(MONITOR_COUNT>5){
							MONITOR_WIFI = false;
						}
						continue;
					} catch (IOException e) {
						Log.i(TAG, ".................. IOException   ................");
						MONITOR_COUNT++;
						if(MONITOR_COUNT>5){
							MONITOR_WIFI = false;
						}
					}
				}
				//				}else{
				//					break;
				//				}
			}
			NetUtil.cleanSocket(socket);
			return super.doInBackground();
		}


		int count = 2 ;
		int jgg_count = 2;
		@Override
		protected void onPostExecute(Void aVoid) {
			Log.i(TAG, ".................. onPostExecute  abort   ................");
			Monitor.printStatu("AbstractMediaSender 线程结束，关闭监控");
			abort();
		}
		public abstract int getInitBufferSize();
	}
	public static  boolean MONITOR_WIFI = true;
	public static int MONITOR_COUNT = 0;
	BufferedOutputStream outputStream = null;
	private void createfile(){
		File file = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + "/zql333.h264");
		if(file.exists()){
			file.delete();
		}
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file));
		} catch (Exception e){ 
			e.printStackTrace();
		}
	}
	public class VideoSender extends AbstractMediaSender implements VideoFrameConsumer {
		
		public void closeVideoSocket(){
			super.cleanCloseSocket();
		}
		public VideoSender(String remoteAddr, int port) {
			super(remoteAddr, port);
		}
		@Override
		public void addVideoFrame(ByteBuffer data, MediaCodec.BufferInfo info) {
			ByteBuffer buffer = pool.dequeue(info.size + 4+8+4);//分配缓存大小    ------ 视频数据（摄像头采样决定  已被H264编码过）+ int（视频校验长度，4位）
			if (buffer == null) {//分配失败
				return;
			}
			data.position(info.offset);//设置此缓冲区的位置：0
			data.limit(info.size + info.offset);//设置缓冲区大小
			buffer.put(new byte[]{0x54, 0x4b, 0x4c, 0x59, 0x54, 0x4b, 0x4c ,0x59});//8  TKLYTKLY   同科联赢同科联赢   包头
			buffer.putInt(0);//4
			buffer.putInt(info.size);//写入帧的长度  （4字节）
			buffer.put(data);//写入视频采集到的数据   （info.size字节）
			Log.i(TAG, info.size+":Video"); 
			buffer.flip();//为下次ByteBuffer写入操作做好准备
			list.offer(buffer);//将这个ByteBuffer装入到  队列尾部
		}
		@Override
		public int getInitBufferSize() {
			return 128 * 1024;
		}
	}

	public class AudioSender extends AbstractMediaSender implements AudioFrameConsumer {
		public void closeAudioSocket(){
			super.cleanCloseSocket();
		}
		public AudioSender(String remoteAddr, int port) {
			super(remoteAddr, port);
		}
		@Override
		public void addAudioFrame(byte[] adtsHeader, ByteBuffer data, MediaCodec.BufferInfo info) {
			//			count++;
			//			if(count%100==0){
			//				Log.v("INFO", "网络传输data:"+data);
			//			}


			ByteBuffer buffer = pool.dequeue(info.size + adtsHeader.length + 4+8+4);//分配缓存大小        ------ 音频数据（麦克采样决定）+音频固定头adts格式+ int（音频校验长度，4位）
			if (buffer == null ) {//分配失败
				return;
			}
			data.position(info.offset);//设置此缓冲区的位置：0
			data.limit(info.size + info.offset);//设置缓冲区大小
			buffer.put(new byte[]{0x54, 0x4b, 0x4c, 0x59, 0x54, 0x4b, 0x4c ,0x59});//8
			String counts = "";
			if(MonitorActivity.down){
				//				if(MonitorActivity.IS_BACK){
				//					count =0x11111111;
				//					counts ="0x11111111";
				//				} else{
				jgg_count++;
				count = 0x33000000 + jgg_count;
				counts ="0x33333333";
				//				}
			}else{
				count =0x22222222;
				counts ="0x22222222";
			}
			//		Log.i(TAG, "COUNT:"+counts);
			//			count++;
			buffer.putInt(count);//4 默认填写1
			buffer.putInt(info.size + adtsHeader.length);//写入帧的长度   （4字节）     
			Log.i(TAG, "audio len is :" +(info.size + adtsHeader.length));
			buffer.put(adtsHeader);//写入音频固定的头      （7字节）
			buffer.put(data);//写入音频采集到的数据 
			//				Log.d("RAMY-MP4Muxer", "网络传输info.size:"+info.size+":Audio传输次数"+"---------"+count);
			buffer.flip();//为下次ByteBuffer写入操作做好准备
			list.offer(buffer);//将buffer插入到此队列的尾部（如果立即可行且不会超出此队列的容量），在成功时返回 true，如果此队列已满，则返回 false。
		} 
		@Override
		public int getInitBufferSize() {
			return 1024;
		}
	}
}
