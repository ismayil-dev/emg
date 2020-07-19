package EMGVerarbeitung;

import java.io.Serializable;


public class EMGContainer implements Cloneable, Comparable<Object>, Serializable {

	static final long serialVersionUID = 100;
	private int zahl;
	private int durchlauf;
	private String id;
	private double daten[][];
	
	public EMGContainer clone() {
		EMGContainer erg=new EMGContainer(this.zahl,this.durchlauf);
		erg.id=this.id;
		//do Deep copy of 2D Array
		double copy[][]=new double[daten.length][daten[0].length];
		for(int x=0;x<daten.length;x++) {
			for(int y=0;y<daten[0].length;y++) {
				copy[x][y]=daten[x][y];
			}
		}
		erg.setDaten(copy);
		return erg;
	}
	
	
	public void faltung(double [] kern){
		double [][] z1=this.daten;
		int kernl=kern.length;
		double erg[][]=new double[z1.length][z1[0].length];
		for(int sensor=0;sensor<z1[0].length;sensor++) {
			for(int zeile=kernl/2;zeile<z1.length-(kernl/2);zeile++) {
				double sum=0;
				for(int left=zeile-kernl/2;left<=zeile+kernl/2;left++) {
					sum+=kern[(left+(kernl/2))-zeile]*z1[left][sensor];
				}
				erg[zeile][sensor]=sum;
			}
		}
		this.daten=erg;
	}
	
	
	
	/*

	 */
	public void mittelwertfilter(int groesse) {
		double z1[][]=daten;
		double z2[][]=new double[z1.length][z1[0].length];
		for(int sensor=0;sensor<z1[0].length;sensor++) {
			for(int x=groesse/2;x<z1.length-(groesse/2);x++) {
				double sum=0;
				for(int left=x-(groesse/2);left<=x+(groesse/2);left++) {
					sum+=z1[left][sensor];
				}
				z2[x][sensor]=sum/groesse;
			}	
		}
		this.daten=z2;
	}
	
	
	
	private double[] berechneSumme() {

		double sum[]=new double[daten.length];

		for(int i=0;i<daten.length;i++) {
			for(int j=0;j<daten[0].length;j++) {
				sum[i]+=daten[i][j];
			}
		}
		return sum;
	}
	
	
	public void kuerzeEMG(){
		double[] sum=this.berechneSumme();
		double avg=0;
		double standardabweichung=0;
		
		for(int i=0;i<sum.length;i++) {
			avg+=sum[i];
		}
		avg/=(double)sum.length;
	
		for(int i=0;i<sum.length;i++) {
			standardabweichung+=(sum[i]-avg)*(sum[i]-avg);
		}
		standardabweichung/=sum.length;
		standardabweichung=Math.sqrt(standardabweichung);
		double threshold=avg+standardabweichung;
		this.kuerzeEMG(threshold);
	}
	
	
	private void kuerzeEMG(double schwellwert) {
		int leftcut=0;
		int rightcut=this.daten.length-1;
		double[] sum=this.berechneSumme();
		for(int i=0;i<sum.length;i++) {
			if(sum[i]>schwellwert) {
				leftcut=i;
				break;
			}
		}
		
		for(int i=sum.length-1;i>leftcut;i--) {
			if(sum[i]>schwellwert) {
				rightcut=i;
				break;
			}
		}
		this.schneide(leftcut, rightcut);
	}
	
	
	
	public int berechneLinkenSchnitt(int groesse, double schwellwert) {
		double emgsum[]=berechneSumme();
		for(int i=0;i<daten.length-groesse;i++) {
			double sum=0;
			for(int w=i;w<i+groesse;w++) {
				sum+=emgsum[w];
			}
			sum/=groesse;
			if(sum>=schwellwert) {
				return i;
			}
			
		}
		return 0;
	}
	
	public int berechneRechtenSchnitt(int groesse, double schwellwert) {
		double emgsum[]=berechneSumme();
		for(int i=daten.length-1;i>groesse;i--) {
			double sum=0;
			for(int w=i;w>i-groesse;w--) {
				sum+=emgsum[w];
			}
			sum/=groesse;
			if(sum>=schwellwert) {
				return i;
			}
			
		}
		return 0;
	}
	
	
	public EMGContainer() {}
	
	public EMGContainer(int zahl, int durchlauf) {
		super();
		this.zahl = zahl;
		this.durchlauf = durchlauf;
		this.id=""+zahl+"-"+durchlauf;
	}
	
	public EMGContainer(double[][] test) {
		this.daten=test;
	}

	public void gebeDatenAus() {
		System.out.println("-----EMG------");
		for(int i=0;i<daten.length;i++) {
			for(int j=0;j<daten[0].length;j++) {
				System.out.print(daten[i][j]+" ");
			}
			System.out.println("");
		}
		System.out.println("---------------");
	}
	
	public void gebeIDAus() {
		System.out.println("ID: "+id);
	}
	
	public void setDaten(double daten[][]) {
		this.daten=daten;
	}
	
	public double[][] getDaten(){
		return this.daten;
	}
	
	
	//full wave rectification
	public void gleichrichten() {
		for(int i=0;i<daten.length;i++) {
			for(int j=0;j<daten[0].length;j++) {
				daten[i][j]=Math.abs(daten[i][j]);
			}
		}
	}
	
	
	public int getLeange() {
		return daten.length;
	}
	
	public void schneide(int beginn, int ende) {
		//beginn = 7
		//ende = 9
		if(zahl==-1)return;
		assert ende>beginn;
		double tmp[][]=new double[(ende-beginn)+1][daten[0].length];
		// tmp = [3][8]
		for(int i=beginn;i<=ende;i++) {
			for(int j=0;j<daten[0].length;j++) {
				tmp[i-beginn][j]=daten[i][j];
			}
		}
		this.daten=tmp;
	}
	
	private double getWert(int sensor, double x) {
		int linksi=(int) Math.floor(x);
		int rechtsi=linksi+1;
		
		if(x>=daten.length-1) {
			return daten[daten.length-1][sensor];
		}
		double links=daten[linksi][sensor];
		double rechts=daten[rechtsi][sensor];
		double steigung=(rechts-links)/(rechtsi-linksi);
		return links+steigung*(x-linksi);
		
	}
	
	
	public double[] getSensordaten(int sensor){
		double erg[]=new double[daten.length];
		for(int i=0;i<daten.length;i++) {
			erg[i]=daten[i][sensor];
		}
		return erg;
	}
	
	
	public int getAnzahlSensoren() {
		return daten[0].length;
	}
	
	
	public void strecke(int newsize) {
		double faktor=((double)this.daten.length-1)/(newsize-1);
		double erg[][]=new double[newsize][daten[0].length];
		for(int i=0;i<erg.length;i++) {
			for(int j=0;j<erg[0].length;j++) {
				erg[i][j]=getWert(j, i*faktor);
			}
		}
		this.daten=erg;
	}
	

	public int getZahl() {
		return zahl;
	}

	public void setZahl(int zahl) {
		this.zahl = zahl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		return compareTo(o)==0;
	}


	@Override
	public int compareTo(Object o) {
		EMGContainer e=(EMGContainer) o;
		if(this.zahl!=e.getZahl()) {
			return this.zahl-e.getZahl();
		}
		if(this.durchlauf!=e.getDurchlauf()) {
			return this.durchlauf-e.getDurchlauf();
		}
		return this.id.compareTo(e.getId());
	}


	public int getDurchlauf() {
		return durchlauf;
	}
}
