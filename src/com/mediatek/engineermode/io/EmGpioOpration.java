package com.mediatek.engineermode.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class EmGpioOpration {
	// GPIO 1 为背夹

	/**
	 * ------------------------------------------------------------------------
	 * ------- 描述：GPIO_1 关闭
	 */
	public static void turnOff_Ext_out1() {
		writeToDevice(0, "/sys/devices/platform/zzx-misc/ext_out1_stats");
	}

	/**
	 * 描述：GPIO_1 打开
	 */
	public static void turnOn_Ext_out1() {
		writeToDevice(1, "/sys/devices/platform/zzx-misc/ext_out1_stats");
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------- 描述：GPIO_2 关闭
	 */
	public static void turnOff_Ext_out2() {
		writeToDevice(0, "/sys/devices/platform/zzx-misc/ext_out2_stats");
	} 

	/**
	 * 描述：GPIO_2 打开 
	 */
	public static void turnOn_Ext_out2() {
		writeToDevice(1, "/sys/devices/platform/zzx-misc/ext_out2_stats");
	}

	// =================================================================================

	/**
	 * 
	 * @param state
	 *            0 关闭，1打开
	 * @param filePath
	 *            文件节点设备路径
	 */
	public static void writeToDevice(int state, String filePath) {
		File file = new File(filePath);
		FileWriter outputStream = null;
		try {
			outputStream = new FileWriter(file);
			if (state == 1) {
				outputStream.write("1");
			} else if (state == 0) {
				outputStream.write("0");
			}
			outputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
