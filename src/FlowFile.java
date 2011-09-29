import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;



public class FlowFile {

	BufferedReader ffr;
	HashMap<String, Chromosome> individuals;
	ArrayList<Integer> positions;
	
	public FlowFile(String fileName) throws Exception {
		if (fileName.endsWith(".gz")) {
			ffr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		} else {
			ffr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		}
	}
	
	public void parseFlow() {
		String line;
		individuals = new HashMap<String, Chromosome> ();
		ArrayList<Segment> matHap = null;
		ArrayList<Segment> patHap = null;
		
		try {
			while((line=ffr.readLine()) != null) {
				if (!line.isEmpty()) {
					//assume 1 family per file
					if (!line.startsWith("FAMILY")){
						String[] codes = line.trim().split("\\s+");
						String id      = codes[0];
						String status  = codes[1];
						ArrayList<Segment> seg = findSegments(codes);
						
						if (status.contains("FOUNDER")) {
							if(matHap != null) {
								patHap = seg;
							} else {
								matHap = seg;
							}
						} else if (status.contains("MATERNAL")){
							matHap = seg;
						} else if (status.contains("PATERNAL")) {
							patHap = seg;
						}
						
						if (matHap != null && patHap != null) {
							individuals.put(id, new Chromosome(matHap, patHap));
							matHap = null;
							patHap = null;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

	private ArrayList<Segment> findSegments(String[] codes) {
		ArrayList<Segment> segs = new ArrayList<Segment> ();
		String id    = codes[0];
		String last  = codes[2];
		int segStart = getPos(2);
		int segEnd   = getPos(2);
		
		for(int i=3; i<codes.length; i++) {
			String code = codes[i];
			if (code.contentEquals(last)) {
				segEnd = getPos(i);
			} else {
				// store the segment
				segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
				//reset the values for the next segment
				segStart = getPos(i);
				segEnd   = getPos(i);
			}
			last = code;
		}
		//last segment
		segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
		return segs;
	}

	private int getPos(int i) {
		//offset for the two extra columns in the flow file id and status
		return positions.get(i-2);
	}

	public HashMap<String, Chromosome> getSamples() {
		return individuals;
	}

	public void setPos(ArrayList<Integer> vector) {
		positions = vector;
	}
	
}
