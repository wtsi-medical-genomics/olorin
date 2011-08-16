import java.util.Enumeration;
import java.util.Vector;


public class SampleCompare {

	Vector<Segment> results;
	
	public SampleCompare(Sample a, Sample b) {
		//loop through the possible comparisons 
		Vector<SegmentMatch> comp1 = this.compare(a.getMat(), b.getMat());
		Vector<SegmentMatch> comp2 = this.compare(a.getPat(), b.getPat());
		Vector<SegmentMatch> comp3 = this.compare(a.getMat(), b.getPat());
		Vector<SegmentMatch> comp4 = this.compare(a.getPat(), b.getMat());
		
		results = new Vector<Segment> ();
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
	}
	
	public Vector<SegmentMatch> compare(Haplotype a, Haplotype b, String aId, String bId) {
		Vector<SegmentMatch> matches = new Vector();

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

						matches.add(new SegmentMatch(start,end,currentSegA.getCode(),aId,bId)); 
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

					matches.add(new Segment(start,end,currentSegA.getCode())); 
				}
			}
		}
		return matches;
	}
	
	public Vector<Segment> getResults() {
		return results;
	}
	
}
