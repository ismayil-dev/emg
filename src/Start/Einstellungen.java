package Start;

import java.util.ArrayList;

import EMGVerarbeitung.EMGContainer;
import EMGVerarbeitung.Klassifizierer;

public class Einstellungen {
	private boolean speichern=false;
	private String filterString=null;
	private String pfadDaten=null;
	
	private String infos=null;
	
	public String getInfos() {
		return infos;
	}
	public void setInfos(String infos) {
		this.infos = infos;
	}
	public String getDatenpfad() {
		return pfadDaten;
	}
	public void setDatenpfad(String datapath) {
		this.pfadDaten = datapath;
	}
	private boolean trainiere=false;
	private boolean kuerzen=false;
	private String speichername=null;
	
	private Klassifizierer modell=null;
	EMGContainer ruhewerte=null;
	private double faktor=1;
	
	ArrayList<EMGContainer> input;
	
	
	
	
	
	public double getFaktor() {
		return faktor;
	}
	public void setFaktor(double factor) {
		this.faktor = factor;
	}
	public boolean istSpeichern() {
		return speichern;
	}
	public void setSpeichern(boolean save) {
		this.speichern = save;
	}
	public String getFilterString() {
		return filterString;
	}
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}
	public boolean istTrainiere() {
		return trainiere;
	}
	public void setTrainiere(boolean train) {
		this.trainiere = train;
	}
	public boolean istGekuerzt() {
		return kuerzen;
	}
	public void setKuerzen(boolean cut) {
		this.kuerzen = cut;
	}
	public String getSpeichername() {
		return speichername;
	}
	public void setSpeichername(String savename) {
		this.speichername = savename;
	}
	public Klassifizierer getModell() {
		return modell;
	}
	public void setModell(Klassifizierer model) {
		this.modell = model;
	}
	public EMGContainer getRuhewerte() {
		return ruhewerte;
	}
	public void setRuhewerte(EMGContainer ruhewerte) {
		this.ruhewerte = ruhewerte;
	}
	public ArrayList<EMGContainer> getInput() {
		return input;
	}
	public void setInput(ArrayList<EMGContainer> input) {
		this.input = input;
	}
}
