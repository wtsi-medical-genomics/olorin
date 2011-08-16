import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;


class vcfTableModel extends AbstractTableModel {
    public ArrayList<String> columnNames = new ArrayList();
    columnNames.add("CHROM");
    columnNames.add("POS");
    columnNames.add("ID");
    columnNames.add("REF");
    columnNames.add("ALT");
    columnNames.add("QUAL");
    columnNames.add("FILTER");
    private VCF data;

    
    
    public void addColumnName(String s) {
    	columnNames.add(s);
    }
    
    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

}