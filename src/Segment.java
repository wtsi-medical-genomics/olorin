
public class Segment {

	int  start;
	int  end;
	byte[] code;
	String id;
	
	public Segment(int s, int e, byte[] c, String d) {
		start = s;
		end   = e;
		code  = c;
		id    = d;
	}
	
	public Segment(int s, int e, byte[] c) {
		start = s;
		end   = e;
		code  = c;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public byte[] getCode() {
		return code;
	}
	
	public String getCodeString() {
		return new String(getCode());
	}

	public String getId() {
		return id;
	}
	
}
