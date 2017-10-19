/**
 *�ļ�������
 */
package com.lym.grivider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

public class FilePerate {
	List<String> fileList  = null;//�ļ����飬���ڴ�ŵ�ǰĿ¼��������Ŀ¼
	String currentPath = null;//��ǰĿ¼·��
	Context context = null;
	int fileNum = 0;//�ļ���Ŀ
	int folderNum = 0;//Ŀ¼�ļ�
	public FilePerate() {
		// TODO Auto-generated constructor stub
		fileList = new ArrayList<String>();
		currentPath = getRootFolder();//��ʼ����ǰĿ¼��ΪSD���ĸ�Ŀ¼
	}
	
	
	//�õ���Ŀ¼
	public static String getRootFolder(){
		if((Environment.getExternalStorageState()).equals(Environment.MEDIA_MOUNTED)){
			//���SD�����ڣ��򷵻�SD����Ŀ¼
			return Environment.getExternalStorageDirectory().getAbsolutePath();//�õ�SD����Ŀ¼��·��
		}else{
			return null;
		}
	}
	
	//ѡ���ΪĿ¼
	public List<String> selectFolder(String path){
		File folder = new File(path);
		File[] files = folder.listFiles();//�õ��������ļ���Ŀ¼
		fileList.clear();//����ļ��б�
		setFileNum(0);//���������ļ���Ŀ¼�ĸ���
		setFolderNum(0);
		//���������ļ��б�
		if(files != null){
			for(File file:files){
				if(file.isFile()){
					fileNum++;//ͳ�Ƶ�ǰ�ļ����µ��ļ�����
				}else{
					folderNum++;
				}
				fileList.add(file.getName());//���ļ������б�
			}
		}
		setCurrentFolder(path);//�������õ�ǰ·��
		return fileList;
	}
	//ѡ���Ϊ�ļ�
	public void selectFile(String path){
		System.out.println("ѡ�����ļ�");
	}
	//�õ���ǰĿ¼����Ŀ¼
	public List<String> getAllFile(String path){
		try {
			File filePath = new File(path);
			if(filePath.isFile()){
				//�����������ļ�,����ļ�����صĲ���
				selectFile(path);
				return null;
			}else{
				selectFolder(path);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileList;
	}
	
	
	
	//�õ��ϼ�Ŀ¼
	public String getParentFolder(String path){
		File folder = new File(path);
		if(!folder.equals(getRootFolder())){//������Ǹ�Ŀ¼�򷵻��ϼ�Ŀ¼��·��
			return folder.getParent();
		}
		return null;
	}
	
	//�õ���ǰ��·��
	public String getCurrentPath(){
		return currentPath;
	}
	
//	�õ���ǰ��·��
	public void setCurrentFolder(String path){
		currentPath = path;
	}
	
	//�õ��ļ��б�
	public List<String> getFileList() {
		return fileList;
	}

	//�õ��ļ�����
	public int getFileNum() {
		return fileNum;
	}
	
	//�����ļ�����
	public void setFileNum(int num) {
		this.fileNum = num;
	}

	public int getFolderNum() {
		return folderNum;
	}
	//����Ŀ¼����
	public void setFolderNum(int num) {
		this.folderNum = num;
	}

	//�õ��ļ���Ŀ¼�����б�
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	
}
