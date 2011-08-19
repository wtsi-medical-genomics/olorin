import java.util.Enumeration;
import java.util.Vector;


public class SampleCompare {

	Vector<SegmentMatch> results;
	
	public SampleCompare(Sample a, Sample b) {
		
		
		
	}
	
	public SampleCompare(Vector<Sample> selected) {
		

		// compare these two and get back the results
		// if there are two more 
		
		// for each chromosome
		Vector<Vector<SegmentMatch>> matches = new Vector<Vector<SegmentMatch>> ();
		Enumeration<Sample> e = selected.elements();
		while (e.hasMoreElements()) {
			// pull off the first two samples from the vector
			Sample a = e.nextElement();
			if (e.hasMoreElements()) {
				Sample b = e.nextElement();
				matches.add(compareSamples(a, b));
			} else {
				//only one sample not sure what to do here
			}
		}
		
		// now process the matched until there is only one set of segment matches
		if (matches.size() > 1) {
			
			
			
			
			
		} else {
			// zero or 1 vectors so return matches
		} 
		
	}
	
	public Vector<SegmentMatch> compareSamples (Sample a, Sample b) {
		
		Vector<String> ids = new Vector<String> ();
		ids.add(a.getId());
		ids.add(b.getId());
				
		//loop through the possible comparisons 
		Vector<SegmentMatch> comp1 = this.compare(a.getMat(), b.getMat(), ids);
		Vector<SegmentMatch> comp2 = this.compare(a.getPat(), b.getPat(), ids);
		Vector<SegmentMatch> comp3 = this.compare(a.getMat(), b.getPat(), ids);
		Vector<SegmentMatch> comp4 = this.compare(a.getPat(), b.getMat(), ids);
		
		results = new Vector<SegmentMatch> ();
		
		if(comp1.size() > 0){
			results.addAll(comp1);
		}
		if(comp2.size() > 0){
			results.addAll(comp2);
		}
		if(comp3.size() > 0){
			results.addAll(comp3);
		}
		if(comp4.size() > 0){
			results.addAll(comp4);
		}
		
		return results;
		
	}
	
	public Vector<SegmentMatch> compareMulti (Vector<Sample> samples){
		Vector<Enumeration> segArrays;
		Vector<String> ids;
		for (Sample s : samples){
			//we might want to track that one of these is mat, one is pat, but right now I don't care
			segArrays.add(s.getMatHap());
			ids.add(s.getId());
			segArrays.add(s.getPatHap());
			ids.add(s.getId());
		}
		
		Vector<Segment> currentSegs;
		while (moreToCome(segArrays)){
			//currentMatchStart, currentMatchStop
			for (Segment seg : currentSegs){
				
			}
		}
	}
	
	private boolean moreToCome(Vector<Enumeration> segArrays){
		boolean r = false;
		for (Enumeration e : segArrays){
			//check if this should be single bar or double bar
			r = (r || e.hasMoreElements());
		}
		return r;
	}

	public Vector<SegmentMatch> compare(Haplotype a, Haplotype b, Vector<String> ids) {
		Vector<SegmentMatch> matches = new Vector<SegmentMatch> ();
		
		Enumeration<Segment> hapASegs = a.getSegments();
		Enumeration<Segment> hapBSegs = b.getSegments();

		Segment currentSegA = null;
		Segment currentSegB = null;
		while (hapASegs.hasMoreElements() || hapBSegs.hasMoreElements()){

			if (currentSegA == null){
				//This is the first entrance into the loop, so just pull out the first seg from each and continue
				currentSegA = hapASegs.nextElement();
				currentSegB = hapBSegs.nextElement();
				
				if ((currentSegA.getStart() >= currentSegB.getStart() && currentSegA.getStart() <= currentSegB.getEnd()) ||
						(currentSegA.getEnd() >= currentSegB.getStart() && currentSegA.getEnd() <= currentSegB.getEnd()) ||
						(currentSegA.getStart() >= currentSegB.getStart() && currentSegA.getEnd() >= currentSegB.getEnd())){
					if (currentSegA.getCode() == currentSegB.getCode()){
						int start;
						if (currentSegA.getStart() > currentSegB.getStart()){
							start = currentSegA.getStart();
						}else{
							start = currentSegB.getStart();
						}

						int end;
						if (currentSegA.getEnd() < currentSegB.getEnd()){
							end = currentSegA.getEnd();
						}else{
							end = currentSegB.getEnd();
						}

						matches.add(new SegmentMatch(start,end,currentSegA.getCode(), ids)); 
					}
				}				
				
				continue;
			}

			if (currentSegA.getEnd() > currentSegB.getEnd()){
				currentSegB = hapBSegs.nextElement();
			}else{
				currentSegA = hapASegs.nextElement();
			}

			if ((currentSegA.getStart() >= currentSegB.getStart() && currentSegA.getStart() <= currentSegB.getEnd()) ||
					(currentSegA.getEnd() >= currentSegB.getStart() && currentSegA.getEnd() <= currentSegB.getEnd()) ||
					(currentSegA.getStart() <= currentSegB.getStart() && currentSegA.getEnd() >= currentSegB.getEnd())){
				if (currentSegA.getCode() == currentSegB.getCode()){
					int start;
					if (currentSegA.getStart() > currentSegB.getStart()){
						start = currentSegA.getStart();
					}else{
						start = currentSegB.getStart();
					}

					int end;
					if (currentSegA.getEnd() < currentSegB.getEnd()){
						end = currentSegA.getEnd();
					}else{
						end = currentSegB.getEnd();
					}

					matches.add(new SegmentMatch(start,end,currentSegA.getCode(), ids)); 
				}
			}
		}
		return matches;
	}
	
	public Vector<SegmentMatch> getResults() {
		return results;
	}
	
}
