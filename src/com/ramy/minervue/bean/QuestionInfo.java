package com.ramy.minervue.bean;

import java.io.Serializable;
/**
 * <b>拍照界面-描述-实体类</b><br>
 * 	@see #getQuestionName() 问题名称<br>
 *  @see #getQuestionContent() 描述内容<br>
 *  @author   周乾龙
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
