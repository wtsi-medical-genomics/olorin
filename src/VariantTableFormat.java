
import ca.odell.glazedlists.gui.TableFormat;
import java.util.ArrayList;

class VariantTableFormat implements TableFormat {

    private ArrayList<String> indIds;
    public ArrayList<String> cols;

    VariantTableFormat(ArrayList<String> selectedCols, Boolean freqFilter, ArrayList<String> indIds) {
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

        for (String s : selectedCols) {
            cols.add(s);
        }
    
        cols.add("Total effects");
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