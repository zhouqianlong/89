package com.ramy.minervue.db;

import java.io.Serializable;

public class RtspCamera implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3276071954389649689L;
	/**
	 * 摄像头rtsp摄像头数据
	 */
	public int id;
	public String cameraName;//用户名
	public String cameraAddress;//地址
	public String cameraPort;//端口
	public String getCameraName() {
		return cameraName;
	}
	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}
	public String getCameraAddress() {
		return cameraAddress;
	}
	public void setCameraAddress(String cameraAddress) {
		this.cameraAddress = cameraAddress;
	}
	public String getCameraPort() {
		return cameraPort;
	}
	public void setCameraPort(String cameraPort) {
		this.cameraPort = cameraPort;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public RtspCamera(String cameraName, String cameraAddress,
			String cameraPort) {
		this.cameraName = cameraName;
		this.cameraAddress = cameraAddress;
		this.cameraPort = cameraPort;
	}
	 
	public RtspCamera(int id, String cameraName, String cameraAddress,
			String cameraPort) {
		this.id = id;
		this.cameraName = cameraName;
		this.cameraAddress = cameraAddress;
		this.cameraPort = cameraPort;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public RtspCamera() {
	}
 
	
	
	

}
