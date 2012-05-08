import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class SampleCompare {

	public SampleCompare() {

	}

	public ArrayList<SegmentMatch> compareMulti (ArrayList<Sample> samples){
		ArrayList <SegmentMatch> allMatches = new ArrayList<SegmentMatch> ();  
		Enumeration<String> i = samples.get(0).getChromosomes();
		while (i.hasMoreElements()) {
			String chr = i.nextElement();
			ArrayList<Iterator> segArrays = new ArrayList<Iterator> ();
			for (Sample s : samples){
				//we might want to track that one of these is mat, one is pat, but right now I don't care
                            segArrays.add(s.getChr(chr).getMatHap().iterator());
				segArrays.add(s.getChr(chr).getPatHap().iterator());
			}
			allMatches.addAll(compareChr(segArrays, chr));
		}
		return allMatches;
	}
	
	private  Collection<SegmentMatch> compareChr (ArrayList<Iterator> segArrays, String chr) {
		Hashtable<String, SegmentMatch> matches = new Hashtable<String, SegmentMatch> ();
		ArrayList<Segment> currentSegs = new ArrayList<Segment> ();
		while (moreToCome(segArrays)){
			if (currentSegs.size() == 0) {
				//This is the first entrance into the loop, so just pull out the first seg from each and continue
				for (Iterator i : segArrays) {
					currentSegs.add((Segment) i.next());
					//i.remove();
				}
				//check if the first segments overlap and codes match
				ArrayList<EndPoint> endpoints = getEndpoints(currentSegs);
				Hashtable<String, ArrayList<ArrayList>> codes = findOverlaps(endpoints);
				Enumeration<String> overlaps = codes.keys();
				while (overlaps.hasMoreElements()) {
					String s = overlaps.nextElement();
					//if the count of any of the codes is > 1 then there is an overlapping segment with matching codes
					if (codes.get(s).get(0).size() > 1) {
						ArrayList<Integer> startPos = codes.get(s).get(0);
						ArrayList<Integer> endPos = codes.get(s).get(1);
						ArrayList<Integer> ids = codes.get(s).get(2);
						Integer segStart = startPos.get(startPos.size()-1);
						Integer segEnd = endPos.get(0);
						// how do we stop matches between the same sample?
						// check that all the elements in the ids array are unique?
						if (!matches.containsKey(s+segStart+segEnd+ids.toString())) {
							matches.put(s+segStart+segEnd+ids.toString(), new SegmentMatch(chr, segStart,segEnd, s.getBytes(), ids));
						}
					}	
				}
				continue;
			}
			ArrayList endVals = new ArrayList ();
			for (Segment s : currentSegs) {
				endVals.add(s.getEnd());
			}
			int endsFirstIndex = endVals.indexOf(Collections.min(endVals));
			currentSegs.remove(endsFirstIndex);
			currentSegs.add(endsFirstIndex, (Segment) segArrays.get(endsFirstIndex).next());
			segArrays.get(endsFirstIndex).remove();
			ArrayList<EndPoint> endpoints = getEndpoints(currentSegs);
			Hashtable<String, ArrayList<ArrayList>> codes = findOverlaps(endpoints);
			Enumeration<String> overlaps = codes.keys();
			while (overlaps.hasMoreElements()) {
				String s = overlaps.nextElement();
				//if the count of any of the codes is > 1 then there is an overlapping segment with matching codes
				if (codes.get(s).get(0).size() > 1) {
					ArrayList<Integer> startPos = codes.get(s).get(0);
					ArrayList<Integer> endPos = codes.get(s).get(1);
					ArrayList<Integer> ids = codes.get(s).get(2);
					Integer segStart = startPos.get(startPos.size()-1);
					Integer segEnd = endPos.get(0);
					// how do we stop matches between the same sample?
					// check that all the elements in the ids array are unique?
					if (!matches.containsKey(s+segStart+segEnd+ids.toString())) {
						matches.put(s+segStart+segEnd+ids.toString(), new SegmentMatch(chr, segStart,segEnd, s.getBytes(), ids));
					}
				}	
			}	
		} 
		return matches.values();
	}

	private boolean moreToCome(ArrayList<Iterator> segArrays){
		boolean r = false;
		for (Iterator i : segArrays){
			if (i.hasNext()) {
				return true;
			}
		}
		return r;
	}

	private ArrayList<EndPoint> getEndpoints(ArrayList<Segment> currentSegs) {
		ArrayList<EndPoint> endpoints = new ArrayList();
		for (Segment s : currentSegs) {
			endpoints.add(new EndPoint(s.getStart(), "S", s.getCode(), s.getId()));
			endpoints.add(new EndPoint(s.getEnd(), "E", s.getCode(), s.getId()));
		}
		Collections.sort(endpoints);
//		for (EndPoint e : endpoints) {
//			System.out.print(e.getPos()+" ");
//		}
//		System.out.println();
		return endpoints;
	}

	private Hashtable<String, ArrayList<ArrayList>> findOverlaps (ArrayList<EndPoint> endpoints) {

		int startCount = 0;
		int endCount = 0;
		boolean inOverlap = false;

		Hashtable<String, ArrayList<ArrayList>> codes = new Hashtable<String, ArrayList<ArrayList>> ();

		for (EndPoint e : endpoints ) {
			String code = new String(e.getCode());
			int pos = e.getPos();
			String id = e.getId();
			
			if (e.getStartend().matches("S")) {
				startCount++;
				if (startCount - endCount > 0) {
					inOverlap = true;
					//we are in an overlapping segment record the code and start pos
					if (codes.containsKey(code)) {
						ArrayList<ArrayList> al1 = codes.get(code);
						ArrayList<Integer> al2 = al1.get(0);
						ArrayList<String> al4 = al1.get(2);
						al2.add(pos);
						al4.add(id);
						al1.add(al2);
						al1.add(al4);
						codes.put(code, al1);
					} else {
						ArrayList<ArrayList> al1 = new ArrayList<ArrayList> ();
						ArrayList<Integer> al2 = new ArrayList<Integer> ();
						ArrayList<Integer> al3 = new ArrayList<Integer> ();
						ArrayList<String> al4 = new ArrayList<String> ();
						al2.add(pos);
						al4.add(id);
						al1.add(al2);
						al1.add(al3);
						al1.add(al4);
						codes.put(code, al1);
					}		
				} else {
					inOverlap = false;
				}

			} else if (e.getStartend().matches("E")) {
				endCount++;
				if (inOverlap) {
					ArrayList<ArrayList> al1 = codes.get(code);
					ArrayList<Integer> al3 = al1.get(1);
					al3.add(pos);
					al1.add(al3);
					codes.put(code, al1);
				}
			}
		}
		return codes;
	}

}
