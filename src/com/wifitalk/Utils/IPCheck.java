package com.wifitalk.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPCheck {
	
	public static boolean isboolIP(String ipAddress){ 

		String ip="(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"; 
		Pattern pattern = Pattern.compile(ip); 
		Matcher matcher = pattern.matcher(ipAddress); 
		return matcher.matches(); 
	} 

}
