package EMGVerarbeitung;

public class Statistiken {
	private double konfusionsMatrix[][];
	
	/* Confusion matrix:
	 *    
	 * 	x	x	x
	 * 
	 * 	x	x	x
	 * 
	 * 	x	x	x
	 * 
	 */
	
	
	private String weitereInfos;
	
	public String getWeitereInfos() {
		return weitereInfos;
	}

	public void setWeitereInfos(String weitereInfos) {
		this.weitereInfos = weitereInfos;
	}

	public Statistiken(int klassen) {
		konfusionsMatrix=new double[klassen][klassen];
	}
	
	public Statistiken(double[][] sum) {
		this.konfusionsMatrix=sum;
	}

	public Statistiken() {
		
	}

	public void addErgebnis(int ist, int soll) {
		konfusionsMatrix[soll][ist]++;
	}
	
	
	
	public void setKonfusionsMatrix(double cm[][]) {
		this.konfusionsMatrix=cm;
	}
	
	public void printKonfusionsMatrix() {
		for(int x=0;x<konfusionsMatrix.length;x++) {
			for(int y=0;y<konfusionsMatrix[0].length;y++) {
				System.out.print("  "+konfusionsMatrix[x][y]+" \t");
			}
			System.out.println("");
		}
	}
	
	public double[][] getKonfusionsMatrix() {
		return this.konfusionsMatrix;
	}
	
	
}
