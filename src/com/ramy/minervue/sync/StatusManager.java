package com.ramy.minervue.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * 实现监控、录像、瓦斯检测的过程中，不予实现远程操作命令，给出状态应答。
 * 默认是false
 */
public class StatusManager {
	public static boolean monitor = false;
	public static boolean video = false;
	public static boolean gasPhoto = false;
	public static boolean remotePhoto = false;

	public static boolean isRemotePhoto() {
		return remotePhoto;
	}
	public static void setRemotePhoto(boolean remotePhoto) {
		StatusManager.remotePhoto = remotePhoto;
	}
	public static boolean isMonitor() {
		return monitor;
	}
	public static void setMonitor(boolean monitor) {
		if(monitor==true){
			//状态机  写入监控
			printStatu("tk_monitor:true");
		}else{
			//状态  恢复监控
			printStatu("tk_monitor:false");
		}
		StatusManager.monitor = monitor;
	}
	public static boolean isVideo() {
		return video;
	}
	public static void setVideo(boolean video) {
		if(video==true){
			//状态机  写入录像
			printStatu("tk_video:true");
		}else{
			//状态  恢复
			printStatu("tk_video:false");
		}
		StatusManager.video = video;
	}
	public static boolean isGasPhoto() {
		return gasPhoto;
	}
	public static void setGasPhoto(boolean gasPhoto) {
		StatusManager.gasPhoto = gasPhoto;
	}


	@SuppressWarnings("resource")
	public static HashMap<String, String> getTKStatu() {  
		HashMap<String, String> map = new HashMap<String, String>();
		InputStreamReader inputStreamReader = null;  
		try {  
			InputStream ism =  new FileInputStream(new File("sdcard/statu.txt")); 
			inputStreamReader = new InputStreamReader(ism, "gbk");  //编码gbk
			StringBuffer sb = new StringBuffer("");  //接收buferr
			String line;  //一次读一行
			BufferedReader reader = new BufferedReader(inputStreamReader);  
			while ((line = reader.readLine()) != null) {  
				sb.append(line);  //一次拼一行
			}  
			String [] conent  = sb.toString().split(":");//根据  ‘：’关键字分割
			if(conent.length==2){//通讯协议规定只可能分割2断 
				map.put("object", conent[0]);//状态
				map.put("statu",conent[1]);//只有 true&&false
			}else{
				return null;
			}
		} catch (IOException e) {  
			e.printStackTrace();  
			return null;
		}  
		return map;  
	} 

	public static void printStatu(String content){
		BufferedWriter out = null;  
		try {  
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/statu.txt", false)));  //false表示覆盖内容
			out.write(content);  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				out.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		} 
	}

}
