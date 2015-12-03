package com.sample;

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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
		

public class DroolsTest {
	private static Display display;
	private static Window window;
	
	private static Attribute attribute;
	
    public static final void main(String[] args) {
        try {
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

            display = new Display("Za³ó¿ tiarê, i odpowiadaj na pytania, pomogê.", QuestionType.NONE);
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
            	QueryResults results = ksession.getQueryResults( "Display" );
            	for ( QueryResultsRow row : results ) {
            		display = (Display) row.get("display");
            	}
            	
            	if (display.resultBool == true){
            		display = new Display("Proponowana postaæ: " + display.result, QuestionType.NONE);
           		 	break;
            	}

            	window.Refresh(display);
            	while(next == false){
				    try {
				       Thread.sleep(100);
				    } catch(InterruptedException ex) {
				    	t = false;
				    }
				}
            	
        		if(window.QuestionType == QuestionType.SIMPLECHOICE){
        			attribute = new Attribute(display.result + answer);
        			System.out.println(attribute.content);
        			ksession.insert(attribute);
        		}else if(window.QuestionType == QuestionType.YESNO){
        			for(JRadioButton i :window.radioButtonList){
        				if ((i.isSelected())&& i.getText() == "Tak"){
        					attribute = new Attribute(display.result);
        					System.out.println(attribute.content);
        					ksession.insert(attribute);
        				}
        			}
        		}else if(window.QuestionType == QuestionType.MULTICHOICE){
        			for(JCheckBox i : window.checkBox)
        			{
        				if(i.isSelected())
        				{
        					attribute = new Attribute(display.result+i.getText());
        					System.out.println(attribute.content);
        					ksession.insert(attribute);
        				}
        			
        			}
        		}
	            next = false;
	            ksession.insert(display);
	            ksession.fireAllRules();
            }
           
            window.Refresh(display);
            window.hidebutton();
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
    
    public static enum QuestionType {SIMPLECHOICE, MULTICHOICE, NONE,YESNO};
    public static boolean next;
    public static String answer;
        
    public static class Attribute{
    	public String content;
    	public Attribute(String content){
    		this.content = content;
    	}
    	
    }
    public static class Question{
    	public String text;
    	public Question(String text){
    		this.text = text;
    	}
    }
        
    public static class Window extends JFrame implements ActionListener{
    	private JButton nextButton;
    	private JLabel Question;
    	public List<JCheckBox> checkBox;
    	private JCheckBox option;
    	private ButtonGroup radioGroup;
    	public List<JRadioButton> radioButtonList;
    	private QuestionType QuestionType;
    	
    	public Window(Display wybor){
    		super("Magiczna Tiara Przydzia³u");
        	this.Refresh(wybor);
        	setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - 250, Toolkit.getDefaultToolkit().getScreenSize().height/2 - 300);
    		setSize(600, 600); 
    	}
    	
    	public void Refresh(Display wybor){
        	setLayout(null);
        	try {
        		setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File("image/tiara.bmp")))));
        	} 
        	catch (IOException e) {
        		e.printStackTrace();
        	}
        	
    		next = false;
    		QuestionType = wybor.getQuestionType();
    		radioButtonList = new ArrayList<JRadioButton>();
    		checkBox = new ArrayList<JCheckBox>();
    		Question =  new JLabel("<html><span style='font-size:15px;color:blue;font-family:courier;'>"+wybor.getQuestion()+"</span></html>");
    		Question.setBounds(15, 10, 500, 60);
    		add(Question);
    		nextButton = new JButton("<html><span style='font-size:10px;color:black;font-family:courier;'>"+"Next"+"</span></html>");
    		nextButton.setBounds(480, 50, 100, 40);
    		nextButton.addActionListener(this);
    		add(nextButton);

	    	
	    	 if(wybor.getQuestionType() == QuestionType.MULTICHOICE)
	    	{
	    		JPanel panel = new JPanel();
	    		int count = 0 ;
	    		for (String i : wybor.variants)
	    		{
	    			option  =new JCheckBox(i);
	    			checkBox.add(option);
	    			panel.add(option);
	    			option.setBounds(25, 70+25*count, 200, 20);
	    			add(option);
	    			count++;
	    		}
	    	
	    	}else if(wybor.getQuestionType() == QuestionType.SIMPLECHOICE){
	    		radioGroup = new ButtonGroup();
	    		int count = 0;
	    		for(String i :wybor.variants){
	    	    	JRadioButton radio;
	    	    	if(count == 0){
	    	    		radio = new JRadioButton(i, true);
	    	    	}else{
	    	    		radio = new JRadioButton(i, false);
	    	    	}
	    	    	radio.setBounds(25, 70+25*count, 200, 20);
	    	    	radioGroup.add(radio);
	    	    	radioButtonList.add(radio);
	    	    	add(radio);
	    	    	count++;
	    		}
	    	}else if(wybor.getQuestionType() == QuestionType.YESNO){
	    		radioGroup = new ButtonGroup();
	    		JRadioButton radioButtonYes = new JRadioButton("Tak");
	    		radioButtonYes.setBounds(25, 70, 50, 20);
	    		
	    		JRadioButton radioButtonNo = new JRadioButton("Nie", true);
	    		radioButtonNo.setBounds(25, 95, 50, 20);	    		
	    		radioGroup.add(radioButtonYes);
	    		radioGroup.add(radioButtonNo);
	    		
	    		add(radioButtonYes);
	    		add(radioButtonNo);
    	    	radioButtonList.add(radioButtonYes);
    	    	radioButtonList.add(radioButtonNo);
	    	}
    		setVisible(true);
    		setSize(600, 600); 
    	}
    	
    	public void hidebutton(){
    		nextButton.setVisible(false);
    	}
    	
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		
    		Object source = e.getSource();
    		if(QuestionType == QuestionType.SIMPLECHOICE){
    			for(JRadioButton i :radioButtonList){
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
    	private List<String> variants;
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
    		this.variants = new ArrayList<String>();
    	}

    	public void setResult(String question, String result, QuestionType QuestionType){
    		this.question = question;
    		this.QuestionType = QuestionType;
    		this.result = result;
    	}
    	public void setResult(String question, String result, QuestionType QuestionType, List<String> variants){
    		this.question = question;
    		this.QuestionType = QuestionType;
    		this.result = result;
    		this.variants.clear();
    		this.variants = variants;
    	}
    }     
}
