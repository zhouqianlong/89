package android_serialport_api;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.R.integer;

/**
 *  通讯命令
 * @author long
 *
 */
public class SerialCmdUtils {

	
	//	每提示1%CH4对应7944
	
	//	0%CH4对应26412
	
	//	4%CH4对应58188

	// CMD读取甲烷传感器浓度命令    
	public static byte READ_CH4_PPM = 0x00;
	// CMD读取瓦斯控制单片机的版本号		
	public static byte READ_CH4_VERSION = 0x10;
	/**
	 * int 转 byte []  
	 * @param 
	 * @return   4个字节  byte
	 */
	public static byte[] intToByteArray(int i) { 
		byte[] result = new byte[4]; 
		result[3] = (byte)((i >> 24) & 0xFF);
		result[2] = (byte)((i >> 16) & 0xFF);
		result[1] = (byte)((i >> 8) & 0xFF); 
		result[0] = (byte)(i & 0xFF);
		return result;
	}
	
 
	
	
	//根据电压转换百分比浓度%
	public static float getPercentageByvol(float vol){
		float value = (vol)/7944f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	//根据电压转换百分比浓度ppm
	public static float getPercentageByvolPPM(float vol){
		float value = (vol)/7944f*10000f;
		return value;
	}
	
	//根据百分比转换PPM
	public static float getPPMByPercentage(float Percentage){
		float value = (Percentage)*10000f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	//根据PPM转换百分比
	public static float getPercentageByPPM(float PPM){
		float value = (PPM)/10000f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	
	//根据百分比浓度转换为电压
	public static float getVolByPercentage(float vol){
		if(vol<=0){
			return 0;
		}
		return vol*7944f;
	}
	
	
	/** 
	 * 浮点转换为字节 
	 *  
	 * @param f 
	 * @return 
	 */  
	public static byte[] floatToByteArray(float f) {  

		// 把float转换为byte[]  
		int fbit = Float.floatToIntBits(f);  

		byte[] b = new byte[4];    
		for (int i = 0; i < 4; i++) {    
			b[i] = (byte) (fbit >> (24 - i * 8));    
		}   

		// 翻转数组  
		int len = b.length;  
		// 建立一个与源数组元素类型相同的数组  
		byte[] dest = new byte[len];  
		// 为了防止修改源数组，将源数组拷贝一份副本  
		System.arraycopy(b, 0, dest, 0, len);  
		byte temp;  
		// 将顺位第i个与倒数第i个交换  
		for (int i = 0; i < len / 2; ++i) {  
			temp = dest[i];  
			dest[i] = dest[len - i - 1];  
			dest[len - i - 1] = temp;  
		}  
		return dest;  

	}  
	/**
	 * 计算校验和
	 * @param buffer
	 * @return  校验和   byte
	 */
	public static byte  addBuffer(byte [] buffer){
		int sum = 0;
		for(int i = 0 ; i< buffer.length;i++){
			sum +=buffer[i];
		}
		sum =  sum - (sum*2)-1;//反转
		System.out.println(sum);
		//		return  (byte)(sum&0x7f) ;
		return  (byte)(sum) ;
	}
	/**-4, 8, 6, 1, 0, -12,
	 * 验证校验和
	 * @param buffer
	 * @return
	 */
	public static boolean checkBuffer(byte [] buffer,int length){
		try {
			byte [] dec = new byte[length];
			System.arraycopy(buffer, 0, dec, 0, length);
			int sum = 0;
			for(int i = 0 ; i < length-1;i++){
				sum+= dec[i];
			}
			sum = sum-(sum*2)-1;
			byte bSum = (byte) (sum&0x7f);
			if(bSum == buffer[length-1]){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	 


	public static  byte[] getCheckSum(byte[] cmd) {//[-4, 8, 14, 0, 8, -128, 70, 10, 12, -128, -106, -104, 0, 0]
		int count = 0;
		int count2 = 0;
		//		cmd = new byte[]{-4, 8, 14, 0, 8, -128, -5, 10, 12, -4, -106, -104, 0, 0};
		for(int i = 1; i<cmd.length;i++){
			if(cmd[i]==-4){//遇到 FC
				count++;
				cmd[2] = (byte) ((cmd[2])+1);//长度加1
			}
			if(cmd[i]==-5){//遇到 FC
				count2++;
				cmd[2] = (byte) ((cmd[2])+1);//长度加1
			}
		}
		if(count2>0){
			byte [] perCmdSend = new byte[cmd.length+count2];
			int position=0;
			for( int i= 0 ; i < cmd.length;i++){
				if(i==0){//直接拷贝到长度
					perCmdSend[position] =cmd[i];
				}else if(cmd[i]==-5){//fb
					perCmdSend[position] =-5;//fb
					perCmdSend[position+1] = 0;//00
					position++;
				}else{
					perCmdSend[position] =cmd[i];
				}

				position++;
			}
			cmd = perCmdSend;
		}
		if(count>0){
			byte [] perCmdSend = new byte[cmd.length+count];
			int position=0;
			for( int i= 0 ; i < cmd.length;i++){
				if(i==0){//直接拷贝到长度
					perCmdSend[position] =cmd[i];
				}else if(cmd[i]==-4){//fc
					perCmdSend[position] =-5;//fc
					perCmdSend[position+1] = 1;//01
					position++;
				}else{
					perCmdSend[position] =cmd[i];
				}
				position++;
			}
			cmd = perCmdSend;
		}
		byte checkSum = SerialCmdUtils.addBuffer(cmd);
		cmd[cmd.length-1] = checkSum;
		return cmd;
	}
}

