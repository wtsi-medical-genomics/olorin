

public class Variant {
	
	public int chr;
	public long pos;
	
	Variant(int c, long p) {
			chr = c;
			pos = p;
	}
	int getChr() {
		return chr;
	}
	void setChr(int c) {
		chr = c;
	}
	
	long getPos() {
		return pos;
	}
	void setPos(long p) {
		pos = p;
	}
}
