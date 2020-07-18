package Start;

public class StratifikationNichtMoeglichException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 100;

	/**
	 * 
	 */

	public StratifikationNichtMoeglichException(){
		   	super("Stratifikation nicht Möglich, prüfe Folds");
	}

	  public StratifikationNichtMoeglichException(String fehlermeldung){
		  super(fehlermeldung);
	  }
	
	
	
	
}
