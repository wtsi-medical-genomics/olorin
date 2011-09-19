import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;


public class SegmentMatch<t> extends Segment {

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

	public Collection getArray() {
		ArrayList segment = new ArrayList();
		segment.add(this.getChr());
		segment.add(this.getStart());
		segment.add(this.getEnd());
		segment.add(this.getIds().size());
		segment.add(this.getIds().toString());

		return segment;
	}
	
}
