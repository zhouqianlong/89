package com.ramy.minervue.bean;

import java.io.Serializable;
/**
 * <b>���ս���-����-ʵ����</b><br>
 * 	@see #getQuestionName() ��������<br>
 *  @see #getQuestionContent() ��������<br>
 *  @author   ��Ǭ��
 *
 */
public class QuestionInfo implements Serializable {
	/**
	 * 
	 */
	public int id;
	private static final long serialVersionUID = 1L;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	private String questionName;
	private String questionContent;
	public String getQuestionName() {
		return questionName;
	}
	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}
	public String getQuestionContent() {
		return questionContent;
	}
	public void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
	}

}
