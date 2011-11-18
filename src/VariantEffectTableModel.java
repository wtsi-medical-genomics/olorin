
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class VariantEffectTableModel extends AbstractTableModel {

    ArrayList<VariantEffect> variantEffects;
    private ArrayList<String> columns;

    public VariantEffectTableModel(ArrayList<VariantEffect> variantEffects) {
        this.variantEffects = variantEffects;
        makeCols();
    }

    public void makeCols() {
        columns = new ArrayList<String>();
        columns.add("Gene");
        columns.add("Consequence");
        columns.add("Amino acid change");
        columns.add("Condel prediction");
        columns.add("Condel score");
        columns.add("SIFT prediction");
        columns.add("SIFT score");
        columns.add("PloyPhen prediction");
        columns.add("PolyPhen score");
        columns.add("Grantham score");
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
        return variantEffects.get(row).getValues().get(col);
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}