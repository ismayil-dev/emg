package EMGVerarbeitung;

public class EMGProcessingSettings {
	
		private static String settings="== EMG Preprocessing Settings ==\n";
		
		public static void addSetting(String s) {
			settings+=s+"\n";
		}
		
		public static String getSettigns() {
			return settings;
		}
		
		
	
}
