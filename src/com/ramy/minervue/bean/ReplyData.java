package com.ramy.minervue.bean;

import java.util.Date;
import java.util.List;

public class ReplyData {
	private String[] result;
	//�̶�����
	private String[] fPart;
	//����ֵ
	private String[] gasDatas;
	//cd4ʱ��
	private String[] date;
	//У���
	private String[] cs;
	//������־
	private String[] eFlag;
	public ReplyData() {
		super();
	}
	public ReplyData(String[] fPart, String[] gasDatas, String[] date,
			String[] cs, String[] eFlag) {
		super();
		this.fPart = fPart;
		this.gasDatas = gasDatas;
		this.date = date;
		this.cs = cs;
		this.eFlag = eFlag;
	}
	public String[] getResult() {
		return result;
	}
	public void setResult(String[] result) {
		this.result = result;
	}
	public String[] getfPart() {
		return fPart;
	}
	public void setfPart(String[] fPart) {
		this.fPart = fPart;
	}
	public String[] getGasDatas() {
		return gasDatas;
	}
	public void setGasDatas(String[] gasDatas) {
		this.gasDatas = gasDatas;
	}
	public String[] getDate() {
		return date;
	}
	public void setDate(String[] date) {
		this.date = date;
	}
	public String[] getCs() {
		return cs;
	}
	public void setCs(String[] cs) {
		this.cs = cs;
	}
	public String[] geteFlag() {
		return eFlag;
	}
	public void seteFlag(String[] eFlag) {
		this.eFlag = eFlag;
	}
	
	
	

}
