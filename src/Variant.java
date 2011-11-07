
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

public class Variant {

    public int chr;
    public long pos;
    public String id;
    public String ref;
    public String[] alt;
    public String qual;
    public String filter;
    public double freq;
    public boolean freqFiltered;
    public HashMap<String, String> info;
    public ArrayList<Integer> genotypes;
    public ArrayList<String> genotypeStrings;
    public ArrayList tableArray;
    public ArrayList<String> selectedCols;
    public ArrayList<Integer> selectedInds;

    public Variant() {
    }

    public Variant(String vcfLine, ArrayList<String> selectedCols, ArrayList<Integer> selectedInds) {
        this.selectedCols = selectedCols;
        this.selectedInds = selectedInds;

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

        this.setChr(chr_i);
        this.setPos(pos_i);
        this.setID(values[2]);
        this.setRef(values[3]);
        this.setAlt(values[4].split(","));
        this.setQual(values[5]);
        this.setFilter(values[6]);

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

        genotypes = new ArrayList<Integer>();
        genotypeStrings = new ArrayList<String>();
        for (int i = 9; i < values.length; i++) {
            genotypes.add(parseGenotypes(values[i]));
            genotypeStrings.add(parseGenotypeString(values[i]));
        }
    }

    private int parseGenotypes(String s) {
        String genoVals[] = s.split(":");
        //TODO record all the other vaues along with the genotype
        String alleles[] = genoVals[0].split("[\\|\\/\\\\]");

        if (alleles.length > 1) {
            String a = alleles[0];
            String b = alleles[1];
            if (a.equals(b)) {
                if (a.matches("0") || a.matches(".")) {
                    // homo ref
                    return 0;
                } else {
                    // homo alt
                    return 2;
                }
            } else {
                // het
                return 1;
            }
        } else {
            // missing genotype from merging return as hom ref
            return 0;
        }
    }

    private String parseGenotypeString(String s) {
        String genoVals[] = s.split(":");
        //TODO record all the other vaues along with the genotype
        String ab[] = genoVals[0].split("[\\|\\/\\\\]");

        if (ab.length > 1) {
            String a = ab[0];
            String b = ab[1];
            if (a.matches("0")) {
                a = this.getRef();
            } else {
                a = this.getAlt()[Integer.parseInt(a) - 1];
            }
            if (b.matches("0")) {
                b = this.getRef();
            } else {
                b = this.getAlt()[Integer.parseInt(b) - 1];
            }
            return a + " " + b;
        } else {
            // missing genotype return as hom ref
            return this.getRef() + " " + this.getRef();
        }
    }

    // given an array of user selected columns makes an array containing just the selected data
    public void setTableArray(ArrayList<String> selectedCols, ArrayList<Integer> selectedInds) {
        tableArray = new ArrayList();
        tableArray.add(getChr());
        tableArray.add(getPos());
        tableArray.add(getID());
        tableArray.add(getRef());
        tableArray.add(StringUtils.join(getAlt(), ", "));
        tableArray.add(getQual());
        tableArray.add(getFilter());
        if (freqFiltered) {
            tableArray.add(getFreq());
        }

        for (Integer id : selectedInds) {
            tableArray.add(getGenotypeStrings().get(id));
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
            setTableArray(selectedCols, selectedInds);
            return tableArray;
        } else {
            return tableArray;
        }
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

    private void setAlt(String[] string) {
        alt = string;
    }

    public String[] getAlt() {
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

    public ArrayList<Integer> getGenotypes() {
        return genotypes;
    }

    private ArrayList<String> getGenotypeStrings() {
        return genotypeStrings;
    }

    void setFreq(Double f) {
        freq = f;
        freqFiltered = true;
    }

    public Double getFreq() {
        return freq;
    }
}
