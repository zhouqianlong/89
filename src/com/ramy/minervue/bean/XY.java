package com.ramy.minervue.bean;
public class XY{
	private double x;
	private double y;
	private String ipaddress;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 经度
	 * @return
	 */
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * y纬度
	 * @return
	 */
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	/**
	 * 
	 * @param x经度
	 * @param y纬度
	 * @param msg
	 */
	public XY(double x, double y, String ipaddress,String name) {
		this.x = x;
		this.y = y;
		this.ipaddress = ipaddress;
		this.name=  name;
	}
	public XY(String x, String y, String ipaddress,String name) {
		this.x = Double.valueOf(x);
		this.y = Double.valueOf(y);
		this.ipaddress = ipaddress;
		this.name= name;
	}
	public XY() {
	}
 }