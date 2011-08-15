import java.util.*;

public class Sample {

	//array of haplotypes - one for each autosome
	public Haplotype[] matHaps;
	public Haplotype[] patHaps;
	
	public Sample() {
		
	}
	
	
	//Jeff's code
	public Vector<Segment> compareChromosomes(Haplotype a, Haplotype b){
		Vector<Segment> matches = new Vector();
		
		Enumeration<Segment> hapASegs = a.getSegments();
		Enumeration<Segment> hapBSegs = b.getSegments();

		Segment currentSegA;
		Segment currentSegB;
		while (hapASegs.hasMoreElements() || hapBSegs.hasMoreElements()){
			
			if (currentSegA.equals(null)){
				//This is the first entrance into the loop, so just pull out the first seg from each and continue
				currentSegA = hapASegs.nextElement();
				currentSegB = hapBSegs.nextElement();
				continue;
			}
			
			if (currentSegA.getEnd() > currentSegB.getEnd()){
				currentSegB = hapBSegs.nextElement();
			}else{
				currentSegA = hapASegs.nextElement();
			}
			
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
					
					matches.add(new Segment(start,end,currentSegA.getCode())); 
				}
			}
		}
	}
}