package com.ramy.minervue.bean;
public class Dervic{
	private String mac;//�豸MAC��ַ
	private String des;//�豸����
	private String type;//�豸����
	private String statu;//�Ƿ񱨾�
	private String url;//�Ƿ񱨾�
	
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	
	public Dervic(String mac, String des, String type, String statu, String url) {
		this.mac = mac;
		this.des = des;
		this.type = type;
		this.statu = statu;
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getStatu() {
		return statu;
	}
	public void setStatu(String statu) {
		this.statu = statu;
	}

}