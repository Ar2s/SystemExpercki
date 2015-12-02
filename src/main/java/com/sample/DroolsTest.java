package com.sample;

import java.awt.*;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

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
	private static Wyswietlanie w;
	private static String wOld;
	private static Okno o;
	private static List<Pytanie> p;
	private static Cecha c;
	
    public static final void main(String[] args) {
        try {
            // load up the knowledge base
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

            //tworzenie pierwszego okna
            w = new Wyswietlanie("Pomogê Ci wybrac klasê :)", FormatOdp.NONE);
            o = new Okno(w);
            
            //wrzucamy wysietlenie do bazy wiedzy
            ksession.insert(w);
            
            //oczekiwanie na przyciœniêcie przycisku dalej
        	while(czyDalej == false){
			    try {
			       Thread.sleep(200);
			    } catch(InterruptedException ex) {
			    }
			}
            czyDalej = false;

            //odpalamy system ekspercki
            ksession.fireAllRules();
            boolean t = true;
            while(t){
            	//pobieramy zapytaniem co wyœwietliæ w oknie z bazy wiedzy
            	QueryResults results = ksession.getQueryResults( "Wyswietl" );
            	for ( QueryResultsRow row : results ) {
            		w = (Wyswietlanie) row.get("w");
            	}
            	o.dispose(); //niszczymy okno, aby stworzyc okno
            	
            	//jezeli w wyswietleniu jest pokemon wynikowy to konczymy
            	if (w.czyWynik == true){
           		 	w = new Wyswietlanie("Preferowana postaæ dla ciebie to: " + w.wynik, FormatOdp.NONE);
           		 	break;
            	}
            	
            	//jezeli pytanie jest takie jak bylo poprzednio to zadna regula sie nie odpalila
            	if(w.getTrescZapytania() == wOld){
            		 w = new Wyswietlanie("Nie ma klasy spe³niaj¹cej twoje wymagania, zagraj magiem, bedzie fajnie :)", FormatOdp.NONE);
            		 break;
            	}
            	wOld = w.getTrescZapytania();
            	
            	o = new Okno(w);
            	while(czyDalej == false){
				    try {
				       Thread.sleep(200);
				    } catch(InterruptedException ex) {
				    	t = false;
				    }
				}
            	
            	//w zaleznosci od formatu okna do wyswietlenia dodaje odpowiednia ceche
        		if(o.formatOdp == FormatOdp.LIST){
        			c = new Cecha(w.wynik + o.lista.getSelectedItem().toString());
        			ksession.insert(c);
        		}else if(o.formatOdp == FormatOdp.RADIOBUTTON){
        			c = new Cecha(w.wynik + odpowiedz);
        			ksession.insert(c);
        		}else if(o.formatOdp == FormatOdp.YESNO){
        			for(JRadioButton i :o.radia){
        				if ((i.isSelected())&& i.getText() == "Tak"){
        					c = new Cecha(w.wynik);
        					ksession.insert(c);
        				}
        			}
        		}else if(o.formatOdp == FormatOdp.WIELOKROTYN){
        			for(JCheckBox i : o.checkBox)
        			{
        				if(i.isSelected())
        				{
        					c = new Cecha(w.wynik+i.getText());
        					ksession.insert(c);
        				}
        			
        			}
        		}
	            czyDalej = false;
	            
	            //wrzucamy wyswietlenie, aby jakas regula sie dopasowala
	            ksession.insert(w);
	            ksession.fireAllRules();
            }
           
            //na sam koniec wyswietlamy ostatnie okno
            o = new Okno(w);
            o.notDalej();
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
    
    public static enum FormatOdp {YESNO, RADIOBUTTON, LIST, NONE,WIELOKROTYN};
    public static boolean czyDalej;
    public static String odpowiedz;
        
    public static class Cecha{
    	public String tresc;
    	public Cecha(String tresc){
    		this.tresc = tresc;
    	}
    	
    }
    public static class Pytanie{
    	public String tekst;
    	public Pytanie(String tekst){
    		this.tekst = tekst;
    	}
    }
        
    public static class Okno extends JFrame implements ActionListener{
    	private JButton dalej;
    	private JLabel pytanie;
    	private JComboBox lista;
    	public List<JCheckBox> checkBox;
    	private JCheckBox opcja;
    	private ButtonGroup radioGroup;
    	public List<JRadioButton> radia;
    	private FormatOdp formatOdp;
    	
    	public Okno(Wyswietlanie wybor){
    		super("Wybór postaci");
    		setLayout(null);
    		czyDalej = false;
    		formatOdp = wybor.getFormatOdp();
    		radia = new ArrayList<JRadioButton>();
    		checkBox = new ArrayList<JCheckBox>();
    		dalej = new JButton("Dalej");
    		dalej.setBounds(160, 300, 100, 30);
    		dalej.addActionListener(this);
    		add(dalej);

    		pytanie = new JLabel(wybor.getTrescZapytania());
    		pytanie.setBounds(20, 20, 460, 20);
    		add(pytanie);

	    	if(wybor.getFormatOdp() == FormatOdp.LIST){
	    		lista = new JComboBox();
	    		for (String i : wybor.opcje)
	    		      lista.addItem(i);
	    		lista.setBounds(20, 50, 150, 20);
	    		add(lista);
	    	}else if(wybor.getFormatOdp() == FormatOdp.WIELOKROTYN)
	    	{
	    		JPanel panel = new JPanel();
	    		int count = 0 ;
	    		for (String i : wybor.opcje)
	    		{

	    		
	    			opcja  =new JCheckBox(i);
	    			
	    			checkBox.add(opcja);
	    			
	    			panel.add(opcja);
	    			opcja.setBounds(20, 50+20*count, 200, 20);
	    			add(opcja);
	    			count++;
	    		}
	    	
	    		//chcekBox.setBounds(20, 50+20*count, 200, 20);
	    		
	    	}else if(wybor.getFormatOdp() == FormatOdp.RADIOBUTTON){
	    		radioGroup = new ButtonGroup();
	    		int count = 0;
	    		for(String i :wybor.opcje){
	    	    	JRadioButton radio;
	    	    	if(count == 0){
	    	    		radio = new JRadioButton(i, true);
	    	    	}else{
	    	    		radio = new JRadioButton(i, false);
	    	    	}
	    	    	radio.setBounds(20, 50+20*count, 200, 20);
	    	    	radioGroup.add(radio);
	    	    	radia.add(radio);
	    	    	add(radio);
	    	    	count++;
	    		}
	    	}else if(wybor.getFormatOdp() == FormatOdp.YESNO){
	    		radioGroup = new ButtonGroup();
	    		JRadioButton radio1 = new JRadioButton("Tak");
	    		radio1.setBounds(20, 50, 50, 20);
	    		
	    		JRadioButton radio2 = new JRadioButton("Nie", true);
	    		radio2.setBounds(20, 70, 50, 20);	    		
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
    		setSize(300, 400); // if to zmienisz to zmien up tez
    	}
    	
    	public void notDalej(){
    		dalej.setVisible(false);
    	}
    	
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		Object source = e.getSource();
    		
    		if(formatOdp == FormatOdp.LIST){
    			odpowiedz = lista.getSelectedItem().toString();
    		}else if(formatOdp == FormatOdp.RADIOBUTTON){
    			for(JRadioButton i :radia){
    				if (i.isSelected()) {
        				odpowiedz = i.getText();
    				}
    			}
    		}
    		if(source == dalej){
    			czyDalej = true;			
    		}
    			
    	}
    }
    	
    
    public static class Wyswietlanie{
    	private String trescPytania;
    	private FormatOdp formatOdp;
    	private List<String> opcje;
    	private String wynik;
    	private boolean czyWynik;

    	public Wyswietlanie(String trescPytania, FormatOdp formatOdp){
    		this.trescPytania = trescPytania;
    		this.formatOdp = formatOdp;
    		this.czyWynik = false;
    		this.opcje = new ArrayList<String>();
    	}
    	
    	//metoda sluzy do ustawiania pytan z wieloma radiobuttonami
    	public void set(String trescPytania, String wynik, FormatOdp formatOdp, List<String> opcje){
    		this.trescPytania = trescPytania;
    		this.formatOdp = formatOdp;
    		this.wynik = wynik;
    		this.opcje.clear();
    		this.opcje = opcje;
    	}
    	
    	
    	//metoda sluzy do ustawiania pytan "Czy...?"
    	public void set(String trescPytania, String wynik, FormatOdp formatOdp){
    		this.trescPytania = trescPytania;
    		this.formatOdp = formatOdp;
    		this.wynik = wynik;
    	}
    	
    	//metoda sluzy do zapisu wynikowego pokemona
    	public void set(String wynik, boolean czyWynik){
    		this.wynik = wynik;
    		this.czyWynik = czyWynik;
    	}
    	
    	public String getTrescZapytania(){
    		return trescPytania;
    	}
    	public FormatOdp getFormatOdp(){
    		return formatOdp;
    	}
    	
    	
    	
    }  
    
}
