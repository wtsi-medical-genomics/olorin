import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;


class FilterTableModel extends AbstractTableModel {
	
	VCFMeta meta;
	ArrayList<String> columns;
	ArrayList<Boolean> selected;
	
	public FilterTableModel (VCFMeta m) {
		meta = m;
		makeCols();
		selected = new ArrayList<Boolean> ();
		for (int i=0; i < meta.info.size(); i++ ) {
			selected.add(false);
		}
	}
	
	public void makeCols () {
		columns = new ArrayList<String> ();
		columns.add("ID");
		columns.add("Description");
		columns.add("");			
	}
    
    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
    	return meta.getInfo().size();
    }

    public String getColumnName(int col) {
        return columns.get(col);
    }

    public Object getValueAt(int row, int col) {
    	if (col == 0) {
    		return meta.getInfo().get(row).get("ID");
    	} else if (col == 1) {
    		return meta.getInfo().get(row).get("Description");
    	} else {
    		return selected.get(row);
    	}
    }
    
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        } else {
            return true;
        }
    }

    public void setValueAt(Object value, int row, int col) {
    	if (col == 2) {
    		if (selected.get(row)) {
    			selected.set(row, false);
    		} else {
    			selected.set(row, true);	
    		}
    	}
        fireTableCellUpdated(row, col);
    }
    
    public ArrayList<Boolean> getSelected () {
    	return selected;
    }
  

}