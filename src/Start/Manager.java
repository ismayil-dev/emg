package Start;
import java.util.ArrayList;
import java.util.Collections;

import DynamicTimeWarping.DynamicTimeWarping;
import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.EMGLeser;
import EMGVerarbeitung.Klassifizierer;
import EMGVerarbeitung.ListenFunktionen;
import EMGVerarbeitung.Output;
import EMGVerarbeitung.Statistikauswerter;
import EMGVerarbeitung.Statistiken;
import KNaechsteNachbarn.NaechsteNachbarn;
import Zufallswald.Zufallswald;






public class Manager {
	
	private ArrayList<EMGContainer> datensatz=new ArrayList<EMGContainer>();
	private ArrayList<EMGContainer> trainset=new ArrayList<EMGContainer>();
	private ArrayList<EMGContainer> testset=new ArrayList<EMGContainer>();
	
	
	/*
	 * Verwendung: Schon gesplitteter Datensatz: Aufrufen und dann runDTW oder runRF
	 * Sonst: Daten splitten lassen und dann run.
	 * 
	 */
	
	
	public void lernrate() {
		ListenFunktionen.gleichrichten(datensatz);
		ListenFunktionen.tiefpassfilterFFT(datensatz, 15);
		
		ArrayList<EMGContainer> trainDTW=this.getDataset();
		ArrayList<EMGContainer> trainKNN=this.getDataset();
		ArrayList<EMGContainer> trainRF=this.getDataset();
		
		ListenFunktionen.kuerzeOhneRuhe(trainDTW);
		ListenFunktionen.kuerzeOhneRuhe(trainRF);
		
		
		for(int samples=10;samples<101;samples+=10) {
		
			ArrayList<EMGContainer> trainDTWt=this.getTeilmenge(samples,trainDTW);
			ArrayList<EMGContainer> trainKNNt=this.getTeilmenge(samples,trainKNN);
			ArrayList<EMGContainer> trainRFt=this.getTeilmenge(samples,trainRF);
			
			int k=5;
			
			NaechsteNachbarn n=new NaechsteNachbarn(k);
			DynamicTimeWarping d=new DynamicTimeWarping();
			Zufallswald w=new Zufallswald();
			
			Kreuzvalidation kkkn=new Kreuzvalidation(ListenFunktionen.tiefeKopie(trainKNNt));
			Statistiken knn=kkkn.stratifizierteKreuzvalidation(n, 10);
			Output.schreibeErgebnisse(knn, "Samples: "+samples, "KNN");
			
			Kreuzvalidation kdtw=new Kreuzvalidation(ListenFunktionen.tiefeKopie(trainDTWt));
			Statistiken dtw=kdtw.stratifizierteKreuzvalidation(d, 10);
			Output.schreibeErgebnisse(dtw, "Samples: "+samples, "DTW");
			
			Kreuzvalidation krf=new Kreuzvalidation(ListenFunktionen.tiefeKopie(trainRFt));
			Statistiken rf=krf.stratifizierteKreuzvalidation(w, 10);
			Output.schreibeErgebnisse(rf, "Samples: "+samples, "RF");
		
		}
		
		
	}
	
	
	
	
	public void multiplePersonen() {
		ArrayList<Statistiken> sdtw=new ArrayList<Statistiken>();
		ArrayList<Statistiken> sknn=new ArrayList<Statistiken>();
		ArrayList<Statistiken> srf=new ArrayList<Statistiken>();
		
		ArrayList<ArrayList<EMGContainer>> all =new ArrayList<ArrayList<EMGContainer>>();
		
		try {
			for(int i=1;i<13;i++) {
				if(i!=5) {
					ArrayList<EMGContainer> r= EMGLeser.leseVerzeichnis("/Users/nils/Documents/BA/EclipseData/"+i);
					all.add(r);
				}
			}
		} catch (Exception e) {
			
		}
		//Grundlegende FItlerung
		for(ArrayList<EMGContainer> arl:all) {
			ListenFunktionen.gleichrichten(arl);
			ListenFunktionen.tiefpassfilterFFT(arl, 15);
		}
		
		
		//Jeder Datensatz soll einmal zum Testen dienen.
		for(int i=0;i<all.size();i++) {
			//Build test und train
			ArrayList<EMGContainer> test=all.get(i);
			ArrayList<EMGContainer> train=new ArrayList<EMGContainer>();
			for(int j=0;j<all.size();j++) {
				if(j!=i) {
					train.addAll(all.get(j));
				}
			}
			//Jetzt ist test und train für dsatz i gebildet. 
			//Nun Filtere fü jedes Verfahren und lasse laufen
			
			ArrayList<EMGContainer> trainDTW=ListenFunktionen.tiefeKopie(train);
			ArrayList<EMGContainer> trainKNN=ListenFunktionen.tiefeKopie(train);
			ArrayList<EMGContainer> trainRF=ListenFunktionen.tiefeKopie(train);
			
			ListenFunktionen.kuerzeOhneRuhe(trainDTW);
			ListenFunktionen.kuerzeOhneRuhe(trainRF);
			//Trainsätze sind fertig führe nun Verfahren aus
			
			System.out.println("TEstsatz: "+i);
			NaechsteNachbarn n=new NaechsteNachbarn(26);
			DynamicTimeWarping d=new DynamicTimeWarping();
			Zufallswald w=new Zufallswald();
			
			n.generiereModell(trainKNN);
			d.generiereModell(trainDTW);
			w.generiereModell(trainRF);
			
			Statistiken s;
			
			s=n.klassifiziereTestdaten(test);
			Output.schreibeErgebnisse(s, "Nummer test: "+i, "knn");
			sknn.add(s);
			
			s=d.klassifiziereTestdaten(test);
			Output.schreibeErgebnisse(s, "Nummer test: "+i, "dtw");
			sdtw.add(s);
			
			s=w.klassifiziereTestdaten(test);
			Output.schreibeErgebnisse(s, "Nummer test: "+i, "rf");
			srf.add(s);
			
		}
		
		Output.schreibeErgebnisse(sknn, "Zusammenfassung", "KNN");
		Output.schreibeErgebnisse(sdtw, "Zusammenfassung", "DTW");
		Output.schreibeErgebnisse(srf, "Zusammenfassung", "rf");
	}
	
	
	
	
	
	
	
	
	
	public void calcTime() {
		ListenFunktionen.gleichrichten(datensatz);
		ListenFunktionen.tiefpassfilterFFT(datensatz, 15);
		ListenFunktionen.kuerzeOhneRuhe(datensatz);
		int sum=0;
		for(EMGContainer e:this.datensatz) {
			sum+=e.getLeange();
		}
		System.out.println("Avg Length: "+(sum/(double)this.datensatz.size()));
	}
	
	
	
	public Manager(ArrayList<EMGContainer> datensatz) {
		super();
		this.datensatz = datensatz;
	}
	
	public void runAllThree(int k,int dsatz) {
		ListenFunktionen.gleichrichten(this.datensatz);
		ListenFunktionen.tiefpassfilterFFT(this.datensatz, 15);
		ArrayList<EMGContainer> dtwDset=this.getDataset();
		ListenFunktionen.kuerzeOhneRuhe(dtwDset);
		ArrayList<EMGContainer> rfDset=this.getDataset();
		ListenFunktionen.kuerzeOhneRuhe(rfDset);
		ArrayList<EMGContainer> knnDset=this.getDataset();
		
		NaechsteNachbarn n=new NaechsteNachbarn(k);
		DynamicTimeWarping d=new DynamicTimeWarping();
		Zufallswald w=new Zufallswald();
		
		Kreuzvalidation kkkn=new Kreuzvalidation(ListenFunktionen.tiefeKopie(knnDset));
		Statistiken knn=kkkn.stratifizierteKreuzvalidation(n, 10);
		Output.schreibeErgebnisse(knn, "Dsatz: "+dsatz, "KNN");
		
		Kreuzvalidation kdtw=new Kreuzvalidation(ListenFunktionen.tiefeKopie(dtwDset));
		Statistiken dtw=kdtw.stratifizierteKreuzvalidation(d, 10);
		Output.schreibeErgebnisse(dtw, "Dsatz: "+dsatz, "DTW");
		
		Kreuzvalidation krf=new Kreuzvalidation(ListenFunktionen.tiefeKopie(rfDset));
		Statistiken rf=krf.stratifizierteKreuzvalidation(w, 10);
		Output.schreibeErgebnisse(rf, "Dsatz: "+dsatz, "RF");	
	}
	
	public void runAllThreeVoll(int k, int dsatz) {
		ListenFunktionen.gleichrichten(this.datensatz);
		ListenFunktionen.tiefpassfilterFFT(this.datensatz, 15);
		ArrayList<EMGContainer> dtwDset=this.getDataset();
		ListenFunktionen.kuerzeOhneRuhe(dtwDset);
		ArrayList<EMGContainer> rfDset=this.getDataset();
		ListenFunktionen.kuerzeOhneRuhe(rfDset);
		ArrayList<EMGContainer> knnDset=this.getDataset();
		
		NaechsteNachbarn n=new NaechsteNachbarn(k);
		DynamicTimeWarping d=new DynamicTimeWarping();
		Zufallswald w=new Zufallswald();
		
		Kreuzvalidation kkkn=new Kreuzvalidation(ListenFunktionen.tiefeKopie(knnDset));
		Statistiken knn=kkkn.vollstaendigeKreuzvalidation(n);
		Output.schreibeErgebnisse(knn, "Dsatz: "+dsatz, "KNN");
		
		Kreuzvalidation kdtw=new Kreuzvalidation(ListenFunktionen.tiefeKopie(dtwDset));
		Statistiken dtw=kdtw.vollstaendigeKreuzvalidation(d);
		Output.schreibeErgebnisse(dtw, "Dsatz: "+dsatz, "DTW");
		
		Kreuzvalidation krf=new Kreuzvalidation(ListenFunktionen.tiefeKopie(rfDset));
		Statistiken rf=krf.vollstaendigeKreuzvalidation(w);
		Output.schreibeErgebnisse(rf, "Dsatz: "+dsatz, "RF");	
	}
	
	
	
	public void bestTiefpassUnfiltered() {
		ListenFunktionen.gleichrichten(this.datensatz);
		Collections.sort(this.datensatz);
			ArrayList<EMGContainer> filtered=ListenFunktionen.tiefeKopie(this.getDataset());
			//ListFunctions.setSamplerate(filtered, 33);
			Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			
			NaechsteNachbarn n=new NaechsteNachbarn(26);
			DynamicTimeWarping d=new DynamicTimeWarping();
			Zufallswald z=new Zufallswald();
			
			Statistiken s1=k.stratifizierteKreuzvalidation(n, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s3=k.stratifizierteKreuzvalidation(z, 10);
			
			Output.schreibeErgebnisse(s1,"K: "+26+" Freq: Unfiltriert", "KNN");
			Output.schreibeErgebnisse(s2," Freq: Unfiltriert", "DTW");
			Output.schreibeErgebnisse(s3," Freq: Unfiltriert", "RF");
		
	}
	
	public void bestTiefpass() {
		Collections.sort(this.datensatz);
		ListenFunktionen.gleichrichten(this.datensatz);
		for(int i=20;i<25;i+=5) {
			System.out.println("Freq: "+i);
			ArrayList<EMGContainer> filtered=ListenFunktionen.tiefeKopie(this.getDataset());
			ListenFunktionen.tiefpassfilterFFT(filtered, i);
			//ListFunctions.setSamplerate(filtered, 33);
			
			
			
			NaechsteNachbarn n=new NaechsteNachbarn(26);
			DynamicTimeWarping d=new DynamicTimeWarping();
			Zufallswald z=new Zufallswald();
			
			Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s3=k.stratifizierteKreuzvalidation(z, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s1=k.stratifizierteKreuzvalidation(n, 10);
			
			
			Output.schreibeErgebnisse(s1,"K: "+26+" Freq: "+i, "KNN");
			Output.schreibeErgebnisse(s2," Freq: "+i, "DTW");
			Output.schreibeErgebnisse(s3," Freq: "+i, "RF");
		}
	}
	
	
	public void bestTiefpassTree() {
		Collections.sort(this.datensatz);
		for(int i=25;i<55;i+=5) {
			System.out.println("Freq: "+i);
			ArrayList<EMGContainer> filtered=ListenFunktionen.tiefeKopie(this.getDataset());
			ListenFunktionen.tiefpassfilterFFT(filtered, i);
			ListenFunktionen.aendereTaktrate(filtered, 33);
			Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			
			Zufallswald z=new Zufallswald();
			//NearestNeighbour n=new NearestNeighbour(26);
			//DynamicTimeWarpingReimpl d=new DynamicTimeWarpingReimpl();
			
			Statistiken s1=k.stratifizierteKreuzvalidation(z, 10);
			//k=new Kreuzvalidation(ListFunctions.deepCopy(filtered));
			//Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
			
			
			Output.schreibeErgebnisse(s1, "Freq: "+i, "RF");
			//Output.writeResult(s2," Freq: "+i, "DTW");
			
		}
	}
	
	
	
	public void findBestK() {
		Collections.sort(this.datensatz);
		for(int i=1;i<22;i++) {
			//assert this.datensatz.size()==100;
			System.out.println("k: "+i);
			Kreuzvalidation k=new Kreuzvalidation(this.getDataset());
			NaechsteNachbarn n=new NaechsteNachbarn(i);
			Statistiken s=k.stratifizierteKreuzvalidation(n, 10);
			Output.schreibeErgebnisse(s,"K: "+i, "KNN");
		}
	}
	
	
	
	
	
	
	public Manager(ArrayList<EMGContainer> trainset, ArrayList<EMGContainer> testset) {
		super();
		this.trainset = trainset;
		this.testset = testset;
	}


	//Teile die Daten Fair auf. D.h. In die Trainingsdaten kommen von jeder Klasse gleich viele Exemplare.
	public void splitData(int anzahltraining) {
		this.testset.clear();
		this.trainset.clear();
		int anzahlKlassen=ListenFunktionen.getAnzahlKlassen(datensatz);
		int anzahlProKlasse=anzahltraining/anzahlKlassen;
		int dsetsize=datensatz.size();
		assert trainset.isEmpty();
		for(int i=0;i<anzahlKlassen;i++) {
			ArrayList<EMGContainer> emgs=getKlasse(i,anzahlProKlasse);
			assert emgs.size()==anzahlProKlasse;
			
			trainset.addAll(emgs);
		}
		ArrayList<EMGContainer> tests=getDataset();
		tests.removeAll(trainset);
		this.testset=tests;
		Collections.sort(this.testset);
		//Collections.shuffle(this.trainset);
		
		System.out.println("Trainset: "+trainset.size()+" Testset: "+testset.size()+" Datenset: "+datensatz.size());
		assert (trainset.size()+testset.size())==dsetsize;
	}
	
	
	private ArrayList<EMGContainer> getKlasse(int klasse, int anzahlProKlasse) {
		//System.out.println("Get klasse");
		ArrayList<EMGContainer> result=new ArrayList<EMGContainer>();
		for(int i=0,j=0;j<anzahlProKlasse;i++) {
			//System.out.println("Dsize "+result.size());
			if(datensatz.get(i).getZahl()==klasse) {
				result.add(datensatz.get(i).clone());
				j++;
			}
			
		}
		//datensatz.removeAll(result);
		return result;
	}

	public void splitData(double splitfaktor) {
		int anzTrain=(int) Math.round( (splitfaktor*datensatz.size()));
		splitData(anzTrain);
	}
	
	
	public void runDTW(String modelname) {
		DynamicTimeWarping dyn=EMGLeser.ladeDTWModell(modelname);
		dyn.klassifiziereTestdaten(datensatz);	
	}
	
	
	public void incraseK(int times) {
		ArrayList<Statistiken> res=new ArrayList<Statistiken>();
		for(int k=1;k<51;k++) {
			for(int i=0;i<times;i++) {
				System.out.println("Lauf Nr. "+i +"  K: "+k);
				this.randomise();
				this.splitData(0.5);
				NaechsteNachbarn f=new NaechsteNachbarn(k);
				f.generiereModell(this.getTrainset());
				res.add(f.klassifiziereTestdaten(getTestset()));
			}
			Output.schreibeErgebnisse(res, "runs: "+times+"  k: "+k, "KNN");
			res.clear();
		}
		Output.schreibeEinstellungen();
	}
	

	//Viele läufe mit splitratio
	public void runDTW(int runs, double splitratio) {
		ArrayList<Statistiken> stats=new ArrayList<Statistiken>();
		for(int i=0;i<runs;i++) {
			randomise();
			splitData(splitratio);
			DynamicTimeWarping dyn=new DynamicTimeWarping();
			dyn.generiereModell(getTrainset());
			Statistiken s=dyn.klassifiziereTestdaten(getTestset());
			stats.add(s);
		}
		assert stats.size()==runs;
		Statistikauswerter.printStats(stats);
	}
	
	
	//Falls schon geteilt.
	public Statistiken runDTW() {
		DynamicTimeWarping dyn=new DynamicTimeWarping();
		dyn.generiereModell(getTrainset());
		//System.out.println("Template Building abgeschlossen.");
		return dyn.klassifiziereTestdaten(getTestset());
		//s.printConfusionMatrix();
		//Statistikauswerter.printStats(s);
	}
	
	/*public void runRF() {
		FeaturesOrganizer fo=new FeaturesOrganizer();
		fo.buildModel(getTrainset());
		Statistiken s=fo.klassifiziereTestdaten(getTestset());
		Statistikauswerter.printStats(s);
	}*/
	
	
	
	
	public Statistiken runRF() {
		Zufallswald fo=new Zufallswald();
		fo.setAnzahlBaueme(1000);
		fo.generiereModell(getTrainset());
		return fo.klassifiziereTestdaten(getTestset());
		//Statistikauswerter.printStats(s);
	}
	
	public Statistiken runRF(int trees) {
		Zufallswald fo=new Zufallswald();
		fo.setAnzahlBaueme(trees);
		fo.generiereModell(getTrainset());
		return fo.klassifiziereTestdaten(getTestset());
	}
	
	
	public void runBoth(double splitratio, double runs, String filterInfos) {
		ArrayList<Statistiken> statsDTW=new ArrayList<Statistiken>();
		ArrayList<Statistiken> statsRF=new ArrayList<Statistiken>();
		
		for(int i=0;i<runs;i++) {
			System.out.println("Lauf Nr. "+i);
			Collections.shuffle(datensatz);
			splitData(splitratio);
			System.out.println("Führe DTW aus.");
			Statistiken sDtw=runDTW();
			System.out.println("Führe RF aus.");
			Statistiken sRf=runRF();
			statsDTW.add(sDtw);
			statsRF.add(sRf);
		}
		//String runConfig="runs: "+runs+"  splitratio: "+splitratio+"\n"+ ((filterInfos!=null)?filterInfos: "");
		Output.schreibeErgebnisse(statsRF, "runs: "+runs+"  splitratio: "+splitratio, "RF");
		Output.schreibeErgebnisse(statsDTW, "runs: "+runs+"  splitratio: "+splitratio, "DTW");
		
	}
	
	
	public void runRFStichprobengroesse() {
		for(int i=5;i!=105;i+=5) {
			System.out.println("Percent: "+i);
			System.out.println("DS SIZE: "+this.datensatz.size());
			Kreuzvalidation k=new Kreuzvalidation(this.getDataset());
			Zufallswald w=new Zufallswald();
			w.setAnzahlBaueme(1000);
			w.setStichporbengroesse(i);
			Statistiken s=k.stratifizierteKreuzvalidation(w, 10);
			Output.schreibeErgebnisse(s,"Percent: "+i, "RF");
		}
	}
	
	
	
	
	public void runRFBaumanzahl(){
		for(int i=1;i<101;i+=1) {
			System.out.println("Mom Bäume: "+i);
			Kreuzvalidation k=new Kreuzvalidation(this.getDataset());
			Zufallswald w=new Zufallswald();
			w.setAnzahlBaueme(i);
			Statistiken s=k.stratifizierteKreuzvalidation(w, 10);
			Output.schreibeErgebnisse(s,"Anzahl Bäume: "+i, "RF");
		}
	}
	
	
	
	
	
	
	
	public void runRFAnzahlAtts() {
		for(int i=70;i!=105;i+=5) {
			
			Kreuzvalidation k=new Kreuzvalidation(this.getDataset());
			Zufallswald w=new Zufallswald();
			w.setAnzahlBaueme(1000);
			w.setMerkmalsanzahl(i);
			System.out.println("Starte Valid");
			Statistiken s=k.stratifizierteKreuzvalidation(w, 10);
			System.out.println("Ende Valid");
			Output.schreibeErgebnisse(s,"Percent: "+i, "RF");
		}
	}
	
	
	
	
	
	public void runRFOften(int times) {
		ArrayList<Statistiken> res=new ArrayList<Statistiken>();
		for(int i=0;i<times;i++) {
			System.out.println("Lauf Nr. "+i);
			this.randomise();
			this.splitData(0.5);
			Zufallswald f=new Zufallswald();
			f.generiereModell(this.getTrainset());
			res.add(f.klassifiziereTestdaten(getTestset()));
		}
		Output.schreibeErgebnisse(res, "runs: "+times, "RF");
	}
	
	public void runDTWOften(int times) {
		ArrayList<Statistiken> res=new ArrayList<Statistiken>();
		for(int i=0;i<times;i++) {
			System.out.println("Lauf Nr. "+i);
			this.randomise();
			this.splitData(0.5);
			DynamicTimeWarping f=new DynamicTimeWarping();
			f.generiereModell(this.getTrainset());
			res.add(f.klassifiziereTestdaten(getTestset()));
		}
		Output.schreibeErgebnisse(res, "runs: "+times, "DTW");
	}
	
	public void runKNNOften(int times) {
		ArrayList<Statistiken> res=new ArrayList<Statistiken>();
		for(int i=0;i<times;i++) {
			System.out.println("Lauf Nr. "+i);
			this.randomise();
			this.splitData(0.5);
			NaechsteNachbarn f=new NaechsteNachbarn(9);
			f.generiereModell(this.getTrainset());
			res.add(f.klassifiziereTestdaten(getTestset()));
		}
		Output.schreibeErgebnisse(res, "runs: "+times, "KNN");
	}
	
	
	
	
	//Buildet und klassifizier die daten, gibt Statistiken zurück
	public Statistiken runClassifier(Klassifizierer c) {
		c.generiereModell(this.getTrainset());
		Statistiken s=c.klassifiziereTestdaten(this.getTestset());
		return s;
	}
	
	
	//Lässt den Klassifizierer c laufen.D.h. baut den Classifier und klassifiziert die Testdaten
	//Vorraussetzung: Set ist gesplitted
	//Gibt alles auf der Konsole aus
	public void runClassifierKonsole(Klassifizierer c) {
		c.generiereModell(getTrainset());
		Statistiken s=c.klassifiziereTestdaten(getTestset());
		Statistikauswerter.printStats(s);
	}
	
	
	public void kuerzenOderNicht(int dsatz) {
		Collections.sort(this.datensatz);
		ArrayList<EMGContainer> filtered=ListenFunktionen.tiefeKopie(this.getDataset());
		
		//ListFunctions.mittelwertfilter(filtered, 31);
		ListenFunktionen.tiefpassfilterFFT(filtered, 15);
		EMGContainer ruhewerte=ListenFunktionen.entferneRuhewerte(filtered);
		ListenFunktionen.kuerzeOhneRuhe(filtered);
		//ListFunctions.cutAllEMGS(filtered, ruhewerte, 10, 1.5);
		Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		
		NaechsteNachbarn n=new NaechsteNachbarn(26);
		DynamicTimeWarping d=new DynamicTimeWarping();
		Zufallswald z=new Zufallswald();
		
		Statistiken s3=k.stratifizierteKreuzvalidation(z, 10);
		k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		Statistiken s1=k.stratifizierteKreuzvalidation(n, 10);
		k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
		
		
		
		Output.schreibeErgebnisse(s1,"CUT o Dsatz "+dsatz, "KNN");
		Output.schreibeErgebnisse(s2,"CUT o Dsatz "+dsatz, "DTW");
		Output.schreibeErgebnisse(s3,"CUT o Dsatz "+dsatz, "RF");	
	}
	
	
	public void laufzeit() {
		NaechsteNachbarn n=new NaechsteNachbarn(10);
		DynamicTimeWarping d=new DynamicTimeWarping();
		Zufallswald z=new Zufallswald();
		System.out.println("--- Training ---");
		for(int i=10;i<101;i+=10) {
			System.out.println("--- Samples: "+i+" ---");
			ArrayList<EMGContainer> teilm=this.getTeilmenge(i);
			//ListFunctions.printData(teilm);
			assert teilm.size()==i*10;
			System.out.println("--- KNN ---");
			laufzeitTraining(n, ListenFunktionen.tiefeKopie(teilm));
			System.out.println("--- DTW ---");
			laufzeitTraining(d, ListenFunktionen.tiefeKopie(teilm));
			System.out.println("--- RF ---");
			laufzeitTraining(z, ListenFunktionen.tiefeKopie(teilm));
			
		}
		System.out.println("--- Klassifizierung ---");
		for(int i=1;i<31;i+=1) {
			System.out.println("--- Samples: "+i+" ---");
			ArrayList<EMGContainer> teilm=this.getTeilmenge(i);
			System.out.println("--- KNN ---");
			laufzeitKlassifizierungKNN(n, ListenFunktionen.tiefeKopie(teilm),1);
			System.out.println("--- DTW ---");
			laufzeitKlassifizierung(d, ListenFunktionen.tiefeKopie(teilm),1);
			System.out.println("--- RF ---");
			laufzeitKlassifizierung(z, ListenFunktionen.tiefeKopie(teilm),10);
		}
		
		
		
		
		
	}
	
	
	public void laufzeitTraining(Klassifizierer k, ArrayList<EMGContainer> data) {
		final long start = System.currentTimeMillis(); 
		for(int i=0;i<10;i++) {
			k.generiereModell(data);
		}
		final long ende = System.currentTimeMillis();
		System.out.println("Laufzeit: "+(ende - start) + " Millisek."); 
	}
	
	public void laufzeitKlassifizierung(Klassifizierer k, ArrayList<EMGContainer> data,int times) {
		k.generiereModell(data);
		final long start = System.currentTimeMillis(); 
		for(int i=0;i<times;i++) {
			k.klassifiziereTestdaten(data);
		}
		final long ende = System.currentTimeMillis();
		System.out.println("Laufzeit: "+(ende - start) + " Millisek."); 
	}
	
	public void laufzeitKlassifizierungKNN(Klassifizierer k, ArrayList<EMGContainer> data,int times) {
		k.generiereModell(data);
		ArrayList<EMGContainer> classify=this.getTeilmenge(5);
		final long start = System.currentTimeMillis(); 
		for(int i=0;i<times;i++) {
			k.klassifiziereTestdaten(classify);
		}
		final long ende = System.currentTimeMillis();
		System.out.println("Laufzeit: "+(ende - start) + " Millisek."); 
	}
	
	
	
	
	public void kuerzenOderNicht2(int dsatz) {
		Collections.sort(this.datensatz);
		ArrayList<EMGContainer> filtered=ListenFunktionen.tiefeKopie(this.getDataset());
		EMGContainer ruhewerte=ListenFunktionen.entferneRuhewerte(filtered);
		ListenFunktionen.tiefpassfilterFFT(filtered, 15);
		//ListFunctions.cutAllEMGS(filtered, ruhewerte, 10, 1.5);
		Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		
		NaechsteNachbarn n=new NaechsteNachbarn(26);
		DynamicTimeWarping d=new DynamicTimeWarping();
		Zufallswald z=new Zufallswald();
		
		
		Statistiken s3=k.stratifizierteKreuzvalidation(z, 10);
		k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
		k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
		Statistiken s1=k.stratifizierteKreuzvalidation(n, 10);
		
		Output.schreibeErgebnisse(s1,"NOT CUT Dsatz "+dsatz, "KNN");
		Output.schreibeErgebnisse(s2,"NOT CUT Dsatz "+dsatz, "DTW");
		Output.schreibeErgebnisse(s3,"NOT CUT Dsatz "+dsatz, "RF");	
	}
	
	
	
	
	
	
	
	public void trainSize(int dsatz) {
		Collections.sort(this.datensatz);
		
		for(int i=10;i<101;i+=10) {
			ArrayList<EMGContainer> emgs=this.getTeilmenge(i);
			ArrayList filtered=ListenFunktionen.tiefeKopie(emgs);
			EMGContainer ruhewerte=ListenFunktionen.entferneRuhewerte(filtered);
			
			
			NaechsteNachbarn n=new NaechsteNachbarn(26);
			DynamicTimeWarping d=new DynamicTimeWarping();
			Zufallswald z=new Zufallswald();
			
			
			
			Kreuzvalidation k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s1=k.stratifizierteKreuzvalidation(n, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s2=k.stratifizierteKreuzvalidation(d, 10);
			k=new Kreuzvalidation(ListenFunktionen.tiefeKopie(filtered));
			Statistiken s3=k.stratifizierteKreuzvalidation(z, 10);
			
			Output.schreibeErgebnisse(s1,"Dsatz "+dsatz, "KNN");
			Output.schreibeErgebnisse(s2,"Dsatz "+dsatz, "DTW");
			Output.schreibeErgebnisse(s3,"Dsatz "+dsatz, "RF");	
		}
		
	}
	
	
	
	
	
	
	private ArrayList<EMGContainer> getTrainset() {
		return ListenFunktionen.tiefeKopie(trainset);
	}

	private void randomise() {
		Collections.shuffle(datensatz);
	}
	
	private ArrayList<EMGContainer> getDataset() {
		return ListenFunktionen.tiefeKopie(datensatz);
	}

	private ArrayList<EMGContainer> getTestset() {
		return ListenFunktionen.tiefeKopie(testset);
	}

	//Methode für Teilmenge des Datensatzes
	
	private ArrayList<EMGContainer> getTeilmenge(int samplesProKlasse) {
		ArrayList<EMGContainer> teilmenge= new ArrayList<EMGContainer>();
		for(int klasse=0;klasse<ListenFunktionen.getAnzahlKlassen(this.datensatz);klasse++) {
			int samples=0;
			for(EMGContainer e:this.datensatz) {
				if(samples<samplesProKlasse&&e.getZahl()==klasse) {
					teilmenge.add(e.clone());
					samples++;
				}else {
					if(!(samples<samplesProKlasse))
						break;
				}
			}
			
		}
		assert teilmenge.size()==ListenFunktionen.getAnzahlKlassen(this.datensatz)*samplesProKlasse;
		return teilmenge;
	}
	
	private ArrayList<EMGContainer> getTeilmenge(int samplesProKlasse, ArrayList<EMGContainer> dset) {
		ArrayList<EMGContainer> teilmenge= new ArrayList<EMGContainer>();
		for(int klasse=0;klasse<ListenFunktionen.getAnzahlKlassen(dset);klasse++) {
			int samples=0;
			for(EMGContainer e:dset) {
				if(samples<samplesProKlasse&&e.getZahl()==klasse) {
					teilmenge.add(e.clone());
					samples++;
				}else {
					if(!(samples<samplesProKlasse))
						break;
				}
			}
			
		}
		assert teilmenge.size()==ListenFunktionen.getAnzahlKlassen(dset)*samplesProKlasse;
		return teilmenge;
	}

	
	
	
}
