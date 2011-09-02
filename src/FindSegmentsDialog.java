
import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Hashtable;



public class FindSegmentsDialog extends JDialog implements ActionListener {
		
		private boolean success;
		private ArrayList<JCheckBox> checkBoxes;
		private ArrayList<String> selectedInfo;
	    
		private int minMatches;
		private JTextField minMatchesField;
		
		public FindSegmentsDialog(JFrame parent, int selected, VCFMeta meta){
	    	super(parent,"Find segments",true);
	    	
	        JPanel contents = new JPanel();
	        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

	        JPanel minMatchesPanel = new JPanel();
	        minMatchesPanel.add(new JLabel("Minimum number of matching chromosomes: "));
	        minMatchesField = new JTextField(Integer.toString(selected), 5);
	        minMatchesPanel.add(minMatchesField);	       
	        contents.add(minMatchesPanel);
	        
	        contents.add(new JPanel());
	        
	        JPanel columnSelectPanel = new JPanel();
	        columnSelectPanel.setLayout(new BoxLayout(columnSelectPanel, BoxLayout.Y_AXIS));
	        columnSelectPanel.add(new JLabel("Select Data Columns"));
	        ArrayList<Hashtable<String, String>> info = meta.getInfo();
	        checkBoxes = new ArrayList<JCheckBox> ();
	        for ( Hashtable<String, String> infoField : info) {
	        	JCheckBox cb = new JCheckBox(infoField.get("ID") + " - " + infoField.get("Description"));
	        	columnSelectPanel.add(cb);
	        	checkBoxes.add(cb);	
	        }
	
	        contents.add(columnSelectPanel);
	        
	        JPanel butPan = new JPanel();
	        JButton okbut = new JButton("OK");
	        getRootPane().setDefaultButton(okbut);
	        okbut.addActionListener(this);
	        butPan.add(okbut);
	        JButton cancelbut = new JButton("Cancel");
	        cancelbut.addActionListener(this);
	        butPan.add(cancelbut);
	        contents.add(butPan);

	        this.setContentPane(contents);
	    }

	    public void actionPerformed(ActionEvent e) {
	        if (e.getActionCommand().equals("OK")){
	        	minMatches = Integer.parseInt(minMatchesField.getText());
	        	selectedInfo = new ArrayList<String> ();
	        	for (JCheckBox cb : checkBoxes) {
	        		if (cb.isSelected()) {
	        			String s = cb.getText();
	        			String[] a = s.split(" - ");
	        			selectedInfo.add(a[0]);
	        		}
	        	}
	            setSuccess(true);
	            this.dispose();
	        }else if (e.getActionCommand().equals("Cancel")){
	        	setSuccess(false);
	        	this.dispose();
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
	    
	    public ArrayList<String> getSelectedInfo() {
	    	return selectedInfo;
	    }
	
}
