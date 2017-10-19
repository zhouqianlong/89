package com.bhj.staticwifisetting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.sql.Driver;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

//��̬IP������
public class StaticIpSet {
	// �豸�Ļ���
	Context mContext;

	String ipAddress = "192.168.1.148";
	int preLength = 24;
	String getWay = "192.168.1.1";
	String dns1 = "192.168.1.1";

	public StaticIpSet(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	public StaticIpSet(Context mContext, String ipAddress, int preLength,
			String getWay, String dns1) {
		super();
		this.mContext = mContext;
		this.ipAddress = ipAddress;
		this.preLength = preLength;
		this.getWay = getWay;
		this.dns1 = dns1;
	}

	// ����wifi�ľ�̬ip
	public void confingStaticIp(WifiConfiguration wifiConfig) {
		// �����android2.x�汾�Ļ�
		if (android.os.Build.VERSION.SDK_INT < 11) {
			ContentResolver ctRes = mContext.getContentResolver();
			Settings.System
					.putInt(ctRes, Settings.System.WIFI_USE_STATIC_IP, 1);
			Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_IP,
					ipAddress);
			Settings.System.putString(ctRes,
					Settings.System.WIFI_STATIC_NETMASK, "255.255.0.0");
			Settings.System.putString(ctRes,
					Settings.System.WIFI_STATIC_GATEWAY, getWay);
			Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS1,
					dns1);
			Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS2,
					"61.134.1.9");
		}
		// �����android3.x�汾�����ϵĻ�
		else {
			try {
				setIpType("STATIC", wifiConfig);
				setIpAddress(InetAddress.getByName(ipAddress), preLength,
						wifiConfig);
				setGateway(InetAddress.getByName(getWay), wifiConfig);
				setDNS(InetAddress.getByName(dns1), wifiConfig);
//				Log.i("123", "��̬ip���óɹ���");
//				Toast.makeText(mContext, "��̬ip���óɹ���", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
//				Log.i("123", "��̬ip����ʧ�ܣ�");
//				Toast.makeText(mContext, "��̬ip����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * ����DNS
	 * 
	 * @param byName
	 *            ����
	 * @param wifiConfig
	 *            ������wifi���ö���
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 */
	private void setDNS(InetAddress dns, WifiConfiguration wifiConfig)
			throws Exception {
		// ���wifiConfig��linkProperties�������Լ����е�ֵ
		Object linkProperties = getFieldValue(wifiConfig, "linkProperties");
		if (linkProperties == null) {
			return;
		}
		ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(
				linkProperties, "mDnses");
		mDnses.clear(); // ���ԭ��DNS���ã����ֻ�����ӣ�����������˾��ʡ�ԣ�
		mDnses.add(dns); // �����µ�DNS
	}

	/**
	 * ��������
	 * 
	 * @param gateWay
	 *            ����
	 * @param wifiConfig
	 *            ������wifi���ö���
	 */
	private void setGateway(InetAddress gateWay, WifiConfiguration wifiConfig)
			throws Exception {
		// ���wifiConfig��linkProperties�������Լ����е�ֵ
		Object linkProperties = getFieldValue(wifiConfig, "linkProperties");
		if (linkProperties == null) {
			return;
		}
		// android4.x�汾
		if (android.os.Build.VERSION.SDK_INT >= 14) {

			// �����·����Ϣ��
			Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
			// ���·����Ϣ���һ��������������������
			Constructor<?> routeInfoConstructor = routeInfoClass
					.getConstructor(new Class[] { InetAddress.class });
			// ����ָ�����ص�·����Ϣ�����
			Object routeInfo = routeInfoConstructor.newInstance(gateWay);
			ArrayList<Object> routes = (ArrayList<Object>) getDeclaredField(
					linkProperties, "mRoutes");
			routes.clear();
			routes.add(routeInfo);
		}
		// android3.x�汾
		else {
			ArrayList<InetAddress> gateWays = (ArrayList<InetAddress>) getDeclaredField(
					linkProperties, "mGateWays");
			gateWays.clear();
			gateWays.add(gateWay);
		}
	}

	/**
	 * ����IP��ַ
	 * 
	 * @param ipAddress
	 *            IP��ַ
	 * @param preLength
	 *            ǰ׺����
	 * @param wifiConfig
	 *            ������wifi���ö���
	 */
	private void setIpAddress(InetAddress ipAddress, int preLength,
			WifiConfiguration wifiConfig) throws Exception {
		// ���wifiConfig��linkProperties�������Լ����е�ֵ
		Object linkProperties = getFieldValue(wifiConfig, "linkProperties");
		
		
		
		
		
		if (linkProperties == null) {
			return;
		}
		// ���һ��LinkAddress���ӵ�ַ��
		Class<?> linkAddressClass = Class.forName("android.net.LinkAddress");
		// ���LinkAddress��һ��������,����Ϊip��ַ��ǰ׺����
		Constructor<?> linkAddressConstrcutor = linkAddressClass
				.getConstructor(new Class[] { InetAddress.class, int.class });
		// ͨ���ù��������һ��LinkAddress����
		Object linkAddress = linkAddressConstrcutor.newInstance(ipAddress,
				preLength);
		// ���linkProperties�������Լ�����linkAddresses���ӵ�ַ�����е�ֵ
		ArrayList<Object> linkAddresses = (ArrayList<Object>) getDeclaredField(
				linkProperties, "mLinkAddresses");
		// ���linkAddresses
		linkAddresses.clear();
		// ����û����õ�linkAddress���ӵ�ַ��
		linkAddresses.add(linkAddress);
	}

	/**
	 * ���ĳ������ָ�������е�ֵ���������Ǳ��������ġ�
	 * 
	 * @param obj
	 *            ����
	 * @param name
	 *            ������
	 * @return ���ظö����и������е�ֵ
	 * @throws Exception
	 */
	private Object getDeclaredField(Object obj, String name) throws Exception {
		// ��obj����ָ��������Ϊname������
		Field f = obj.getClass().getDeclaredField(name);
		// ���ø�������Ա�����
		f.setAccessible(true);
		return f.get(obj);
	}

	/**
	 * ���ĳ������ָ�������е�ֵ�����ݶ��󣬺���������
	 * 
	 * @param obj
	 *            ����
	 * @param name
	 *            ������
	 * @return ���ظö�������Ϊname�����е�ֵ
	 * @throws Exception
	 */
	private Object getFieldValue(Object obj, String name) throws Exception {
		// ���obj���࣬�ٻ��������Ϊname������
		Field f = obj.getClass().getField(name);
		// ����obj��f�����е�ֵ
		return f.get(obj);
	}

	/**
	 * ����IP��ַ����
	 * 
	 * @param ipType
	 *            IP��ַ����
	 * @param wifiConfig
	 *            ������wifi���ö���
	 * @throws Exception
	 */
	private void setIpType(String ipType, WifiConfiguration wifiConfig)
			throws Exception {
		Field f = wifiConfig.getClass().getField("ipAssignment");
		f.set(wifiConfig, Enum.valueOf((Class<Enum>) f.getType(), ipType));
	}

}
