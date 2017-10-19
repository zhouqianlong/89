package com.mediatek.engineermode.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class EmGpioOpration {
	// GPIO 1 Ϊ����

	/**
	 * ------------------------------------------------------------------------
	 * ------- ������GPIO_1 �ر�
	 */
	public static void turnOff_Ext_out1() {
		writeToDevice(0, "/sys/devices/platform/zzx-misc/ext_out1_stats");
	}

	/**
	 * ������GPIO_1 ��
	 */
	public static void turnOn_Ext_out1() {
		writeToDevice(1, "/sys/devices/platform/zzx-misc/ext_out1_stats");
	}

	/**
	 * ------------------------------------------------------------------------
	 * ------- ������GPIO_2 �ر�
	 */
	public static void turnOff_Ext_out2() {
		writeToDevice(0, "/sys/devices/platform/zzx-misc/ext_out2_stats");
	} 

	/**
	 * ������GPIO_2 �� 
	 */
	public static void turnOn_Ext_out2() {
		writeToDevice(1, "/sys/devices/platform/zzx-misc/ext_out2_stats");
	}

	// =================================================================================

	/**
	 * 
	 * @param state
	 *            0 �رգ�1��
	 * @param filePath
	 *            �ļ��ڵ��豸·��
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
