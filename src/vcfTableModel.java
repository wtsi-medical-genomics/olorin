import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;


class vcfTableModel extends AbstractTableModel {
	
	ArrayList<String> selectedCols;
	ArrayList<Variant> vcfData;
	ArrayList<String> columns;
	
	public vcfTableModel (ArrayList<Variant> d, ArrayList<String> sc) {
		selectedCols = sc;
		vcfData = d;
		makeCols();
	}
	
	public void makeCols () {
		columns = new ArrayList<String> ();
		//add mandatory columns
		columns.add("#");
		columns.add("CHROM");
		columns.add("POS");
		columns.add("ID");
		columns.add("REF");
		columns.add("ALT");
		columns.add("QUAL");
		columns.add("FILTER");
		
		for (String s : selectedCols) {
			columns.add(s);
		}
		
		// print all the samples (filter the samples to only those in the ped?)
		
		// add the format info to tooltips for over the genotype
		
	}
    
    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
    	return vcfData.size();
    }

    public String getColumnName(int col) {
        return columns.get(col);
    }

    public Object getValueAt(int row, int col) {
    	if (col == 0) {
    		// row number - however does not work if the data is re-sorted
    		return row+1;
    	} else {
    		return vcfData.get(row).getArray(selectedCols).get(col-1);
    	}
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }    

}