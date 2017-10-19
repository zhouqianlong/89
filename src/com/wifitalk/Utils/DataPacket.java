package com.wifitalk.Utils;
//www.javaapk.com
public class DataPacket
{
	public static int headLen = 100;// ͷ��Ϣ��С
	public static int bodyLen = 1000;// ��������С
//	public static int bodyLen = 63;// ��������С
	private byte[] recordBytes = new byte[headLen + bodyLen];
	public DataPacket(byte[] headInfo, byte[] bodyBytes)
	{
		for (int i = 0; i < headInfo.length; i++)
		{
			recordBytes[i] = headInfo[i];
		}
		for (int i = 0; i < bodyBytes.length; i++)
		{
			recordBytes[i + headLen] = bodyBytes[i];
		}
	}
	public byte[] getHeadInfo()
	{
		byte[] head = new byte[headLen];
		for (int i = 0; i < head.length; i++)
		{
			head[i] = recordBytes[i];
		}
		return head;
	}
	public byte[] getBody()
	{
		byte[] body = new byte[bodyLen];
		for (int i = 0; i < body.length; i++)
		{
			body[i] = recordBytes[i + headLen];
		}
		return body;
	}
	public byte[] getAllData()
	{
		byte[] data = new byte[headLen + bodyLen];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = recordBytes[i];
		}
		return data;
	}
}
