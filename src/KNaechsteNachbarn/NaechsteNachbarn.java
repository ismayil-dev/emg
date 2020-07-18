package KNaechsteNachbarn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.Klassifizierer;
import EMGVerarbeitung.ListenFunktionen;
import EMGVerarbeitung.Output;
import EMGVerarbeitung.Statistiken;

public class NaechsteNachbarn implements Klassifizierer, Serializable {

	
	private static final long serialVersionUID = 100;
	
	private ArrayList<EMGContainer> klassifizierer=null;
	private int k=11;
	
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	


	public NaechsteNachbarn(int k) {
		super();
		this.k = k;
	}

	public NaechsteNachbarn() {
		super();
	}


	@Override
	public void generiereModell(ArrayList<EMGContainer> trainset) {
		this.klassifizierer=null;
		assert this.klassifizierer==null;
		this.klassifizierer=trainset;
	}

	
	
	
	
	
	@Override
	public int klassifiziereInstanz(EMGContainer e) {
		// Sortiere alle Instanzen dem Abstand von e entsprechend.
		/* Eigentlich könnte man mit einem Comperator alles schön sortieren, aber der muss leider transitiv sein...
		 * Schade, das hier wäre so eine schöne Lösung, stattdessen muss eine Wrapperklasse her.
		 * 
		 * 
		 */

		ArrayList<EMGContainer> newlist=new ArrayList<EMGContainer>();
		ArrayList<EMGHuelle> wrappedEMGS=new ArrayList<EMGHuelle>();
		for(EMGContainer es: this.klassifizierer) {
			wrappedEMGS.add(new EMGHuelle(es));
		}
		assert this.klassifizierer.size()==wrappedEMGS.size();
		
		for(EMGHuelle wr: wrappedEMGS) {
			wr.distanzZuDemEinenDingsDa=messeDTWAbstand(e, wr.emg);
		}
		
		Collections.sort(wrappedEMGS);
		for(EMGHuelle wrp: wrappedEMGS) {
			newlist.add(wrp.emg);
		}
		this.klassifizierer=newlist;
		

		//classifier.sort(new NearestComperator(e));
		int klasse = getKlasse();
//		if(learnLive) {
//			this.klassifizierer.add(e);
//		}
		//System.out.println("Real Class: "+e.getZahl()+"  Calc Class: "+klasse);
		return klasse;
	}

	private int getKlasse() {
		List<EMGContainer> firstk= this.klassifizierer.subList(0, this.k);
		ArrayList<Haeufigkeit> res=new ArrayList<Haeufigkeit>();
		//System.out.println("FKSIZE: "+firstk.size());
		assert firstk.size()==this.k;
		
		for(EMGContainer e: firstk) {
			//Ist die Zahl schon vorgekommen?
			if(!res.contains(new Haeufigkeit(e.getZahl(),0))){
				res.add(new Haeufigkeit(e.getZahl(),1));
			}else {
				//Falls ja, suche den Eintrag und erhöhe ihn um 1
				int ai=res.indexOf(new Haeufigkeit(e.getZahl(),0));
				res.get(ai).auftauchen++;
			}
		}
		
		//Suche maximales auftauchen
		int max=res.get(0).auftauchen;
		int maxKlasse=res.get(0).klasse;
		
		for(Haeufigkeit a: res) {
			if(a.auftauchen>max) {
				max=a.auftauchen;
				maxKlasse=a.klasse;
			}
		}
		return maxKlasse;
	}

	@Override
	public Statistiken klassifiziereTestdaten(ArrayList<EMGContainer> test) {
		Statistiken s=new Statistiken(ListenFunktionen.getAnzahlKlassen(klassifizierer));
		for(EMGContainer e:test) {
			int erg=klassifiziereInstanz(e);
			s.addErgebnis(erg, e.getZahl());
		}
		s.setWeitereInfos(this.k+"-Nearest-Neighbour");
		return s;
	}

	@Override
	public void speichereModell(String name) {
		Output.schreibeModell(this, name);
	}
	
	class Haeufigkeit {
		int klasse;
		int auftauchen;
		
		Haeufigkeit(int klasse, int auftauchen){
			this.klasse=klasse;
			this.auftauchen=auftauchen; 
		}
		
		public boolean equals(Object o) {
			Haeufigkeit oe;
			if(o instanceof Haeufigkeit) {
				oe=(Haeufigkeit) o;
			}else {
				return false;
			}
			return this.klasse==oe.klasse;
		}
	}
	
	class EMGHuelle implements Comparable<EMGHuelle>{
		
		EMGContainer emg;
		int distanzZuDemEinenDingsDa;
		
		public EMGHuelle(EMGContainer es) {
			this.emg=es;
		}
		@Override
		public int compareTo(EMGHuelle o) {
			return (int)(this.distanzZuDemEinenDingsDa-o.distanzZuDemEinenDingsDa);
		}
		
	}

	//Berechne eine Kostenmatrix
	private double[][] generiereKostenmatrix(double[] templatesensor, double[] erow) {
		double cost[][]=new double[templatesensor.length][erow.length];
		//System.out.println(templatesensor.length+"  "+erow.length);
		cost[0][0]=0;
		//cost[0][0]=Math.abs(a[0]-b[0]);
		for(int i=1;i<cost.length;i++) {
			cost[i][0]=Double.POSITIVE_INFINITY;
			//cost[i][0]=Math.abs(a[i]-b[0])+cost[i-1][0];
		}
		
		for(int i=1;i<cost[0].length;i++) {
			cost[0][i]=Double.POSITIVE_INFINITY;
			//cost[0][i]=Math.abs(a[i]-b[0])+cost[0][i-1];
		}
		for(int i=1;i<templatesensor.length;i++) {
			for(int j=1;j<erow.length;j++) {
				cost[i][j]=Math.abs(templatesensor[i]-erow[j])+Math.min(Math.min(cost[i-1][j], cost[i][j-1]), cost[i-1][j-1]);
			}
		}
		return cost;
	}
	
	//Ruft methode zur Berechnung der Kostenmatrix auf und gibt die Entfernung (letztes Element der Matrix) zurück. Für einen Sensor.
	private double kalkuliereDTWDistanz(double a[], double b[]) {
		double[][] costM=generiereKostenmatrix(a,b);
		
		return costM[costM.length-1][costM[0].length-1];
	}

	//Misst den DTW Abstand zwischen zwei EMGs. Dabei wird die Summe der einzelnen Abstände zurückgegeben.
	private int messeDTWAbstand(EMGContainer template, EMGContainer obj) {
		double sum=0;
		for(int i=0;i<obj.getAnzahlSensoren();i++) {
			sum+=kalkuliereDTWDistanz(template.getSensordaten(i), obj.getSensordaten(i));
		}
		return (int)sum;
	}
	
	
	

}
