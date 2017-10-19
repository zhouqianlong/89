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
	public static boolean FTP_CONNE_STATU = false;// FTP��½״̬
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
			for (String file : server.keySet()) {// ��ȡ�������ļ�
				Long toSize = server.get(file);// �������ļ���С
				if (toSize!= null) {//�������ļ�����
					Long sdSzie = sdcard.get(file);
					if(sdSzie==null){//���ص�һ������
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
			Log.i(TAG, "��Ҫ���أ�" + ret.size() + "���ļ�   �̺߳ţ�" + threadNumber);
		hasTask = true;
		return ret;
	}
	
	
	
	
	private List<Task> generateTasks(HashMap<String, Long> phoneFile,HashMap<String, Long> serverFile, boolean isDownload) {
		LinkedList<Task> ret = new LinkedList<Task>();
		if (isDownload) {// ���ط������ļ�
			for (String file : phoneFile.keySet()) {// ��ȡserver�·���ÿһ���ļ�
				Long toSize = serverFile.get(file);// �鿴�����ļ��Ƿ���ڹ�����ļ�
				if (toSize != null) {// ���ش��ڸ��ļ�
					ret.add(new Task(file, 0, true));// �·��ļ� ������ʾ���� q
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
			Log.i(TAG, "��Ҫ���أ�" + ret.size() + "���ļ�   �̺߳ţ�" + threadNumber);
		} else {// �ϴ��ļ���������
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
			Log.i(TAG, "��Ҫ�ϴ���" + ret.size() + "���ļ�   �̺߳ţ�" + threadNumber);
		}
		hasTask = true;
		return ret;
	}

	// ����

	private boolean downloadDirectory(String dirName) {
		if (!makeEnter(dirName)) {
			disconnect();
			return false;
		}
		try {
			HashMap<String, Long> localMap = toFileMap(localFileUtil
					.listFiles(dirName));// ��ȡ�����·�Ŀ¼���ļ���Ϣ
			HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡserver�·��ļ���Ϣ
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
		Log.e(TAG, "׼������" + task.fileName + "...");
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
		if (checkFileLen(client.listFiles(task.fileName), file)) {// ��֤�ļ������Ƿ�һ��
			client.deleteFile(task.fileName);
			if (task.append) {
				//				boolean statu = 
				LocalFileUtil.delete(new File(localFileUtil
						.getDir(dirName), task.fileName));
				//				Log.i(TAG, "ɾ���ļ���" + statu);
			}
			file.renameTo(new File(localFileUtil.getDir(dirName), task.fileName));
			// Log.i(TAG,
			// "�޸����ƣ�"+file.getPath()+"==========================================������"+file.renameTo(new
			// File(localFileUtil.getDir(dirName), task.fileName)));
			PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
			 
			
			if(task.fileName.equals("list.txt")){
				util.setWorkDownload(util.getWorkDownload(null) + 1);// �����ļ�
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
			file.delete();// ɾ��sd���ļ�
		}
	}
	private void download(String dirName, Task task) throws IOException {
		Log.e(TAG, "׼������" + task.fileName + "...");
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
		if (checkFileLen(client.listFiles(task.fileName), file)) {// ��֤�ļ������Ƿ�һ��
			client.deleteFile(task.fileName);
			if (task.append) {
				//				boolean statu = 
				LocalFileUtil.delete(new File(localFileUtil
						.getDir(dirName), task.fileName));
				//				Log.i(TAG, "ɾ���ļ���" + statu);
			}
			file.renameTo(new File(localFileUtil.getDir(dirName), task.fileName));
			// Log.i(TAG,
			// "�޸����ƣ�"+file.getPath()+"==========================================������"+file.renameTo(new
			// File(localFileUtil.getDir(dirName), task.fileName)));
			PreferenceUtil util = MainService.getInstance().getPreferenceUtil();
			if (task.append == false) {
				// Log.i(TAG, "���سɹ���(��һ���·����ļ�)"+dirName);
				util.setUnreadDownload(util.getUnreadDownload(null) + 1);// �����ļ�
			} else {// �ٴ��·���ͬ�ļ�
				// �鿴�ļ��Ƿ��Ѷ��� ���Ѷ������ļ�����ʾ����һ������Ҫ���¼�һ����ʾ��
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
			file.delete();// ɾ��sd���ļ�
		}
	}

	public boolean checkFileLen(FTPFile[] ftpFile, File sdFile) {
		if (ftpFile.length != 1) {
			Log.e(TAG, "FTP�ļ�Ϊ��");
			return false;
		} else if (ftpFile[0].getSize() == sdFile.length()) {
			Log.i(TAG, "�ļ�����һ��:" + sdFile.length());
			return true;
		} else {
			Log.e(TAG,
					"�ļ����Ȳ�һ�� ftp:" + ftpFile[0].getSize() + ", sd:"
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
		if (!makeEnter(dirName)) {//��FTP���л���dirNameĿ¼
			disconnect();
			return false;
		}
		try { 
			HashMap<String, Long> localMap = toFileMap(localFileUtil
					.listFiles(dirName));//��ȡ dirName ��ǰĿ¼�������ļ�
			HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡ��������ǰĿ¼�������ļ�
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
				while ((byteread = in.read(tempbytes)) != -1) {// ѭ����ȡ//TODO
					// ����������������������������������������������������������������������������
					if (byteread == 50) {
						if (tempbytes[40] == 0 && tempbytes[41] == 0
								&& tempbytes[42] == 0 && tempbytes[43] == 0) {
							fileCheck = false;
							Thread.sleep(500);
							break;
						} else {
							fileCheck = true;
							// Log.i(TAG, "�ϴ���֤��Ƶ�Ƿ���ڣ�FTPĿ¼�л��ɹ�!");
							// HashMap<String, Long> remoteMap =
							// toFileMap(client.listFiles());//{}
							// Log.i(TAG, "FTP��ǰ�û��ϴ������ļ�����"+remoteMap.size());
							// for(String
							// remoteFileName:remoteMap.keySet()){//�жϷ������·��ļ��������Ƿ����
							// if( task.fileName.equals(remoteFileName)){
							// Log.e(TAG, remoteFileName+"���ļ�����,ȡ���ϴ�");
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
			if (!fileCheck) {// �ļ�У�� 0000 ���ϴ�
				return;
			}
		}

		BufferedInputStream input = new BufferedInputStream(fStream);
		if (task.offset < file.length()) {// file.length : 18779853
			// offset:18779853
			// Log.i(TAG,
			// "000000������δ�ϴ��������ļ���"+task.fileName+"\t������ȣ�"+task.offset);
			//			input.skip(file.length()-1);
			client.setRestartOffset(task.offset);
		}
		client.enterLocalPassiveMode();// src 10M 5
		// Log.i(TAG, "1111�ļ���"+task.fileName+"\t������ȣ�"+task.offset);
		if (task.offset < file.length()) {
			try {
				input.skip(task.offset);
				 FTPFile[] list_file = client.listFiles();
				 for(int f = 0; f< list_file.length;f++){
					 if(list_file[f].getName().equals("list.txt")){
						 boolean bol  = client.deleteFile("list.txt");
						 Log.i(TAG, "ɾ��list.txt�ļ�"+bol);
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
		FTPFile[] serverFile = client.listFiles(task.fileName + ".tmp");// ��ȡServe�ļ���Ϣ
		for (int i = 0; i < 5; i++) {
			if (serverFile.length == 0) {
				try {
					Thread.sleep(1000);
					serverFile = client.listFiles(task.fileName + ".tmp");// ��ȡServe�ļ���Ϣ
					if (serverFile.length > 0) {// �������ļ�
						break;// �˳� �ϴ�
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
		Log.i(TAG, "�ӳ�1���ϴ��ļ�");
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
				client.setDataTimeout(60000); // ���ô��䳬ʱʱ��Ϊ60��
				client.setConnectTimeout(60000); // ���ӳ�ʱΪ60��
				Log.i(TAG, "Connecting to " + serverAddr + "...");
				client.connect(InetAddress.getByName(serverAddr));
				Log.i(TAG, "����������ֵ��" + client.getReplyCode());
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
			FTP_CONNE_STATU = false;// FTP�˳���½
			try {
				boolean logoutsta = client.logout();
				client.disconnect();
				Log.i(TAG, "11111111111111111FTp�˳�   :" + logoutsta);
			} catch (IOException e) {
				// Expected.
				Log.i(TAG, "33333333333333�˳�:  ����˵�ַ��" + serverAddr);
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
			FTP_CONNE_STATU = true;// FTP��½�ɹ�
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
			if (!uploadDirectory(LocalFileUtil.DIR_IMAGE)//�ϴ�
					|| !uploadDirectory(LocalFileUtil.DIR_SENSOR)
					|| !uploadDirectory(LocalFileUtil.DIR_VIDEO)
					|| !uploadDirectory(LocalFileUtil.DIR_LOACL)
					|| !uploadLocal("/storage/sdcard0/Android/data/cfg/")//�ϴ���λ�㵼��
//					|| !uploadingMany(LocalFileUtil.DIR_WORK)  //ȡ�������ϴ�
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
	 *  �ϴ�����ļ�.
	 * @param localFile �����ļ���
	 * @return
	 */
	private boolean uploadingMany(String dirName) {
		try {
			File [] files = localFileUtil.listFiles(dirName);
			if(files==null){
				return false;
			}
			makeEnter("\\test");//FTP�л�������Ŀ¼
			// �õ���ǰĿ¼�������ļ�
			// �����õ�ÿ���ļ����ϴ�
			for (File _file : files) {
				if (_file.isDirectory()) {//�����ǰ�ļ����ļ���
					String packFileName = _file.getName();
					makeEnter(packFileName);//FTP�л�������Ŀ¼
					try { 
						File fileName = new File(localFileUtil.getRoot(),LocalFileUtil.DIR_WORK+"/"+packFileName);
						HashMap<String, Long> localMap = toFileMap(fileName.listFiles());//��ȡ dirName ��ǰĿ¼�������ļ�
						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡ��������ǰĿ¼�������ļ�
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
			enterParent();//�л�����Ŀ¼
			if (!makeEnter(DIR_INCOMING) || !makeEnter(MainService.getInstance().getUUID())) {
				Log.e(TAG, "Failed to enter user directory.");
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	//�ϴ���λ���ļ�
	private boolean uploadLocal(String dirName) {
			try {
				File ff = new File(dirName);
			File [] files = ff.listFiles();///storage/sdcard0/Android/data/flie/
			if(files==null){
				return false;
			}
			makeEnter("\\file");//FTP�л�������Ŀ¼
			// �õ���ǰĿ¼�������ļ�
			// �����õ�ÿ���ļ����ϴ�
					try { 
						for(int i=0;i<files.length;i++){
							FileInputStream fStream = new FileInputStream(files[i]);
							BufferedInputStream input = new BufferedInputStream(fStream);
							client.enterLocalPassiveMode();// src 10M 5
							client.storeFile(files[i].getName(), input);
							fStream.close();
							input.close();
							files[i].delete();
//							Toast.makeText(MainActivity.getGetInstance(), "�ϴ��ɹ�", 0).show();
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
//			makeEnter(dirName);//FTP�л�������Ŀ¼
//			// �õ���ǰĿ¼�������ļ�
//			// �����õ�ÿ���ļ����ϴ�
//			for (File _file : files) {
//				if (_file.isDirectory()) {//�����ǰ�ļ����ļ���
//					String packFileName = _file.getName();
//					makeEnter(packFileName);//FTP�л�������Ŀ¼
//					try { 
//						File fileName = new File(localFileUtil.getRoot(),LocalFileUtil.DIR_WORK+"/"+packFileName);
//						HashMap<String, Long> localMap = toFileMap(fileName.listFiles());//��ȡ dirName ��ǰĿ¼�������ļ�
//						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡ��������ǰĿ¼�������ļ�
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
	 * ����
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
				if (_file.isDirectory()) {//�����ǰ�ļ����ļ���
					String packFileName = _file.getName();
					makeEnter(packFileName);//FTP�л�������Ŀ¼
					try {
						HashMap<String, Long> localMap = toFileMap(localFileUtil
								.listFiles(dirName+"/"+packFileName));// ��ȡ�����·�Ŀ¼���ļ���Ϣ
						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡserver�·��ļ���Ϣ
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
	 * ���ض��
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
//				if (_file.isDirectory()&&_file.getName().equals(dirName)) {//�����ǰ�ļ����ļ���
//					String packFileName = _file.getName();
//					try {
//						HashMap<String, Long> localMap = toFileMap(localFileUtil
//								.listFiles(dirName+"/"+packFileName));// ��ȡ�����·�Ŀ¼���ļ���Ϣ
//						HashMap<String, Long> remoteMap = toFileMap(client.listFiles());// ��ȡserver�·��ļ���Ϣ
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
