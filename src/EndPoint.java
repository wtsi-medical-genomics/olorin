
public class EndPoint implements Comparable<EndPoint> {
	private final int pos;
	private final String startend;
	private final byte[] code;
	private final String id;
	
	public EndPoint (int p, String s, byte[] c, String i ) {
		pos = p;
		startend = s;
		code = c;
		id = i;
	}

	@Override
	public int compareTo(EndPoint e) {
		if (this.getPos() < e.getPos()) {
			return -1;
		} else if (this.getPos() > e.getPos()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public int getPos() {
		return pos;
	}

	public String getStartend() {
		return startend;
	}

	public byte[] getCode() {
		return code;
	}

	public String getId() {
		return id;
	}
	
}
