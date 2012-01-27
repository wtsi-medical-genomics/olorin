
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class StringMatcherEditor {

    private JPanel widget;
    private TextComponentMatcherEditor tme;
    private JTextField filterField;

    public StringMatcherEditor(String id, final int i) {
        widget = new JPanel();
        widget.add(new JLabel(id + ":"));
        filterField = new JTextField(5);
        widget.add(filterField);
        TextFilterator idFilterator = new TextFilterator() {

            @Override
            public void getFilterStrings(java.util.List baseList, Object e) {
                Variant variant = (Variant) e;
                baseList.add(variant.getTableArray().get(i));
            }
        };
        tme = new TextComponentMatcherEditor(filterField, idFilterator);
    }

    public JPanel getWidget() {
        return widget;
    }

    public TextComponentMatcherEditor getTextComponentMatcherEditor() {
        return tme;
    }

    void setText(String text) {
        this.filterField.setText(text);
    }
}