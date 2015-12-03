package com.sample;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

/**
 * This is a sample class to launch a rule.
 */

public class DroolsTest {
	private static Display display;
	private static String displayOld;
	private static Window window;
	private static Next_ next;
	private static Attribute attribute;
	
    public static final void main(String[] args) {
        try {
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

            display = new Display("Witaj.", QuestionType.NONE);
            window = new Window(display,next);
           
            ksession.insert(display);
            
        	while(next.get() == false){
			    try {
			       Thread.sleep(200);
			    } catch(InterruptedException ex) {
			    }
			}
            next.set(false);

            ksession.fireAllRules();
            boolean t = true;
            while(t){
            	QueryResults results = ksession.getQueryResults( "Wyswietl" );
            	for ( QueryResultsRow row : results ) {
            		display = (Display) row.get("display");
            	}
            	window.dispose(); 
            	
            	if (display.resultBool == true){
            		display = new Display("Preferowana postaæ dla ciebie to: " + display.result, QuestionType.NONE);
           		 	break;
            	}
            	
            	if(display.getQuestion() == displayOld){
            		display = new Display("Nie ma klasy spe³niaj¹cej twoje wymagania, zagraj magiem, bedzie fajnie :)", QuestionType.NONE);
            		 break;
            	}
            	displayOld = display.getQuestion();
            	
            	window = new Window(display,next);
            	while(next.get() == false){
				    try {
				       Thread.sleep(200);
				    } catch(InterruptedException ex) {
				    	t = false;
				    }
				}
            	
        		if(window.QuestionType == QuestionType.RADIOBUTTON){
        			attribute = new Attribute(display.result + answer);
        			ksession.insert(attribute);
        		}else if(window.QuestionType == QuestionType.YESNO){
        			for(JRadioButton i :window.radia){
        				if ((i.isSelected())&& i.getText() == "Tak"){
        					attribute = new Attribute(display.result);
        					System.out.println(attribute.tresc);
        					ksession.insert(attribute);
        				}
        			}
        		}else if(window.QuestionType == QuestionType.WIELOKROTYN){
        			for(JCheckBox i : window.checkBox)
        			{
        				if(i.isSelected())
        				{
        					attribute = new Attribute(display.result+i.getText());
        					
        					ksession.insert(attribute);
        				}
        			
        			}
        		}
	            next.set(false);
	            ksession.insert(display);
	            ksession.fireAllRules();
            }
           
            window = new Window(display,next);
            window.notkolejne();
            logger.close();
                       
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Koniec");
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("Sample.drl"), ResourceType.DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }
    
    public static enum QuestionType {RADIOBUTTON, WIELOKROTYN, NONE,YESNO};
  
    public static String answer;
        
    public static class Attribute{
    	public String tresc;
    	public Attribute(String tresc){
    		this.tresc = tresc;
    	}
    	
    }
    public static class Question{
    	public String tekst;
    	public Question(String tekst){
    		this.tekst = tekst;
    	}
    }
  
    public static class Display{
    	private String question;
    	private QuestionType QuestionType;
    	public List<String> warianty;
    	private String result;
    	private boolean resultBool;

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
}
