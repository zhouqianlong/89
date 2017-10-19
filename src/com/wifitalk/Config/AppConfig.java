package com.wifitalk.Config;

import com.wifitalk.Utils.DataPacket;

public class AppConfig
{
	public static String IPAddress = "192.168.1.124";// ·¢ËÍ¸ø89
	public static int PortAudio = 8300;
	public static int AUDIO_CAI_YANG_LV = 11025 ;
	public static int AUDIO_BPS = 38;
	public static int AUDIO_BUFFER = DataPacket.bodyLen-1 - AUDIO_BPS;
//	public static int AUDIO_CAI_YANG_LV = 11025;
//	public static int AUDIO_CAI_YANG_LV = 8000;
}