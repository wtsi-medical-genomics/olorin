import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;


public class FreqFile {

	BufferedReader ffr;
	
	public FreqFile(String fileName) throws FileNotFoundException, IOException {
		if (fileName.endsWith(".gz")) {
			ffr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		} else {
			ffr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		}
	}
	
	public BufferedReader getReader() {	
		return ffr;
	}
	
	public String readLine() throws IOException {
		// method for testing
		return ffr.readLine();
	}
	
}
