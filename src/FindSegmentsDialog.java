
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Hashtable;



public class FindSegmentsDialog extends JDialog implements ActionListener,ChangeListener {
		
		private boolean success;
		private ArrayList<JCheckBox> checkBoxes;
		private ArrayList<String> selectedInfo;
		JTable filterTable;
	    
		private int minMatches;
		private JTextField minMatchesField;
		private VCFMeta meta;
		
		public FindSegmentsDialog(JFrame parent, int selected, VCFMeta metainfo){
	    	super(parent,"Filter Variants",true);
	    	
	    	meta = metainfo;
	    	
	        JPanel contents = new JPanel();
	        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
	        JPanel minMatchesPanel = new JPanel();
	        minMatchesPanel.add(new JLabel("Minimum number of matching chromosomes: "));
	        JSlider matchNum = new JSlider(JSlider.HORIZONTAL,2, selected, selected);
	        //set the default value of minMatches
	        setMinMatches(selected);
	        matchNum.addChangeListener(this);
	        matchNum.setMajorTickSpacing(1);
	        matchNum.setPaintTicks(true);
	        matchNum.setPaintLabels(true);
	        matchNum.setSnapToTicks(true);
	        minMatchesPanel.add(matchNum);
	        contents.add(minMatchesPanel);
	        
	        filterTable = new JTable(new FilterTableModel(meta));
	        filterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        filterTable.setFillsViewportHeight(true);
	        TableColumnAdjuster tca = new TableColumnAdjuster(filterTable);
			tca.setColumnDataIncluded(true);
			tca.adjustColumns();
	        JScrollPane scrollPane = new JScrollPane(filterTable);
	        contents.add(scrollPane);
	        
	        JPanel butPan = new JPanel();
	        JButton okbut = new JButton("OK");
	        getRootPane().setDefaultButton(okbut);
	        okbut.addActionListener(this);
	        butPan.add(okbut);
	        JButton cancelbut = new JButton("Cancel");
	        cancelbut.addActionListener(this);
	        butPan.add(cancelbut);
	        contents.add(butPan);
	        this.pack();
	        this.setContentPane(contents);
	    }

	    public void actionPerformed(ActionEvent e) {
	        if (e.getActionCommand().equals("OK")){
	        	ArrayList<Boolean> selected = ((FilterTableModel) filterTable.getModel()).getSelected();
	        	selectedInfo = new ArrayList<String> ();
	        	for (int i=0; i < meta.info.size(); i++ ) {
	        		if (selected.get(i)) {
	        			selectedInfo.add(meta.getInfo().get(i).get("ID"));
	        		}
	        	}
	            setSuccess(true);
	            this.dispose();
	        }else if (e.getActionCommand().equals("Cancel")){
	        	setSuccess(false);
	        	this.dispose();
	        }
	    }
	    
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        if (!source.getValueIsAdjusting()) {
	        	setMinMatches((int)source.getValue());
	        }
	    }

		public boolean success() {
	    	return success;
	    }
	
		public void setSuccess(boolean b) {
			success = b;
		}
		    
	    public int getMinMatches() {
	    	return minMatches;
	    }
	    
	    private void setMinMatches(int value) {
	    	minMatches = value;
		}
	    
	    public ArrayList<String> getSelectedInfo() {
	    	return selectedInfo;
	    }
	
}
