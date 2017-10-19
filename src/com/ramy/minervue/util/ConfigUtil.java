package com.ramy.minervue.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.ramy.minervue.app.MainService;


import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/**
 * ��������Ϣ�ķ�װ��
 * @author Administrator
 *
 */
public class ConfigUtil {

    public static enum ConfigType {

        KEY_SERVER_ADDR("ServerAdd"),
        KEY_UDID("DeviceName"),
        KEY_CONTROL_PORT("PortControl"),
        KEY_VIDEO_PORT("PortVideo"),
        KEY_AUDIO_PORT("PortAudio");

        private String key;

        private ConfigType(String key) {
            this.key = key;
        }

    }

    private static final String TAG = "RAMY-ConfigUtil";
    private static final String CONFIG_FILE = "Devicename.txt";
    private static ConfigUtil instance = null;

    private HashMap<String, LinkedList<PreferenceListener>> listenerMap = new HashMap<String, LinkedList<PreferenceListener>>();
    private HashMap<String, String> configMap;
    private File configFile;
    private FileObserver observer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateConfig();
        }
    };

    public void addListener(ConfigType type, PreferenceListener listener) {
        if (listener != null) {
            LinkedList<PreferenceListener> list = listenerMap.get(type.key);
            if (list == null) {
                list = new LinkedList<PreferenceListener>();
                listenerMap.put(type.key, list);
            }
            list.add(listener);
        }
    }

    public void release() {
        observer.stopWatching();
    }

    public void removeListener(ConfigType type, PreferenceListener listener) {
        LinkedList<PreferenceListener> list = listenerMap.get(type.key);
        if (list != null && listener != null) {
            list.remove(listener);
        }
    }

    private ConfigUtil() throws IllegalStateException {
        configMap = parseConfigFile();
        if (!isValid(configMap)) {
            throw new IllegalStateException("Error retrieving configuration file.");
        }
        //��һ���ļ��л���һ���ļ����м���
        observer = new FileObserver(configFile.getAbsolutePath(), FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                handler.sendEmptyMessage(0);
            }
        };
        observer.startWatching();
    }

    private void checkConfigChange(String key, HashMap<String, String> oldMap) {
//        String oldValue = oldMap.get(key);
//        if (oldValue.equals(configMap.get(key))) {
//            return;
//        }
        LinkedList<PreferenceListener> list = listenerMap.get(key);
        if (list != null) {
            for (PreferenceListener listener : list) {
                listener.onPreferenceChanged();
            }
        }
    }

    private void updateConfig() {
        Log.i(TAG, "Configuration file changed.");
        HashMap<String, String> map = parseConfigFile();
        if (!isValid(map)) {
            return;
        }
        HashMap<String, String> oldMap = configMap;
        configMap = map;
        for (ConfigType type : ConfigType.values()) {
            checkConfigChange(type.key, oldMap);
        }
    }

    public static ConfigUtil getInstance() {
        if (instance == null) {
            try {
                instance = new ConfigUtil();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error initiating.");
            }
        }
        return instance;
    }
    /**
     * 
     * @return �������ļ��еļ�/ֵ�Ա��浽hashmap��
     */

    private HashMap<String, String> parseConfigFile() {
        HashMap<String, String> map = new HashMap<String, String>();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
         //sdcard�������á�
        	return map;
        }
        String dir = Environment.getExternalStorageDirectory().getPath();
        configFile = new File(dir, CONFIG_FILE);
        if (!configFile.exists()) {
        	//config�ļ�����sdcard��Ŀ¼��
            return map;
        }
        BufferedReader reader = null;
        try {
        	//��ȡ�����ļ������ļ��е�������Ϣ�ü�/ֵ�Ա�ʾ�����洢��hashmap�С�
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(":", 2);
                if (args.length == 2 && !"".equals(args[0]) && !"".equals(args[1])) {
                    map.put(args[0], args[1]);
                }
            }
        } catch (IOException e) {
            return map;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignored.
                }
            }
        }
        return map;
    }
//�ж�map���Ƿ�洢����Ҫ���ȫ����Ϣ
    private boolean isValid(HashMap<String, String> map) {
        return map.keySet().containsAll(Arrays.asList(
                ConfigType.KEY_SERVER_ADDR.key,
                ConfigType.KEY_UDID.key,
                ConfigType.KEY_CONTROL_PORT.key,
                ConfigType.KEY_VIDEO_PORT.key,
                ConfigType.KEY_AUDIO_PORT.key));
    }

    public String getServerAddr() {
        return configMap.get(ConfigType.KEY_SERVER_ADDR.key);
    }
    
    

//    public int getPortControl() {
//        return Integer.parseInt(configMap.get(ConfigType.KEY_CONTROL_PORT.key));
//    }
//
//    public int getPortVideo() {
//        return Integer.parseInt(configMap.get(ConfigType.KEY_VIDEO_PORT.key));
//    }
//
//    public int getPortAudio() {
//       return Integer.parseInt(configMap.get(ConfigType.KEY_AUDIO_PORT.key));
//    }
    public int getPortControl() {
//        return Integer.parseInt(configMap.get(ConfigType.KEY_CONTROL_PORT.key));
    	return 6000;
    }
    
    public int getPortVideo() {
//        return Integer.parseInt(configMap.get(ConfigType.KEY_VIDEO_PORT.key));
    	return 6100;
    }
    
    public int getPortAudio() {
//        return Integer.parseInt(configMap.get(ConfigType.KEY_AUDIO_PORT.key));
    	return 6200;
    }

    public String getUDID() {
        return configMap.get(ConfigType.KEY_UDID.key);
    }
    //����ӿ��Ƕ�������Ϣ�ļ���key���еļ���
    public interface PreferenceListener {
        public void onPreferenceChanged();
    }

    
    
    public static void createFile(){
    	File folder = new File(android.os.Environment.getExternalStorageDirectory()+"");
		File file = new File(folder, "Devicename.txt");
		FileWriter out = null;
		try {
			out = new FileWriter(file);
			out.write("ServerAdd:192.168.1.254\n");
			out.write("PortControl:6000\n");
			out.write("PortVideo:6100\n");
			out.write("PortAudio:6200\n");
			out.write("DeviceName:"+MainService.getInstance().getMacAddressLastSix()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignored.
				}
			}
		}
    }
    
    
    public static void changeFile(String ip,String name){
    	File folder = new File(android.os.Environment.getExternalStorageDirectory()+"");
		File file = new File(folder, "Devicename.txt");
		FileWriter out = null;
		try {
			out = new FileWriter(file);
			out.write("ServerAdd:"+ip+"\n");
			out.write("PortControl:6000\n");
			out.write("PortVideo:6100\n");
			out.write("PortAudio:6200\n");
			out.write("DeviceName:"+name+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignored.
				}
			}
		}
//		ConfigUtil.getInstance().updateConfig();
    }
}
