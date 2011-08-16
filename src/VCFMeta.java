import java.util.ArrayList;


public class VCFMeta {
	
	String fileFomat;
	ArrayList<String> info;
	ArrayList<String> filter;
	ArrayList<String> format;

	// replace the arraylists with info, filter and format objects
	
	public VCFMeta() {
		
	}
	
	public void add (String s) {
		// remove leading #s
		// split the string by =
		// to give name and data
		
		if (name == fileformat) {
			fileFormat = data
		} else if (name == INFO) {
			
		} else if (name == FILTER) {
			
		} else if (name == FORMAT) {
			
		}
		
		String[] info = infoLine.trim().split("[<>]");
		String[] values = info[1].split(",");
		
	}
	
	public VCFMeta(String infoLine, String mt) {
		metaType = mt;
		
		
		for (String key : values) {
			if (key.startsWith("ID")) {
				String[] aID = key.split("=");
				ID = aID[1];
			} else if (key.startsWith("Number")) {
				String[] aNumber = key.split("=");
				number = Integer.parseInt(aNumber[1]);
			} else if (key.startsWith("Type")) {
				//Possible Types for INFO fields are: Integer, Float, Flag, Character, and String.
				String[] aType = key.split("=");
				type = aType[1];
			} else if (key.startsWith("Description")) {
				String[] aDescription = key.split("=");
				description = aDescription[1];
			}
		}
	}

	
	
}
