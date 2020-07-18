package Zufallswald;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.Klassifizierer;
import EMGVerarbeitung.Output;
import EMGVerarbeitung.Statistiken;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class Zufallswald implements Klassifizierer, Serializable {
	
	private static final long serialVersionUID = 100;
	private Instances tdaten;
	private RandomForest wald=null;
	private int numStichprgroesse=-1;
	private int numAttribute=-1;
	
	int anzBaueme=100;
	public int getTreeNumber() {
		return anzBaueme;
	}


	public Zufallswald() {
		
	}
	
	
	public void setAnzahlBaueme(int num) {
		this.anzBaueme=num;
	}
	
	
	public void generiereModell(ArrayList<EMGContainer> trainset) {
		this.wald=null;
		assert this.wald==null;
		this.wald=new RandomForest();
		wald.setNumExecutionSlots(4);
		wald.setNumIterations(anzBaueme);
		if(this.numStichprgroesse>0) {
			wald.setBagSizePercent(numStichprgroesse);
		}
		
		ArrayList<Instanz> myins=generiereAttribute(trainset);
		Instances trainData=generiereWekaHeader(myins);
		
		if(this.numAttribute>0) {
			int gesamtAtts=trainData.numAttributes();
			
			double proz=(double)gesamtAtts*((double)this.numAttribute/100.0);
			wald.setNumFeatures((int)proz);
			
			
		}else {
			int gesamtAtts=trainData.numAttributes();
			double proz=Math.sqrt(gesamtAtts);
			wald.setNumFeatures((int)proz);
			
		}
		
		this.tdaten=trainData;
		
		try {
			wald.buildClassifier(trainData);
			ZufallswaldEinstellungen.addSetting(tdaten.toSummaryString());
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	
	public void setStichporbengroesse(int prozent) {
		this.numStichprgroesse=prozent;
	}
	
	public void setMerkmalsanzahl(int prozent) {
		this.numAttribute=prozent;
	}
	
	//Transformier eine Instanz in eine WEKA Instance
	private Instances transformiereInstanzenZuWeka(ArrayList<Instanz> ins) {
		Instances data=new Instances(this.tdaten,ins.size());
		for(Instanz my: ins) {
			DenseInstance in=(DenseInstance) my.getInstance(data);
			data.add(in);
		}
		//data.setClassIndex(data.numAttributes() - 1);
		assert data.size()==ins.size();
		return data;
	}
	
	
	
	
	//Wird benötigt um den Header für WEKA zu generieren, also alle Attribute etc. 
	private Instances generiereWekaHeader(ArrayList<Instanz> ins) {
		AttributeHalter h=new AttributeHalter(ins);
		Instances data=new Instances("data",h.getAttribute(ins),ins.size());		
		for(Instanz my: ins) {
			DenseInstance in=(DenseInstance) my.getInstance(data);
			data.add(in);
		}
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
	
	
	public ArrayList<Instanz> generiereAttribute(ArrayList<EMGContainer> emgs){
		ArrayList<Instanz> erg=new ArrayList<Instanz>();
		Instanz tmp;
		for(EMGContainer e: emgs) {
			tmp=generiereAttribute(e);
			erg.add(tmp);
		}
		return erg;
	}
	
	//Generiert die Attribute für ein EMG
	public Instanz generiereAttribute(EMGContainer e) {
		Instanz my= new Instanz();
		my.setKlasse(e.getZahl());
		AttributGenerator.partiellerDurchschnitt(e,my,15);
		AttributGenerator.regression(e,my,15);
		return my;
	}
	
	
	//Funktion dient zur Ausgabe der Daten als ARFF Format, welches mit WEKA genutzt werden kann.
	public void gebeARFFDatenAus(Instances daten) {
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(daten);
		 try {
			saver.setFile(new File("data.arff")); 
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public int klassifiziereInstanz(EMGContainer e) {
		ArrayList<EMGContainer> emg= new ArrayList<EMGContainer>();
		emg.add(e);
		ArrayList<Instanz> myins=generiereAttribute(emg);
		Instances ins=transformiereInstanzenZuWeka(myins);
		double result=-1;
		try {
			result=wald.classifyInstance(ins.firstInstance());
		} catch (Exception e1) {
			System.out.println("Fehler in Klassifiziere Instanz");
			e1.printStackTrace();
		}
		assert result>-1;
		return 0;
	}


	@Override
	public Statistiken klassifiziereTestdaten(ArrayList<EMGContainer> test) {
		ArrayList<Instanz> myins=generiereAttribute(test);
		Instances ins=transformiereInstanzenZuWeka(myins);
		Evaluation eval;
		Statistiken s=null;
		try {
			eval = new Evaluation(tdaten);
			eval.evaluateModel(wald, ins);
			s=new Statistiken();
			s.setKonfusionsMatrix(eval.confusionMatrix());
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		s.setWeitereInfos("Random Forest - Number Trees: "+this.anzBaueme);
		assert s!=null;
		return s;
	}

	public void setWald(RandomForest w) {
		this.wald=w;
	}
	
	public void speichereModell(String name) {
		Output.schreibeModell(this.wald, name);	
	}
}
