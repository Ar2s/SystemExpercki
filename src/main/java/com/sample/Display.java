package com.sample;

import java.util.ArrayList;
import java.util.List;

import com.sample.DroolsTest.*;
public class Display{
	private String question;
	private QuestionType QuestionType;
	public List<String> warianty;
	public String result;
	public boolean resultBool;

	public void setResult(String result, boolean resultBool){
		this.result = result;
		this.resultBool = resultBool;
	}
	
	public String getQuestion(){
		return question;
	}
	public QuestionType getQuestionType(){
		return QuestionType;
	}
	public Display(String question, QuestionType QuestionType){
		this.question = question;
		this.QuestionType = QuestionType;
		this.resultBool = false;
		this.warianty = new ArrayList<String>();
	}
	
	
	
	
	
	public void setResult(String question, String result, QuestionType QuestionType){
		this.question = question;
		this.QuestionType = QuestionType;
		this.result = result;
	}
	
	public void setResult(String question, String result, QuestionType QuestionType, List<String> warianty){
		this.question = question;
		this.QuestionType = QuestionType;
		this.result = result;
		this.warianty.clear();
		this.warianty = warianty;
	}
	
	
	
	
}  