import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class Sample {

	public Hashtable<String, Chromosome> chr = new Hashtable<String, Chromosome> ();
	public String id;
	
	public Sample(String c, Chromosome s) {
		chr.put(c, s);
	}

	public Sample() {

	}

	public Chromosome getChr (String c) { 
		return chr.get(c);
	}
	
	public void setChr (String c, Chromosome s) {
		chr.put(c, s);
	}
	
	public String getId() {
		return id;
	}
	
	public Enumeration<String> getChromosomes () {
		return chr.keys();
	}
	
}