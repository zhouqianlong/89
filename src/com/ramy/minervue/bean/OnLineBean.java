package com.ramy.minervue.bean;

import java.io.Serializable;
/**
 * 在线列表
 * @author 周乾龙
 *
 */
public class OnLineBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2289568826710936005L;
	private String ip;//ip地址
	private String longitude;//经度
	private String latitude;//纬度
	private int id ;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public OnLineBean(String ip, String longitude, String latitude, int id) {
		this.ip = ip;
		this.longitude = longitude;
		this.latitude = latitude;
		this.id = id;
	}
	public OnLineBean() {
	}
	
}
