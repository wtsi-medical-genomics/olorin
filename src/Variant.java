import java.util.ArrayList;
import java.util.HashMap;


public class Variant {
	
	public int chr;
	public long pos;
	public String id;
	public String ref;
	public String alt;
	public String qual;
	public String filter;
        public double freq;
        public boolean freqFiltered;
	public HashMap<String, String> info;
	public ArrayList<String> geno;
	
	public Variant () {
		
	}
	
	public Variant(String vcfLine) {
		String values[] = vcfLine.split("\t");
		String chr_s = values[0];
		String pos_s = values[1];
		int chr_i = 0;
		int pos_i = 0;
		try {
			chr_i = Integer.parseInt(chr_s);
		} catch (NumberFormatException nfe) {
			if (chr_s.matches("X")) {
				chr_i = 23;
			} else if (chr_s.matches("Y")) {
				chr_i = 24;
			} else if (chr_s.matches("MT")) {
				chr_i = 25;
			} else {
				System.out.println("can't parse chromosome '" + chr_s + "'");
			}
		}
		
		try {
			pos_i = Integer.parseInt(pos_s);
		} catch (NumberFormatException nfe) {
			System.out.println("position is not a number '" + pos_s + "'");
		}
		
		info = new HashMap<String, String> ();
		String infoVals[] = values[7].split(";");
		for (int i = 0; i < infoVals.length; i++) {
			if (infoVals[i].contains("=")) {
				String[] keyValuePairs = infoVals[i].split("=");
				info.put(keyValuePairs[0], keyValuePairs[1]);
			} else {
				info.put(infoVals[i], "TRUE");
			}
		}
		
		geno = new ArrayList<String> ();
		for (int i = 9; i < values.length; i++) {
			geno.add(values[i]);
		}
		
		this.setChr(chr_i);
		this.setPos(pos_i);
		this.setId(values[2]);
		this.setRef(values[3]);
		this.setAlt(values[4]);
		this.setQual(values[5]);
		this.setFilter(values[6]);
		
	}
	private void setFilter(String string) {
		filter= string;
	}

	private void setQual(String string) {
		qual = string;
	}

	private void setAlt(String string) {
		alt = string;
	}

	private void setRef(String string) {
		ref = string;
	}

	private void setId(String string) {
		id = string;
	}

	public int getChr() {
		return chr;
	}
	
	public void setChr(int c) {
		chr = c;
	}
	
	public long getPos() {
		return pos;
	}
	
	public void setPos(long p) {
		pos = p;
	}
	
	public String getId() {
		return id;
	}
	public String getRef() {
		return ref;
	}
	public String getAlt() {
		return alt;
	}
	public String getQual() {
		return qual;
	}
	public String getFilter() {
		return filter;
	}
	public HashMap<String, String> getInfo() {
		return info;
	}
	public ArrayList<String> getGenotypes() {
		return geno;
	}

	public ArrayList<Variant> getArray() {
		ArrayList variant = new ArrayList();
		variant.add(getChr());
		variant.add(getPos());
		variant.add(getId());
		variant.add(getRef());
		variant.add(getAlt());
		variant.add(getQual());
		variant.add(getFilter());
                if (freqFiltered) {
                    variant.add(getFreq());
                }
		return variant;
	}
        
        public ArrayList<Variant> getArray(ArrayList<String> selectedCols) {
		ArrayList variant = new ArrayList();
		variant.add(getChr());
		variant.add(getPos());
		variant.add(getId());
		variant.add(getRef());
		variant.add(getAlt());
		variant.add(getQual());
		variant.add(getFilter());
		if (freqFiltered) {
                    variant.add(getFreq());
                }
		for (String s : selectedCols) {
			String value = getInfo().get(s);
			if (value != null) {
				variant.add(value);
			} else {
				variant.add(".");
			}
			
		}
		return variant;
	}

    void setFreq(Double f) {
        freq = f;
        freqFiltered = true;
    }
    
    public Double getFreq() {
        return freq;
    }
    
}
