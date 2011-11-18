
import ca.odell.glazedlists.gui.TableFormat;
import java.util.ArrayList;

class VariantTableFormat implements TableFormat {

    private ArrayList<String> usrSelectedCols;
    private ArrayList<String> indIds;
    public ArrayList<String> cols;

    VariantTableFormat(ArrayList<String> selectedCols, Boolean freqFilter, ArrayList<String> indIds) {
        usrSelectedCols = selectedCols;
        cols = new ArrayList();
        cols.add("Chromosome");
        cols.add("Position");
        cols.add("ID");
        cols.add("Ref");
        cols.add("Alt");
        cols.add("Quality");
        cols.add("Filter");
        if (freqFilter) {
            // change this to be the freq file name?
            cols.add("Frequency");
        }
        cols.addAll(indIds);
        Boolean csqSelected = false;
        for (String s : usrSelectedCols) {
            if (s.matches("CSQ")) {
                csqSelected = true;
            } else {
                cols.add(s);
            }
        }

        if (csqSelected) {            
            cols.add("CSQ-Gene");
            cols.add("CSQ-Consequence");
            cols.add("CSQ-Amino acid change");
            cols.add("CSQ-Condel prediction");
            cols.add("CSQ-Condel score");
            cols.add("CSQ-SIFT prediction");
            cols.add("CSQ-SIFT score");
            cols.add("CSQ-PloyPhen prediction");
            cols.add("CSQ-PolyPhen score");
            cols.add("CSQ-Grantham score");
            cols.add("CSQ-GERP score");
            cols.add("Total effects");
        }
    }

    @Override
    public int getColumnCount() {
        return cols.size();
    }

    @Override
    public String getColumnName(int i) {
        return cols.get(i);
    }

    @Override
    public Object getColumnValue(Object e, int i) {
        Variant v = (Variant) e;
        return v.getTableArray().get(i);
    }
}