package com.example.myapp;  
  
import com.example.music.MusicMainActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;  
import android.media.AudioManager;
import android.media.AudioRecord;  
import android.media.MediaRecorder;  
import android.os.Handler;
import android.os.Message;
import android.util.Log;  
  
/** 
 * Created by greatpresident on 2014/8/5.  
 */  
public class AudioRecordDemoTest {  
	 public AudioManager am;
    private static final String TAG = "AudioRecord";  
    static final int SAMPLE_RATE_IN_HZ = 8000;  
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,  
                    AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);  
    AudioRecord mAudioRecord;  
    boolean isGetVoiceRun;  
    Object mLock;  
    Handler uiHandler;
    public AudioRecordDemoTest(Handler uiHandler,Context context) {  
        mLock = new Object();  
        this.uiHandler= uiHandler;
        am =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }  
  
    public void getNoiseLevel() {  
        if (isGetVoiceRun) {  
            Log.e(TAG, "还在录着呢");  
            return;  
        }  
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,  
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,  
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);  
        if (mAudioRecord == null) {  
            Log.e("sound", "mAudioRecord初始化失败");  
        }  
        isGetVoiceRun = true;  
  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                mAudioRecord.startRecording();  
                short[] buffer = new short[BUFFER_SIZE];  
                while (isGetVoiceRun) {  
                    //r是实际读取的数据长度，一般而言r会小于buffersize  
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);  
                    long v = 0;  
                    // 将 buffer 内容取出，进行平方和运算  
                    for (int i = 0; i < buffer.length; i++) {  
                        v += buffer[i] * buffer[i];  
                    }  
                    // 平方和除以数据总长度，得到音量大小。  
                    double mean = v / (double) r;  
                    double volume = 10 * Math.log10(mean); 
                    Message msg= Message.obtain();
                    msg.what=1;
                    if(volume>MusicMainActivity.db_values){
                    	msg.obj= "(开启mic)分贝值:" + (int)volume;
                    	am.setMicrophoneMute(true);//（开启mic）
                    }else{
                    	msg.obj= "(关闭mic)分贝值:" +  (int)volume;
                    	am.setMicrophoneMute(false);//（关闭mic）
                    }
                    uiHandler.sendMessage(msg);
                    Log.d(TAG, "分贝值:" + volume);  
                    // 大概一秒十次  
                    synchronized (mLock) {  
                        try {  
                            mLock.wait(100);  
                        } catch (InterruptedException e) {  
                            e.printStackTrace();  
                        }  
                    }  
                }  
                mAudioRecord.stop();  
                mAudioRecord.release();  
                mAudioRecord = null;  
            }  
        }).start();  
    }  
}