/**
 *文件操作类
 */
package com.lym.grivider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

public class FilePerate {
	List<String> fileList  = null;//文件数组，用于存放当前目录的所有子目录
	String currentPath = null;//当前目录路径
	Context context = null;
	int fileNum = 0;//文件数目
	int folderNum = 0;//目录文件
	public FilePerate() {
		// TODO Auto-generated constructor stub
		fileList = new ArrayList<String>();
		currentPath = getRootFolder();//初始化当前目录，为SD卡的根目录
	}
	
	
	//得到根目录
	public static String getRootFolder(){
		if((Environment.getExternalStorageState()).equals(Environment.MEDIA_MOUNTED)){
			//如果SD卡存在，则返回SD卡的目录
			return Environment.getExternalStorageDirectory().getAbsolutePath();//得到SD卡的目录的路径
		}else{
			return null;
		}
	}
	
	//选择的为目录
	public List<String> selectFolder(String path){
		File folder = new File(path);
		File[] files = folder.listFiles();//得到所有子文件和目录
		fileList.clear();//清空文件列表
		setFileNum(0);//重新设置文件和目录的个数
		setFolderNum(0);
		//重新生成文件列表
		if(files != null){
			for(File file:files){
				if(file.isFile()){
					fileNum++;//统计当前文件夹下的文件个数
				}else{
					folderNum++;
				}
				fileList.add(file.getName());//将文件加入列表
			}
		}
		setCurrentFolder(path);//重新设置当前路径
		return fileList;
	}
	//选择的为文件
	public void selectFile(String path){
		System.out.println("选中了文件");
	}
	//得到当前目录的子目录
	public List<String> getAllFile(String path){
		try {
			File filePath = new File(path);
			if(filePath.isFile()){
				//如果点击的是文件,则对文件做相关的操作
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
	
	
	
	//得到上级目录
	public String getParentFolder(String path){
		File folder = new File(path);
		if(!folder.equals(getRootFolder())){//如果不是根目录则返回上级目录的路劲
			return folder.getParent();
		}
		return null;
	}
	
	//得到当前的路劲
	public String getCurrentPath(){
		return currentPath;
	}
	
//	得到当前的路劲
	public void setCurrentFolder(String path){
		currentPath = path;
	}
	
	//得到文件列表
	public List<String> getFileList() {
		return fileList;
	}

	//得到文件个数
	public int getFileNum() {
		return fileNum;
	}
	
	//设置文件个数
	public void setFileNum(int num) {
		this.fileNum = num;
	}

	public int getFolderNum() {
		return folderNum;
	}
	//设置目录个数
	public void setFolderNum(int num) {
		this.folderNum = num;
	}

	//得到文件和目录名的列表
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	
}
