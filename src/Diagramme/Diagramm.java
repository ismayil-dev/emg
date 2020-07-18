package Diagramme;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import EMGVerarbeitung.EMGContainer;

public class Diagramm {
	
	
	public static void printArray(double d[], int length) {
		double xachse[]=new double[d.length];
		for(int i=0;i<xachse.length;i++) {
			xachse[i]=(i*(200/(double)length));
		}
		
		XYChart chart = QuickChart.getChart("Frequenz Domain", "Frequenz in Hz", "Amplitude", "y(x)", xachse, d);
		 
	    // Show it
	    new SwingWrapper(chart).displayChart();
	}
	
	
	
	public static void plotEMGSum(EMGContainer e) {
		double vals[]=new double[e.getLeange()];
		double evals[][]=e.getDaten();
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			//double r[]=e.getSensorData(i);
			for(int j=0;j<vals.length;j++) {
				vals[j]+=evals[j][i];
			}	
		}
		
		double xachse[]=new double[e.getLeange()];
		for(int i=0;i<xachse.length;i++) {
			xachse[i]=i;
		}
		
		XYChart ch=QuickChart.getChart("Summe der Signale "+e.getId(), "Zeit", "Amplitude","Signal",xachse,vals);
		new SwingWrapper(ch).displayChart();
		
		
		
	}
	
	
	public static void plotEMGContainer(EMGContainer e) {
		/*ArrayList<Double[]> zs=new ArrayList<Double[]>();
		
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			Double[] tmpW=new Double[e.getSamplelength()];
			double[] tmp=e.getSensorData(i);
			for(int x=0;x<tmp.length;x++) {
				tmpW[x]=tmp[x];
			}
			zs.add(tmpW);
		}*/
		double vals[][]=new double[e.getAnzahlSensoren()][e.getLeange()]; 
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			double r[]=e.getSensordaten(i);
			for(int j=0;j<vals[0].length;j++) {
				vals[i][j]=r[j];
			}	
		}
		
		
		double xachse[]=new double[e.getLeange()];
		for(int i=0;i<xachse.length;i++) {
			xachse[i]=i;
		}
		String names[]=new String[e.getAnzahlSensoren()];
		for(int i=0;i<names.length;i++) {
			names[i]="Sensor "+i;
		}
		
		XYChart ch=QuickChart.getChart(e.getId(), "Zeit", "Amplitude", names, xachse, vals );
		new SwingWrapper(ch).displayChart();
		
	}
}
