package com.ramy.minervue.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ramy.minervue.app.MainService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by peter on 3/6/14.
 */
public class License {

	private static final String TAG = "RAMY-License";

	private static final String KEY_SN = "serialNumber";

	private static byte[] KEY = {(byte) 0xB8, (byte) 0x99, (byte) 0x6, (byte) 0x64, (byte) 0xF0,
		(byte) 0x52, (byte) 0x91, (byte) 0xC7, (byte) 0xE3, (byte) 0x61, (byte) 0xC9,
		(byte) 0x30, (byte) 0x75, (byte) 0x98, (byte) 0x98, (byte) 0x85, (byte) 0x28,
		(byte) 0xA2, (byte) 0x18, (byte) 0x91, (byte) 0x25, (byte) 0xC6, (byte) 0x84,
		(byte) 0x8A, (byte) 0x07, (byte) 0x33, (byte) 0x41, (byte) 0x77, (byte) 0xF7,
		(byte) 0x3D, (byte) 0xF3, (byte) 0x24};

	private static byte[] IV = {(byte) 0x4E, (byte) 0xD, (byte) 0x48, (byte) 0x3B, (byte) 0xC6,
		(byte) 0xC4, (byte) 0x69, (byte) 0x9B, (byte) 0x85, (byte) 0xCC, (byte) 0x88,
		(byte) 0x35, (byte) 0xF1, (byte) 0xCF, (byte) 0x54, (byte) 0x2C};

	private static String ALGORITHM = "AES/CBC/PKCS7Padding";

	public byte[] getMac() {
		List<NetworkInterface> interfaces = null;
		try {
			interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface i : interfaces) {
				if (i.getName().equalsIgnoreCase("wlan0")) {
					byte[] addr = i.getHardwareAddress();
					if (addr != null) {
						return addr;
					}
				}
			}
		} catch (SocketException e) {
			// Ignore.
		}
		return null;
	}

	public String getMachineDigest() {
//		initSpreadDoubleSim();
//		initMtkDoubleSim();
		try {
			initQualcommDoubleSim();
			TelephonyManager m;
			Context c = MainService.getInstance();
			m = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
//			String aaa = m.getDeviceId();
//		System.out.println(aaa);
			return m.getDeviceId();//121177000010009
		} catch (Exception e) {
			return "";
		}
	}

	private static void initSpreadDoubleSim() {
		String spreadTmService;
		Context cxt = MainService.getInstance();
		try {
			Class<?> c = Class .forName("com.android.internal.telephony.PhoneFactory");
			Method m = c.getMethod("getServiceName", String.class, int.class);
			spreadTmService = (String) m.invoke(c, Context.TELEPHONY_SERVICE, 1);
			TelephonyManager tm = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
			//			imsi_1 = tm.getSubscriberId();
			String imei_1 = tm.getDeviceId();
			//			phoneType_1 = tm.getPhoneType();
			TelephonyManager tm1 = (TelephonyManager) cxt.getSystemService(spreadTmService);
			//			imsi_2 = tm1.getSubscriberId();
			String imei_2 = tm1.getDeviceId();
			//			phoneType_2 = tm1.getPhoneType();
			//			if (TextUtils.isEmpty(imsi_1) && (!TextUtils.isEmpty(imsi_2))) {
			//				defaultImsi = imsi_2;
			//			}
			//			if (TextUtils.isEmpty(imsi_2) && (!TextUtils.isEmpty(imsi_1))) {
			//				defaultImsi = imsi_1;
			//			}
			Log.i("syso", imei_1);
			Log.i("syso", imei_2);
		} catch (Exception e) {
			//			isSpreadDoubleSim = false;
			return;
		}
		//		isSpreadDoubleSim = true;
	}

	private static void initMtkDoubleSim() {
		Context mContext = MainService.getInstance();
		try {
			TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> c = Class.forName("com.android.internal.telephony.Phone");
			Field fields1 = c.getField("GEMINI_SIM_1");//java.lang.reflect.Field
			fields1.setAccessible(true);
			Integer simId_1 = (Integer) fields1.get(null);
			Field fields2 = c.getField("GEMINI_SIM_2");
			fields2.setAccessible(true);
			Integer simId_2 = (Integer) fields2.get(null);
			Method m = TelephonyManager.class.getDeclaredMethod(
					"getSubscriberIdGemini", int.class);
			String imsi_1 = (String) m.invoke(tm, simId_1);
			String imsi_2 = (String) m.invoke(tm, simId_2);
			Method m1 = TelephonyManager.class.getDeclaredMethod(
					"getDeviceIdGemini", int.class);
			String imei_1 = (String) m1.invoke(tm, simId_1);
			String imei_2 = (String) m1.invoke(tm, simId_2);
			Method mx = TelephonyManager.class.getDeclaredMethod(
					"getPhoneTypeGemini", int.class);
			Integer phoneType_1 = (Integer) mx.invoke(tm, simId_1);
			Integer phoneType_2 = (Integer) mx.invoke(tm, simId_2);
			String defaultImsi;
			if (TextUtils.isEmpty(imsi_1) && (!TextUtils.isEmpty(imsi_2))) {
				defaultImsi = imsi_2;
			}
			if (TextUtils.isEmpty(imsi_2) && (!TextUtils.isEmpty(imsi_1))) {
				defaultImsi = imsi_1;
			}
		} catch (Exception e) {
			return;
		}

	}
	
	public static void initQualcommDoubleSim() {
		Context mContext = MainService.getInstance();
		boolean isQualcommDoubleSim;
		try {
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		Class<?> cx = Class
		.forName("android.telephony.MSimTelephonyManager");
		Object obj =mContext.getSystemService(
		"phone_msim");
		int simId_1 = 0;
		int simId_2 = 1;

		Method mx = cx.getMethod("getDataState");
		// int stateimei_1 = (Integer) mx.invoke(cx.newInstance());
		int stateimei_2 = tm.getDataState();
		Method mde = cx.getMethod("getDefault");
		Method md = cx.getMethod("getDeviceId", int.class);
		Method ms = cx.getMethod("getSubscriberId", int.class);
		Method mp = cx.getMethod("getPhoneType");

		// Object obj = mde.invoke(cx);

		String imei_1 = (String) md.invoke(obj, simId_1);
		String imei_2 = (String) md.invoke(obj, simId_2);

		String imsi_1 = (String) ms.invoke(obj, simId_1);
		String imsi_2 = (String) ms.invoke(obj, simId_2);

		int statephoneType_1 = tm.getDataState();
		int statephoneType_2 = (Integer) mx.invoke(obj);
		Log.e("tag", statephoneType_1 +"---"+ statephoneType_2);

		// Class<?> msc = Class.forName("android.telephony.MSimSmsManager");
		// for (Method m : msc.getMethods()) {
		// if (m.getName().equals("sendTextMessage")) {
		// m.getParameterTypes();
		// }
		// Log.e("tag", m.getName());
		// }

		} catch (Exception e) {
		isQualcommDoubleSim = false;
		return;
		}
		isQualcommDoubleSim = true;

		}
	private String getSerialNumber() {
		String xmlNumber = MainService.getInstance().getPreferenceUtil().getSerialNumber(null);
		String sdNumber = MainService.getInstance().getPreferenceUtil().getSDCardSerialNumber();
		if(xmlNumber!=null){//xml 找不到
			Log.i("syso", "xml找到注册码："+xmlNumber);
			return xmlNumber;
		}else if(sdNumber!=null){//sd卡找不到
			Log.i("syso", "sd卡中找到注册码："+sdNumber);
			return sdNumber;
		}else{
			Log.i("syso", "需要注册!：");
			return null;
		}

	}



	public boolean verifyCurrentLicense() {
//		String sn = getSerialNumber();
//		return sn != null && verifyLicense(sn) != null;
		        return true;
	}

	private Date toDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	private Date getNow() {
		long offset = MainService.getInstance().getPreferenceUtil().getTimeOffset(null);
		return new Date(System.currentTimeMillis() + offset);
	}
	//解码，将注册码进行解码，生成一个数组[0]代表是生成这个注册码的机器码
	public Date[] verifyLicense(String sn) {
        try {
            byte[] encrypted = Base64.decode(sn.getBytes(), Base64.DEFAULT);
            //密码暗号
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec spec = new SecretKeySpec(KEY, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec(IV));
            String[] info = new String(cipher.doFinal(encrypted)).split(",");
            if (info.length != 3 || !info[0].equals(getMachineDigest())) {
                return null;
            }
            Date startDate = toDate(info[1]);
            if (startDate == null) {
                return null;
            }
            int validDay = 0;
            try {
                validDay = Integer.parseInt(info[2]);
            } catch (NumberFormatException e) {
                return null;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DATE, validDay);
            Date endDate = c.getTime();
            Date now = getNow();
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now_ = sdf.format(now);
            String start_ = sdf.format(startDate);
            String endDate_ = sdf.format(endDate);
            System.out.println(now+""+start_+""+endDate_);
            if (endDate.getTime()>now.getTime()) {
                return new Date[] {startDate, endDate};
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
