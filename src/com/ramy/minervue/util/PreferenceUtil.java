package com.ramy.minervue.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.preference.PreferenceManager;

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
import java.util.LinkedList;

import com.ramy.minervue.R;
import com.ramy.minervue.dao.PubDao;

/**
 * Created by peter on 11/24/13.
 */
public class PreferenceUtil implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_TIME_OFFSET = "timeOffset";
    public static final String KEY_UNREAD_DOWNLOAD = "unreadDownload";
    public static final String KEY_WORK_DOWNLOAD = "workDownload";
    public static final String KEY_SERIAL_NUMBER = "serialNumber";

    private Context mContext;
    private SharedPreferences sharedPreferences = null;

    private HashMap<String, LinkedList<PreferenceListener>> listenerMap = new HashMap<String, LinkedList<PreferenceListener>>();
	private SoundPool sp;
	private HashMap<Integer,Integer> spMap;
    public PreferenceUtil(Context context) {
    	this.mContext= context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		sp = new SoundPool(2,AudioManager.STREAM_MUSIC,0);
		spMap = new HashMap<Integer,Integer>();
		spMap.put(1, sp.load(context,R.raw.folder, 1));
		spMap.put(2, sp.load(context,R.raw.open, 2));
		spMap.put(3, sp.load(context,R.raw.close, 3));
		spMap.put(4, sp.load(context,R.raw.baojin, 4));
		spMap.put(5, sp.load(context,R.raw.photo_open, 5));
    }

    public void release() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void addListener(String key, PreferenceListener listener) {
        if (listener != null) {
            LinkedList<PreferenceListener> list = listenerMap.get(key);
            if (list == null) {
                list = new LinkedList<PreferenceListener>();
                listenerMap.put(key, list);
            }
            list.add(listener);
        }
    }

    public void removeListener(String key, PreferenceListener listener) {
        LinkedList<PreferenceListener> list = listenerMap.get(key);
        if (list != null && listener != null) {
            list.remove(listener);
        }
    }

    public long getTimeOffset(PreferenceListener listener) {
        addListener(KEY_TIME_OFFSET, listener);
        return sharedPreferences.getLong(KEY_TIME_OFFSET, 0);
    }

    public void setTimeOffset(long offset) {
        sharedPreferences.edit().putLong(KEY_TIME_OFFSET, offset).commit();
    }

    public void setUnreadDownload(int unread) {
        sharedPreferences.edit().putInt(KEY_UNREAD_DOWNLOAD, unread).commit();
    }

    public int getUnreadDownload(PreferenceListener listener) {
        addListener(KEY_UNREAD_DOWNLOAD, listener);
        return sharedPreferences.getInt(KEY_UNREAD_DOWNLOAD, 0);
    }
    
    public void setWorkDownload(int unread) {
    	sharedPreferences.edit().putInt(KEY_WORK_DOWNLOAD, unread).commit();
    }
    
    public int getWorkDownload(PreferenceListener listener) {
    	addListener(KEY_WORK_DOWNLOAD, listener);
    	return sharedPreferences.getInt(KEY_WORK_DOWNLOAD, 0);
    }
    /**
     * 
     * @param serialNumber 注册码。
     */

    public void setSerialNumber(String serialNumber) {
    	setSDCardSerialNumber("sdcard/TKSerialNumber.txt",serialNumber);
        sharedPreferences.edit().putString(KEY_SERIAL_NUMBER, serialNumber).commit();
    }
	public static void setSDCardSerialNumber(String file, String conent) {  
		BufferedWriter out = null;  
		try {  
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));  
			out.write(conent);  
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
    public String getSDCardSerialNumber(){
    	InputStreamReader inputStreamReader = null;  
		try {  
			InputStream ism =  new FileInputStream(new File("sdcard/TKSerialNumber.txt"));
			inputStreamReader = new InputStreamReader(ism, "UTF-8");  
			StringBuffer sb = new StringBuffer("");  
			String line;  
			BufferedReader reader = new BufferedReader(inputStreamReader);  
			while ((line = reader.readLine()) != null) {  
				sb.append(line);  
			}  
			inputStreamReader.close();
			reader.close();
			ism.close();
			return sb.toString();
//			String [] conent  = sb.toString().split("&&");
//			if(conent.length==3){
//				map.put("serverPort", conent[0]);
//				map.put("videoPort", conent[1]);
//				map.put("audioPort", conent[2]);
//			}
		} catch (IOException e) {  
			e.printStackTrace();  
		}
		return null;  
    }
    public String getSerialNumber(PreferenceListener listener) {
        addListener(KEY_SERIAL_NUMBER, listener);
        return sharedPreferences.getString(KEY_SERIAL_NUMBER, null);
    }
    //当共享文件发生改变时	
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        LinkedList<PreferenceListener> list = listenerMap.get(key);
        if (list != null) {
            for (PreferenceListener listener : list) {
                listener.onPreferenceChanged();
            }
        }
    }
//当存储文件中某一个键发生改变时，某个键的监听者。
    public interface PreferenceListener {
        public void onPreferenceChanged();
    }
    public void deleteDB(String path){
    	PubDao pubDao = new PubDao(mContext);
		//已经查看过的文件直接删除
		if (pubDao.isExist(path)) {
			//TODO 删除的处理
			pubDao.delete(path);
		}
    }
    public void deleteWorkDB(String path){
    	PubDao pubDao = new PubDao(mContext);
    	//已经查看过的文件直接删除
    	if (pubDao.isExistWork(path)) {
    		//TODO 删除的处理
    		pubDao.deleteWork(path);
    	}
    }
    /**
     * 查看文件是否已读
     * @param true /已读  false /未读
     */
    public boolean findFileIsRead(String path){
    	PubDao pubDao = new PubDao(mContext);
		//已经查看过的文件直接删除
    	return pubDao.isExist(path);
    }
    
    /**
     * 查看文件是否已读
     * @param true /已读  false /未读
     */
    public boolean findWorkFileIsRead(String path){
    	PubDao pubDao = new PubDao(mContext);
    	//已经查看过的文件直接删除
    	return pubDao.isExistWork(path);
    }
    
   
    
	public void playSounds(int sound, int number){
		AudioManager am = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volumnRatio = audioCurrentVolumn/audioMaxVolumn;
		sp.play(spMap.get(sound), 0.1f, 0.1f, 0, number, 1);
//		sp.play(spMap.get(sound), 1f, 1f, 0, number, 1);
	}
}
