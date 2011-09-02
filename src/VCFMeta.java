import java.util.ArrayList;
import java.util.Hashtable;


public class VCFMeta {
	
	ArrayList<Hashtable<String, String>> info;
	ArrayList<Hashtable<String, String>> filter;
	ArrayList<Hashtable<String, String>> format;
	Hashtable<String,String> other;
	ArrayList<String> samples;
	ArrayList<String> infoIds;
	
	// replace the arraylists with info, filter and format objects?
	
	public VCFMeta() {
		info   = new ArrayList<Hashtable<String,String>> ();
		filter = new ArrayList<Hashtable<String,String>> ();
		format = new ArrayList<Hashtable<String,String>> ();
		other  = new Hashtable<String,String> ();
		infoIds = new ArrayList<String> ();
	}
	
	public void add (String s) {
		if (s.startsWith("##")) {
			s = s.substring(2);
			if (s.startsWith("INFO")) {
				String[] values = s.trim().split("[<>]");
				//split on the comma only if that comma has zero, or an even number of quotes in ahead of it
				// for explaination see here http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
				String[] values2 = values[1].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				Hashtable ht = new Hashtable<String,String> ();
				for (String val : values2) {
					if (val.contains("=")) {
						String[] values3 = val.split("=");
						if (values3[0].matches("ID")) {
							infoIds.add(values3[1]);
						}
						ht.put(values3[0], values3[1]);
					} else {
						ht.put(val, "true");
					}
				}
				info.add(ht);
			} else if (s.startsWith("FILTER")) {
				String[] values = s.trim().split("[<>]");
				String[] values2 = values[1].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				Hashtable ht = new Hashtable<String,String> ();
				for (String val : values2) {
					String[] values3 = val.split("=");
					ht.put(values3[0], values3[1]);
				}
				filter.add(ht);
			} else if (s.startsWith("FORMAT")) {
				String[] values = s.trim().split("[<>]");
				String[] values2 = values[1].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				Hashtable ht = new Hashtable<String,String> ();
				for (String val : values2) {
					String[] values3 = val.split("=");
					ht.put(values3[0], values3[1]);
				}
				format.add(ht);
			} else {
				String[] values = s.trim().split("=");
				other.put(values[0], values[1]);
			}
		} else if (s.startsWith("#")) {
			s = s.substring(1);
			// if there are format fields then there are genotypes in the file
			// so parse the sample ids
			if (format.size() > 0) {
				samples = new ArrayList<String> ();
				String[] values = s.trim().split("\t");
				for (int i = 9; i < values.length; i++) {
					samples.add(values[i]);
				}
			}
		}
	}

	public String getFileFormat() {
		return other.get("fileformat");
	}

	public ArrayList<Hashtable<String,String>> getInfo() {
		return info;
	}

	public ArrayList<Hashtable<String,String>> getFilter() {
		return filter;
	}

	public ArrayList<Hashtable<String,String>> getFormat() {
		return format;
	}

	public ArrayList<String> getSamples() {
		return samples;
	}

	public ArrayList<String> getInfoIds() {
		return infoIds;
	}

}
