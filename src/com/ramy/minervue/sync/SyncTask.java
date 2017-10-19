package com.ramy.minervue.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.PreferenceUtil;

/**
 * Created by peter on 11/25/13.
 */
public class SyncTask extends ATask<Void, Void, Boolean> {

	private static final String TAG = "RAMY-SyncTask";
	private static final String LOGIN_NAME = "ramy";
	private static final String LOGIN_PASS = "ramy";
	private static final String DIR_INCOMING = "incoming";
	public static boolean FTP_CONNE_STATU = false;// FTP登陆状态
	private FTPClient client = null;
	private String serverAddr;
	private LocalFileUtil localFileUtil;
	private boolean hasTask = false;
	public int threadNumber = 0;

	public SyncTask(LocalFileUtil localFileUtil, String serverAddr) {
		this.serverAddr = serverAddr;
		this.localFileUtil = localFileUtil;
		threadNumber = (int) (Math.random() * 90000 + 10000);
	}

	private HashMap<String, Long> toFileMap(FTPFile[] files) {
		HashMap<String, Long> ret = new HashMap<String, Long>(files.length);
		if (files != null) {
			for (FTPFile file : files) {
				if (file != null && file.isFile()) {
					ret.put(file.getName(), file.getSize());
				}
			}
		}
		return ret;
	}

	private HashMap<String, Long> toFileMap(File[] files) {
		if (files == null) {
			return null;
		}
		HashMap<String, Long> ret = new HashMap<String, Long>(files.length);
		if (files != null) {
			for (File file : files) {
				if (file != null && file.isFile()
						&& !LocalFileUtil.isFileInUse(file)) {
					ret.put(file.getName(), file.length());
				}
			}
		}
		return ret;
	} 
	private List<Task> generateTasks(HashMap<String, Long> server,HashMap<String, Long> sdcard) {
		LinkedList<Task> ret = new LinkedList<Task>();
			for (String file : server.keySet()) {// 获取服务器文件
				Long toSize = server.get(file);// 服务器文件大小
				if (toSize!= null) {//服务器文件存在
					Long sdSzie = sdcard.get(file);
					if(sdSzie==null){//本地第一次下载
						ret.add(new Task(file, 0, false));
					}else{
						Long TmpsdSzie = sdcard.get(file + ".tmp");	
						if(TmpsdSzie!=null){
							if(server.get(file).compareTo(TmpsdSzie) >= 0){
								ret.add(new Task(file, toSize, false));
							}
						}						
						if(file.equals("list.txt")){
							ret.add(new Task(file, 0, false));
						}
					}
				}
			}
			Log.i(TAG, "需要下载：" + ret.size() + "个文件   线程号：" + threadNumber);
		hasTask = true;
		return ret;
	}
	
	
	
	
	private List<Task> generateTasks(HashMap<String, Long> phoneFile,HashMap<String, Long> serverFile, boolean isDownload) {
		LinkedList<Task> ret = new LinkedList<Task>();
		if (isDownload) {// 下载服务器文件
			for (String file : phoneFile.keySet()) {// 获取server下发的每一个文件
				Long toSize = serverFile.get(file);// 查看本地文件是否存在过这个文件
				if (toSize != null) {// 本地存在该文件
					ret.add(new Task(file, 0, true));// 下发文件 数量显示错误 q
				} else {
					toSize = serverFile.get(file + ".tmp");
					if (toSize == null) {
						ret.add(new Task(file, 0, false));
					} else {
						if (phoneFile.get(file).compareTo(toSize) >= 0) {
							ret.add(new Task(file, toSize, false));
						}
					}
				}
			}
			Log.i(TAG, "需要下载：" + ret.size() + "个文件   线程号：" + threadNumber);
		} else {// 上传文件到服务器
			for (String file : phoneFile.keySet()) {
				Long toSize = serverFile.get(file);
				if (toSize != null && toSize.equals(phoneFile.get(file))) {
					//					Long tmpsize ;
					if(serverFile.get(file + ".tmp")!=null){
						try {
							client.deleteFile(file + ".tmp");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					continue;
				}
				toSize = serverFile.get(file + ".tmp");
				if (toSize == null) {
					ret.add(new Task(file, 0, false));
				} else {
					if (phoneFile.get(file).compareTo(toSize) >= 0) {
						ret.add(new Task(file, toSize, false));
					}
				}
			}
			Log.i(TAG, "需要上传：" + ret.size() + "个文件   线程号：" + threadNumber);
		}
		hasTask = true;
		return ret;
	}

	// 下载

	private boolean downloadDirectory(String dirName) {
		if (!makeEnter(dirName)) {
			disconnect();
			return false;
		}
		try {
			HashMap<String, Long> localMap = toFileMap(localFileUtil
					.listFiles(dirName));// 获取本地下发目录的文件信息
			HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取server下发文件信息
			if (localMap == null || remoteMap == null) {
				return false;
			}
			List<Task> tasks = generateTasks(remoteMap, localMap, true);
			for (Task task : tasks) {
				download(dirName, task);
			}
		} catch (IOException e) {
			disconnect();
			return false;
		}
		return enterParent();
	}

	private void downWorkload(String dirName, Task task) throws IOException {
		Log.e(TAG, "准备下载" + task.fileName + "...");
		File file = new File(localFileUtil.getDir(dirName), task.fileName
				+ ".tmp");
		client.setFileType(FTP.BINARY_FILE_TYPE);
		BufferedOutputStream output = new BufferedOutputStream(
				new FileOutputStream(file, true));
		if (task.offset > 0) {
			client.setRestartOffset(task.offset);
		}
		client.enterLocalPassiveMode();
		client.retrieveFile(task.fileName, output);
		output.close();
		if (checkFileLen(client.listFiles(task.fileName), file)) {// 验证文件长度是否一致
			client.deleteFile(task.fileName);
			if (task.append) {
				//				boolean statu = 
				LocalFileUtil.delete(new File(localFileUtil
						.getDir(dirName), task.fileName));
				//				Log.i(TAG, "删除文件：" + statu);
			}
			file.renameTo(new File(localFileUtil.getDir(dirName), task.fileName));
			// Log.i(TAG,
			// "修改名称："+file.getPath()+"==========================================改名："+file.renameTo(new
			// File(localFileUtil.getDir(dirName), task.fileName)));
			PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
			 
			
			if(task.fileName.equals("list.txt")){
				util.setWorkDownload(util.getWorkDownload(null) + 1);// 正常文件
//				util.playSounds(1, 0);
				  Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  
                  Ringtone r = RingtoneManager.getRingtone(MainService.getInstance(), notification);  
                  r.play();   
				String path = file.getPath().substring(0,
						file.getPath().length() - 4);
				if (util.findWorkFileIsRead(path)) {
					util.setWorkDownload(util.getWorkDownload(null) + 1);
					util.deleteWorkDB(path);
				}
			}
		} else {
			file.delete();// 删除sd卡文件
		}
	}
	private void download(String dirName, Task task) throws IOException {
		Log.e(TAG, "准备下载" + task.fileName + "...");
		File file = new File(localFileUtil.getDir(dirName), task.fileName
				+ ".tmp");
		client.setFileType(FTP.BINARY_FILE_TYPE);
		BufferedOutputStream output = new BufferedOutputStream(
				new FileOutputStream(file, true));
		if (task.offset > 0) {
			client.setRestartOffset(task.offset);
		}
		client.enterLocalPassiveMode();
		client.retrieveFile(task.fileName, output);
		output.close();
		if (checkFileLen(client.listFiles(task.fileName), file)) {// 验证文件长度是否一致
			client.deleteFile(task.fileName);
			if (task.append) {
				//				boolean statu = 
				LocalFileUtil.delete(new File(localFileUtil
						.getDir(dirName), task.fileName));
				//				Log.i(TAG, "删除文件：" + statu);
			}
			file.renameTo(new File(localFileUtil.getDir(dirName), task.fileName));
			// Log.i(TAG,
			// "修改名称："+file.getPath()+"==========================================改名："+file.renameTo(new
			// File(localFileUtil.getDir(dirName), task.fileName)));
			PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
			if (task.append == false) {
				// Log.i(TAG, "下载成功！(第一次下发该文件)"+dirName);
				util.setUnreadDownload(util.getUnreadDownload(null) + 1);// 正常文件
			} else {// 再次下发相同文件
				// 查看文件是否已读过 （已读过的文件，显示会少一个，需要重新加一个显示）
				String path = file.getPath().substring(0,
						file.getPath().length() - 4);
				if (util.findFileIsRead(path)) {
					util.setUnreadDownload(util.getUnreadDownload(null) + 1);
					util.deleteDB(path);
				}
			}
			if (!StatusManager.isMonitor() && !StatusManager.isVideo()
					&& !StatusManager.isGasPhoto()
					&& !StatusManager.isRemotePhoto()) {
//				util.playSounds(1, 0);
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  
                Ringtone r = RingtoneManager.getRingtone(MainService.getInstance(), notification);  
                r.play();
                MainService.getInstance().dervicList = null;
			}
		} else {
			file.delete();// 删除sd卡文件
		}
	}

	public boolean checkFileLen(FTPFile[] ftpFile, File sdFile) {
		if (ftpFile.length != 1) {
			Log.e(TAG, "FTP文件为空");
			return false;
		} else if (ftpFile[0].getSize() == sdFile.length()) {
			Log.i(TAG, "文件长度一致:" + sdFile.length());
			return true;
		} else {
			Log.e(TAG,
					"文件长度不一致 ftp:" + ftpFile[0].getSize() + ", sd:"
							+ sdFile.length());
			return false;
		}
	}

	public void updateTempMp4(HashMap<String, Long> localMap) {

	}
	private boolean uploadDirectory(String dirName) {
		//		if (!MainService.getInstance().getServerOnlineStatus()) {
		//			return false;
		//		}
		if (!makeEnter(dirName)) {//在FTP下切换到dirName目录
			disconnect();
			return false;
		}
		try { 
			HashMap<String, Long> localMap = toFileMap(localFileUtil
					.listFiles(dirName));//获取 dirName 当前目录下所有文件
			HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取服务器当前目录下所有文件
			if (localMap == null || remoteMap == null) {
				return false;
			}
			Iterator<Map.Entry<String, Long>> iter = localMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				String file = iter.next().getKey();
				if (file.charAt(1) != '-') {
					Log.v(TAG, "Ignore file without priority: " + file + ".");
					iter.remove();
				}
			}
			List<Task> tasks = generateTasks(localMap, remoteMap, false);
			for (Task task : tasks) {
				upload(dirName, task);
			}
		} catch (IOException e) {
			disconnect();
			return false;
		}
		return enterParent();
	}

	private synchronized void upload(String dirName, Task task)throws IOException {
		if (MainService.MONITOR_STATU == true) {
			return;
		}
		boolean fileCheck = false;
		File file = new File(localFileUtil.getDir(dirName), task.fileName);
		client.setFileType(FTP.BINARY_FILE_TYPE);
		FileInputStream fStream = new FileInputStream(file);
		if (dirName.equals("video")) {
			FileInputStream in = null;
			try {
				Thread.sleep(100);
				byte[] tempbytes = new byte[50];
				int byteread = 0;
				in = new FileInputStream(file);
				while ((byteread = in.read(tempbytes)) != -1) {// 循环读取//TODO
					// ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
					if (byteread == 50) {
						if (tempbytes[40] == 0 && tempbytes[41] == 0
								&& tempbytes[42] == 0 && tempbytes[43] == 0) {
							fileCheck = false;
							Thread.sleep(500);
							break;
						} else {
							fileCheck = true;
							// Log.i(TAG, "上传验证视频是否存在：FTP目录切换成功!");
							// HashMap<String, Long> remoteMap =
							// toFileMap(client.listFiles());//{}
							// Log.i(TAG, "FTP当前用户上传所有文件数："+remoteMap.size());
							// for(String
							// remoteFileName:remoteMap.keySet()){//判断服务器下发文件，本地是否存在
							// if( task.fileName.equals(remoteFileName)){
							// Log.e(TAG, remoteFileName+"此文件存在,取消上传");
							// fileCheck = false;
							// }
							// }
							break;
						}
					}
				}
				in.close();
				in = null;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
					}
				}
			}
			if (!fileCheck) {// 文件校验 0000 不上传
				return;
			}
		}

		BufferedInputStream input = new BufferedInputStream(fStream);
		if (task.offset < file.length()) {// file.length : 18779853
			// offset:18779853
			// Log.i(TAG,
			// "000000发现有未上传完整的文件："+task.fileName+"\t传输进度："+task.offset);
			//			input.skip(file.length()-1);
			client.setRestartOffset(task.offset);
		}
		client.enterLocalPassiveMode();// src 10M 5
		// Log.i(TAG, "1111文件："+task.fileName+"\t传输进度："+task.offset);
		if (task.offset < file.length()) {
			try {
				input.skip(task.offset);
				 FTPFile[] list_file = client.listFiles();
				 for(int f = 0; f< list_file.length;f++){
					 if(list_file[f].getName().equals("list.txt")){
						 boolean bol  = client.deleteFile("list.txt");
						 Log.i(TAG, "删除list.txt文件"+bol);
					 }
				 }
				
				client.storeFile(task.fileName + ".tmp", input);
			} finally {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fStream.close();
				input.close();
			}
		}
		FTPFile[] serverFile = client.listFiles(task.fileName + ".tmp");// 获取Serve文件信息
		for (int i = 0; i < 5; i++) {
			if (serverFile.length == 0) {
				try {
					Thread.sleep(1000);
					serverFile = client.listFiles(task.fileName + ".tmp");// 获取Serve文件信息
					if (serverFile.length > 0) {// 搜索到文件
						break;// 退出 上传
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		if (serverFile.length == 0) {
			return;
		}
		Log.i(TAG, "延迟1秒上传文件");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean boorename = client.rename(task.fileName + ".tmp",
				task.fileName);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "Uploaded: " + task.fileName + ".");
	}

	private boolean connect() {
		for (int i = 0; i < 20; ++i) {
			try {
				client = new FTPClient();
				client.setControlEncoding("UTF-8");
				client.setDataTimeout(60000); // 设置传输超时时间为60秒
				client.setConnectTimeout(60000); // 连接超时为60秒
				Log.i(TAG, "Connecting to " + serverAddr + "...");
				client.connect(InetAddress.getByName(serverAddr));
				Log.i(TAG, "服务器返回值：" + client.getReplyCode());
				if (client.login(LOGIN_NAME, LOGIN_PASS)) {

					return true;
				} else {
					disconnect();
				}
			} catch (IOException e) {
				disconnect();
			}
			if (i < 3) {
				Log.i(TAG, "Retry in 3 seconds...");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					return false;
				}
			} else {
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					return false;
				}
			}
		}
		return false;
	}

	private boolean makeEnter(String dirName) {
		if (dirName == null) {
			disconnect();
			return false;
		}
		try {
			client.makeDirectory(dirName);
			if (!client.changeWorkingDirectory(dirName)) {
				disconnect();
				return false;
			}
			return true;
		} catch (IOException e) {
			disconnect();
			return false;
		}
	}

	private boolean enterParent() {
		try {
			client.changeToParentDirectory();
			return true;
		} catch (Exception e) {
			disconnect();
			return false;
		}
	}
	private void disconnect() {
		if (client != null) {
			FTP_CONNE_STATU = false;// FTP退出登陆
			try {
				boolean logoutsta = client.logout();
				client.disconnect();
				Log.i(TAG, "11111111111111111FTp退出   :" + logoutsta);
			} catch (IOException e) {
				// Expected.
				Log.i(TAG, "33333333333333退出:  服务端地址：" + serverAddr);
				try {
					client.logout();
					client.disconnect();
				} catch (IOException e1) {
					try {
						client.logout();
						client.disconnect();
					} catch (IOException e2) {
						//
					}
				}
			}
			client = null;
			Log.i(TAG, "22222222222222222client==null");
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if (!connect()) {
			Log.e(TAG, "Failed to connect.");
			return false;
		} else {
			FTP_CONNE_STATU = true;// FTP登陆成功
		}
		String uuid = MainService.getInstance().getUUID();
		if (!makeEnter(DIR_INCOMING) || !makeEnter(uuid)) {
			Log.e(TAG, "Failed to enter user directory.");
			return false;
		}

//				uploadingMany( new File(localFileUtil.getRoot(), LocalFileUtil.DIR_WORK));
		do {
			hasTask = false;
			if (!downloadDirectory(LocalFileUtil.DIR_PUB)
//				||!downloadPackageDirectory(LocalFileUtil.DIR_UPWORK)
				) {
				Log.e(TAG, "Sync failed.");
				return false;
			}
			if (!uploadDirectory(LocalFileUtil.DIR_IMAGE)//上传
					|| !uploadDirectory(LocalFileUtil.DIR_SENSOR)
					|| !uploadDirectory(LocalFileUtil.DIR_VIDEO)
					|| !uploadDirectory(LocalFileUtil.DIR_LOACL)
					|| !uploadLocal("/storage/sdcard0/Android/data/cfg/")//上传定位点导入
//					|| !uploadingMany(LocalFileUtil.DIR_WORK)  //取消工单上传
					) {
				Log.e(TAG, "Sync failed.");
				return false;
			}

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (hasTask);
		Log.v(TAG, "Sync succeeded.");
		disconnect();
		return true;
	}


	/**
	 *  上传多个文件.
	 * @param localFile 本地文件夹
	 * @return
	 */
	private boolean uploadingMany(String dirName) {
		try {
			File [] files = localFileUtil.listFiles(dirName);
			if(files==null){
				return false;
			}
			makeEnter("\\test");//FTP切换到工作目录
			// 得到当前目录下所有文件
			// 遍历得到每个文件并上传
			for (File _file : files) {
				if (_file.isDirectory()) {//如果当前文件是文件夹
					String packFileName = _file.getName();
					makeEnter(packFileName);//FTP切换到工作目录
					try { 
						File fileName = new File(localFileUtil.getRoot(),LocalFileUtil.DIR_WORK+"/"+packFileName);
						HashMap<String, Long> localMap = toFileMap(fileName.listFiles());//获取 dirName 当前目录下所有文件
						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取服务器当前目录下所有文件
						if (localMap == null || remoteMap == null) {
							return false;
						}
						//						Iterator<Map.Entry<String, Long>> iter = localMap.entrySet()
						//								.iterator();
						//						while (iter.hasNext()) {
						//							String file = iter.next().getKey();
						//							if (file.charAt(1) != '-') {
						//								Log.v(TAG, "Ignore file without priority: " + file + ".");
						//								iter.remove();
						//							}
						//						}
						List<Task> tasks = generateTasks(localMap, remoteMap, false);
						for (Task task : tasks) {
							upload(dirName+"/"+packFileName, task);
						}
						enterParent();
					} catch (IOException e) {
						disconnect();
						return false;
					}
				} 
			}
			enterParent();//切换到根目录
			if (!makeEnter(DIR_INCOMING) || !makeEnter(MainService.getInstance().getUUID())) {
				Log.e(TAG, "Failed to enter user directory.");
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	//上传定位点文件
	private boolean uploadLocal(String dirName) {
			try {
				File ff = new File(dirName);
			File [] files = ff.listFiles();///storage/sdcard0/Android/data/flie/
			if(files==null){
				return false;
			}
			makeEnter("\\file");//FTP切换到工作目录
			// 得到当前目录下所有文件
			// 遍历得到每个文件并上传
					try { 
						for(int i=0;i<files.length;i++){
							FileInputStream fStream = new FileInputStream(files[i]);
							BufferedInputStream input = new BufferedInputStream(fStream);
							client.enterLocalPassiveMode();// src 10M 5
							client.storeFile(files[i].getName(), input);
							fStream.close();
							input.close();
							files[i].delete();
//							Toast.makeText(MainActivity.getGetInstance(), "上传成功", 0).show();
						}
						
						
					} catch (IOException e) {
						disconnect();
						return false;
					}
		} catch (Exception e) {
			return false;
		}
			
		return makeEnter("\\"+DIR_INCOMING+"\\"+MainService.getInstance().getUUID());
	}
//	private boolean uploadingMany(String dirName) {
//		try {
//			File [] files = localFileUtil.listFiles(dirName);
//			if(files==null){
//				return false;
//			}
//			makeEnter(dirName);//FTP切换到工作目录
//			// 得到当前目录下所有文件
//			// 遍历得到每个文件并上传
//			for (File _file : files) {
//				if (_file.isDirectory()) {//如果当前文件是文件夹
//					String packFileName = _file.getName();
//					makeEnter(packFileName);//FTP切换到工作目录
//					try { 
//						File fileName = new File(localFileUtil.getRoot(),LocalFileUtil.DIR_WORK+"/"+packFileName);
//						HashMap<String, Long> localMap = toFileMap(fileName.listFiles());//获取 dirName 当前目录下所有文件
//						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取服务器当前目录下所有文件
//						if (localMap == null || remoteMap == null) {
//							return false;
//						}
//						//						Iterator<Map.Entry<String, Long>> iter = localMap.entrySet()
//						//								.iterator();
//						//						while (iter.hasNext()) {
//						//							String file = iter.next().getKey();
//						//							if (file.charAt(1) != '-') {
//						//								Log.v(TAG, "Ignore file without priority: " + file + ".");
//						//								iter.remove();
//						//							}
//						//						}
//						List<Task> tasks = generateTasks(localMap, remoteMap, false);
//						for (Task task : tasks) {
//							upload(dirName+"/"+packFileName, task);
//						}
//						enterParent();
//					} catch (IOException e) {
//						disconnect();
//						return false;
//					}
//				} 
//			}
//		} catch (Exception e) {
//			return false;
//		}
//		return enterParent();
//	}
	/**
	 * 下载
	 * @param dirName
	 * @return
	 */
	private boolean downloadPackageDirectory(String dirName) {
		if (!makeEnter(dirName)) {
			disconnect();
			return false;
		}
		try {
			FTPFile[] files = client.listFiles();
			if(files==null){
				return false;
			}
			for (FTPFile _file : files) {
				if (_file.isDirectory()) {//如果当前文件是文件夹
					String packFileName = _file.getName();
					makeEnter(packFileName);//FTP切换到工作目录
					try {
						HashMap<String, Long> localMap = toFileMap(localFileUtil
								.listFiles(dirName+"/"+packFileName));// 获取本地下发目录的文件信息
						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取server下发文件信息
						if (localMap == null || remoteMap == null) {
							return false;
						}
						List<Task> tasks = generateTasks(remoteMap, localMap);
						for (Task task : tasks) {
							downWorkload(dirName+"/"+packFileName, task);
						}
						enterParent();
						client.removeDirectory(packFileName);
					} catch (Exception e) {
						enterParent();
						disconnect();
						return false;
					}
					
				} 
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return enterParent();
	}
	/**
	 * 下载多个
	 * @param dirName
	 * @return
	 */
//	private boolean downloadPackageDirectory(String dirName) {
//		try {
//			client.changeToParentDirectory();
//			client.changeToParentDirectory();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//		try {
//			FTPFile[] files = client.listFiles();
//			if(files==null){
//				return false;
//			}
//			for (FTPFile _file : files) {
//				if (_file.isDirectory()&&_file.getName().equals(dirName)) {//如果当前文件是文件夹
//					String packFileName = _file.getName();
//					try {
//						HashMap<String, Long> localMap = toFileMap(localFileUtil
//								.listFiles(dirName+"/"+packFileName));// 获取本地下发目录的文件信息
//						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// 获取server下发文件信息
//						if (localMap == null || remoteMap == null) {
//							return false;
//						}
//						List<Task> tasks = generateTasks(remoteMap, localMap, true);
//						for (Task task : tasks) {
//							downWorkload(dirName+"/"+packFileName, task);
//						}
//						enterParent();
//						client.removeDirectory(packFileName);
//					} catch (Exception e) {
//						enterParent();
//						disconnect();
//						return false;
//					}
//					
//				} 
//			}
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		return enterParent();
//	}

	private class Task {

		public boolean append = false;
		public String fileName;
		public long offset;

		public Task(String fileName, long offset, boolean append) {
			this.fileName = fileName;
			this.offset = offset;
			this.append = append;
		}

	}

}
