package com.ramy.minervue.camera.color;

import android.hardware.Camera;

/**
 * Created by peter on 12/3/13.
 */
public class YV12ToYUV420Converter extends YUV420Converter {

	private int ySize;
	private int uvSize;
	private byte[] buffer;
	private int width;
	private int height;
	public YV12ToYUV420Converter(Camera.Size size) {
		super(size);
		int yStride = (int) Math.ceil(size.width / 16.0) * 16;
		int uvStride = (int) Math.ceil( (yStride / 2) / 16.0) * 16;
		ySize = yStride * size.height;
		uvSize = uvStride * size.height / 2;
		buffer = new byte[uvSize];
	}

	@Override
	public void convert(byte[] data) {
		System.arraycopy(data, ySize, buffer, 0, uvSize);
		System.arraycopy(data, ySize + uvSize, data, ySize, uvSize);
		System.arraycopy(buffer, 0, data, ySize + uvSize, uvSize);
	}



	private static void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
	{
		if (yuv420sp == null ||yuv420 == null)
			return;
		int framesize = width*height;
		int i = 0, j = 0;
		//copy y
		for (i = 0; i < framesize; i++)
		{
			yuv420[i] = yuv420sp[i];
		}
		i = 0;
		for (j = 0; j < framesize/2; j+=2)
		{
			yuv420[i + framesize*5/4] = yuv420sp[j+framesize];
			i++;
		}
		i = 0;
		for(j = 1; j < framesize/2;j+=2)
		{
			yuv420[i+framesize] = yuv420sp[j+framesize];
			i++;
		}


	}

	@Override
	public void yuv420sToconvert(byte[] data) {
		System.arraycopy(data, ySize, buffer, 0, uvSize);
		System.arraycopy(data, ySize + uvSize, data, ySize, uvSize);
		System.arraycopy(buffer, 0, data, ySize + uvSize, uvSize);
	}
}