/*
 * Copyright 2013-2015 duolabao.com All right reserved. This software is the
 * confidential and proprietary information of duolabao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with duolabao.com.
 */

package com.ramy.minervue.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 类Util的实现描述：//TODO 类实现描述
 *
 * @author HELONG 2016/3/8 17:42
 */
public class Util {

	/**
	 * 将YUV420SP数据顺时针旋转90度
	 *
	 * @param data        要旋转的数据
	 * @param imageWidth  要旋转的图片宽度
	 * @param imageHeight 要旋转的图片高度
	 * @return 旋转后的数据
	 */
	//    public static byte[] rotateNV21Degree90(byte[] data, int imageWidth, int imageHeight) {
	//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
	//        // Rotate the Y luma
	//        int i = 0;
	//        for (int x = 0; x < imageWidth; x++) {
	//            for (int y = imageHeight - 1; y >= 0; y--) {
	//                yuv[i] = data[y * imageWidth + x];
	//                i++;
	//            }
	//        }
	//        // Rotate the U and V color components
	//        i = imageWidth * imageHeight * 3 / 2 - 1;
	//        for (int x = imageWidth - 1; x > 0; x = x - 2) {
	//            for (int y = 0; y < imageHeight / 2; y++) {
	//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
	//                i--;
	//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
	//                i--;
	//            }
	//        }
	//        return yuv;
	//    }

	public static  byte[] YUV42RotateDegree90(byte[] data, int imageWidth, int imageHeight) 
	{
		byte [] yuv = new byte[imageWidth*imageHeight*3/2];

		// Y 
		int i = 0;
		for(int x = 0;x < imageWidth;x++)
		{
			for(int y = imageHeight-1;y >= 0;y--)                               
			{
				yuv[i] = data[y*imageWidth+x];
				i++;
			}
		}

		// U and V
		i = imageWidth*imageHeight*3/2-1;
		int pos = imageWidth*imageHeight;
		for(int x = imageWidth-1;x > 0;x=x-2)
		{

			for(int y = 0;y < imageHeight/2;y++)                                
			{
				yuv[i] = data[pos+(y*imageWidth)+x];
				i--;
				yuv[i] = data[pos+(y*imageWidth)+(x-1)];
				i--;
			}
		}
		return yuv;
	}

	/**
	 * 旋转数据
	 * 
	 * @param src
	 *            源数据
	 * @param srcWidth
	 *            源数据宽
	 * @param srcHeight
	 *            源数据高
	 *            逆时针旋转90度
	 */
	public static byte[] YV12RotateNegative90( byte[] src, int srcWidth,
			int srcHeight) {
		int t = 0;
		int i, j;
		byte[] dst = new byte[src.length];
		int wh = srcWidth * srcHeight;

		for (i = srcWidth - 1; i >= 0; i--) {
			for (j = srcHeight - 1; j >= 0; j--) {
				dst[t++] = src[j * srcWidth + i];
			}
		}

		for (i = srcWidth / 2 - 1; i >= 0; i--) {
			for (j = srcHeight / 2 - 1; j >= 0; j--) {
				dst[t++] = src[wh + j * srcWidth / 2 + i];
			}
		}

		for (i = srcWidth / 2 - 1; i >= 0; i--) {
			for (j = srcHeight / 2 - 1; j >= 0; j--) {
				dst[t++] = src[wh * 5 / 4 + j * srcWidth / 2 + i];
			}
		}
		return dst;

	}

	/**
	 * 保存数据到本地
	 *
	 * @param buffer 要保存的数据
	 * @param offset 要保存数据的起始位置
	 * @param length 要保存数据长度
	 * @param path   保存路径
	 * @param append 是否追加
	 */
	public static void save(byte[] buffer, int offset, int length, String path, boolean append) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path, append);
			fos.write(buffer, offset, length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static byte[] rotateYv12Degree90(byte[] src, int width, int height, boolean clockwise) 
	{
		byte[] dst = new byte[src.length];
		int area = width * height; 
		if (!clockwise) //顺时针
		{
			rotateRectClockwiseDegree90(src, 0, width, height, dst, 0); 
			rotateRectClockwiseDegree90(src, area, width / 2, height / 2, dst, area);
			rotateRectClockwiseDegree90(src, area * 5 / 4, width / 2, height / 2, dst, area * 5 / 4); 
		} else {
			rotateRectAnticlockwiseDegree90(src, 0, width, height, dst, 0);
			rotateRectAnticlockwiseDegree90(src, area, width / 2, height / 2, dst, area); 
			rotateRectAnticlockwiseDegree90(src, area * 5 / 4, width / 2, height / 2, dst, area * 5 / 4); 
		}
		return dst;
	} 


	public static void rotateRectClockwiseDegree90(byte[] src, int srcOffset, int width, int height, byte dst[], int dstOffset)
	{ 
		int i, j; int index = dstOffset; 
		for (i = 0; i < width; i++) 
		{ 
			for (j = height - 1; j >= 0; j--) 
			{ 
				dst[index] = src[srcOffset + j * width + i]; index++; 
			} 
		} 
	}

	public static void rotateRectAnticlockwiseDegree90(byte[] src, int srcOffset, int width, int height, byte dst[], int dstOffset) 
	{
		int i, j; int index = dstOffset;
		for (i = width - 1; i >= 0; i--) {
			for (j = 0; j < height; j++) {
				dst[index] = src[srcOffset + j * width + i]; index++;
			} 
		} 
	}


}