package EMGVerarbeitung;
import java.util.ArrayList;

public class ListenFunktionen {

	public static void printIDs(ArrayList<EMGContainer> emgs) {
		for(EMGContainer e:emgs) {
			System.out.println(e.getId());
		}
	}
	
	
	/*
		route/distance/track
	 */
	public static void strecke(ArrayList<EMGContainer> data, int size) {
		EMGProcessingSettings.addSetting("Stretch "+size);
		for(EMGContainer e:data) {
			e.strecke(size);
		}
	}
	
	/*
	public static void medianfilter(ArrayList<EMGContainer> data, int size) {
		for(EMGContainer e:data) {
			e.medianfilter(size);
		}
	}*/

	/*
		reticfy
	 */
	public static void gleichrichten(ArrayList<EMGContainer> data) {
		EMGProcessingSettings.addSetting("Gleichrichter");
		for(EMGContainer e:data) {
			e.gleichrichten();
		}
	}

	/*
	 	average filter
	 */
	public static void mittelwertfilter(ArrayList<EMGContainer> data, int size) {
		EMGProcessingSettings.addSetting("Mittelwertfilter "+size);
		for(EMGContainer e: data) {
			e.mittelwertfilter(size);
		}
	}

	/*
	 	low pass filter
	 */
	public static void tiefpassfilterFFT(ArrayList<EMGContainer> data, int cuttoff) {
		for(EMGContainer e:data) {
			FourierTransformation.tiefpassFFT(e, cuttoff);
		}
	}

	/*
		high pass filter
	 */
	public static void hochpassfilterFFT(ArrayList<EMGContainer> data, int cuttoff) {
		for(EMGContainer e:data) {
			FourierTransformation.hochpassFFT(e, cuttoff);
		}
	}
	
	
	/*
		folding
	 */
	public static void faltung(ArrayList<EMGContainer> data,double kern[]) {
		String kernS="";
		for(int i=0;i<kern.length;i++) {
			kernS+=kern[i]+" ";
		}
		EMGProcessingSettings.addSetting("Faltung size: "+kern.length+" Kern: "+kernS);
		for(EMGContainer e: data) {
			e.faltung(kern);
		}
	}
	

	/*
		binominal filter
	 */
	private static double binominalkoeffizient(int n, int k) {
		if(k==0)return 1;
		if(2*k>n)k=n-k;
		double erg=1;
		for(int i=1;i<=k;i++) {
			erg=erg*(n-k+i)/i;
		}
		return erg;
	}

	/*
		binomal filter
	 */
	public static double[] binominalfilter(int groesse) {
		groesse--;
		double[] kern=new double[groesse+1];
		for(int k=0;k<kern.length;k++) {
			kern[k]=binominalkoeffizient(groesse,k);
		}
		//kern normieren
		double sum=0;
		for(int i=0;i<kern.length;i++) {
			sum+=kern[i];
		}
		for(int i=0;i<kern.length;i++) {
			kern[i]/=sum;
		}
		return kern;
	}
	/*

	 */
	public static int getAnzahlKlassen(ArrayList<EMGContainer> emgs) {
		ArrayList<Integer> cont=new ArrayList<Integer>();
		for(EMGContainer e: emgs) {
			if(!cont.contains(e.getZahl())){
				cont.add(e.getZahl());
			}
		}
		return cont.size();
	}
	
	/*
		copy array
	 */
	public static ArrayList<EMGContainer> tiefeKopie(ArrayList<EMGContainer> emgs){
		ArrayList<EMGContainer> copy=new ArrayList<EMGContainer>();
		for(EMGContainer e: emgs) {
			copy.add(e.clone());
		}
		return copy;
	}

	/*
		average total
	 */
	public static double durchsnittDerSumme(EMGContainer e) {
		double sum[]=e.getSensordaten(0);
		for(int i=1;i<e.getAnzahlSensoren();i++) {
			double tmp[]=e.getSensordaten(i);
			for(int j=0;j<sum.length;j++) {
				sum[j]+=tmp[j];
			}
		}
		double sum2=0;
		for(int i=0;i<sum.length;i++) {
			sum2+=sum[i];
		}
		sum2/=sum.length;
		return sum2;
	}
	
	/*
		short without rest
	 */
	public static void kuerzeOhneRuhe(ArrayList<EMGContainer> emgs) {
		for(EMGContainer e: emgs) {
			e.kuerzeEMG();
		}
	}

	/*
		short with rest
	 */
	public static void kuerzeMitRuhe(ArrayList<EMGContainer> emgs, EMGContainer ruhe, int fenstergroesse, double faktor) {
		EMGProcessingSettings.addSetting("Cut EMGs window: "+fenstergroesse+" Faktor: "+faktor);
		double threshold=faktor*(durchsnittDerSumme(ruhe)+berechneStandartabweichung(ruhe));
		//System.out.println("Threshold: "+threshold);
		for(EMGContainer e: emgs) {
			int left=e.berechneLinkenSchnitt(fenstergroesse, threshold);
			int right=e.berechneRechtenSchnitt(fenstergroesse, threshold);
			//System.out.println("Left: "+left+"  Right: "+right);
			if(left<right)e.schneide(left, right);
		}
	}
	
	
	/*
		remove rest values
	 */
	public static EMGContainer entferneRuhewerte(ArrayList<EMGContainer> emgs) {
		int so=emgs.size();
		EMGContainer r;
		for(EMGContainer e: emgs) {

			if(e.getZahl()==-1) {
				r=e;
				emgs.remove(r);
				assert so>emgs.size();
				return r;
			}
		}
		assert so>emgs.size();
		return null;
	}

	
	/*
		calculate standard deviation
	 */
	public static double berechneStandartabweichung(EMGContainer e) {
		double mean=durchsnittDerSumme(e);
		double stdsum=0;
		// Summiere alle Datenreihen auf
		// Sum up all data series
		double sum[]=e.getSensordaten(0);
		for(int i=1;i<e.getAnzahlSensoren();i++) {
			double tmp[]=e.getSensordaten(i);
			for(int j=0;j<sum.length;j++) {
				sum[j]+=tmp[j];
			}
		}
		
		for(int i=0;i<sum.length;i++) {
			stdsum+=(sum[i]-mean)*(sum[i]-mean);
		}
		stdsum/=sum.length;
		stdsum=Math.sqrt(stdsum);
		return stdsum;
		
	}
	

	/*
		rectify
	 */
	public static void gleichrichten(EMGContainer ruhewerte) {
		ruhewerte.gleichrichten();
	}
	
	/*public static void studentisiere(ArrayList<EMGContainer> emgs) {
		for(EMGContainer e: emgs) {
			e.studentisiere();
		}
	}*/
	
	
//	public static double[] getOwnFilter() {
//		return new double[] {
//				  0.0005390547608303915,
//				  0.0034780145531085813,
//				  0.01191458759243169,
//				  0.029514084891600317,
//				  0.0580620683518338,
//				  0.09522329382791941,
//				  0.13363464598788297,
//				  0.16292109092755946,
//				  0.1739025208317618,
//				  0.16292109092755946,
//				  0.13363464598788297,
//				  0.09522329382791941,
//				  0.0580620683518338,
//				  0.029514084891600317,
//				  0.01191458759243169,
//				  0.0034780145531085813,
//				  0.0005390547608303915
//
//
//				  };
//	}
	
	
	
	//Samplet die Eingabe auf die entsprechende Frequenz (MYO hat 200HZ sampling Rate)
	// Sample the input to the appropriate frequency
	public static void aendereTaktrate(ArrayList<EMGContainer> emgs,int frequenz) {
		EMGProcessingSettings.addSetting("Samplerate "+frequenz);

		final int samplerate=200;
		
		for(EMGContainer e: emgs) {
			int  newlength=(int)(((double)frequenz/(double)samplerate)*e.getLeange());
			e.strecke(newlength);
			System.out.println("Stertch auf: "+newlength);
		}
	}
	
}
