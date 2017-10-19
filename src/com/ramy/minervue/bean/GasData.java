package com.ramy.minervue.bean;

import java.io.Serializable;

public class GasData implements Serializable{
	private String gasName;
	private String gasValue;
	public String getGasName() {
		return gasName;
	}
	public void setGasName(String gasName) {
		this.gasName = gasName;
	}
	public String getGasValue() {
		return gasValue;
	}
	public void setGasValue(String gasValue) {
		this.gasValue = gasValue;
	}
	public GasData(String gasName, String gasValue) {
		super();
		this.gasName = gasName;
		this.gasValue = gasValue;
	}
	public GasData() {
		super();
	}
	

}
