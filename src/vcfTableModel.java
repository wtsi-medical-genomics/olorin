import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;


class vcfTableModel extends AbstractTableModel {
	private VCF vcf;
	
	public Vector<String> columnNames = vcf.getMeta().getCols;
    
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