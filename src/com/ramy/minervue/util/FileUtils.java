package com.ramy.minervue.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import we_smart.com.data.PollingInfo;

import android.util.Log;

import com.ramy.minervue.bean.Dervic;

public class FileUtils {

	/**
	 * ��ȡ�ļ���׺��
	 * @param fileName
	 * @return �ļ���׺��
	 */
	public static String getFileType(String fileName) {
		if (fileName != null) {
			int typeIndex = fileName.lastIndexOf(".");
			if (typeIndex != -1) {
				String fileType = fileName.substring(typeIndex + 1)
						.toLowerCase();
				return fileType;
			}
		}
		return "";
	}

	public static String getPathFileName(String pathName){
		if(pathName!=null){
			int typeIndex  = pathName.lastIndexOf("/");
			if(typeIndex!=-1){
				return pathName.substring(typeIndex+1);
			}
		}
		return "";
	}
	/**
	 * ���ݺ�׺���ж��Ƿ���ͼƬ�ļ�
	 * 
	 * @param type
	 * @return �Ƿ���ͼƬ���true or false
	 */
	public static boolean isImage(String type) {
		if (type != null
				&& (type.equals("jpg") || type.equals("gif")
						|| type.equals("png") || type.equals("jpeg")
						|| type.equals("bmp") || type.equals("wbmp")
						|| type.equals("ico") || type.equals("jpe"))) {
			return true;
		}
		return false;
	}
	public static boolean isVedio(String type){
		if(type!=null && type.equals("mp4")
				&& type.equals("m4v")&& type.equals("3gp")&& type.equals("3gpp")
				&& type.equals("wmv")){
			return true;
		}
		return false;
	}





	/** 
	 * ���Ƶ����ļ� 
	 * @param oldPath String ԭ�ļ�·�� �磺c:/fqf.txt 
	 * @param newPath String ���ƺ�·�� �磺f:/fqf.txt 
	 * @return boolean 
	 * @throws IOException 
	 */ 
	public static void copyFile(String oldPath, String newPath) throws IOException { 
			int bytesum = 0; 
			int byteread = 0; 
			File oldfile = new File(oldPath); 
			if (oldfile.exists()) { //�ļ�����ʱ 
				InputStream inStream = new FileInputStream(oldPath); //����ԭ�ļ� 
				FileOutputStream fs = new FileOutputStream(newPath); 
				byte[] buffer = new byte[1444]; 
				int length; 
				while ( (byteread = inStream.read(buffer)) != -1) { 
					bytesum += byteread; //�ֽ��� �ļ���С 
					System.out.println(bytesum); 
					fs.write(buffer, 0, byteread); 
				} 
				inStream.close(); 
			} 
	} 

	/** 
	 * ���������ļ������� 
	 * @param oldPath String ԭ�ļ�·�� �磺c:/fqf 
	 * @param newPath String ���ƺ�·�� �磺f:/fqf/ff 
	 * @return boolean 
	 * @throws IOException 
	 */ 
	public static void copyFolder(String oldPath, String newPath) throws IOException { 

			(new File(newPath)).mkdirs(); //����ļ��в����� �������ļ��� 
			File a=new File(oldPath); 
			String[] file=a.list(); 
			File temp=null; 
			for (int i = 0; i < file.length; i++) { 
				if(oldPath.endsWith(File.separator)){ 
					temp=new File(oldPath+file[i]); 
				} 
				else{ 
					temp=new File(oldPath+File.separator+file[i]); 
				} 

				if(temp.isFile()){ 
					FileInputStream input = new FileInputStream(temp); 
					FileOutputStream output = new FileOutputStream(newPath + "/" + 
							(temp.getName()).toString()); 
					byte[] b = new byte[1024 * 5]; 
					int len; 
					while ( (len = input.read(b)) != -1) { 
						output.write(b, 0, len); 
					} 
					output.flush(); 
					output.close(); 
					input.close(); 
				} 
				if(temp.isDirectory()){//��������ļ��� 
					copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]); 
				} 
			} 

	}
	
	
	
	
	
	public static String readFile(String filePathAndName) {
		  String fileContent = "";
		  try {  
		   File f = new File(filePathAndName);
		   if(f.isFile()&&f.exists()){
		    InputStreamReader read = new InputStreamReader(new FileInputStream(f),"UTF-8");
		    BufferedReader reader=new BufferedReader(read);
		    String line;
		    while ((line = reader.readLine()) != null) {
		     fileContent += line;
		    }   
		    read.close();
		   }
		  } catch (Exception e) {
		   System.out.println("��ȡ�ļ����ݲ�������");
		   e.printStackTrace();
		  }
		  return fileContent;
		}
	
	
	public static void writeFile(String filePathAndName, String fileContent) {
		  try {
		   File f = new File(filePathAndName);
			File mk = new File(f.getParent());
			mk.mkdirs();
		   if (!f.exists()) {
		    f.createNewFile();
		   }
		   OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		   BufferedWriter writer=new BufferedWriter(write);   
		   //PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePathAndName)));
		   //PrintWriter writer = new PrintWriter(new FileWriter(filePathAndName));
		   writer.write(fileContent);
		   writer.close();
		  } catch (Exception e) {
		   System.out.println("д�ļ����ݲ�������");
		   e.printStackTrace();
		  }
	}
	

	public static String enCodeJson(List<PollingInfo> list){
		JSONArray array = new JSONArray();
		try {
			for(int i = 0 ; i < list.size();i++){
				JSONObject value = new JSONObject();
				value.put("Mac", list.get(i).mac);
				value.put("Des", list.get(i).name+"");
				value.put("Type", "blueTooth");
				value.put("Statu", "������");
				value.put("url", "Ĭ��");
				array.put(value);
			}
			Log.i("CameraZQL", array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array.toString();
	}
	
	
	
	
}
