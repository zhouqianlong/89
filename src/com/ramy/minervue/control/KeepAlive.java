package com.ramy.minervue.control;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ramy.minervue.app.MainService;
import com.ramy.minervue.util.ATask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by peter on 11/7/13.
 */
public class KeepAlive extends ATask<Void, Integer, Void> {
    private static final String TAG = "RAMY-KeepAlive";

    private ControlManager controller;

    private int packetsSent = 0;

    public KeepAlive(ControlManager controller) {
        this.controller = controller;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    	controller.onKeepAliveSent();
    	if(MainService.MONITOR_STATU){
    		controller.onKeepVideoAliveSent(); 
    	}
    }
    //wifi开启状态：3s/次{uuid:设备名,type:keep-alive}		
    @Override
    protected Void doInBackground(Void... params) {
        JSONObject json = new JSONObject();
        JSONObject json_m = new JSONObject();
       
        int i = 0 ;
        while (!isCancelled()) {//线程是正常的
        	i++;
        	 try {
                 json.put("uuid", MainService.getInstance().getUUID());
                 json.put("location", MainService.getInstance().getLocation());
                 json.put("action", "keep-alive");
                 json_m.put("uuid", MainService.getInstance().getUUID());
                 json_m.put("location", MainService.getInstance().getLocation());
                 json_m.put("action", "keep-alive-video");
                 
                 
 
     			
             } catch (JSONException e) {
                 return super.doInBackground();
             }
        	if(MainService.MONITOR_STATU){
        		controller.sendPacket(json_m);
        		Log.i(TAG, i+""+json_m.toString());
        	}else{
        		controller.sendPacket(json);
        		Log.i(TAG, i+""+json.toString());
        	}
            publishProgress(++packetsSent);
        	
            try {
            	if(MainService.getInstance().system_Action==false){
            		Log.d(TAG, "/休眠状态 心跳包6秒等待");
    				Thread.sleep(6000);//休眠状态
    			}
                Thread.sleep(3000);//3秒发一次
            } catch (InterruptedException e) {
                break;
            }
        }
        return super.doInBackground();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "Keep-alive terminated unexpectedly.");
        controller.startKeepAlive();
//        controller.startReceiver();
    }
}
