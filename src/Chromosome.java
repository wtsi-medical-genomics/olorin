import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


public class Chromosome {
	
	ArrayList<Segment> matHap;
	ArrayList<Segment> patHap;
	
	public Chromosome(ArrayList<Segment> m, ArrayList<Segment> p) {
		matHap = m;
		patHap = p;
	}
	
	public ArrayList<Segment> getMatHap() {
		return matHap;
	}

	public ArrayList<Segment> getPatHap() {
		return patHap;
	}
	
	public Iterator<Segment> getMatIt() {
		return matHap.iterator();
	}
	
	public Iterator<Segment> getPatIt() {
		return patHap.iterator();
	}
	
	public int getMatSegNum () {
		return matHap.size();
	}
	
	public int getPatSegNum () {
		return patHap.size();
	}
	
}

