import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class VCF {

	TabixReader tabixVCF;
	TabixReader tabixFreq;
	VCFMeta meta;
	String freqFile; 
	
	public VCF(final String fileName) throws Exception {
		File vcfIndex = new File(fileName + ".tbi");
		if (fileName.endsWith("gz")) {
			if (vcfIndex.exists()) {
				tabixVCF = new TabixReader(fileName);
				this.parseHeader();
			} else {
				throw new Exception("VCF needs to be indexed using tabix");
			}
		} else {
			throw new Exception("VCF needs to be compressed using bgzip");
		}
	}
		
	private void parseHeader() throws IOException {
		String line = tabixVCF.readLine();
		while(line.startsWith("#")) {
			meta.add(line);
			line = tabixVCF.readLine();
		}		
		// the minimum meta info for a vcf header is the fileformat and the the header line
		// die "Not a valid VCF" unless the input vcf has at least these two lines 
	}
	
	
	public Vector<Variant> getVariants(Vector<SegmentMatch> matches, int matchNum) throws IOException {
		
		Vector<Variant> variants = new Vector<Variant> ();
		
		for(SegmentMatch m : matches) {
			
			if (m.getIds().size() >= matchNum) {				
				//not sure where chr should be stored, seems a waste of mem to store it in every SegmentMatch
				TabixReader.Iterator i = tabixVCF.query(chr + ":" + m.getStart() + "-" + m.getEnd());				
				while (i.next() != null) {
					String vcfLine = i.next();
					variants.add(new Variant(vcfLine));		
				}
			}
		}
		return variants;
	}

	public Vector<Variant> getVariants(Vector<SegmentMatch> matches, int matchNum, double cutoff) {
		//check that a freq file has been loaded
		Vector<Variant> variants = new Vector<Variant> ();
		for(SegmentMatch m : matches) {
			if (m.getIds().size() >= matchNum) {
				TabixReader.Iterator i = tabixVCF.query(chr + ":" + m.getStart() + "-" + m.getEnd());
				while (i.next() != null) {
					String vcfLine = i.next();
					Variant var = new Variant(vcfLine);
					TabixReader.Iterator j = tabixFreq.query(chr + ":" + m.getStart() + "-" + m.getEnd());
					while (j.next() != null) {
						String freqLine = j.next();
						String[] vals = freqLine.split("\\s+");
						String freq = vals[2];
						double freq_d = Double.parseDouble(freq); 
						if (freq_d < cutoff) {
							variants.add(var);
						}
					}	
				}
			}
		}
		return variants;
	}
	
	public VCFMeta getMeta() {
		return meta;
	}

	public void loadFreqFile(String ff) {
		//tabixed freq file name
		freqFile = ff;
	}

}
