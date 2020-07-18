package Zufallswald;

public class DatenReiheZuKurz extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DatenReiheZuKurz(){
		   	super("Es befindet sich eine Datenreihe in den Daten, die zu Kurz ist um genügend Attribute zu generieren. Prüfe die Filtereinstellungen.");
	}

		  public DatenReiheZuKurz(String fehlermeldung){
			  super(fehlermeldung);
		  }
	
	
	
	
}
