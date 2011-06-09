import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class VCF {

	TabixReader tabixVCF;
	Hashtable<Integer, Hashtable> vcfVariants;
	int variantTotal;
	int flaggedTotal;
	boolean open = false;
	
	
	public VCF(final String fileName) throws Exception {
		File vcfIndex = new File(fileName + ".tbi");
		if (fileName.endsWith("gz")) {
			if (vcfIndex.exists()) {
				tabixVCF = new TabixReader(fileName);
				this.parseVariants();
				open = true;
			} else {
				throw new Exception("VCF needs to be indexed using tabix");
			}
		} else {
			throw new Exception("VCF needs to be compressed using bgzip");
		}
	}

	private void parseVariants() throws IOException {
		vcfVariants = new Hashtable();
		String line;
		while((line=tabixVCF.readLine()) != null) {
			if (line.substring(0,1).matches("#") ) {
				// todo
				// store the header information
				// create a header object containing the different info fields
				//System.out.println(line);
			} else {
				String values[] = line.split("\t");
				String chr = values[0];
				String pos = values[1];
				int chr_i = 0;
				int pos_i = 0;
				try {
					chr_i = Integer.parseInt(chr);
				} catch (NumberFormatException nfe) {
					if (chr.matches("X")) {
						chr_i = 23;
					} else if (chr.matches("Y")) {
						chr_i = 24;
					} else if (chr.matches("MT")) {
						chr_i = 25;
					} else {
						System.out.println("can't parse chromosome '" + chr + "'");
					}
				}
				try {
					pos_i = Integer.parseInt(pos);
				} catch (NumberFormatException nfe) {
					System.out.println("position is not a number '" + pos + "'");
				}
				if (vcfVariants.containsKey(chr_i)) {
					vcfVariants.get(chr_i).put(pos_i, false);
				} else {
					Hashtable chromosome = new Hashtable();
					chromosome.put(pos_i, false);
					vcfVariants.put(chr_i, chromosome);
				}
			}
		}
	}
	
	public TabixReader.Iterator getVariants(int chr, int start, int end) {
		return tabixVCF.query(chr+":"+start+"-"+end);
	}
	
	// move this out of VCF into the main program?
	
	public void flagCommon(String freqFile, double cutoff ) throws NumberFormatException, IOException {
		
		BufferedReader freqFileReader = new FreqFile(freqFile).getReader();

		flaggedTotal = 0;
		String line;
		
		while((line=freqFileReader.readLine()) != null) {
			String values[] = line.split("\t");
			String chr = values[0];
			String pos = values[1];
			String freq = values[3];
			int chr_i = Integer.parseInt(chr);
			int pos_i = Integer.parseInt(pos);
			Double freq_d = Double.parseDouble(freq);
			
			if (vcfVariants.containsKey(chr_i) && vcfVariants.get(chr_i).containsKey(pos_i)) {
				if (freq_d >= cutoff) {
					vcfVariants.get(chr_i).put(pos_i, true);
					flaggedTotal++;
				}		
			}
		}
	}
	
	// slower implementation using a tabix index
	public void flagCommonTabix(String freqFile, double cutoff ) throws NumberFormatException, IOException {
		
		TabixReader tabixFreq = new TabixReader(freqFile);
		Enumeration c = vcfVariants.keys();

		while(c.hasMoreElements()) {
			Integer chr = (Integer) c.nextElement();
			Enumeration p = vcfVariants.get(chr).keys();
			while(p.hasMoreElements()) {
				Integer pos = (Integer) p.nextElement();
				TabixReader.Iterator iter = tabixFreq.query(chr+":"+pos+"-"+pos);
				String line = null;
				try {
					line = iter.next();
				} catch (NullPointerException npe) {
					// do something with these?
				}
			
				if (line != null) {
					String values[] = line.split("\t");
					String freq = values[3];
					Double freq_d = Double.parseDouble(freq);
					if (freq_d >= cutoff) {
						vcfVariants.get(chr).put(pos, true);
					}
				}
			}		 	
		}
	}
		
	public int getFlaggedTotal() {
		return flaggedTotal;
	}
	
	public int getVariantTotal() {
		int variantTotal = 0;
		Enumeration<Integer> e = vcfVariants.keys();
		
		while(e.hasMoreElements()) {
			Integer chr = e.nextElement(); 
			variantTotal += vcfVariants.get(chr).size(); 
		}
		return variantTotal;
	}

	public Hashtable<Integer, Hashtable> getVariants() {
		return vcfVariants;
	}
}
