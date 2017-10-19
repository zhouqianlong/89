package com.ramy.minervue.bean;

import java.io.Serializable;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkBean implements Serializable{
	private String path;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * 工单名
	 */
	private String WorkName;
	/**
	 * 工单图片描述
	 */
	private List<WorkContent> content;
	/**
	 * 情况描述
	 */
	private String describe;
	
	private String CreateTime;
	
	private String EndTime;
	
	private MyHandler handler;

	private List<String> SENDName;
	public String getWorkName() {
		return WorkName;
	}

	public void setWorkName(String workName) {
		WorkName = workName;
	}

	public List<WorkContent> getContent() {
		return content;
	}

	public void setContent(List<WorkContent> content) {
		this.content = content;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	public MyHandler getHandler() {
		return handler;
	}

	public void setHandler(MyHandler handler) {
		this.handler = handler;
	}

	public WorkBean(String workName, List<WorkContent> content,
			String describe, String createTime, String endTime,
			MyHandler handler) {
		super();
		WorkName = workName;
		this.content = content;
		this.describe = describe;
		CreateTime = createTime;
		EndTime = endTime;
		this.handler = handler;
	}

	public WorkBean() {
		super();
	}

	public List<String> getSENDName() {
		return SENDName;
	}

	public void setSENDName(List<String> sENDName) {
		SENDName = sENDName;
	}

 
	
	
}
