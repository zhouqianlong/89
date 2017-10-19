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
 * 对配置信息的封装。
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
        //对一个文件夹或者一个文件进行监听
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
     * @return 将配置文件中的键/值对保存到hashmap中
     */

    private HashMap<String, String> parseConfigFile() {
        HashMap<String, String> map = new HashMap<String, String>();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
         //sdcard不可以用。
        	return map;
        }
        String dir = Environment.getExternalStorageDirectory().getPath();
        configFile = new File(dir, CONFIG_FILE);
        if (!configFile.exists()) {
        	//config文件不再sdcard根目录。
            return map;
        }
        BufferedReader reader = null;
        try {
        	//读取配置文件，将文件中的配置信息用键/值对表示，并存储在hashmap中。
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
//判断map中是否存储了所要求的全部信息
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
    //这个接口是对配置信息文件的key进行的监听
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
