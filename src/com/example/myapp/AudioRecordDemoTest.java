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
            Log.e(TAG, "����¼����");  
            return;  
        }  
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,  
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,  
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);  
        if (mAudioRecord == null) {  
            Log.e("sound", "mAudioRecord��ʼ��ʧ��");  
        }  
        isGetVoiceRun = true;  
  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                mAudioRecord.startRecording();  
                short[] buffer = new short[BUFFER_SIZE];  
                while (isGetVoiceRun) {  
                    //r��ʵ�ʶ�ȡ�����ݳ��ȣ�һ�����r��С��buffersize  
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);  
                    long v = 0;  
                    // �� buffer ����ȡ��������ƽ��������  
                    for (int i = 0; i < buffer.length; i++) {  
                        v += buffer[i] * buffer[i];  
                    }  
                    // ƽ���ͳ��������ܳ��ȣ��õ�������С��  
                    double mean = v / (double) r;  
                    double volume = 10 * Math.log10(mean); 
                    Message msg= Message.obtain();
                    msg.what=1;
                    if(volume>MusicMainActivity.db_values){
                    	msg.obj= "(����mic)�ֱ�ֵ:" + (int)volume;
                    	am.setMicrophoneMute(true);//������mic��
                    }else{
                    	msg.obj= "(�ر�mic)�ֱ�ֵ:" +  (int)volume;
                    	am.setMicrophoneMute(false);//���ر�mic��
                    }
                    uiHandler.sendMessage(msg);
                    Log.d(TAG, "�ֱ�ֵ:" + volume);  
                    // ���һ��ʮ��  
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