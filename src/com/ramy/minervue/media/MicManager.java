package com.ramy.minervue.media;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
/**
 * 管理Mic 开启or关闭状态
 * @author Administrator
 */
public class MicManager {
	private static final String TAG = "MIC-Manager";
	private AudioManager audioManager = null;
	private Activity micActivity = null;
	private boolean micStatu;
	/**
	 * 
	 * @return  mic状态  false开启    ||||||true关闭
	 */
	public boolean isMicStatu() {
		return micStatu;
	}
	public void setMicStatu(boolean micStatu) {
		this.micStatu = micStatu;
	}
	/**
	 * 设置手机mic状态
	 * @param micActivity
	 * @param statu    false开启    ||||||true关闭
	 */
	public void setPhoneMicStatu(boolean statu){
		if(audioManager==null){
			audioManager =  (AudioManager)micActivity.getSystemService(Context.AUDIO_SERVICE);
		}
		setMicStatu(statu);//设置当前mic状态
		audioManager.setMicrophoneMute(statu);
		Log.i(TAG, "micManager:"+statu);
	}
	/**管理Mic 开启or关闭状态
	 * 默认开启状态
	 * @param micActivity
	 */
	public MicManager(Activity micActivity) { 
		this.micActivity = micActivity;
		this.micStatu = false;//默认开启状态
	}
}
