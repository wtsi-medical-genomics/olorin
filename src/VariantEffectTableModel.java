
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class VariantEffectTableModel extends AbstractTableModel {

    ArrayList<VariantEffect> variantEffects;
    ArrayList<String> selectedCols;
    ArrayList<String> columns;

    public VariantEffectTableModel(ArrayList<VariantEffect> variantEffects, ArrayList<String> selectedCols) {
        this.variantEffects = variantEffects;
        this.selectedCols = selectedCols;
        makeCols();
    }

    public void makeCols() {
        columns = new ArrayList<String>();

        for (String col : selectedCols) {
            if (col.startsWith("CSQ")) {
                if (col.matches("CSQ Gene")) {
                    columns.add("Gene");
                }
                if (col.matches("CSQ Feature")) {
                    columns.add("Feature");
                }
                if (col.matches("CSQ Consequence")) {
                    columns.add("Consequence");
                }
                if (col.matches("CSQ Amino Acid Change")) {
                    columns.add("Amino acid change");
                }
                if (col.matches("CSQ Sift Prediction")) {
                    columns.add("SIFT prediction");
                }
                if (col.matches("CSQ Sift Score")) {
                    columns.add("SIFT score");
                }
                if (col.matches("CSQ PolyPhen Prediction")) {
                    columns.add("PloyPhen prediction");
                }
                if (col.matches("CSQ PolyPhen Score")) {
                    columns.add("PolyPhen score");
                }
                if (col.matches("CSQ Condel Prediction")) {
                    columns.add("Condel prediction");
                }
                if (col.matches("CSQ Condel Score")) {
                    columns.add("Condel score");
                }
                if (col.matches("CSQ Grantham Score")) {
                    columns.add("Grantham score");
                }

            }
        }
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public int getRowCount() {
        return variantEffects.size();
    }

    @Override
    public String getColumnName(int col) {
        return columns.get(col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        return variantEffects.get(row).getValues(selectedCols).get(col);
    }

    @Override
    public Class getColumnClass(int c) {
        
        if (getValueAt(0, c) != null) {
            return getValueAt(0, c).getClass();
        } else {
            // no values for this column so just return a string class
            return new String().getClass();
        }
    }
}