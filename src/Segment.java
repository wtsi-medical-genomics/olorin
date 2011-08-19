
public class Segment {

	int  start;
	int  end;
	byte code;
	
	public Segment(int s, int e, byte c) {
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

	public byte getCode() {
		return code;
	}
	
}
