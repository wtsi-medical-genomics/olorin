import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;


public class FlowFile {

	BufferedReader ffr;
	Hashtable<String, Hashtable> inds;
	Hashtable<String, String[]> haps;
	Hashtable<String, String> founders;
	Hashtable<String, String> founderCodes;
	

	public FlowFile(String fileName) throws Exception {
		if (fileName.endsWith(".gz")) {
			ffr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		} else {
			ffr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		}
		parseFlow();
	}
	
	public void parseFlow() {
		String line;
		inds = new Hashtable<String, Hashtable> ();
		
		try {
			while((line=ffr.readLine()) != null) {
				if (line.startsWith("FAMILY")){
					// ignore header
					// can/will there be > 1 family in a file? 
				} else if (!line.isEmpty()) {
					String values[] = line.trim().split("\\s+");
					String ind = values[0];
					String status = values[1];
					
					System.out.println(values);
					
					String haps[] = new String[5];
										
					System.arraycopy(values, 2, haps, 0, 5);
					
					//System.out.println(haps);
					
					Hashtable h = inds.get(ind);
					
					if (status.contains("FOUNDER")) {
						System.out.println(status + " founder");
						//inds.put(ind, haps)
						// if is the first line it is maternal
//						inds.get(ind)
//						if (h == null) {
//							
//						} else {
//							
//						}
//						
						
					} else if (status.contains("MATERNAL")) {
						System.out.println(status + " mat");
					} else {
						System.out.println(status + " pat");
					}
					
					// the first line of a sample is the maternal chromosome
					
					// the second line of a sample is the paternal chromosome
					
					
//					MutableInt value = inds.get(ind);
//					
//					if (value == null) {
//					  value = new MutableInt ();
//					  inds.put (ind, value);
//					} else {
//					  value.inc ();
//					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	// how to return the matching segments?
	// return array indexes of matching haplotypes
	
	
	public void findMatchingSegments(String ind1, String ind2) {
		
//		// get array of haplotypes 
//		String ind1[] = 
//		String ind2[] =            
//		
//		
//		if (ind1.length() == ind2.length()) {
//			for(int i = 0; i < ind1.length(); i++){
//				// loop through
//				
//				
//				
//			}
//		} else {
//			// error
//		}
	}
	
	public int getIndNum() {
		return inds.size();
	}

}
