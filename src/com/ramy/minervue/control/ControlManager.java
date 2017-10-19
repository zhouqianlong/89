package com.ramy.minervue.control;

import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.bean.Dervic;
import com.ramy.minervue.media.Monitor;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.ConfigUtil;
import com.ramy.minervue.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peter on 11/7/13.
 */
public class ControlManager implements ConfigUtil.PreferenceListener {
	private static final String TAG = "RAMY-ControlManager";

	private KeepAlive keepAlive = null;

	private DatagramChannel datagramChannel;

	private HashMap<String, LinkedList<PacketListener>> listenerMap = new HashMap<String, LinkedList<PacketListener>>();

	private ConfigUtil configUtil;
	private ByteBuffer buffer = ByteBuffer.wrap(new byte[65536]);
	private Receiver receiver;

	private int noResponseCount = 0;
	private int noVideoResponseCount = 0;
	public ControlManager() {
		configUtil = MainService.getInstance().getConfigUtil();
		configUtil.addListener(ConfigUtil.ConfigType.KEY_SERVER_ADDR, this);
	}

	public void onKeepAliveSent() {
		++noResponseCount;
		//		MainService.getInstance().isBackground(MainService.getInstance().getApplicationContext());
		if (noResponseCount > 3) {
			MainService.getInstance().updateServerOnline(false);
			Log.i(TAG, "在线心跳包，大于3次结束为离线："+noResponseCount);

			if(noResponseCount ==40){
				if(MainActivity.getGetInstance()!=null){
					MainActivity.getGetInstance().restartWifi(1000);
					Log.i("TEST", "重启在线心跳包，大于3次结束为离线");
				}
			}
		}
	}




	public void onKeepVideoAliveSent() {
		++noVideoResponseCount;
		Log.i(TAG, "监控中，等待服务器心跳包，大于20次结束："+noVideoResponseCount);
		if (noVideoResponseCount >=20) {
			Log.i(TAG, "监控异常大于20次结束："+noVideoResponseCount);
			MainService.getInstance().videoStop();
			noVideoResponseCount = 0;
			//			noResponseCount= 0;
		}
	}

	public void onKeepAliveReceived() {
		noResponseCount = 0;
		MainService.getInstance().updateServerOnline(true);
	}
	private void onKeepAliveVideoReceived() {
		noVideoResponseCount = 0;
	}
	public void updateWifiAvailability(boolean available) {
		if (available) {
			startReceiver();
			startKeepAlive();
		} 
		else {
			stopReceiver();
			stopKeepAlive();
		}
	}

	private synchronized void initChannel(String serverAddr, int controlPort) {
		if(!isboolIP(serverAddr)){
			Log.i(TAG, "你大爷！ip设置错误了");
			return;
		}else{
			Log.i(TAG, "正确ip地址"+serverAddr);
		}

		try {
			SocketAddress remoteAddr = new InetSocketAddress(serverAddr, controlPort);
			SocketAddress boundPort = new InetSocketAddress(controlPort);
			datagramChannel = DatagramChannel.open();
			datagramChannel.socket().bind(boundPort);
			datagramChannel.connect(remoteAddr);
			Log.v(TAG, "Control channel " + serverAddr + ":" + controlPort
					+ ", connected: " + datagramChannel.isConnected() + ".");
		} catch (Exception e) {
			//			initChannel(serverAddr, controlPort);
			datagramChannel = null;
		}
	}


	public static boolean isboolIP(String ipAddress){ 

		String ip="(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"; 
		Pattern pattern = Pattern.compile(ip); 
		Matcher matcher = pattern.matcher(ipAddress); 
		return matcher.matches(); 
	} 
	public void release() {
		stopKeepAlive();
		stopReceiver();
		configUtil.removeListener(ConfigUtil.ConfigType.KEY_SERVER_ADDR, this);
	}

	private synchronized void closeChannel() {
		if (datagramChannel != null) {
			try {
				datagramChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			datagramChannel = null;
		}
	}

	public synchronized void sendPacket(JSONObject json) {


		if (datagramChannel != null && datagramChannel.isConnected()) {
			try {
				if(MainActivity.getInstance!=null){
					if(MainActivity.getInstance.isSendWifiData()){
						datagramChannel.write(ByteBuffer.wrap(json.toString().getBytes()));
					}
				}
			} catch (IOException e) {
				Log.w(TAG, "Send packet failed, trying re-establishment.");
			}
		}
	}
	 

	public QueryTask createQueryTask(JSONObject json, PacketListener listener, int repeat) {
		return new QueryTask(this, json, listener, repeat);
	}

	public void addPacketListener(PacketListener listener) {
		if (listener == null) {
			return;
		}
		LinkedList<PacketListener> list = listenerMap.get(listener.getPacketType());
		if (list == null) {
			list = new LinkedList<PacketListener>();
			listenerMap.put(listener.getPacketType(), list);
		}
		list.add(listener);
	}

	public void removePacketListener(PacketListener listener) {
		LinkedList<PacketListener> list = listenerMap.get(listener.getPacketType());
		if (list != null && listener != null) {
			list.remove(listener);
		}
	}

	public void startKeepAlive() {
		if (keepAlive == null || keepAlive.isFinished()) {
			keepAlive = new KeepAlive(this);
			keepAlive.start();
			Log.v(TAG, "Keep-alive started.");
		}
	}

	public void startReceiver() {
		if (receiver == null || receiver.isFinished()) {
			receiver = new Receiver();
			receiver.start();
			Log.v(TAG, "Controller started listening.");
		}
	}

	public void stopKeepAlive() {
		if (keepAlive != null) {
			keepAlive.cancelAndClear(true);
			keepAlive = null;
			Log.v(TAG, "Keep-alive exited.");
		}
	}

	public void stopReceiver() {
		if (receiver != null) {
			receiver.cancelAndClear(true);
			receiver = null;
			Log.v(TAG, "Controller stopped listening.");
		}
	}

	@Override
	public void onPreferenceChanged() {
		if (receiver != null) {
			Log.i(TAG, "Server changed, restarting.");
			stopReceiver();
			startReceiver();
		}
	}

	public interface PacketListener {
		public void onPacketArrival(JSONObject packet);
		public String getPacketType();
	}
	int keepcount = 0;
	int count;
	private class Receiver extends ATask<Void, JSONObject, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String serverAddr = configUtil.getServerAddr();
			int controlPort = configUtil.getPortControl();
			initChannel(serverAddr, controlPort);
			while (!isCancelled()) {
				buffer.clear();
				try {
					count = datagramChannel.read(buffer);//阅读服务端的协议
					if (count < 0) {
						continue;
					}
					String s = new String(buffer.array(), 0, count);    // Safe since it's wrapped.
					Log.i(TAG, s);
					JSONObject json = new JSONObject(s);//[{"uuid":"ym89","action":"intercom","result":"192.168.1.125"},{"uuid":"ym8955","action":"intercom","result":"192.168.1.124"}]
					publishProgress(json);
				} catch (ClosedByInterruptException e) {
					break;
				} catch (IOException e) {
					// Ignored.
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
				}
			}
			closeChannel();
			return super.doInBackground();
		}
		@Override
		protected void onProgressUpdate(JSONObject... values) {
			String type = null;
			try {
				//  [{"uuid":"ym89","action":"intercom","result":"192.168.1.125"},{"uuid":"ym8955","action":"intercom","result":"192.168.1.124"}]
				type = values[0].getString("action");//[{"action":"video-start","result":"requested-video"}]
			} catch (JSONException e) {
				return;
			}
			LinkedList<PacketListener> listeners = listenerMap.get(type);
			if (listeners != null) {
				for (PacketListener listener : listeners) {
					listener.onPacketArrival(values[0]);
				}
			}
			if (type.equals("keep-alive")) {
				onKeepAliveReceived();
				if(MainService.MONITOR_STATU==true){
					keepcount++;
					Log.e(TAG, "服务器没有监控！关闭本地监控！");
					if(keepcount>=3){
						LinkedList<PacketListener> stoplisteners = listenerMap.get("video-stop");
						if (listeners != null) {
							for (PacketListener listener : stoplisteners) {
								listener.onPacketArrival(values[0]);
								Monitor.printStatu("服务器没有监控！关闭本地监控! keepcount:"+keepcount);
								Log.e(TAG, "服务器没有监控！关闭本地监控！end");
							}
						}
						keepcount =0;
					}
				}else{
					keepcount =0;
				}
			}if(type.equals("keep-alive-video")){
				//监控
				keepcount = 0;
				noVideoResponseCount= 0;
				Log.d(TAG, "收到服务器监控的心跳包."+noVideoResponseCount);
				onKeepAliveVideoReceived();
				onKeepAliveReceived();
			}
		}


		@Override
		protected void onPostExecute(Void aVoid) {
			Log.d(TAG, "Receiver terminated unexpectedly.");
			startReceiver();
			//	       	startKeepAlive();
		}
	}
	public void killKeepCount() {
		keepcount = 0;
	}

}
