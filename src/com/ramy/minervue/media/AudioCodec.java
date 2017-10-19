package com.ramy.minervue.media;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.ramy.minervue.app.BaseSurfaceActivity;
import com.ramy.minervue.util.ATask;

/**
 * Created by peter on 10/24/13.
 * 录音内容大于最小缓冲区才能取出数据
 */
public class AudioCodec implements AudioFrameProvider {

	public static final int RATE = 44100;
	public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int FORMAT_BYTES = 2;
	public static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
	public static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
	public static final int CHANNEL_COUNT = 1;
	public static final int PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLD;  //AACObjectLC
	public static final int INPUT_SIZE = 2048;
	public static final int BIT_RATE = 54000;

	private static final int BYTES_PER_SEC = RATE * CHANNEL_COUNT * FORMAT_BYTES;
	private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
	private static final String TAG = "RAMY-AudioCodec";
	int playBufSize;
	private static final int TIME_OUT = 100000;

	private boolean isCapturing = false;
	private MediaCodec codec;
	private AudioRecord record;
	private PCMFetcher pcmFetcher;
	private FrameFetcher frameFetcher;
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	private ArrayList<AudioFrameConsumer> consumerList = new ArrayList<AudioFrameConsumer>();

	public boolean isCapturing() {
		return isCapturing;
	}

	@Override
	public void addConsumer(AudioFrameConsumer consumer) {
		if (consumer != null) {
			Log.i(TAG, "consumerList.size():"+consumerList.size());
			consumerList.add(consumer);
		}
	}

	@Override
	public int getSampleRate() {
		return RATE;
	}

	@Override
	public int getChannelCount() {
		return CHANNEL_COUNT;
	}

	@Override
	public int getSampleSizeInBits() {
		return FORMAT_BYTES * 8;
	}

	@Override
	public int getBitRate() {
		return BIT_RATE;
	}

	@Override
	public int getProfile() {
		return PROFILE;
	}
//	private boolean initCodec() {
//		int size = AudioRecord.getMinBufferSize(RATE, CHANNEL_IN, FORMAT);
//				size = Math.max(size, BYTES_PER_SEC * 10);
//		record = new AudioRecord(AUDIO_SOURCE, RATE, CHANNEL_IN, FORMAT, size);
//		codec = MediaCodec.createEncoderByType("audio/mp4a-latm");//实例化一个编码器支持给定mime类型的输出数据。参数:输出数据的类型所需的mime类型
//		MediaFormat mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", RATE, CHANNEL_COUNT);//mime内容的mime类型。sampleRate采样率的内容。channelCount音频频道内容的数量
//    	mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
//		mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
//		mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
//		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64 * 1024);//AAC-HE 64kbps
//		mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 2);
//		try {
//			codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//		} catch (IllegalStateException e) {
//			releaseCodec();
//			return false;
//		}
//		return record.getState() == AudioRecord.STATE_INITIALIZED;
//	}
	
	private boolean initCodec() {
		int bufferSize = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*2;
		record = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		codec = MediaCodec.createEncoderByType("audio/mp4a-latm");
		MediaFormat format = new MediaFormat();
		format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
		format.setInteger(MediaFormat.KEY_BIT_RATE, 64 * 1024);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
		format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
		codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); 
		return true;
	}


	private void releaseCodec() {
		if (record != null) {
			record.release();
			record = null;
		}
		if (codec != null) {
			codec.release();
			codec = null;
		}
	}

	public void startCapture() {
		if (!isCapturing && initCodec()) {
			codec.start();//启动编码器
			inputBuffers = codec.getInputBuffers();
			outputBuffers = codec.getOutputBuffers();
			frameFetcher = new FrameFetcher();//输出 （在Audio每一帧头前，设置一个adts格式头）
			frameFetcher.start();
			pcmFetcher = new PCMFetcher();
			pcmFetcher.start();
			isCapturing = true;
			Log.i(TAG, "Start capture.");
		}
	}

	public void stopCapture() {
		if (isCapturing) {
			pcmFetcher.cancelAndClear(false);
			frameFetcher.clear();
			codec.stop();
			releaseCodec();
			consumerList.clear();
			isCapturing = false;
			Log.i(TAG, "Stop capture");
		}
	}

	private class FrameFetcher extends ATask<Void, Void, Void> {

		private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

		private byte[] header = new byte[] {
				(byte) 0xFF,
				(byte) 0xF9,
				(byte)(((PROFILE - 1) << 6) | (4 << 2) | (2 >>> 2)),    // Assume 44100.
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0xFC,
		};
		@Override
		protected Void doInBackground(Void... params) {
			while (true) {
				//				int index = -1;
				//				if(MonitorActivity.down){
				int	index = codec.dequeueOutputBuffer(info, 0);
				//				}
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					break;
				}
				if (index >= 0) {
					setADTSHeader(info.size + header.length-1);
					try {
						for (AudioFrameConsumer consumer : consumerList) {
							if(BaseSurfaceActivity.audioswitch){
								consumer.addAudioFrame(header, outputBuffers[index], info);
							}
						}
					} catch (Exception e) {
						continue;
					}
					codec.releaseOutputBuffer(index, false);
				} else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					outputBuffers = codec.getOutputBuffers();
				}
			}
			return null;
		}

		private void setADTSHeader(int size) {
			header[3] = (byte)(((2 & 0x03) << 6) | (size >>> 11));
			header[4] = (byte)((size & 0x7FF) >>> 3);
			header[5] = (byte)(((size & 0x07) << 5) | 0x1F);
		}
	}

	/**
	 *   Audio录声音
	 */
	private class PCMFetcher extends ATask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			record.startRecording();
			while (true) {

				int index = codec.dequeueInputBuffer(TIME_OUT);
				if (index >= 0) {
					inputBuffers[index].clear();
					long time = System.nanoTime() / 1000;
					record.read(inputBuffers[index], INPUT_SIZE); 
					if (isCancelled()) {
						codec.queueInputBuffer(index, 0, INPUT_SIZE, time,
								MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						break;
					} else {
						codec.queueInputBuffer(index, 0, INPUT_SIZE, time, 0);
					}
				}
			}
			record.stop();
			return super.doInBackground();
		}

	}

}
