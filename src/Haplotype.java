import java.util.Enumeration;
import java.util.Vector;


public class Haplotype {
	
	Vector<Segment> segs;
	
	public Haplotype(Vector<Segment> s) {
		segs = s;
	}

	public int getSegNum () {
		return segs.size();
	}
	
	public Enumeration<Segment> getSegments() {
		return segs.elements();
	}
}
