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
//	//	public static boolean THREAD_STATU = true;//�߳�״̬  Ĭ�ϣ� true
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
//		vSender.cancelAndClear(true);//video����ֹͣ����
//	}
//
//	public void monitorstop(){
//		//			vSender.cancelAndClear(true);//ȡ�����緢��
//		//				aSender.cancelAndClear(true);//ȡ�����緢��
//		super.stop();//�رս�����
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
//			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/��Ҫɾ��ERRORTEST.txt", true)));  //false��ʾ��������
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd�� HHʱmm��ss��");
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
//			while (!isCancelled()) {//�̱߳�  ����ȡ��״̬  �� isCancelled Ϊ  false  
//				synchronized (list) {
//					try {
//						buffer = list.take();
//						if(buffer==null){
//							continue;	
//						}
//						byte [] senddata = buffer.array();
//						//						Log.i(TAG, senddata[0]+"/"+senddata[1]+"/"+senddata[2]+"/"+senddata[3]+"/"+senddata[4]+"/"+senddata[5]+"/"+senddata[6]+"/");
//						clientSocket.send(new DatagramPacket(senddata, senddata.length,InetAddress.getByName(remoteAddr), port));
//						pool.queue(buffer);//�������
//					} catch (InterruptedException e) {
//						Log.i(TAG, "..................InterruptedException��Ƶ�Խ����ʹ���   ................");
//						continue;
//					} catch (IOException e) {
//						e.printStackTrace();
//						Log.i(TAG, "..................IOException��Ƶ�Խ����ʹ���  ................");
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
//			MonitorLocal.printStatu("AbstractMediaSender �߳̽������رռ��");
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
//			ByteBuffer buffer = pool.dequeue(info.size +2+4);//���仺���С    ------ ��Ƶ���ݣ�����ͷ��������  �ѱ�H264�������+ int����ƵУ�鳤�ȣ�4λ��
//			if (buffer == null) {//����ʧ��
//				return;
//			}
//			data.position(info.offset);//���ô˻�������λ�ã�0
//			data.limit(info.size + info.offset);//���û�������С
//			buffer.put(new byte[]{0x54, 0x4b});//8  TK    ͬ��   ��ͷ
//			buffer.putInt(info.size);//д��֡�ĳ���  ��4�ֽڣ�
//			buffer.put(data);//д����Ƶ�ɼ���������   ��info.size�ֽڣ�
//			Log.i(TAG, info.size+":Video"); 
//			buffer.flip();//Ϊ�´�ByteBufferд���������׼��
//			list.offer(buffer);//�����ByteBufferװ�뵽  ����β��
//		}
//		@Override
//		public int getInitBufferSize() {
//			return 128 * 1024;
//		}
//	}
//
//}
