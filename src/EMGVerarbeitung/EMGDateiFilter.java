package EMGVerarbeitung;
import java.io.File;
import java.io.FilenameFilter;

public class EMGDateiFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		if(name.contains("emg"))return true;
		return false;
	}

}
