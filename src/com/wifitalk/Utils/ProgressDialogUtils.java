package com.wifitalk.Utils;

import java.util.ArrayList;
import java.util.List;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.widget.Toast;

public class ProgressDialogUtils {
	private static ProgressDialog mProgressDialog;
	private static Context mContext;
	private static CharSequence mMessage;
	/**
	 * 显示ProgressDialog
	 * @param context
	 * @param message
	 */
	public static void showProgressDialog(Context context, CharSequence message){
		try {
			mContext = context;
			mMessage = message;
			if(mProgressDialog == null){
				mProgressDialog = ProgressDialog.show(context, "", message);
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.setOnKeyListener(onKeyListener);
			}else{
				mProgressDialog.setMessage(message);
				mProgressDialog.show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void change(String message){
		try {
			if(mContext==null)
				return;
			if(mProgressDialog == null){
				mProgressDialog = ProgressDialog.show(mContext, "", mMessage.toString()+message);
				mProgressDialog.setOnKeyListener(onKeyListener);
			}else{
				mProgressDialog.setMessage(mMessage.toString()+message.toString());
				mProgressDialog.show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 关闭ProgressDialog
	 */
	public static void dismissProgressDialog(){
		try {
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
				mProgressDialog = null;
				mContext = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static OnKeyListener onKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            	dismissProgressDialog();
            }
            return false;
        }
    };
}
