package Zufallswald;

import java.util.ArrayList;
import java.util.Collections;

import weka.core.Attribute;


/* 
 * Klasse dient nur dazu, um aus den Instanzen eine Datenset für WEKA zu generieren.
 * 
 */
public class AttributeHalter {
	public ArrayList<Attribute> ats=new ArrayList<Attribute>();
	public ArrayList<ArrayList<String>> nomvalues=new ArrayList<ArrayList<String>>();
	public ArrayList<String> klassen=new ArrayList<String>();
	
	public AttributeHalter(ArrayList<Instanz> myins) {
		getAttribute(myins);
	}
	
	public ArrayList<Attribute> getAttribute(ArrayList<Instanz> myins){
		//Generiere die Attribute für die nums
		if(!ats.isEmpty())return ats;
		for(String s:myins.get(0).numerischeNamen) {
			ats.add(new Attribute(s));
		}
		//Numerische abgeschlossen jetzt wird kniffelig
		//Mache als erstes alle listen
		for(String s:myins.get(0).nominaleNamen) {
			nomvalues.add(new ArrayList<String>());
		}
		
		//Gehe durch alle Instanzen um die Nominal Values zu suchen.
		for(Instanz my: myins) {
			for(int i=0;i<my.nominaleWerte.size();i++) {
				if(!nomvalues.get(i).contains(my.nominaleWerte.get(i))) {
					nomvalues.get(i).add(my.nominaleWerte.get(i));
				}
				
			}
			if(!this.klassen.contains(""+my.klasse)) {
				this.klassen.add(""+my.klasse);
			}
		}
		
		for(int i=0;i<myins.get(0).nominaleNamen.size();i++) {
			ats.add(new Attribute(myins.get(0).nominaleNamen.get(i),nomvalues.get(i)));
		}
		Collections.sort(klassen);
		ats.add(new Attribute("class",klassen));	
		return ats;
	}
	
	
}
