package com.ramy.minervue.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class ServiceLoader extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    Context mContext;
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	
//        if (ACTION.equals(intent.getAction())) {
//        	mContext = context;
//        	new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(15000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					mHandler.sendEmptyMessage(1);
//
//				}
//			}).start();
//        }
    }
    
    Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
			Intent service = new Intent(mContext, LoginActivity.class);
			mContext.startService(service);
    	};
    };

}
