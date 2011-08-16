import java.util.Vector;


public class SegmentMatch extends Segment {

	Vector<String> ids;
	
	public SegmentMatch(int s, int e, byte c, Vector<String> i) {
		super(s, e, c);
		ids = i;
	}
	
	public Vector<String> getIDs () {
		return ids;
	}

}
