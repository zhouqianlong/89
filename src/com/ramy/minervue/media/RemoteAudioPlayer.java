package com.ramy.minervue.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.MonitorActivity;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.NetUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by peter on 12/6/13.
 */
public class RemoteAudioPlayer {

	private static final String TAG = "RAMY-RemoteAudioPlayer";

	private static final int TIME_OUT = 100000;

	public static final int RATE = 44100;
	public static final int CHANNEL_COUNT = 1;
	private static final int INPUT_SIZE = 20480;

	private boolean isCapturing = false;
	private MediaCodec codec = null;
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	private Receiver receiver = null;
	private PCMPlayer player = null;
 
	private byte[] FLAGs = { 84, 75, 76, 89, 84, 75, 76, 89};
	public static final int SEARCH_LEN = 100;
	public final int FLAG_LEN = 8;
 

	private boolean initCodec() {
		codec = MediaCodec.createDecoderByType("audio/mp4a-latm");
		MediaFormat mediaFormat = MediaFormat.createAudioFormat(
				"audio/mp4a-latm", RATE, CHANNEL_COUNT);
		mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, INPUT_SIZE);
		mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
		mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(new byte[]{(byte) 0x12, (byte)0x08}));
		try {
			codec.configure(mediaFormat, null, null, 0);
		} catch (IllegalStateException e) {
			releaseCodec();
			return false;
		}
		return true;
	}

	private void releaseCodec() {
		if (codec != null) {
			codec.release();
			codec = null;
		}
	}
	/**
	 * 
	 * @param port    端口
	 * @param type    重置端口  1      其他默认
	 */
	public void startPlayback(int port,int type) {
		if (!isCapturing && initCodec()) {
			codec.start();
			inputBuffers = codec.getInputBuffers();
			outputBuffers = codec.getOutputBuffers();
			receiver = new Receiver();
			receiver.start(port);
			player = new PCMPlayer();
			player.start();
			Log.i(TAG, "Start playback.");
			isCapturing = true;
		}else if(isCapturing&&type==1){
			receiver.cancel(true);
			receiver = new Receiver();
			Log.i(TAG, ".........startPlayback");
			receiver.start(port);
		}
	}

	public void stopPlayback() {
		if (isCapturing) {
			receiver.cancelAndClear(true);//关闭8200
			Log.i(TAG, "Stop receiving audio.");
			player.cancelAndClear(true);//关闭播放器
			codec.stop();
			releaseCodec();
			Log.i(TAG, "Stop playback.");
			isCapturing = false;
		}
	}

	public boolean isCapturing() {
		return isCapturing;
	}
	public boolean ADTS_Statu = true;
	int ADTSCount = 0 ;
	private class Receiver extends ATask<Integer, Void, Void> {
		private ByteBuffer buffer = ByteBuffer.wrap(new byte[INPUT_SIZE]);
		private ServerSocketChannel serverSocket = null;
		private byte [] formatedataArray = null;
		private SocketChannel socket = null;
		private ByteBuffer succeBuffer = null;
		private int datasize ;
		private int keypositon;
		private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		private void receiveADTS() throws IOException {
			ADTSCount++;
			if(ADTSCount>=2147483640){//达到最大值了
				ADTSCount = 0;
			}
			buffer.clear();
			buffer.limit(16);
			while (buffer.hasRemaining()) {
				socket.read(buffer);
			}	
			int size = buffer.getInt(12);//读帧长 
			if(size>400||size<=0){
				datasize = findBySize();//寻找关键针出现的位置
				if(datasize>0&&datasize<400&&formatedataArray!=null){
					size = datasize;
					buffer.clear();
					int checklimit  = datasize-(200-keypositon);
					if(checklimit>0&&checklimit<400){
						Log.i(TAG, "checklimit没有问题"+checklimit);
						buffer.limit(datasize-(200-keypositon));
					}else{
						Log.e(TAG, "checklimit异常"+checklimit);
						buffer.limit(156);
					}
				}
			}else{
				buffer.clear();
				buffer.limit(size);
			}
			while (buffer.hasRemaining()) {
				socket.read(buffer);
			}
			int index = 0;
			try {
				index = codec.dequeueInputBuffer(TIME_OUT);
				Log.i(TAG, "dequeueInputBuffer:"+index);
			} catch (Exception e) {
				return;
			} 
			Log.i(TAG, "音频数据："+size+",index:"+index+",info.flags:"+info.flags+",info.offset:"+info.offset+",info.size:"+info.size);
			if (index >= 0) {
				inputBuffers[index].clear();
				buffer.flip();
				if(formatedataArray!=null){
					//错误数据删除，把正确数据复制到  buffer前面
					succeBuffer = ByteBuffer.wrap(new byte[INPUT_SIZE]);
					succeBuffer.put(formatedataArray);
					succeBuffer.put(buffer);
					formatedataArray=null;
					if(datasize>0&&datasize<400){
						inputBuffers[index].put(succeBuffer);
						codec.queueInputBuffer(index, 0, datasize, System.nanoTime() / 1000, 0);
						Log.i(TAG, "成功处理掉错误针！！！");
					}else{
						return;
					}
				}else{
					inputBuffers[index].put(buffer);
					codec.queueInputBuffer(index, 0, size, System.nanoTime() / 1000, 0);
				}

			}
		}

		public int findBySize() throws IOException {
			boolean succe = true;
			while (succe) {
				buffer.clear();
				buffer.limit(200); //限制读200个元素
				while (buffer.hasRemaining()) {
					socket.read(buffer);
				}
				ByteBuffer data =buffer;//从管道获取的元素，放到变量里面
				int size =-1;//状态   -1为没找到
				for(int i = 0 ; i < 200; i ++){//200次循环比对
					if(i<=184){
						if(data.get(i)==FLAGs[0]&&data.get(i+1)==FLAGs[1]&&data.get(i+2)==FLAGs[2]&&data.get(i+3)==FLAGs[3]){//比对成功
							Log.e(TAG, "错误帧处理中"+i);
							keypositon = i+16;
							size = data.getInt(i+12);
							byte [] dataArray = buffer.array();
							formatedataArray = new byte[200-keypositon];
							System.arraycopy(dataArray, keypositon, formatedataArray, 0, 200-keypositon);
							succe =false;
							break;
						}else{
							formatedataArray=null;
						}
					}else{
						formatedataArray=null;
					}
				}
				if(size>0&&size<400){
					return size;
				}
				buffer.flip();
			}
			return -1;

		}

		@Override
		protected Void doInBackground(Integer... params) {
			Log.i(TAG, "Start doInBackground + ......................." );
			int port = params[0];
			serverSocket = NetUtil.openServerSocketChannel(port);//本地8200 
			if (serverSocket == null) {
				return super.doInBackground();
			}
			Log.i(TAG, "Start waiting at " + port + ".");
			try {
				socket = serverSocket.accept();
				socket.socket().setSoTimeout(60000);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} finally {
				NetUtil.cleanSocket(serverSocket);
			}
			Log.i(TAG, "Remote connected, playback pending.");
			ADTSCount = 0;
			try {
				while (!isCancelled()) {
						receiveADTS();
				}
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} finally {
				NetUtil.cleanSocket(socket);
			}
			return super.doInBackground();
		}
		@Override
		protected void onPostExecute(Void aVoid) {
			if(receiver!=null&&MainService.getInstance().getServerOnlineStatus()){
				startPlayback(8200, 1);
			}
		}
	}


	private class PCMPlayer extends ATask<Void, Void, Void> {
		private AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, INPUT_SIZE,
				AudioTrack.MODE_STREAM);

		private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		private byte[] buffer = new byte[INPUT_SIZE];

		private void playFrameFromCodec() {
//			Log.i(TAG, "playFrame info.flags:"+info.flags+",info.offset:"+info.offset+",info.size:"+info.size);
			if(MonitorActivity.down){
				track.setStereoVolume(0.0f, 0.0f);
			}else{
				track.setStereoVolume(1.0f, 1.0f);
			}
			int index = codec.dequeueOutputBuffer(info, TIME_OUT);//-1不能播放
			Log.i(TAG, "playFrame.监控index:"+index+",info.flags:"+info.flags+",info.offset:"+info.offset+",info.size:"+info.size);
			if (index >= 0) {
				ADTSCount = 0 ;
				outputBuffers[index].position(info.offset);
				outputBuffers[index].limit(info.size + info.offset);
				outputBuffers[index].get(buffer, 0, info.size);
				codec.releaseOutputBuffer(index, false);
				track.write(buffer, 0, info.size);
				ADTS_Statu  = true; 
			} else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				outputBuffers = codec.getOutputBuffers();
			} else if (index == -1&&MonitorActivity.ONLINE){//TODO  播放失败
				ADTSCount++;
				ADTS_Statu  = false;
				if(ADTS_Statu == false&&ADTSCount%800==0&&MonitorActivity.ONLINE){//多次接收带网络数据但是播放器不能播放  ->重置WiFi
					try {
//						MainActivity.getGetInstance().setWifi(false);
						Log.i(TAG, "MainActivity.getGetInstance().setWifi(false);");
						try {
							Thread.sleep(10);
							startPlayback(8200, 1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
//						MainActivity.getGetInstance().setWifi(true);
//						Log.i(TAG, "MainActivity.getGetInstance().setWifi(false);");
//					MainService.RCONN = true;
					} catch (Exception e) {
					}
				}
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			track.setStereoVolume(0.0f, 0.0f);
			track.play();
			while (!isCancelled()) {
					playFrameFromCodec();
			}
			track.stop(); 	
			return super.doInBackground();
		}
	}
}
