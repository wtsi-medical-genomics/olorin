
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class NumericMatcherEditor extends AbstractMatcherEditor implements ActionListener, ChangeListener {

    private JComboBox signChooser;
    private JTextField valueField;
    private JSlider slider;
    private String id;
    private int i;
    private JPanel widget;
    private ArrayList<String> selectedCols;
    private Boolean doubleSlider;

    NumericMatcherEditor(String id, int i) {
        this.id = id;
        this.i = i;
        this.widget = new JPanel();

        widget.add(new JLabel(id + ":"));

        this.signChooser = new JComboBox(new Object[]{">", ">=", "<", "<="});
        this.signChooser.addActionListener(this);
        widget.add(signChooser);

        this.valueField = new JTextField(5);
        valueField.setText("0");
        this.valueField.addActionListener(this);
        widget.add(valueField);
    }

    NumericMatcherEditor(String id, int i, Double min, Double max) {
        this.id = id;
        this.i = i;
        this.widget = new JPanel();
        this.doubleSlider = false;
        
        widget.add(new JLabel(id + ":"));

        this.signChooser = new JComboBox(new Object[]{">", ">=", "<", "<="});
        this.signChooser.addActionListener(this);
        widget.add(signChooser);

        this.valueField = new JTextField(5);
        valueField.setText("0");
        this.valueField.addActionListener(this);
        widget.add(valueField);

        // special case for 0 - 1 values        
        if (min >= 0 && max <= 1) {            
            this.doubleSlider = true;
            this.slider = new JSlider(0, 10);
            this.slider.setPaintLabels(true);
            this.slider.addChangeListener(this);
        } else {
            this.slider = new JSlider(min.intValue(), max.intValue());
            this.slider.setPaintLabels(true);
            this.slider.addChangeListener(this);
        }
        widget.add(slider);
    }

    public JPanel getWidget() {
        return widget;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String sign = (String) this.signChooser.getSelectedItem();
        final String cutoff = (String) this.valueField.getText();
        if (sign == null || cutoff == null) {
            this.fireMatchAll();
        } else {
            this.fireChanged(new NumericMatcher(i, sign, cutoff));
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        if (doubleSlider) {
            double sliderValue = (double) this.slider.getValue();            
            this.valueField.setText(Double.toString(sliderValue/10d));
        } else {
            this.valueField.setText(Integer.toString(this.slider.getValue()));
        }
        
        final String sign = (String) this.signChooser.getSelectedItem();
        final String cutoff = (String) this.valueField.getText();

        if (sign == null || cutoff == null) {
            this.fireMatchAll();
        } else {
            this.fireChanged(new NumericMatcher(i, sign, cutoff));
        }
    }

    String getSign() {
        return (String) this.signChooser.getSelectedItem();
    }

    public void setSignChooser(String sign) {
        this.signChooser.setSelectedItem(sign);
    }

    public void setValueField(String value) {
        this.valueField.setText(value);
    }

    String getCutoff() {
        return this.valueField.getText();
    }

    String getID() {
        return id;
    }

    void setSlider(String value) {
        this.slider.setValue(Integer.parseInt(value));
    }

    private static class NumericMatcher implements Matcher {

        private final String sign;
        private final String cutoff;
        private final int i;

        public NumericMatcher(int i, String sign, String cutoff) {
            this.sign = sign;
            this.cutoff = cutoff;
            this.i = i;
        }

        @Override
        public boolean matches(Object item) {
            final Variant variant = (Variant) item;
            String value = (String) variant.getTableArray().get(i);
            if (value != null) {
                if (sign.matches("<")) {
                    if (Double.parseDouble(value) < Double.parseDouble(cutoff)) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (sign.matches(">")) {
                    if (Double.parseDouble(value) > Double.parseDouble(cutoff)) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (sign.matches(">=")) {
                    if (Double.parseDouble(value) >= Double.parseDouble(cutoff)) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (sign.matches("<=")) {
                    if (Double.parseDouble(value) <= Double.parseDouble(cutoff)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                // if value is missing keep in the sorted list 
                // should this only be true for frequency values?                
                return true;
            }
        }
    }
}
