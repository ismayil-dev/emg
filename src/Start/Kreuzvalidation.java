package Start;
import java.util.ArrayList;
import java.util.Collections;

import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.Klassifizierer;
import EMGVerarbeitung.ListenFunktionen;
import EMGVerarbeitung.Statistikauswerter;
import EMGVerarbeitung.Statistiken;


/*
	Cross validation
 */
public class Kreuzvalidation {
	
	ArrayList<EMGContainer> daten;
	
	
	public Kreuzvalidation(ArrayList<EMGContainer> e) {
		this.daten=e;
	}
	
	
	
	public void randomisiere() {
		Collections.shuffle(this.daten);
	}
	
	
	public Statistiken kstratifizierteKreuzvalidation(Klassifizierer k, int faltungen,int runs) {
		ArrayList<Statistiken> res=new ArrayList<Statistiken>();
		ArrayList<EMGContainer> orig=ListenFunktionen.tiefeKopie(this.daten);
		
		
		for(int i=0;i<runs;i++) {
			this.randomisiere();
			res.add(stratifizierteKreuzvalidation(k,faltungen));
			this.daten=ListenFunktionen.tiefeKopie(orig);
		}
		Statistiken s=Statistikauswerter.fasseZusammen(res);
		s.setWeitereInfos(s.getWeitereInfos()+"Kreuzvalidierung Folds: "+faltungen+"  Runs: "+runs);
		return s;
	}
	
	
	
	public Statistiken stratifizierteKreuzvalidation(Klassifizierer k, int faltungen) {
		int anzahlKlassen = ListenFunktionen.getAnzahlKlassen(daten);
		int samplesProKlasse= daten.size()/anzahlKlassen;
		int anzahlKlassenSamplesProFold = samplesProKlasse/faltungen;
		
		if(samplesProKlasse%faltungen!=0) {
			throw new StratifikationNichtMoeglichException();
		}

		ArrayList<ArrayList<EMGContainer>> folds = new ArrayList<ArrayList<EMGContainer>>();
		//Habe nun eine list, die Listen enthalten soll.
		
		//Genereiere nun für jeden fold eine Liste
		
		for(int i=0;i<faltungen;i++) {
			folds.add(new ArrayList<EMGContainer>());
		}
		//Habe jetzt eine Array List mit ArrayListen entsprechend der Faltungen

		//Laufe durch Liste
		for(ArrayList<EMGContainer> a: folds) {
			//Jetzt bin ich in Array x, füge dort alle klassen ein
			for(int klasse=0;klasse<anzahlKlassen;klasse++) {
				//Durchlaufe jede mgl Klasse einmal
				//Füge anazah an Samples hinzu
				for(int anzahlSamples=0;anzahlSamples<anzahlKlassenSamplesProFold;anzahlSamples++) {
					a.add(getSample(klasse));
				}
			}
		}
		ArrayList<Statistiken> stats=new ArrayList<Statistiken>(); 
		//Berechne x folds
		for(int i=0;i<faltungen;i++) {
			ArrayList<EMGContainer> trainset=new ArrayList<EMGContainer>();
			ArrayList<EMGContainer> testset=new ArrayList<EMGContainer>();
			
			
			//Generiere Test und Trainset für eine Aufteilung
			for(int x=0;x<faltungen;x++) {
				
				if(x!=i) {
					trainset.addAll(folds.get(x));
				}else {
					testset.addAll(folds.get(x));
				}
			}
			System.out.println("Faltung: "+i);
			k.generiereModell(trainset);
			stats.add(k.klassifiziereTestdaten(testset));
		}

		Statistiken res=Statistikauswerter.fasseZusammen(stats);
		res.setWeitereInfos(stats.get(0).getWeitereInfos()+" Kreuzvalidation, Folds: "+faltungen);
		return res;
	}
	
	
	public Statistiken vollstaendigeKreuzvalidation(Klassifizierer k) {
		ArrayList<Statistiken> stats=new ArrayList<Statistiken>(); 
		
		//Laufe durch alle daten
		for(int i=0;i<this.daten.size();i++) {
			ArrayList<EMGContainer> trainset=new ArrayList<EMGContainer>();
			ArrayList<EMGContainer> testset=new ArrayList<EMGContainer>();
			
			//Bilde test und trainset für i
			for(int x=0;x<this.daten.size();x++) {
				if(x!=i) {
					trainset.add(this.daten.get(x));
				}else {
					testset.add(this.daten.get(x));
				}	
			}
			
			k.generiereModell(trainset);
			stats.add(k.klassifiziereTestdaten(testset));
			System.out.println("Lauf: "+i);
		
		}
		
		double confmatrix[][]=new double[stats.get(0).getKonfusionsMatrix().length][stats.get(0).getKonfusionsMatrix()[0].length];
		
		for(Statistiken s: stats) {
			double tmp[][]=s.getKonfusionsMatrix();
			for(int i=0;i<tmp.length;i++) {
				for(int x=0;x<tmp[0].length;x++) {
					confmatrix[i][x]+=tmp[i][x];
				}
			}	
		}
		
		Statistiken res=new Statistiken(confmatrix);
		res.setWeitereInfos(stats.get(0).getWeitereInfos()+" Vollständige Kreuzvalidation, Folds: "+daten.size());

		return res;
		
	}
	
	//Get Sample und entferne von Liste
	private EMGContainer getSample(int klasse) {
		for(EMGContainer e: daten) {
			if(e.getZahl()==klasse) {
				daten.remove(e);
				return e;
			}
		}
		throw new RuntimeException("Keine Klasse gefunden");
	}
	
}
