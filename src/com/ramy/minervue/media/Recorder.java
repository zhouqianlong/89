package com.ramy.minervue.media;

import android.util.Log;
import android.view.TextureView;

import com.ramy.minervue.app.MainService;
import com.ramy.minervue.ffmpeg.MP4Muxer;
import com.ramy.minervue.ffmpeg.MP4Muxer.RecordTimeListener;
import com.ramy.minervue.ffmpeg.MP4Muxer;
import com.ramy.minervue.media.Monitor.VideoSender;

/**
 * Created by peter on 12/29/13.
 */
public class Recorder {

    protected VideoCodec videoCodec = new VideoCodec();
    protected AudioCodec audioCodec = new AudioCodec();
    protected MP4Muxer muxer = null;
    protected boolean isCapturing = false;

    public VideoCodec getVideoCodec() {
        return videoCodec;
    }

    public AudioCodec getAudioCodec() {
        return audioCodec; 
    }

    public MP4Muxer getMuxer() {
    	return muxer;
    }
 
    public void start(String recordFile,RecordTimeListener listener) {
        if (!isCapturing) {
            muxer = new MP4Muxer(recordFile, videoCodec, audioCodec);
            audioCodec.startCapture();
            videoCodec.startCapture();
            muxer.setRecordTimeListener(listener);
            isCapturing = true;
        }
    }
//    public void start() {
//    	if (!isCapturing) {
//    		Log.i("RAMY-MonitorLocal", "MoitorLocal:47"+getVideoCodec().getSize().height+"\""+getVideoCodec().getSize().width);
//    		muxer = new MP4Muxer(MainService.getInstance().getSyncManager().getLocalFileUtil().generateVideoFilename(),videoCodec);
//    		Log.i("RAMY-MonitorLocal", "MoitorLocal:49"+getVideoCodec().getSize().height+"\""+getVideoCodec().getSize().width);
//    		videoCodec.startCapture();
//    		isCapturing = true;
//    	}
//    }

    public void stopVideo() {
    	if (isCapturing) {
    		videoCodec.stopCapture();
    		muxer.finish_();
    		isCapturing = false;
    	}
    }
    public void stop() {
        if (isCapturing) {
            videoCodec.stopCapture();
            audioCodec.stopCapture();
            muxer.finish();
            isCapturing = false;
        }
    } 

    public boolean isCapturing() {
        return isCapturing;
    }

}
