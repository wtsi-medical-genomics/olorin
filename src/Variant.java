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
    public ArrayList tableArray;
    public ArrayList<String> selectedCols;

    public Variant() {
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

        info = new HashMap<String, String>();
        String infoVals[] = values[7].split(";");
        for (int i = 0; i < infoVals.length; i++) {
            if (infoVals[i].contains("=")) {
                String[] keyValuePairs = infoVals[i].split("=");
                info.put(keyValuePairs[0], keyValuePairs[1]);
            } else {
                info.put(infoVals[i], "TRUE");
            }
        }

        geno = new ArrayList<String>();
        for (int i = 9; i < values.length; i++) {
            geno.add(values[i]);
        }
        
        this.setChr(chr_i);
        this.setPos(pos_i);
        this.setID(values[2]);
        this.setRef(values[3]);
        this.setAlt(values[4]);
        this.setQual(values[5]);
        this.setFilter(values[6]);
    }
    
    public Variant(String vcfLine, ArrayList<String> selectedCols) {
        this.selectedCols = selectedCols;
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

        info = new HashMap<String, String>();
        String infoVals[] = values[7].split(";");
        for (int i = 0; i < infoVals.length; i++) {
            if (infoVals[i].contains("=")) {
                String[] keyValuePairs = infoVals[i].split("=");
                info.put(keyValuePairs[0], keyValuePairs[1]);
            } else {
                info.put(infoVals[i], "TRUE");
            }
        }

        geno = new ArrayList<String>();
        for (int i = 9; i < values.length; i++) {
            geno.add(values[i]);
        }
        
        this.setChr(chr_i);
        this.setPos(pos_i);
        this.setID(values[2]);
        this.setRef(values[3]);
        this.setAlt(values[4]);
        this.setQual(values[5]);
        this.setFilter(values[6]);
    }

    private void setFilter(String string) {
        filter = string;
    }

    public String getFilter() {
        return filter;
    }

    private void setQual(String string) {
        qual = string;
    }

    public String getQual() {
        return qual;
    }

    private void setAlt(String string) {
        alt = string;
    }

    public String getAlt() {
        return alt;
    }

    private void setRef(String string) {
        ref = string;
    }

    public String getRef() {
        return ref;
    }

    public void setID(String string) {
        id = string;
    }

    public String getID() {
        return id;
    }

    public void setChr(int c) {
        chr = c;
    }

    public int getChr() {
        return chr;
    }

    public void setPos(long p) {
        pos = p;
    }

    public long getPos() {
        return pos;
    }

    public HashMap<String, String> getInfo() {
        return info;
    }

    public ArrayList<String> getGenotypes() {
        return geno;
    }

    public void setTableArray() {
        tableArray = new ArrayList();
        tableArray.add(getChr());
        tableArray.add(getPos());
        tableArray.add(getID());
        tableArray.add(getRef());
        tableArray.add(getAlt());
        tableArray.add(getQual());
        tableArray.add(getFilter());
        if (freqFiltered) {
            tableArray.add(getFreq());
        }
    }
    
    
    // given an array of user selected columns makes an array containing just the selected data
    public void setTableArray(ArrayList<String> selectedCols) {
        tableArray = new ArrayList();
        tableArray.add(getChr());
        tableArray.add(getPos());
        tableArray.add(getID());
        tableArray.add(getRef());
        tableArray.add(getAlt());
        tableArray.add(getQual());
        tableArray.add(getFilter());
        if (freqFiltered) {
            tableArray.add(getFreq());
        }
        for (String s : selectedCols) {
            String value = getInfo().get(s);
            if (value != null) {
                tableArray.add(value);
            } else {
                tableArray.add(".");
            }
        }
    }
    
    public ArrayList getTableArray() {
        if (tableArray == null) {
            setTableArray(selectedCols);
            return tableArray;
        } else {
            return tableArray;
        }
    }
    
    public ArrayList getArray (ArrayList<String> selectedCols) {
        ArrayList array = new ArrayList();
        array = new ArrayList();
        array.add(getChr());
        array.add(getPos());
        array.add(getID());
        array.add(getRef());
        array.add(getAlt());
        array.add(getQual());
        array.add(getFilter());
        if (freqFiltered) {
            array.add(getFreq());
        }
        for (String s : selectedCols) {
            String value = getInfo().get(s);
            if (value != null) {
                array.add(value);
            } else {
                array.add(".");
            }
        }
        return array;
    }
    

    void setFreq(Double f) {
        freq = f;
        freqFiltered = true;
    }

    public Double getFreq() {
        return freq;
    }

    Object getData(int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
