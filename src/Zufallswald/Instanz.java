package Zufallswald;
import java.util.ArrayList;


import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Instanz {
	ArrayList<String> numerischeNamen=new ArrayList<String>();
	ArrayList<Double> numerischeWerte=new ArrayList<Double>();
	
	ArrayList<String> nominaleNamen=new ArrayList<String>();
	ArrayList<String> nominaleWerte=new ArrayList<String>();;
	
	int klasse=-1;
	
	
	public void addAttribut(String name, double val) {
		this.numerischeNamen.add(name);
		this.numerischeWerte.add(val);
	}
	
	
	public void addAttribut(String name, String val) {
		nominaleNamen.add(name);
		nominaleWerte.add(val);
	}
	
	
	public void printNumerischeAttribute() {
		for(int i=0;i<this.numerischeNamen.size();i++) {
			System.out.println(this.numerischeNamen.get(i)+"   "+this.numerischeWerte.get(i));
		}
	}
	
	public void setKlasse(int klasse) {
		this.klasse=klasse;
	}
	
	
	//Macht aus der Instanz eine WEKA Instance
	public Instance getInstance(Instances s) {
		DenseInstance in=new DenseInstance(numerischeNamen.size()+nominaleNamen.size()+1);
		in.setDataset(s);
		for(int i=0;i<numerischeNamen.size();i++) {
			in.setValue(i, numerischeWerte.get(i));
		}
		for(int i=0;i<nominaleNamen.size();i++) {
			in.setValue(i+numerischeNamen.size(), nominaleWerte.get(i));
			
		}
		in.setValue(numerischeNamen.size()+nominaleNamen.size(),""+this.klasse);
		return in;
	}
	
	
	
}
