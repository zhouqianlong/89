package android_serialport_api;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.R.integer;

/**
 *  ͨѶ����
 * @author long
 *
 */
public class SerialCmdUtils {

	
	//	ÿ��ʾ1%CH4��Ӧ7944
	
	//	0%CH4��Ӧ26412
	
	//	4%CH4��Ӧ58188

	// CMD��ȡ���鴫����Ũ������    
	public static byte READ_CH4_PPM = 0x00;
	// CMD��ȡ��˹���Ƶ�Ƭ���İ汾��		
	public static byte READ_CH4_VERSION = 0x10;
	/**
	 * int ת byte []  
	 * @param 
	 * @return   4���ֽ�  byte
	 */
	public static byte[] intToByteArray(int i) { 
		byte[] result = new byte[4]; 
		result[3] = (byte)((i >> 24) & 0xFF);
		result[2] = (byte)((i >> 16) & 0xFF);
		result[1] = (byte)((i >> 8) & 0xFF); 
		result[0] = (byte)(i & 0xFF);
		return result;
	}
	
 
	
	
	//���ݵ�ѹת���ٷֱ�Ũ��%
	public static float getPercentageByvol(float vol){
		float value = (vol)/7944f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	//���ݵ�ѹת���ٷֱ�Ũ��ppm
	public static float getPercentageByvolPPM(float vol){
		float value = (vol)/7944f*10000f;
		return value;
	}
	
	//���ݰٷֱ�ת��PPM
	public static float getPPMByPercentage(float Percentage){
		float value = (Percentage)*10000f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	//����PPMת���ٷֱ�
	public static float getPercentageByPPM(float PPM){
		float value = (PPM)/10000f;
		return Float.valueOf(new DecimalFormat("0.00").format(value));
	}
	
	
	//���ݰٷֱ�Ũ��ת��Ϊ��ѹ
	public static float getVolByPercentage(float vol){
		if(vol<=0){
			return 0;
		}
		return vol*7944f;
	}
	
	
	/** 
	 * ����ת��Ϊ�ֽ� 
	 *  
	 * @param f 
	 * @return 
	 */  
	public static byte[] floatToByteArray(float f) {  

		// ��floatת��Ϊbyte[]  
		int fbit = Float.floatToIntBits(f);  

		byte[] b = new byte[4];    
		for (int i = 0; i < 4; i++) {    
			b[i] = (byte) (fbit >> (24 - i * 8));    
		}   

		// ��ת����  
		int len = b.length;  
		// ����һ����Դ����Ԫ��������ͬ������  
		byte[] dest = new byte[len];  
		// Ϊ�˷�ֹ�޸�Դ���飬��Դ���鿽��һ�ݸ���  
		System.arraycopy(b, 0, dest, 0, len);  
		byte temp;  
		// ��˳λ��i���뵹����i������  
		for (int i = 0; i < len / 2; ++i) {  
			temp = dest[i];  
			dest[i] = dest[len - i - 1];  
			dest[len - i - 1] = temp;  
		}  
		return dest;  

	}  
	/**
	 * ����У���
	 * @param buffer
	 * @return  У���   byte
	 */
	public static byte  addBuffer(byte [] buffer){
		int sum = 0;
		for(int i = 0 ; i< buffer.length;i++){
			sum +=buffer[i];
		}
		sum =  sum - (sum*2)-1;//��ת
		System.out.println(sum);
		//		return  (byte)(sum&0x7f) ;
		return  (byte)(sum) ;
	}
	/**-4, 8, 6, 1, 0, -12,
	 * ��֤У���
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
			if(cmd[i]==-4){//���� FC
				count++;
				cmd[2] = (byte) ((cmd[2])+1);//���ȼ�1
			}
			if(cmd[i]==-5){//���� FC
				count2++;
				cmd[2] = (byte) ((cmd[2])+1);//���ȼ�1
			}
		}
		if(count2>0){
			byte [] perCmdSend = new byte[cmd.length+count2];
			int position=0;
			for( int i= 0 ; i < cmd.length;i++){
				if(i==0){//ֱ�ӿ���������
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
				if(i==0){//ֱ�ӿ���������
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

