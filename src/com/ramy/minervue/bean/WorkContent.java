package com.ramy.minervue.bean;

public class WorkContent {
	private String ImagePath;
	private String Describe;
	public String getImagePath() {
		return ImagePath;
	}
	public void setImagePath(String imagePath) {
		ImagePath = imagePath;
	}
	public String getDescribe() {
		return Describe;
	}
	public void setDescribe(String describe) {
		Describe = describe;
	}
	public WorkContent(String imagePath, String describe) {
		ImagePath = imagePath;
		Describe = describe;
	}
	public WorkContent() {
	}
	
	
	
}
