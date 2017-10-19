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
 * ʵ�ּ�ء�¼����˹���Ĺ����У�����ʵ��Զ�̲����������״̬Ӧ��
 * Ĭ����false
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
			//״̬��  д����
			printStatu("tk_monitor:true");
		}else{
			//״̬  �ָ����
			printStatu("tk_monitor:false");
		}
		StatusManager.monitor = monitor;
	}
	public static boolean isVideo() {
		return video;
	}
	public static void setVideo(boolean video) {
		if(video==true){
			//״̬��  д��¼��
			printStatu("tk_video:true");
		}else{
			//״̬  �ָ�
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
			inputStreamReader = new InputStreamReader(ism, "gbk");  //����gbk
			StringBuffer sb = new StringBuffer("");  //����buferr
			String line;  //һ�ζ�һ��
			BufferedReader reader = new BufferedReader(inputStreamReader);  
			while ((line = reader.readLine()) != null) {  
				sb.append(line);  //һ��ƴһ��
			}  
			String [] conent  = sb.toString().split(":");//����  �������ؼ��ַָ�
			if(conent.length==2){//ͨѶЭ��涨ֻ���ָܷ�2�� 
				map.put("object", conent[0]);//״̬
				map.put("statu",conent[1]);//ֻ�� true&&false
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
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/statu.txt", false)));  //false��ʾ��������
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
