
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

class FlagMatcherEditor extends AbstractMatcherEditor implements ActionListener {

    private JCheckBox flagBox;
    private String id;
    private int i;
    private JPanel widget;
    private ArrayList<String> selectedCols;

    public FlagMatcherEditor(String id, int i) {
        this.id = id;
        this.i = i;
        this.widget = new JPanel();

        widget.add(new JLabel(id + ":"));
        this.flagBox = new JCheckBox();
        this.flagBox.addActionListener(this);
        widget.add(flagBox);
    }

    public JPanel getWidget() {
        return widget;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Boolean flag = this.flagBox.isSelected();
        if (flag) {
            this.fireChanged(new FlagMatcher(i, flag));
        } else {
            this.fireMatchAll();
        }
    }

    private static class FlagMatcher implements Matcher {

        private final Boolean flag;
        private final int i;

        public FlagMatcher(int i, Boolean flag) {
            this.flag = flag;
            this.i = i;
        }

        @Override
        public boolean matches(Object item) {
            final Variant variant = (Variant) item;
            String value = (String) variant.getTableArray().get(i);
            if (value.matches(".")) {
                return false;
            } else {
                return true;
            }
        }
    }
}
