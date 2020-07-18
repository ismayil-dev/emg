package DynamicTimeWarping;

import java.io.Serializable;
import java.util.ArrayList;

import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.Klassifizierer;
import EMGVerarbeitung.ListenFunktionen;
import EMGVerarbeitung.Output;
import EMGVerarbeitung.Statistiken;


public class DynamicTimeWarping implements Klassifizierer, Serializable {
	private static final long serialVersionUID = 100;
	private ArrayList<EMGContainer> trainingsdaten;
	private ArrayList<EMGContainer> vorlage=new ArrayList<EMGContainer>();
	
	public DynamicTimeWarping() {}
	
	public DynamicTimeWarping(ArrayList<EMGContainer> templates) {
		this.vorlage=templates;
	}
		
	public Statistiken klassifiziereTestdaten(ArrayList<EMGContainer> emgs) {
		return klassifiziereViele(emgs);
	}
	
	private Statistiken klassifiziereViele(ArrayList<EMGContainer> emgs) {
		Statistiken stats=new Statistiken(10);
		assert !vorlage.isEmpty();
		for(EMGContainer e: emgs) {
			int klasse=klassifiziereInstanz(e);
			stats.addErgebnis(klasse, e.getZahl());
		}
		stats.setWeitereInfos("Dynamic Time Warping");
		return stats;
	}
	
	public int klassifiziereInstanz(EMGContainer e) {
		
		double min=Double.MAX_VALUE;
		int zahl=-1;
		
		for(EMGContainer econt: this.vorlage) {
			double distance=berechneDTWAbstandEMG(econt, e);
			if(distance<min) {
				min=distance;
				zahl=econt.getZahl();
			}
		}
		return zahl;
	}

	private double berechneDTWAbstandEMG(EMGContainer template, EMGContainer obj) {
		double sum=0;
		for(int i=0;i<obj.getAnzahlSensoren();i++) {
			sum+=berechneDTWAbstand1D(template.getSensordaten(i), obj.getSensordaten(i));
		}
		return sum;
	}
	
	private double berechneDTWAbstand1D(double a[], double b[]) {
		double[][] costM=berechneKostenmatrix(a,b);
		return costM[costM.length-1][costM[0].length-1];
	}
	
	public void setVorlagen(ArrayList<EMGContainer> templ) {
		this.vorlage=templ;
	}
	
	public ArrayList<EMGContainer> getVorlagen(){
		return this.vorlage;
	}
		
	//10 ersetzen durch anzahl klassen für flexibilität - done
	public void generiereModell(ArrayList<EMGContainer> emgs) {
		assert !emgs.isEmpty();
		this.vorlage.clear();
		assert this.vorlage.isEmpty();
		this.trainingsdaten=ListenFunktionen.tiefeKopie(emgs);
		assert !this.trainingsdaten.isEmpty();
		for(int i=0;i<ListenFunktionen.getAnzahlKlassen(emgs);i++) {
			//System.out.println("buildTemplates i: "+i );
			ArrayList<EMGContainer> emgsclass=sucheTrainingsmenge(i);
			assert !emgsclass.isEmpty();
			EMGContainer templ=generiereEineVorlage(emgsclass);
			this.vorlage.add(templ);
		}	
	}

	private EMGContainer generiereEineVorlage(ArrayList<EMGContainer> emgsclass) {
		//Wie lange muss das Template sein?
		//Laut Paper werden dazu die länge Trainingsinstanzen der Zahl gemittelt.
		int size=berechneMittlereGroesse(emgsclass);
		for(EMGContainer e: emgsclass) {
			e.strecke(size);
		}
		
		//Für welche Klasse wird das Template angelegt?
		int zahl=emgsclass.get(0).getZahl();
		
		//Prüfe ob ich einen Fehler gemacht habe
		for(EMGContainer e: emgsclass) {
			assert e.getZahl()==zahl;
		}
		//Bilde den Schnitt der Daten. Hierbei wird ein neuer EMGContainer gebildet.
		//Die klasse wird auch gesetzt.
		EMGContainer template = mittlereEMGs(emgsclass);
		
		//Verfeinere Sie, in dem mittels DTW die entsprechenden punkte gefunden und gemittelt werden.
		verfeinereVorlage(template, emgsclass);
		//Output.writeEMGContainer(template, "nachVerfeinern");
		return template;
	}

	private int berechneMittlereGroesse(ArrayList<EMGContainer> emgsclass) {
		int sum=0;
		for(EMGContainer e:emgsclass) {
			sum+=e.getLeange();
		}
		return (sum/emgsclass.size());
	}

	//Verfeinert die Vorlage indem die Pfade berechnet werden und ein neues EMG generiert wird, bei dem die einzelnen Zeitpunkte korrekt justiert sind.
	private void verfeinereVorlage(EMGContainer template, ArrayList<EMGContainer> emgsclass) {
		double tempdata[][]=template.getDaten();
		double newtemp[][]=new double[template.getDaten().length][template.getDaten()[0].length];
		for(EMGContainer e:emgsclass) {

			for(int sensor=0;sensor<tempdata[0].length;sensor++) {
				double templatesensor[]=template.getSensordaten(sensor);
				double erow[]=e.getSensordaten(sensor);
				double cost[][]=berechneKostenmatrix(templatesensor, erow);
				int corr[]=berechnePfad(cost);
				for(int i=0;i<corr.length;i++) {
					newtemp[i][sensor]+=erow[corr[i]];
				}
			}
		}
		for(int i=0;i<newtemp.length;i++) {
			for(int j=0;j<newtemp[0].length;j++) {
				newtemp[i][j]/=emgsclass.size();
			}
		}
		template.setDaten(newtemp);
	}

	//Berechnet den Pfad. Dabei wird am Ende der Matrix begonnen und geschaut, welches Vorgänger Element am kleinsten ist. 
	private int[] berechnePfad(double[][] cost) {
		int i=cost.length-1;
		int j=cost[0].length-1;
		int erg[]=new int[cost.length];
		while(i>0&&j>0) {
			erg[i]=j;
			if(cost[i-1][j-1]<=cost[i-1][j]&&cost[i-1][j-1]<=cost[i][j-1]) {
				i--;
				j--;
			}
			
			else 
			if(cost[i][j-1]<=cost[i-1][j]&&cost[i][j-1]<=cost[i-1][j-1]) {
				j--;
			}else
				
			if(cost[i-1][j]<=cost[i][j-1]&&cost[i-1][j]<=cost[i-1][j-1]) {
				i--;
			}
			
//			else {
//				System.out.println("FEHLER BEI GET CORRESPONDING ARRAY");
//				
//				for(int o=0;o<cost.length;o++) {
//					for(int p=0;p<cost[0].length;p++) {
//						System.out.print(" "+cost[o][p]);
//					}
//					System.out.println("");
//				}
//				System.out.println("J: "+j);
//				System.out.println("I: "+i);
//				
//				for(int o=j-5;o<j+5;o++) {
//					for(int p=i-5;p<i+5;p++) {
//						System.out.print(" "+cost[o][p]);
//					}
//					System.out.println("");
//				}
//				
//				
//				assert false;
//			}
		}
		//System.out.println("Get Corresp Array Ende");

		return erg;
	}

	
	//Berechnet die Kostenmatrix für zwei Zeitreihen
	private double[][] berechneKostenmatrix(double[] templatesensor, double[] erow) {
		double cost[][]=new double[templatesensor.length][erow.length];
		cost[0][0]=0;
		
		for(int i=1;i<cost.length;i++) {
			cost[i][0]=Double.POSITIVE_INFINITY;
		}
		
		for(int i=1;i<cost[0].length;i++) {
			cost[0][i]=Double.POSITIVE_INFINITY;
		}
		for(int i=1;i<templatesensor.length;i++) {
			for(int j=1;j<erow.length;j++) {
				cost[i][j]=Math.abs(templatesensor[i]-erow[j])+Math.min(Math.min(cost[i-1][j], cost[i][j-1]), cost[i-1][j-1]);
			}
		}
		return cost;
	}

	
	//Addiert die EMGs auf und teilt sie durch die Anzahl der EMGs
	private EMGContainer mittlereEMGs(ArrayList<EMGContainer> emgsclass) {
		int samplesize= emgsclass.get(0).getLeange();
		int sensorcount=emgsclass.get(0).getDaten()[0].length;
		EMGContainer ergebnis = new EMGContainer();
		
		double averagemat[][]=new double[samplesize][sensorcount];
		
		int momZahl=emgsclass.get(0).getZahl();

		//Addiere alle Werte der Datenmatrizen auf.
		for(EMGContainer e: emgsclass) {
			assert (e.getZahl()==momZahl);
			averagemat=addiereMatrix(averagemat, e.getDaten());
		}
		
		//Teile jeden durch die Anzahl der Trainingsdaten, da die alle aufaddiert wurden.
		for(int i=0;i<averagemat.length;i++) {
			for(int j=0; j<averagemat[0].length;j++) {
				averagemat[i][j]=averagemat[i][j]/emgsclass.size();
			}
		}
		
		ergebnis.setZahl(momZahl);
		ergebnis.setDaten(averagemat);
		ergebnis.setId("template");
		//Output.writeEMGContainer(ergebnis, "nachAVG");
		return ergebnis;
	}

	//Addiert die EMGs auf.
	private double[][] addiereMatrix(double[][] averagemat, double[][] daten) {
		for(int i=0;i<averagemat.length;i++) {
			for(int j=0; j<averagemat[0].length;j++) {
				averagemat[i][j]+=daten[i][j];
			}
		}
		return averagemat;
	}

	//Sucht für die jeweilige Klasse die EMGs
	private ArrayList<EMGContainer> sucheTrainingsmenge(int i) {
		ArrayList<EMGContainer> result=new ArrayList<EMGContainer>();
		for(EMGContainer e: trainingsdaten) {
			if(e.getZahl()==i) {
				result.add(e);
			}
		}
		return result;
	}

	@Override
	public void speichereModell(String name) {
		Output.schreibeModell(this, name);
	}
	
}
