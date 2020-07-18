package EMGVerarbeitung;

import java.util.ArrayList;

public interface Klassifizierer {
	public void generiereModell(ArrayList<EMGContainer> trainingsdaten);
	public int klassifiziereInstanz(EMGContainer e);
	public Statistiken klassifiziereTestdaten(ArrayList<EMGContainer> testdaten);
	public void speichereModell(String name);
}
