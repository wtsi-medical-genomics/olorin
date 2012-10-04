import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class Sample {

	public HashMap<String, Chromosome> chr = new HashMap<String, Chromosome> ();
	
	public Sample(String chrNum, Chromosome chrObj) {
		chr.put(chrNum, chrObj);
	}

	public Sample() {

	}

	public Chromosome getChr (String chrNum) { 
		return chr.get(chrNum);
	}
	
	public void setChr (String chrNum, Chromosome chrObj) {
		chr.put(chrNum, chrObj);
	}
	
	public Iterator<String> getChromosomes () {
		return chr.keySet().iterator();
	}
	
}