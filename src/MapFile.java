import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class MapFile {

	BufferedReader mfr;
	Vector<Integer> positions;
	
	public MapFile (String fileName) throws IOException {
		if (fileName.endsWith(".gz")) {
			mfr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		} else {
			mfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		}
		parseMap();
	}

	private void parseMap() throws IOException {
		String line;
		positions = new Vector<Integer> ();		
		while((line=mfr.readLine()) != null) {
			if (!line.isEmpty()) {
				String[] values = line.trim().split("\\s+");
				positions.add(new Integer(Integer.parseInt(values[3])));
			}
		}
	}

	public Vector<Integer> getPositions() {
		return positions;
	}

}
