import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


public class Chromosome {
	
	private ArrayList<Segment> matHap;
	private ArrayList<Segment> patHap;
	
	public Chromosome(ArrayList<Segment> m, ArrayList<Segment> p) {
		matHap = m;
		patHap = p;
	}
	
	public ArrayList<Segment> getMatHap() {
		ArrayList<Segment> copy = (ArrayList<Segment>) matHap.clone();
		return copy;
	}

	public ArrayList<Segment> getPatHap() {
		ArrayList<Segment> copy = (ArrayList<Segment>) patHap.clone();
		return copy;
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

