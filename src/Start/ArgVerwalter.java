package Start;
import java.io.IOException;
import java.util.ArrayList;

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

public class ArgVerwalter {
	
	Einstellungen sr=new Einstellungen();
	
	public ArgVerwalter(String args[]) {
		ArrayList<String> argsList=new ArrayList<String>();

		for(int i=0;i<args.length;i++) {
			argsList.add(args[i]);
		}
		
		if(argsList.get(0).equals("LDTW")||argsList.get(0).equals("LRF")||argsList.get(0).equals("LNN")) {
			handleLoadModel(argsList);
		}
		
		if(argsList.get(0).equals("NDTW")||argsList.get(0).equals("NRF")||argsList.get(0).equals("NKNN")) {
			handleNewModel(argsList);
		}
		//
		System.out.println("Filter String: "+argsList.get(0));
		sr.setFilterString(argsList.get(0));
		argsList.remove(0);
		//Erste drei sachen abgearbeitet die IMMER gleich sind.
		
		
		//Jetzt kommen entwerder die Ruhewerte oder das speichern oder einstellungen etc.
		//Now come either the rest values or the saving of the settings etc.
		//Falls ein neues Modell angelegt wird, sind die folgenden parameter Optional.
		//If a new model is created, the following parameters are optional
		if(!argsList.isEmpty()&&argsList.get(0).equals("R")) {
			sr.setKuerzen(true);
			argsList.remove(0);		
		}
		
		if(!argsList.isEmpty()&&argsList.get(0).equals("S")) {
			System.out.println("Modell wird nach Generierung gespeichert.");
			sr.setSpeichern(true);
			sr.setSpeichername(argsList.get(1));
			argsList.remove(0);	
			argsList.remove(0);	
		}
		//Jetzt muss wenn modell geladen worden die daten kommen, bei neuem können die Settings kommen.
		//Now if the model has been loaded, the data must come, the settings can come again
		
		//Nach den beiden können keine Args mehr folgen, falls ein Modell geladen wurde
		//Args cannot follow after the two if a model has been loaded
		if(!argsList.isEmpty()&&argsList.get(0).equals("KT")) {
			handleKT(argsList.get(1));	//Der Ordner mit den zu klassifizierenden Daten //The folder with the data to be classified
			return;
		}
		//Bei Rohdaten
		else if(!argsList.isEmpty()&&argsList.get(0).equals("KN")) {
			handleKN(argsList.get(1)); //Der Ordner mit den zu klassifizierenden Daten
			return;
		}else if(!argsList.isEmpty()&&argsList.get(0).equals("KF")) {
			handleKF(argsList.get(1));
			return;
		}
		
		
		//Lese Einstellungen für die jeweiligen Algs
		if(!argsList.isEmpty()&&argsList.get(0).equals("SDTW")) {
			//DTW hat keine Parameter
		}else
		if(!argsList.isEmpty()&&argsList.get(0).equals("SRF")) {
			argsList.remove(0);
			handleRFSettings(argsList);
		}else
		if(!argsList.isEmpty()&&argsList.get(0).equals("SKNN")) {
			argsList.remove(0);
			handleKNNSettings(argsList);
		}
		
		if(sr.istTrainiere()) {
			handleNewModel();
		}
		
		
	}

	/*
		handle kNN
	 */
	private void handleKNNSettings(ArrayList<String> argsList) {
		NaechsteNachbarn n=(NaechsteNachbarn) sr.getModell();
		
		if(!argsList.get(0).equals("E")) {
			int k=Integer.parseInt(argsList.get(0));
			System.out.println("Setze das k auf: "+k);
			n.setK(k);
		}
	}

	private void handleRFSettings(ArrayList<String> argsList) {
		int trees=Integer.parseInt(argsList.get(0));
		Zufallswald f=(Zufallswald) sr.getModell();
		System.out.println("Setzte die Anzahl der Bäume auf: "+trees);
		f.setAnzahlBaueme(trees);
	}

	private void handleNewModel() {
		Klassifizierer k=sr.getModell();
		filter(sr.getInput());
		System.out.println("Trainiere das Modell...Dies kann einen Moment dauern.");
		k.generiereModell(sr.getInput());
		if(sr.istSpeichern()) {
			Output.schreibeModell(k, sr.getSpeichername());
		}
		
	}


	/*

	 */
	private void filter(ArrayList<EMGContainer> input) {
		EMGContainer ruhewerte = null;
		if(sr.istGekuerzt()) {
			System.out.println("Lese die Ruhewerte.");
			ruhewerte=EMGLeser.leseRuhewerte(sr.getDatenpfad()+"/ruhewerte");
			assert ruhewerte!=null;
			input.add(ruhewerte);
		}
		ListenFunktionen.gleichrichten(input);
		String splitted[]=sr.getFilterString().split("-");
		
		for(int i=0;i<splitted.length;i++) {
			//Mittelwertfilter
			if(splitted[i].charAt(0)=='M') {
				int val=Integer.parseInt(splitted[i].substring(1));
				System.out.println("Mittelwertfilter: "+val);
				ListenFunktionen.mittelwertfilter(input, val);
			}
			//Binominalfilter
			if(splitted[i].charAt(0)=='B') {
				
				int val=Integer.parseInt(splitted[i].substring(1));
				System.out.println("Binominalfilter: "+val);
				ListenFunktionen.faltung(input, ListenFunktionen.binominalfilter(val));
			}
			//Cutten
			if(splitted[i].charAt(0)=='C') {
				
				double val=Double.parseDouble(splitted[i].substring(1));
				System.out.println("Kürze die EMG Daten, Faktor: "+val);
				ListenFunktionen.kuerzeMitRuhe(input, ruhewerte, 10, val);
			}
			if(splitted[i].charAt(0)=='K') {
				System.out.println("Kürze die EMG Daten");
				ListenFunktionen.kuerzeOhneRuhe(input);
			}
			//FFT Lowpass
			if(splitted[i].charAt(0)=='F') {
				int val=Integer.parseInt(splitted[i].substring(1));
				System.out.println("Fourier-Tiefpassfilter bis Frequenz: "+val);
				ListenFunktionen.tiefpassfilterFFT(input, val);
			}
			//stretch
			if(splitted[i].charAt(0)=='L') {
				int val=Integer.parseInt(splitted[i].substring(1));
				System.out.println("Strecke/Stauche auf Länge: "+val);
				ListenFunktionen.strecke(input, val);
			}
			//Resample
			if(splitted[i].charAt(0)=='R') {
				int val=Integer.parseInt(splitted[i].substring(1));
				System.out.println("Ändere Taktrate auf: "+val);

				ListenFunktionen.aendereTaktrate(input, val);
			}
		}

		if(sr.istGekuerzt()) {
			ListenFunktionen.entferneRuhewerte(input);
		}
	}

	private void handleKN(String pfad) {
		System.out.println("Die Daten werden gelesen und klassifiziert...Dies je nach Verfahren eine Weile dauern.");
		try {
			ArrayList<EMGContainer> input=EMGLeser.leseVerzeichnisUnklassifiziert(pfad);
			filter(input);
			for(EMGContainer e: input) {
				int result=sr.getModell().klassifiziereInstanz(e);
				System.out.println(e.getId()+" Klassifiziert als: "+result);
			}
			if(sr.istSpeichern()) {
				System.out.println("Speichere Modell...");
				Output.schreibeModell(sr.getModell(), sr.getSpeichername());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleKF(String pfad) {
		System.out.println("Die Trainingsdaten werden gelesen und klassifiziert...Dies je nach Verfahren eine Weile dauern.");
		try {
			System.out.println("Lese die zu klassifizierenden Daten.");
			ArrayList<EMGContainer> input=EMGLeser.leseVerzeichnis(pfad);
			assert !input.isEmpty();
			assert sr.getModell()!=null;
			System.out.println("Filtere die zu klassifizierenden Daten.");
			filter(input);
			System.out.println("Klassifiziere die Daten. Dies kann eine Weile dauern.");
			for(EMGContainer e: input) {
				System.out.println("Klassifiziere: "+e.getId()+"  Klassifiziert als: "+sr.getModell().klassifiziereInstanz(e));
			}
			if(sr.istSpeichern()) {
				System.out.println("Speichere das Modell.");
				Output.schreibeModell(sr.getModell(), sr.getSpeichername());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private void handleKT(String pfad) {
		System.out.println("Die Trainingsdaten werden gelesen und klassifiziert...Dies je nach Verfahren eine Weile dauern.");
		
		try {
			System.out.println("Lese die zu klassifizierenden Daten.");
			ArrayList<EMGContainer> input=EMGLeser.leseVerzeichnis(pfad);
			assert !input.isEmpty();
			assert sr.getModell()!=null;
			System.out.println("Filtere die zu klassifizierenden Daten.");
			filter(input);
			System.out.println("Klassifiziere die Daten. Dies kann eine Weile dauern.");
			Statistiken s=sr.getModell().klassifiziereTestdaten(input);
			Statistikauswerter.printStats(s);
			ArrayList<Statistiken> l=new ArrayList<Statistiken>();
			l.add(s);
			Output.schreibeErgebnisse(l, " ", sr.getInfos());
			//Output.writeSettings();
			if(sr.istSpeichern()) {
				System.out.println("Speichere das Modell.");
				Output.schreibeModell(sr.getModell(), sr.getSpeichername());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Erzeugt einen neuen Klassifizierer und setzt die Trainingsvariable auf true.
	//Creates a new classifier and sets the training variable to true.
	private void handleNewModel(ArrayList<String> argsList) {
		System.out.println("Es wird ein neues Modell generiert."); // A new model is generated
		sr.setDatenpfad(argsList.get(1));
		if(argsList.get(0).equals("NDTW")) {
			System.out.println("Als Verfahren wurde DTW ausgewählt."); // Als Verfahren wurde DTW ausgewählt
			sr.setModell(new DynamicTimeWarping());
		}
		if(argsList.get(0).equals("NRF")) {
			System.out.println("Als Verfahren wurde RF ausgewählt."); // RF was chosen as the method.
			sr.setModell(new Zufallswald());
		}
		if(argsList.get(0).equals("NKNN")) {
			System.out.println("Als Verfahren wurde KNN ausgewählt."); // KNN was selected as the procedure.
			sr.setModell(new NaechsteNachbarn(10));
		}
		try {
			
			System.out.println("Lese: "+argsList.get(1));
			

			sr.setInput(EMGLeser.leseVerzeichnis(argsList.get(1)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		sr.setTrainiere(true);
		argsList.remove(0);
		argsList.remove(0);

	}

	private void handleLoadModel(ArrayList<String> argsList) {
		System.out.println("Modell wird geladen..."); // Loading model ...
		if(argsList.get(0).equals("LDTW")) {
			System.out.println("Lade DTW Modell"); //Load DTW model
			sr.setInfos("Dynamic Time Warping"); // Dynamic Time Warping
			sr.setModell(EMGLeser.ladeDTWModell(argsList.get(1)));
		}
		if(argsList.get(0).equals("LRF")) {
			System.out.println("Lade RF Modell"); //Charge RF Model
			Zufallswald f=EMGLeser.ladeRFModell(argsList.get(1));
			sr.setInfos("RandomForest Trees: "+f.getTreeNumber());
			sr.setModell(f);
		}
		if(argsList.get(0).equals("LNN")) {
			System.out.println("Lade KNN Modell"); // Load KNN Model
			NaechsteNachbarn n=EMGLeser.ladeKNNModell(argsList.get(1));
			sr.setInfos("Nearest Neighbour k: "+n.getK()); // Nearest Neighbour:
			sr.setModell(n);
		}
		argsList.remove(0);
		argsList.remove(0);
	}
	
}
