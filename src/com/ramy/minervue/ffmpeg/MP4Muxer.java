package com.ramy.minervue.ffmpeg;

import android.hardware.Camera;
import android.media.MediaCodec;
import android.util.Log;

import com.ramy.minervue.media.AudioFrameConsumer;
import com.ramy.minervue.media.AudioFrameProvider;
import com.ramy.minervue.media.VideoFrameConsumer;
import com.ramy.minervue.media.VideoFrameProvider;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by peter on 12/5/13.
 */
public class MP4Muxer implements VideoFrameConsumer, AudioFrameConsumer {

	private static final String TAG = "RAMY-MP4Muxer";

	private String filePath;
	private int formatContext;
	private VideoFrameProvider vProvider;
	private AudioFrameProvider aProvider;
	private volatile int vIndex = -1;
	private volatile int aIndex = -1;
	private volatile boolean ready = false;
	private RecordTimeListener listener;
	private Long ptsOffset = null;

	/**
	 * If the muxer is not ready for writing, we save up to 100 frames here for further writing.
	 */
	private LinkedBlockingQueue<Frame> buffers = new LinkedBlockingQueue<Frame>(100);

	public MP4Muxer(String filePath, VideoFrameProvider vProvider) {
		this.filePath = filePath;
		this.formatContext = create(filePath);
		this.vProvider = vProvider;
		vProvider.addConsumer(this);
	}
	public MP4Muxer(String filePath, VideoFrameProvider vProvider, AudioFrameProvider aProvider) {
		this.filePath = filePath;
		this.formatContext = create(filePath);//1580703024              ==-1419086992
		this.vProvider = vProvider;
		this.aProvider = aProvider;
		vProvider.addConsumer(this);
		aProvider.addConsumer(this);
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void finish_() {
		formatContext = 0;
	}
	public void finish() {
		try {
			release(formatContext);
		} catch (Exception e) {
		}
		formatContext = 0;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setRecordTimeListener(RecordTimeListener listener) {
		this.listener = listener;
	}

	private boolean containsFlag(int flags, int flag) {
		return (flags & flag) != 0;
	}

	private synchronized void writeHeader() {
		if (vIndex >= 0 && aIndex >= 0 && !ready) {
			writeHeader(formatContext);
			while (true) {
				Frame frame = buffers.poll();
				if (frame == null) {
					break;
				}
				writeFrame(formatContext, frame.index, frame.buffer, 0, frame.buffer.remaining(),
						frame.pts);
			}
			ready = true;
		}
	}

	private ByteBuffer duplicateBuffer(ByteBuffer ref, MediaCodec.BufferInfo info) {
		ByteBuffer ret = ByteBuffer.allocateDirect(info.size);
		ref.limit(info.offset + info.size);
		ref.position(info.offset);
		ret.put(ref);
		ret.flip();
		return ret;
	}
	//<embed width="320" height="240" type="audio/mpeg" autostart="1" loop="0" class="block-centered"   src="ftp/incoming/803/video/3-803-20161202-111736.mp4" ></embed>



	@Override
	public void addVideoFrame(ByteBuffer data, MediaCodec.BufferInfo info) {
		if (vIndex < 0 && containsFlag(info.flags, MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
			Camera.Size size = vProvider.getSize();
			vIndex = addVideo(formatContext, vProvider.getFpsRange()[1] * 10, size.width,size.height, data, info.offset, info.size);
//			Log.i(TAG, "addVideo "+size.width+" X"+size.height+" info.offset:"+info.offset+" info.size:"+info.size);
			writeHeader();
		} else {
			if (ptsOffset == null) {
				ptsOffset = info.presentationTimeUs;
			}
			if (listener != null) {
				listener.onUpdateTime((info.presentationTimeUs - ptsOffset) / 1000,filePath);
			}
			synchronized (this) {
				if (ready) {
//					Log.i(TAG, "writeFrame  info.offset:"+info.offset+" info.size:"+info.size);
					writeFrame(formatContext, vIndex, data, info.offset, info.size,
							info.presentationTimeUs);
				} else {
					ByteBuffer dup = duplicateBuffer(data, info);
					buffers.offer(new Frame(vIndex, dup, info.presentationTimeUs));
				}
			}
		}
	}

	@Override
	public void addAudioFrame(byte[] adtsHeader, ByteBuffer data, MediaCodec.BufferInfo info) {
//					Log.d("INFO", "±¾µØ´«Êädata:"+data);
		if (aIndex < 0 && containsFlag(info.flags, MediaCodec.BUFFER_FLAG_CODEC_CONFIG)) {
			aIndex = addAudio(formatContext, data, info.offset, info.size);
			writeHeader();
		} else {
			synchronized (this) {
				if (ready) {
					writeFrame(formatContext, aIndex, data, info.offset, info.size,info.presentationTimeUs);
				} else {
					ByteBuffer dup = duplicateBuffer(data, info);
					buffers.offer(new Frame(aIndex, dup, info.presentationTimeUs));
				}
			}
		}
	}

	private native synchronized int create(String filePath);

	private native synchronized int addVideo(int context, int base, int width, int height, ByteBuffer extra, int offset, int size);

	private native synchronized int addAudio(int context, ByteBuffer extra, int offset, int size);

	private native synchronized void writeHeader(int context);

	private native synchronized void writeFrame(int context, int index, ByteBuffer buffer, int offset, int size, long pts);

	private native synchronized void release(int context);

	static {
		System.loadLibrary("ramymedia");
	}

	public interface RecordTimeListener {

		public void onUpdateTime(long recordMilli,String path);
		public int onUpdateTimeForSave(int count,String path);
		public int onBatteryError();

	}

	private class Frame {

		public int index;
		public ByteBuffer buffer;
		public long pts;

		public Frame(int index, ByteBuffer buffer, long pts) {
			this.index = index;
			this.buffer = buffer;
			this.pts = pts;
		}

	}

}
