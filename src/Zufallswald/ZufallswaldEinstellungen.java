package Zufallswald;

public class ZufallswaldEinstellungen {
	private static boolean used=false;
	private static String settings="== Random Forest Settings ==\n";
	
	public static void addSetting(String s) {
		if(!used) {
			settings+=(s+"\n");
			used=true;
		}
		
	}
	
	public static String getSettigns() {
		return settings;
	}
}
