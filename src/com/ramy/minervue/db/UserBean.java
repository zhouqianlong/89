package com.ramy.minervue.db;

import java.io.Serializable;

import com.example.tst.bean.ChatInfo;

public class UserBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1706233808417297683L;
	public String username;//用户名
	public String userIp;//ip地址
	public String likename;//昵称
	public String location;//位置
	public int callid;//电话组id
	public ChatInfo chatInfo;
	public int getCallid() {
		return callid;
	}
	public void setCallid(int callid) {
		this.callid = callid;
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	public String callTime;//拨打时间
	public String id;//
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserIp() {
		return userIp;
	}
	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}
	public String getMacAddress() {
		if(likename==null){
			return "";
		}
		return likename;
	}
	public void setMacAddress(String likename) {
		this.likename = likename;
	}
 
	public String getCallTime() {
		return callTime;
	}
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public UserBean(String username, String userIp, String likename,
			int callid, String callTime, String id) {
		this.username = username;
		this.userIp = userIp;
		this.likename = likename;
		this.callid = callid;
		this.callTime = callTime;
		this.id = id;
	}
	public UserBean(String username, String userIp) {
		this.username = username;
		this.userIp = userIp;
	}
	public UserBean(String username, String userIp,String id) {
		this.username = username;
		this.userIp = userIp;
		this.id = id;
	}
	public UserBean() {
	}
	
	public String getUserLastIP(){
		String [] ipSplit = userIp.split(".");
		return ipSplit[ipSplit.length-1];
	}
	public ChatInfo getChatInfo() {
		return chatInfo;
	}
	public void setChatInfo(ChatInfo chatInfo) {
		this.chatInfo = chatInfo;
	}
	
	
	

}
