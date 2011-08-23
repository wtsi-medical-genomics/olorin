import java.util.ArrayList;
import java.util.Vector;


public class SegmentMatch extends Segment {

	ArrayList<Integer> ids;
	String chr;
	
	public SegmentMatch(String c, int s, int e, byte[] bs, ArrayList<Integer> d) {
		super(s, e, bs);
		ids = d;
		chr = c;
	}
	
	public ArrayList<Integer> getIds () {
		return ids;
	}

	public String getChr () {
		return chr;
	}
	
}
