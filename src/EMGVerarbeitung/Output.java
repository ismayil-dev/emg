package EMGVerarbeitung;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import DynamicTimeWarping.DynamicTimeWarping;
import EMGVerarbeitung.EMGContainer;
import KNaechsteNachbarn.NaechsteNachbarn;
import Zufallswald.ZufallswaldEinstellungen;
import Zufallswald.Zufallswald;

import java.io.BufferedWriter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Output {
	


	public static void schreibeEMG(EMGContainer e, String name) {
		double [][] data=e.getDaten();
		Path pfad=Paths.get("bin/output/out-"+e.getZahl()+"-"+e.getDurchlauf()+name);
		BufferedWriter writer = null;
		try {
			writer= Files.newBufferedWriter(pfad);
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");
			e1.printStackTrace();
			
		}
		int time=0;
		
		try {
			for(int i=0;i<data.length;i++) {
				
				writer.write(""+time+" ");
				for(int j=0;j<data[0].length;j++) {
					writer.write(""+data[i][j]+" ");
				}
				time+=5;
				writer.newLine();
			}
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");
			e1.printStackTrace();
		}
		try {
			writer.close();
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");
			e1.printStackTrace();
		}	
	}
	
	
	
	public static void schreibeEMG(EMGContainer e) {
		double [][] data=e.getDaten();
		Path pfad=Paths.get("bin/output/out-"+e.getZahl()+"-"+e.getDurchlauf());
		BufferedWriter writer = null;
		try {
			writer= Files.newBufferedWriter(pfad);
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");

			e1.printStackTrace();
		}
		int time=0;
		
		try {
			for(int i=0;i<data.length;i++) {
				writer.write(""+time+" ");
				for(int j=0;j<data[0].length;j++) {
					writer.write(""+data[i][j]+" ");
				}
				time+=5;
				writer.newLine();
			}
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");

			e1.printStackTrace();
		}
		try {
			writer.close();
		} catch (IOException e1) {
			System.out.println("Fehler beim schreiben des EMGContainers.");

			e1.printStackTrace();
		}	
	}
	
	public static void schreibeModell(Object o, String dateiname) {
		if(o instanceof DynamicTimeWarping) {
			schreibeDTWModell((DynamicTimeWarping) o, dateiname);
		}else if(o instanceof Zufallswald) {
			schreibeWEKAModell((Zufallswald) o,dateiname);
		}else if(o instanceof NaechsteNachbarn){
			schreibeKNNModell((NaechsteNachbarn) o,dateiname);
		}
	}
	
	
	
	private static void schreibeKNNModell(NaechsteNachbarn o, String dateiname) {
		System.out.println("Speichere Modell");
		Path pfad=Paths.get("knnModel/"+dateiname);
		try {
			OutputStream out=Files.newOutputStream(pfad);
			ObjectOutputStream outo=new ObjectOutputStream(out);
			outo.writeObject(o);
		} catch (IOException e) {
			System.out.println("Fehler beim schreiben des DTWModels.");
			e.printStackTrace();
		}
		
	}



	private static void schreibeWEKAModell(Zufallswald o, String dateiname) {
		System.out.println("Speichere RF Modell.");
		Path pfad=Paths.get("rfModel/"+dateiname);
		try {
			OutputStream out=Files.newOutputStream(pfad);
			ObjectOutputStream outo=new ObjectOutputStream(out);
			outo.writeObject(o);
			//TODO Check functionality
			//weka.core.SerializationHelper.write("rfModel/"+dateiname, o);
		} catch (Exception e) {
			System.out.println("Fehler beim Speichern des Modells.");
			e.printStackTrace();
		}
	}
	
	private static void schreibeDTWModell(DynamicTimeWarping dtw, String dateiname) {
		Path pfad=Paths.get("dtwModel/"+dateiname);
		
		try {
			OutputStream out=Files.newOutputStream(pfad);
			ObjectOutputStream outo=new ObjectOutputStream(out);
			outo.writeObject(dtw);
		} catch (IOException e) {
			System.out.println("Fehler beim schreiben des DTWModels.");

			e.printStackTrace();
		}
		
	}
	
	
	public static void schreibeErgebnisse(ArrayList<Statistiken> stats, String add, String name) {
		Date today = Calendar.getInstance().getTime();
		String pattern = "dd-MM-yyyy-HH-mm-ss";
		DateFormat df = new SimpleDateFormat(pattern);
		String todayAsString = df.format(today);
		//Path pfad=Paths.get("bin/results/"+todayAsString+name);
		
		
		Path pfad=Paths.get("results/"+todayAsString+name);
		
		String out=add+"\n"+Statistikauswerter.statsToString(stats);
		//System.out.println(out);
		
		try {
			Files.write(pfad, out.getBytes());
			
		} catch (IOException e) {
			System.out.println("Fehler beim schreiben der Ergebnisse.");

			e.printStackTrace();
		}
	}

//	public static void writeLaufzeit(String infos) {
//		Date today = Calendar.getInstance().getTime();
//		String pattern = "dd-MM-yyyy-HH:mm:ss";
//		DateFormat df = new SimpleDateFormat(pattern);
//		String todayAsString = df.format(today);
//		Path pfad=Paths.get("bin/results/"+todayAsString+"LAUFZEIT");
//		
//		Laufzeithandler l=Laufzeithandler.getLaufzeithandler();
//		infos+="\n";
//		infos+="AVG DTW: "+l.getDTWAvg()+"ms \n";
//		infos+="AVG RF: "+l.getRFAvg()+"ms \n";
//		infos+="AVG KNN: "+l.getKNNAvg()+"ms \n";
//		
//		
//		try {
//			Files.write(pfad, infos.getBytes());
//		} catch (IOException e) {
//			System.out.println("Fehler beim schreiben der Einstellungen.");
//
//			e.printStackTrace();
//		}
//	}
	
	
	public static void schreibeErgebnisse(Statistiken s,String add, String name) {
		ArrayList<Statistiken> tmp=new ArrayList<Statistiken>();
		tmp.add(s);
		schreibeErgebnisse(tmp,add,name);
	}
	
	

	public static void schreibeEinstellungen(){
		String write=EMGProcessingSettings.getSettigns()+"\n \n"+ZufallswaldEinstellungen.getSettigns();
		Date today = Calendar.getInstance().getTime();
		String pattern = "dd-MM-yyyy-HH:mm:ss";
		DateFormat df = new SimpleDateFormat(pattern);
		String todayAsString = df.format(today);
		Path pfad=Paths.get("results/"+todayAsString+"SETTINGS");
		try {
			Files.write(pfad, write.getBytes());
		} catch (IOException e) {
			System.out.println("Fehler beim schreiben der Einstellungen.");

			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
}
