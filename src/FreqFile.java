import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;


public class FreqFile {

        TabixReader tabixFreq;
    	
	public FreqFile(String fileName) throws Exception {
		File freqIndex = new File(fileName + ".tbi");
                
                System.out.println(fileName);
                System.out.println(freqIndex.getAbsoluteFile());
                
		if (fileName.endsWith("gz")) {
			if (freqIndex.exists()) {
				tabixFreq = new TabixReader(fileName);
			} else {
				throw new Exception("Freq file needs to be indexed using tabix");
			}
		} else {
			throw new Exception("Freq file needs to be compressed using bgzip");
		}
	}
	
	public TabixReader getReader() {	
		return tabixFreq;
	}
	
}
