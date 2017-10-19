package com.ramy.minervue.media;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
/**
 * ����Mic ����or�ر�״̬
 * @author Administrator
 */
public class MicManager {
	private static final String TAG = "MIC-Manager";
	private AudioManager audioManager = null;
	private Activity micActivity = null;
	private boolean micStatu;
	/**
	 * 
	 * @return  mic״̬  false����    ||||||true�ر�
	 */
	public boolean isMicStatu() {
		return micStatu;
	}
	public void setMicStatu(boolean micStatu) {
		this.micStatu = micStatu;
	}
	/**
	 * �����ֻ�mic״̬
	 * @param micActivity
	 * @param statu    false����    ||||||true�ر�
	 */
	public void setPhoneMicStatu(boolean statu){
		if(audioManager==null){
			audioManager =  (AudioManager)micActivity.getSystemService(Context.AUDIO_SERVICE);
		}
		setMicStatu(statu);//���õ�ǰmic״̬
		audioManager.setMicrophoneMute(statu);
		Log.i(TAG, "micManager:"+statu);
	}
	/**����Mic ����or�ر�״̬
	 * Ĭ�Ͽ���״̬
	 * @param micActivity
	 */
	public MicManager(Activity micActivity) { 
		this.micActivity = micActivity;
		this.micStatu = false;//Ĭ�Ͽ���״̬
	}
}
