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
	
	private static Attribute attribute;
	
    public static final void main(String[] args) {
        try {
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

            display = new Display("Witaj.", QuestionType.NONE);
            window = new Window(display);
           
            ksession.insert(display);
            
        	while(next == false){
			    try {
			       Thread.sleep(200);
			    } catch(InterruptedException ex) {
			    }
			}
            next = false;

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
            	
            	window = new Window(display);
            	while(next == false){
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
	            next = false;
	            ksession.insert(display);
	            ksession.fireAllRules();
            }
           
            window = new Window(display);
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
    public static boolean next;
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
        
    public static class Window extends JFrame implements ActionListener{
    	private JButton nextButton;
    	private JLabel Question;
    	private JComboBox lista;
    	public List<JCheckBox> checkBox;
    	private JCheckBox option;
    	private ButtonGroup radioGroup;
    	public List<JRadioButton> radia;
    	private QuestionType QuestionType;
    	
    	public Window(Display wybor){
    		super("Wybór rasy i klasy");
        	setLayout(null);
    		next = false;
    		QuestionType = wybor.getQuestionType();
    		radia = new ArrayList<JRadioButton>();
    		checkBox = new ArrayList<JCheckBox>();
    		Question =  new JLabel("<html><span style='font-size:15px;color:blue;font-family:courier;'>"+wybor.getQuestion()+"</span></html>");
    		Question.setBounds(15, 10, 500, 60);
    		add(Question);
    		nextButton = new JButton("<html><span style='font-size:10px;color:black;font-family:courier;'>"+"Next"+"</span></html>");
    		nextButton.setBounds(480, 50, 100, 40);
    		nextButton.addActionListener(this);
    		add(nextButton);

	    	
	    	 if(wybor.getQuestionType() == QuestionType.WIELOKROTYN)
	    	{
	    		JPanel panel = new JPanel();
	    		int count = 0 ;
	    		for (String i : wybor.warianty)
	    		{
	    			option  =new JCheckBox(i);
	    			checkBox.add(option);
	    			panel.add(option);
	    			option.setBounds(25, 70+25*count, 200, 20);
	    			add(option);
	    			count++;
	    		}
	    	
	    	}else if(wybor.getQuestionType() == QuestionType.RADIOBUTTON){
	    		radioGroup = new ButtonGroup();
	    		int count = 0;
	    		for(String i :wybor.warianty){
	    	    	JRadioButton radio;
	    	    	if(count == 0){
	    	    		radio = new JRadioButton(i, true);
	    	    	}else{
	    	    		radio = new JRadioButton(i, false);
	    	    	}
	    	    	radio.setBounds(25, 70+25*count, 200, 20);
	    	    	radioGroup.add(radio);
	    	    	radia.add(radio);
	    	    	add(radio);
	    	    	count++;
	    		}
	    	}else if(wybor.getQuestionType() == QuestionType.YESNO){
	    		radioGroup = new ButtonGroup();
	    		JRadioButton radio1 = new JRadioButton("Tak");
	    		radio1.setBounds(25, 70, 50, 20);
	    		
	    		JRadioButton radio2 = new JRadioButton("Nie", true);
	    		radio2.setBounds(25, 95, 50, 20);	    		
	    		radioGroup.add(radio1);
	    		radioGroup.add(radio2);
	    		
	    		add(radio1);
	    		add(radio2);
    	    	radia.add(radio1);
    	    	radia.add(radio2);
	    	}
	
	    	pack();
    		setVisible(true);
    		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - 250, Toolkit.getDefaultToolkit().getScreenSize().height/2 - 300);
    		setSize(600, 600); 
    	}
    	
    	public void notkolejne(){
    		nextButton.setVisible(false);
    	}
    	
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		
    		Object source = e.getSource();
    		if(QuestionType == QuestionType.RADIOBUTTON){
    			for(JRadioButton i :radia){
    				if (i.isSelected()) {
        				answer = i.getText();
    				}
    			}
    		}
    		if(source == nextButton){
    			next = true;			
    		}
    			
    	}
    }
    	
    public static class Display{
    	private String question;
    	private QuestionType QuestionType;
    	private List<String> warianty;
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
