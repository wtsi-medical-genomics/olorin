import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class VCF {

	TabixReader tabixVCF;
	VCFMeta meta;
	
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
		meta = new VCFMeta();
		String line = tabixVCF.readLine();
		while(line.startsWith("#")) {
			meta.add(line);
			line = tabixVCF.readLine();
		}		
		// the minimum meta info for a vcf header is the fileformat and the the header line
		// die "Not a valid VCF" unless the input vcf has at least these two lines 
	}
	
	
	public ArrayList<Variant> getVariants(ArrayList<SegmentMatch> matches, int matchNum) throws IOException {
		
		ArrayList<Variant> variants = new ArrayList<Variant> ();
		
		for(SegmentMatch m : matches) {
			
			if (m.getIds().size() >= matchNum) {
				if (tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd()) != null) {
					TabixReader.Iterator i = tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd());
					if (i.next() != null) {
						String vcfLine = i.next();
						while (vcfLine != null) {
							variants.add(new Variant(vcfLine));
							vcfLine = i.next();
						}
					}
				}
			}
		}
		return variants;
	}

	public ArrayList<Variant> getVariants(ArrayList<SegmentMatch> matches, int matchNum, FreqFile ff, double cutoff) throws NumberFormatException, IOException {
//		TabixReader tabixFreq = ff.getReader();
		ArrayList<Variant> variants = new ArrayList<Variant> ();
//		for(SegmentMatch m : matches) {
//			if (m.getIds().size() >= matchNum) {
//				// get all the frequency values for this region
//				// store them in a hash on position
//				Hashtable freqs = new Hashtable ();
//				TabixReader.Iterator f = tabixFreq.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd());
//				String freqLine = f.next();
//				while (freqLine != null) {
//					String[] vals = freqLine.split("\\s+");
//					String pos = vals[1];
//					int nAlleles = Integer.parseInt(vals[2]);
//					double mFreq;
//					for (int i = 4; i < nAlleles + 4; i++ ) {
//						Double freq = Double.parseDouble(vals[i]);
//						if (freq == null) {
//							mFreq = freq;
//						} else if  (freq < mFreq) {
//							mFreq = freq;
//						}
//					}
//					freqs.put(pos, mFreq);	
//					freqLine = f.next();
//				}
//				
//				TabixReader.Iterator i = tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd());
//				String vcfLine = i.next();
//				while (vcfLine != null) {
//					Variant var = new Variant(vcfLine);
//					if (freqs.containsKey(var.getPos())) {
//						if (freqs.get(var.getPos()) < cutoff) {
//							variants.add(var);
//						}
//					} else {
//						// variant not in freq file - so include variant
//						variants.add(var);
//					}
//					vcfLine = i.next();
//				}
//			}
//		}
		return variants;
	}
	
	public VCFMeta getMeta() {
		return meta;
	}
	
}
