package android_serialport_api;
/**
 * 
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ramy.minervue.app.MainActivity;
import com.ramy.minervue.util.BinaryUtils;
import com.tk.ch4gas.GasMainFragement;
import com.tk.ch4gas.GasMainActivity;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
 
/**
 * @author tao
 * 
 */
public class GasDetector {
	private Activity sensorOpenActivity;
	private static final String TAG = "GasDetector";
	private static final GasDetector instance = new GasDetector();
	private static final String DEVICE_PATH = "/dev/ttyMT0";
	private static final int BAUDRATE = 9600;//19200
	public static final int GAS_JCGRBD_SUSS = 11;
	protected SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	byte[] sp_buffer;
	byte [] bufferArray = new byte[512];
	private boolean isOpened;
	private ExecutorService executorService;
	private float responseGasData = -1;
	private float lastReplyCmd = -1;
	
	
	public static int CMD_READ_CH4_SUCCE = 1;   //读取甲烷成功
	public static int CMD_READ_CH4_ERROR = -100;//读取甲烷失败
	
	public float getLastResponseGasData(){
		return responseGasData;
	}
	public float getLastReplyCmd() {
		return lastReplyCmd;
	}
	private GasDetector() {
		sp_buffer = new byte[1024];
		this.isOpened = false;
		this.mSerialPort = null;
		this.executorService = Executors.newFixedThreadPool(1);
		
	}
	public GasDetector(Activity sensorOpenActivity) {
		sp_buffer = new byte[1024];
		this.isOpened = false;
		this.mSerialPort = null;
		this.executorService = Executors.newFixedThreadPool(1);
		this.sensorOpenActivity = sensorOpenActivity;
	}
	//存储模块返回的命令
	private String showBuffer;
	public String getShowBuffer() {
		return showBuffer;
	}
	public void setShowBuffer(String showBuffer) {
		this.showBuffer = showBuffer;
	}
	public Handler mhandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			if(msg.what==9){
				StringBuffer sb = new StringBuffer();
				for(int i = 0 ; i < sp_buffer.length; i ++){
					sb.append(sp_buffer[i]+",");
				}
				Log.i("GasDetector", "测试结果:"+BinaryUtils.toHex(sp_buffer, sb.length()));
				//				Toast.makeText(sensorOpenActivity, "测试结果:"+BinaryUtils.toHex(buffer, 10), 0).show();
			}
		};
	};
	//错误记录  默认是错误true      false代表通讯正常
	private boolean error_status  = true;

	public boolean getError_status(){
		return error_status;
	}
	SPReadTask task;
	private  int readWithTimeout(long timeout) {
		if(executorService == null||executorService.isShutdown()){	
			this.executorService = Executors.newFixedThreadPool(1);
		}
		Future<Integer> future = executorService.submit(task);
		try {
 
			future.get(timeout, TimeUnit.MILLISECONDS);
			int len = task.buffer_len;
			task.buffer_len = 0;
			return JieXiDate(len);
		} catch (InterruptedException e) {
			e.printStackTrace();
			task.buffer_len = 0;
			Log.d(TAG, future.cancel(true)+"("+task.id+")TIME OUT SP read:" + BinaryUtils.toHex(sp_buffer, task.buffer_len));
			this.lastReplyCmd = -1;
		} catch (ExecutionException e) {
			e.printStackTrace();
			task.buffer_len = 0;
			sp_buffer = new byte[1024];
			this.lastReplyCmd = -1;
		} catch (TimeoutException e) {
			e.printStackTrace();
			error_status = true;//错误状态为 True;
			task.buffer_len = 0;
			this.lastReplyCmd = -1;
			Log.d(TAG, future.cancel(true)+"("+task.id+")TIME OUT SP read:" + BinaryUtils.toHex(sp_buffer, task.buffer_len));
		}catch(Exception  e){
			e.printStackTrace();
			error_status = true;//错误状态为 True;
			task.buffer_len = 0;
			this.lastReplyCmd = -1;
			Log.d(TAG, future.cancel(true)+"("+task.id+")TIME OUT SP read:" + BinaryUtils.toHex(sp_buffer, task.buffer_len));
		}finally{
			executorService.shutdown();
			task.buffer_len = 0;
		}
		return -1;
	}
	private int JieXiDate(Integer readByte) {
		//消息未读
		if(readLen>10){
			error_status = false;//错误状态为 True;
		}
		isRead = false;
		float xs =1;
		try {
			xs = GasMainActivity.getIntances().getBJ();
		} catch (Exception e) {
			return -1;
		}
		Log.d(TAG, "SP read:" + BinaryUtils.toHex(sp_buffer, readByte)+"系数："+	xs);
		readByte = filterBuffer(readByte);
		if(readByte<4&&SerialCmdUtils.checkBuffer(sp_buffer,readByte)){
			return readByte;
		}
		
		if(sp_buffer[0]==-4&&sp_buffer[1]==9&&sp_buffer[4]==0){//fc 09 00 ?? xx xx xx xx sum
			this.lastReplyCmd = CMD_READ_CH4_SUCCE;//读取成功
			GasMainFragement.gasInitGAS = Integer.valueOf(bufferArrayInteger(8));//初始电压
			float value = (float)SerialCmdUtils.getPercentageByPPM((GasMainFragement.gasInitGAS-GasMainActivity.getIntances().zero_)*GasMainActivity.getIntances().getBJ()+GasMainActivity.getIntances().zero);
			if(value<0){
				value=0;
			}
			GasMainFragement.gasDATA_CHECK = value;//
			this.responseGasData = Integer.valueOf(bufferArrayInteger(8));
		
		
		}if(sp_buffer[0]==-4&&sp_buffer[1]==9&&sp_buffer[4]==10){
			
		}
		
		return readByte;
	}
 
	/**
	 * 模拟一次浓度
	 * @param gas
	 */
	public void setTestData(int gas){//fc 09 00 ?? xx xx xx xx sum
		this.lastReplyCmd = CMD_READ_CH4_SUCCE;//读取成功
		GasMainFragement.gasInitGAS = Integer.valueOf(gas);//初始电压
		float value = (float)SerialCmdUtils.getPercentageByPPM((GasMainFragement.gasInitGAS-GasMainActivity.getIntances().zero_)*GasMainActivity.getIntances().getBJ()+GasMainActivity.getIntances().zero);
		if(value<0){
			value=0;
		}
		GasMainFragement.gasDATA_CHECK = value;//
		this.responseGasData = Integer.valueOf(bufferArrayInteger(8));
	}
	 
	private String bufferArrayInteger(int position) {
		int number = ((sp_buffer[position-3]&0x00ff)<<24)+((sp_buffer[position-2]&0x00ff)<<16)+((sp_buffer[position-1]&0x00ff)<<8)+(sp_buffer[position]&0x00ff);
		return ""+number;
	}
	private int filterBuffer(Integer buffLen) {
		int len = buffLen;
		int count =0;
		for(int i = 1; i < buffLen;i++){
			if(sp_buffer.length<=i){
//				Log.e(TAG, "处理(FB)第"+count+"次:read:" + BinaryUtils.toHex(sp_buffer, len));
				break;
			}
			if(sp_buffer[i]==-5){//遇到 FB
				count ++;
				sp_buffer[i] =(byte) ((sp_buffer[i]) + (sp_buffer[i+1]));
				byte [] temp   = new byte[len-1]; 
				int tempIndex = 0;
				for(int j = 0  ;  j < temp.length;j++){
					if(j==(i+1)){
						tempIndex = 1;
					} 
					temp[j] = sp_buffer[j+tempIndex];
				}
				sp_buffer = temp;
				len = temp.length; 
			} 
		}
		return len;
	}


	private synchronized boolean sendAndRecvWithRetry(byte[] cmd, int retry) {
		if(cmd==null){
			return false;
		}
		StringBuffer sb = new StringBuffer();
		for(int i =0;i<cmd.length;i++){
			sb.append(cmd[i]+","); 
		}
		Log.d(TAG, "传感器发送cmd："+ BinaryUtils.toHex(cmd, cmd.length)+"///"+sb.toString()); 
		if (!isOpened || cmd == null || cmd.length <= 0) {
			return false;
		}
		byte[] toSend = new byte[cmd.length];
		System.arraycopy(cmd, 0, toSend, 0, cmd.length);
		int count = 0;
		while (count < retry) {
			//			toSend[3] = (byte) (count & 0xff);
			count++;
			try {
				this.mOutputStream.write(toSend);
				if (this.readWithTimeout(3001) > 0) {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public synchronized boolean open() {
		if (isOpened) {
			return true;
		}
		try { 
			this.mSerialPort = new SerialPort(new File(DEVICE_PATH), BAUDRATE,
					0);
			isOpened = true;
			this.mOutputStream = this.mSerialPort.getOutputStream();
			this.mInputStream = this.mSerialPort.getInputStream();
			 task = new SPReadTask(this.mSerialPort.getInputStream(), sp_buffer);
			Log.i(TAG, "open");
			return true;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public synchronized void close() {
		if (this.mSerialPort != null) {
			Log.i("GasDetector", "StartClose:"+BinaryUtils.toHex(sp_buffer, 10));
			try {
				mInputStream.close();
				mOutputStream.close();
			} catch (IOException e) {
				Log.i("GasDetector", "CloseErrr"+e.toString());
			}
			this.mSerialPort.close();
			Log.i("GasDetector", "EndClose:"+BinaryUtils.toHex(sp_buffer, 10));
		}
		this.mSerialPort = null;
		this.isOpened = false;
		if(executorService!=null){
			executorService.shutdown();
		}
	}

	/**
	 * 串口通讯
	 * 	cmd  通讯代码
	 * @return
	 */
	int readLen;
	public synchronized boolean SerialPointCommunication(byte[] cmd) {
		return this.sendAndRecvWithRetry(cmd, 1);
	}
	public synchronized boolean SerialPointCommunication(byte type) {
		byte [] cmd = new byte[6];
		cmd[0] = (byte) 0xfc;
		cmd[1] = (byte) 0x09;
		cmd[2] = (byte) 0x06;
		cmd[3] = type;
		cmd[4] = 0x01;
		cmd[5] = SerialCmdUtils.addBuffer(cmd);
		return this.sendAndRecvWithRetry(cmd, 1);
	}
	int outCount ;
	public  boolean isRead = false;
	class SPReadTask implements Callable<Integer> {
		byte[] checkBuffer = new byte[512];
		int buffer_len = 0;//缓冲区长度
		InputStream is;
		byte[] buffer = null;
		int id;
		public SPReadTask(InputStream inputStream, byte[] byteBuffer) {
			this.is = inputStream;
			this.buffer = byteBuffer;
			isRunning = true;
			id = (int)(Math.random()*9000000) + 1;
			Log.d(TAG,"生成缓冲区(id:"+id+")");
		}
		int count =0;
		boolean isRunning = true;
		@Override
		public Integer call() throws Exception {

			while (isRunning) {
				if(isRead==true){
					return 1;
				}
				readLen = mInputStream.read(buffer);
				StringBuffer sb = new StringBuffer();
				for(int i = 0 ; i < readLen;i++){
					sb.append(buffer[i]+",");
				}
				//				//1、 将数据放入到缓冲区里，进行处理  并且进行 关键帧长度验证
				System.arraycopy(buffer, 0, checkBuffer, buffer_len, readLen);
				buffer_len = buffer_len+readLen;
				Log.d(TAG,"读取了:"+readLen+"位数据  "+sb.toString()+"缓冲区长度为："+buffer_len);
				if(checkLen()==0){
					isRead = true;//读取成功
					return buffer_len;
				}
				if(buffer_len>=512){
					throw new Exception();  
				}
				
				Thread.sleep(100);
//				System.arraycopy(checkBuffer,0, sp_buffer, 0, readLen);
//				isRead = true;//读取成功
				return buffer_len;
			}
			return 1;

		}
		public int checkLen(){ 
			int len = -3;
			int i;
 			for(i= 0 ; i < checkBuffer.length;i++){
				if(checkBuffer[i]==-4){
					if((i+2)<=checkBuffer.length){
						len = checkBuffer[i+2];
						break;
					}else{
						return -1;//重新接受
					}
				}
			}
			if((len+i)>buffer_len){
				int count  = (len+i)-buffer_len;
				return -1;
			}else{
				try {
					sp_buffer = new byte[len];
					System.arraycopy(checkBuffer,i, sp_buffer, 0, len);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, e.toString()+"======================");
				}
				return 0;
			}
		}

		public int findBufferPrimaryLen(){
			for(int i = 0 ; i < checkBuffer.length;i++){
				if(checkBuffer[i]==-4&&checkBuffer[i+1]==8){
					int bufferSize = checkBuffer[i+2];
					byte [] buffer = new byte[bufferSize];
					for(int j = 0 ; j < buffer.length;j++){
						buffer[j] = checkBuffer[i+j];
					}
					if(checkBuffer[i+bufferSize-1]==checkBuffer(buffer, bufferSize)){
						this.buffer = buffer;
						return bufferSize;
					}
				}
			}
			return -1;
		}
	}


	public static int checkBuffer(byte [] buffer,int length){
		byte [] dec = new byte[length];
		System.arraycopy(buffer, 0, dec, 0, length);
		int sum = 0;
		for(int i = 0 ; i < length-1;i++){
			sum+= dec[i];
		}
		sum = sum-(sum*2)-1;
		return sum;
	}

}
