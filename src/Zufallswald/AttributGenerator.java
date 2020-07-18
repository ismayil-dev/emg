package Zufallswald;
import EMGVerarbeitung.EMGContainer;

public class AttributGenerator {

	
	
	//Berechnet die Regressonsgeraden zur Approximation der EMGs
	public static void regression(EMGContainer e, Instanz  my, int parts) {
	
		double intervallsize=e.getDaten().length/(double)parts;
		if(intervallsize<1) {
			System.out.println(e.getId());
			throw new DatenReiheZuKurz();
		}
		
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double data[]=e.getSensordaten(sensor);
			int partl=0;
			int part=0;
			int startIndex=0;
			int endIndex=0;
			int x=0;
			for(;x<data.length;x++,partl++) {
	
				
				if(x>=part*intervallsize+intervallsize) {
					endIndex=x-1;
					
	
					double meanx=0;
					double meany=0;
					double sumo=0;
					double sumu=0;
	
					for(int i=startIndex;i<=endIndex;i++) {
						meanx+=i;
						meany+=data[i];
					}
	
					meanx/=(double)partl;
					meany/=(double)partl;
	
					for(int i=startIndex;i<=endIndex;i++) {
						sumo+=(i-meanx)*(data[i]-meany);
					}
	
					for(int i=startIndex;i<=endIndex;i++) {
						sumu+=(i-meanx)*(i-meanx);
					}
					my.addAttribut("regx"+sensor+"p"+part, (sumo/sumu));
					my.addAttribut("regb"+sensor+"p"+part, (meany-((sumo/sumu)*meanx)));
						
					startIndex=x;
					part++;
					partl=0;
				}
	
			}
			endIndex=x-1;
	
			double meanx=0;
			double meany=0;
			double sumo=0;
			double sumu=0;
	
			for(int i=startIndex;i<=endIndex;i++) {
				meanx+=i;
				meany+=data[i];
			}
	
			meanx/=(double)partl;
			meany/=(double)partl;
	
			for(int i=startIndex;i<=endIndex;i++) {
				sumo+=(i-meanx)*(data[i]-meany);
			}
	
			for(int i=startIndex;i<=endIndex;i++) {
				sumu+=(i-meanx)*(i-meanx);
			}
			
			my.addAttribut("regx"+sensor+"p"+part, sumo/sumu);
			my.addAttribut("regb"+sensor+"p"+part, (meany-((sumo/sumu)*meanx)));
	
			
		}
		
	
	}

	//Kalkuliert die Summe aller messwerte je sensor und insgesamt.
	public static void summe(EMGContainer e, Instanz ins) {
		double sumoverall=0;
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			double sensordata[]=e.getSensordaten(i);
			double sensorsumme=0;
			for(int n=0;n<sensordata.length;n++) {
				sensorsumme+=sensordata[i];
				sumoverall+=sensordata[i];
			}
			ins.addAttribut("sum"+i, sensorsumme);
			
			
		}
		ins.addAttribut("sumoverall", sumoverall);
	}

	public static void standartabweichung(EMGContainer e, Instanz ins){
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			double sensordata[]=e.getSensordaten(i);
			double mean=AttributGenerator.mean(sensordata);
			double sum=0;
			for(int n=0;n<sensordata.length;n++) {
				sum+=(sensordata[i]-mean)*(sensordata[i]-mean);
			}
			sum/=sensordata.length;
			sum=Math.sqrt(sum);
			ins.addAttribut("stdabw"+i, sum);
		}
	}

	private  static double mean(double[] data) {
		
		double mean=0;
		for(int j=0;j<data.length;j++) {
			mean+=data[j];
		}
		mean/=data.length;
		return mean;
	}

	public static void durchschnitt(EMGContainer e, Instanz ins) {
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			double sensordata[]=e.getSensordaten(i);
			double mean=mean(sensordata);
			ins.addAttribut("avg"+i, mean);
	
		}
	}

	//Berechnett den Durchschnitt auf einzlenen Intervallen.
	public static void partiellerDurchschnitt(EMGContainer e, Instanz my, int parts) {
		double intervallsize=e.getDaten().length/((double)parts);
		int partl=0;
		double sum=0;
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double data[]=e.getSensordaten(sensor);
			int part=0;
			for(int x=0;x<data.length;x++,partl++) {
	
				if(x>=part*intervallsize+intervallsize) {
					my.addAttribut("avgS"+sensor+"P"+part, sum/(double)partl);
					sum=0;
					part++;
					partl=0;
					
				}
				sum+=data[x];
				
			}
			my.addAttribut("avgS"+sensor+"P"+part, sum/(double)partl);
		}
	
	}

	//Daten werden 1 zu 1 als Attribut genommen. Vorsicht! EMGs müssen alle gleichlang sein.
	public static void copy1to1(EMGContainer e, Instanz my) {
		for(int i=0;i<e.getAnzahlSensoren();i++) {
			double data[]=e.getSensordaten(i);
			for(int x=0;x<data.length;x++) {
				my.addAttribut("ori"+i+"x"+x, data[x]);
			}
			
		}
	}

	public static void lokalemaxima(EMGContainer e, Instanz my) {
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double data[]=e.getSensordaten(sensor);
			double anzahl=0;
			for(int i=1;i<data.length-1;i++) {
				if(data[i-1]<data[i]&&data[i+1]<data[i])anzahl++;	
			}
			my.addAttribut("maxnum"+sensor, anzahl);	
		}
	}

	public static void lokaleminima(EMGContainer e, Instanz my) {
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double data[]=e.getSensordaten(sensor);
			double anzahl=0;
			for(int i=1;i<data.length-1;i++) {
				if(data[i-1]>data[i]&&data[i+1]>data[i])anzahl++;	
			}
			my.addAttribut("minnum"+sensor, anzahl);	
		}
	}

}
