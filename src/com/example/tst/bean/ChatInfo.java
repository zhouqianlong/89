package com.example.tst.bean;

import java.io.Serializable;

public class ChatInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6240488099748291325L;
	public int iconFromResId;
	public String iconFromUrl;
	public String content;
	public String time;
	public int type;
	public String url;
	public String mac;
	public String toMac;
	/**
	 * 0-未下载  /  1-下载中 / 2-下载完毕  / 3-下载失败
	 */
	public int downStatu; 
	public int fromOrTo;// 0 是收到的消息，1是发送的消息
	@Override
	public String toString() {
		return "ChatInfoEntity [iconFromResId=" + iconFromResId
				+ ", iconFromUrl=" + iconFromUrl + ", content=" + content
				+ ", time=" + time + ", fromOrTo=" + fromOrTo + "]";
	}
}
