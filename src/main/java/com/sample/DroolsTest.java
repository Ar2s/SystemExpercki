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
            // load up the knowledge base
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

            //tworzenie pierwszego okna
            display = new Display("Witaj.", QuestionType.NONE);
            window = new Window(display);
            
            //wrzucamy wysietlenie do bazy wiedzy
            ksession.insert(display);
            
            //oczekiwanie na przyciœniêcie przycisku nextButton
        	while(next == false){
			    try {
			       Thread.sleep(200);
			    } catch(InterruptedException ex) {
			    }
			}
            next = false;

            //odpalamy system ekspercki
            ksession.fireAllRules();
            boolean t = true;
            while(t){
            	//pobieramy zaQuestionm co wyœwietliæ w oknie z bazy wiedzy
            	QueryResults results = ksession.getQueryResults( "Wyswietl" );
            	for ( QueryResultsRow row : results ) {
            		display = (Display) row.get("display");
            	}
            	window.dispose(); //niszczymy Window, aby stworzyc Window
            	
            	//jezeli w wyswietleniu jest pokemon resultowy to konczymy
            	if (display.resultBool == true){
            		display = new Display("Preferowana postaæ dla ciebie to: " + display.result, QuestionType.NONE);
           		 	break;
            	}
            	
            	//jezeli Question jest takie jak bylo poprzednio to zadna regula sie nie odpalila
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
            	
            	//w zaleznosci od formatu okna do wyswietlenia dodaje odpowiednia ceche
        		if(window.QuestionType == QuestionType.RADIOBUTTON){
        			attribute = new Attribute(display.result + odpowiedz);
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
	            
	            //wrzucamy wyswietlenie, aby jakas regula sie dopasowala
	            ksession.insert(display);
	            ksession.fireAllRules();
            }
           
            //na sam koniec wyswietlamy ostatnie Window
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
    
    public static enum QuestionType {YESNO, RADIOBUTTON, LIST, NONE,WIELOKROTYN};
    public static boolean next;
    public static String odpowiedz;
        


        
    public static class Window extends JFrame implements ActionListener{
    	private JButton nextButton;
    	private JLabel Question;
    	private JComboBox lista;
    	public List<JCheckBox> checkBox;
    	private JCheckBox opcja;
    	private ButtonGroup radioGroup;
    	public List<JRadioButton> radia;
    	private QuestionType QuestionType;
    	
    	public Window(Display wybor){
    		super("Wybór rasy i klasy");

        	setLayout(null);
        	try {
	    		setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File("image/tiara.bmp")))));
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
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

	    		
	    			opcja  =new JCheckBox(i){
	    			    protected void paintComponent(Graphics g)
	    			    {
	    			        g.setColor( getBackground() );
	    			        g.fillRect(0, 0, getWidth(), getHeight());
	    			        super.paintComponent(g);
	    			    }
	    			};;
	    			
	    			checkBox.add(opcja);
	    			
	    			panel.add(opcja);
	    			opcja.setBounds(25, 70+25*count, 200, 20);
	    			add(opcja);
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
    		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - 150, Toolkit.getDefaultToolkit().getScreenSize().height/2 - 200);
    		setSize(600, 600); // if to zmienisz to zmien up tez
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
        				odpowiedz = i.getText();
    				}
    			}
    		}
    		if(source == nextButton){
    			next = true;			
    		}
    			
    	}
    }
    	
    
   
    
}
