package EMGVerarbeitung;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import DynamicTimeWarping.DynamicTimeWarping;
import KNaechsteNachbarn.NaechsteNachbarn;
import Zufallswald.Zufallswald;


public class EMGLeser {
	
	public static EMGContainer leseRuhewerte(String ppfad) {
		Path pfad=Paths.get(ppfad);
		return leseRuhewerte(pfad);
	}
	
	public static EMGContainer leseRuhewerte(Path pfad) {
		List<String> datei;
		try {
			datei = Files.readAllLines(pfad);
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen aufgetreten. Class: EMGParser");
			e.printStackTrace();
			return null;
		}
		String idstring = datei.get(0);
		EMGContainer erg=new EMGContainer();
		erg.setZahl(-1);
		erg.setId(idstring);
		datei.remove(0);
		String[] str;
		double matrix[][]= new double[datei.size()][8];
		for(int i=0; i<datei.size();i++) {
			str = datei.get(i).split(" ");
			for(int z=0;z<str.length;z++) {
				matrix[i][z]=Integer.parseInt(str[z]);
			}
		}
		erg.setDaten(matrix);
		return erg;
	}
	
	
	
	public static EMGContainer leseEMG(Path pfad) {
		List<String> datei;
		try {
			datei = Files.readAllLines(pfad);
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen aufgetreten. Class: EMGParser");
			e.printStackTrace();
			return null;
		}
		datei.remove(0);
		datei.remove(0);
		String idstring = datei.get(0);
		System.out.println("Lese: "+pfad.toString());
		idstring=idstring.substring(4, idstring.length());
		String[] str=idstring.split("-");
		EMGContainer ergebnis=new EMGContainer(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
		datei.remove(0);
		double matrix[][]= new double[datei.size()][8];
		for(int i=0; i<datei.size();i++) {
			str = datei.get(i).split(" ");
			for(int z=0;z<str.length;z++) {
				matrix[i][z]=Integer.parseInt(str[z]);
			}
		}
		ergebnis.setDaten(matrix);
		return ergebnis;
	}
	
	public static EMGContainer leseEMG(String ppfad) {
		Path pfad=Paths.get(ppfad);
		return leseEMG(pfad);
	}
	
	
	public static Zufallswald ladeRFModell(String fn) {
		Path file=Paths.get("rfModel/"+fn);
		InputStream in;
		Zufallswald zf=null;
		
		try {
			in=Files.newInputStream(file);
			ObjectInputStream deser = new ObjectInputStream(in);
			zf=(Zufallswald) deser.readObject();
			return zf;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return zf;
	}
	
	public static NaechsteNachbarn ladeKNNModell(String filename) {
		Path file=Paths.get("knnModel/"+filename);
		InputStream in;
		NaechsteNachbarn n=null;
		try {
			in = Files.newInputStream(file);
			ObjectInputStream deser = new ObjectInputStream(in);
			n=(NaechsteNachbarn) deser.readObject();
			return n;
		}catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return n;
	}

	
	public static DynamicTimeWarping ladeDTWModell(String filename) {
		Path file=Paths.get("dtwModel/"+filename);
		
		InputStream in;
			try {
				in = Files.newInputStream(file);
				ObjectInputStream deser = new ObjectInputStream(in);
			    DynamicTimeWarping model = (DynamicTimeWarping) deser.readObject();
			    return model;
			} catch (IOException e) {
				System.out.println("Fehler beim Laden des DTW Models.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("Fehler beim Laden des DTW Models.");
				e.printStackTrace();
			}
			return null;
	}
	
	
	public static ArrayList<EMGContainer> leseVerzeichnis(String ppfad) throws IOException {
		ArrayList<EMGContainer> ergebnis=new ArrayList<EMGContainer>();
		File fpfad=new File(ppfad);
		File dateien[] = fpfad.listFiles(new EMGDateiFilter());
		if(dateien.length==0) {
			throw new IOException("Keine Datei gefunden.");
		}
		for(File f: dateien) {
			ergebnis.add(leseEMG(f.toPath()));
		}
		System.out.println("Anzahl gelesener Datein: "+ergebnis.size());
		return ergebnis;
	}
	
	public static ArrayList<EMGContainer> leseVerzeichnisUnklassifiziert(String ppfad) throws IOException{
		ArrayList<EMGContainer> ergebnis=new ArrayList<EMGContainer>();
		File fpfad=new File(ppfad);
		File dateien[] = fpfad.listFiles(new EMGDateiFilter());
		if(dateien.length==0) {
			throw new IOException("Keine Datei gefunden.");
		}
		for(File f: dateien) {
			ergebnis.add(leseUnklassifiziertesEMG(f.toPath()));
		}
		System.out.println("Anzahl gelesener Dateien: "+ergebnis.size());
		return ergebnis;
	}

	private static EMGContainer leseUnklassifiziertesEMG(Path pfad) {
		List<String> datei;
		try {
			datei = Files.readAllLines(pfad);
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen aufgetreten. Klasse: EMGParser");
			e.printStackTrace();
			return null;
		}
		System.out.println("Lese: "+pfad.toString());
		String str[];
		datei.remove(0);
		double matrix[][]= new double[datei.size()][8];
		for(int i=0; i<datei.size();i++) {
			str = datei.get(i).split(" ");
			for(int z=0;z<str.length;z++) {
				matrix[i][z]=Integer.parseInt(str[z]);
			}
		}
		EMGContainer ergebnis=new EMGContainer();
		ergebnis.setId(pfad.getFileName().toString());
		ergebnis.setDaten(matrix);
		return ergebnis;
	}
		
}
