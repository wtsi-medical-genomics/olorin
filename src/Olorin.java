
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ideogram.Marker;
import ideogram.MarkerCollection;
import ideogram.db.IdeogramDB;
import ideogram.input.DataSlot;
import ideogram.tree.Interval;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.LineBorder;
import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.graph.Node;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.DefaultEdgeView;
import pedviz.view.DefaultNodeView;
import pedviz.view.GraphView;
import pedviz.view.GraphView2D;
import pedviz.view.NodeEvent;
import pedviz.view.NodeListener;
import pedviz.view.NodeView;
import pedviz.view.rules.ColorRule;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;
import pedviz.view.symbols.SymbolSexUndesignated;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import util.FileFormatException;

public class Olorin extends JFrame implements ActionListener, ListEventListener, MouseListener {
    
    FindSegmentsDialog fsd;
    JFileChooser jfc;
    DataDirectory data;
    JTable variantsTable;
    JPanel pedigreePanel;
    JScrollPane ideogramPane;
    JScrollPane variantsPane;
    JPanel modePanel;
    JPanel filterPanel;
    JPanel contentPanel;
    JSplitPane mainPane;
    JRadioButton anyMode;
    JRadioButton selectedMode;
    JRadioButton allMode;
    static JTextArea logPanel;
    private JMenu fileMenu;
    private JMenu toolMenu;
    private JMenuItem openDirectory;
    private JMenuItem exportVariants;
    private JMenuItem exportSegments;
    private JMenuItem findSegments;
    private JMenuItem savePlot;
    private int minMatches;
    // initialise a global arraylist that will hold all the selected ids from the pedigree
    private Hashtable<String, Integer> filterList = new Hashtable<String, Integer>();
    private ArrayList<String> selectedCols = new ArrayList();
    private ArrayList<Variant> vcfData;
    private ArrayList<SegmentMatch> segments;
    FlowFile flow;
    private static ArrayList<String> indIds;
    private ArrayList<Integer> indIndexes;
    // regular expression for splitting part of the CSQ string
    public static Pattern variantEffectPattern = Pattern.compile("(\\w+),(\\w+)\\((\\d+.*\\d*)\\)");
    
    public static void main(String[] args) {
        
        new Olorin();
        
    }
    private boolean freqFilter;
    private double freqCutoff;
    private String freqFile;
    EventList<Variant> variants;
    private JLabel variantCount;
    private int currentVariantCount;
    private int totalVariantCount;
    private JTable table;
    private SortedList sortedFilteredVariants;
    private EventList matcherEditors;
    private Object filterProfile;
    
    Olorin() {
        super("Olorin");
        
        this.setLayout(new BorderLayout());
        
        jfc = new JFileChooser("user.dir");
        JMenuBar mb = new JMenuBar();
        
        int menumask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        fileMenu = new JMenu("File");
        openDirectory = new JMenuItem("Open Directory");
        openDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menumask));
        openDirectory.addActionListener(this);
        exportVariants = new JMenuItem("Export Variants");
        exportVariants.addActionListener(this);
        exportVariants.setEnabled(false);
        exportSegments = new JMenuItem("Export Segments");
        exportSegments.addActionListener(this);
        exportSegments.setEnabled(false);
        fileMenu.add(openDirectory);
        fileMenu.add(exportVariants);
        fileMenu.add(exportSegments);
        
        mb.add(fileMenu);
        
        toolMenu = new JMenu("Tools");
        findSegments = new JMenuItem("Find Segments");
        findSegments.addActionListener(this);
        findSegments.setEnabled(false);
        toolMenu.add(findSegments);
        
        savePlot = new JMenuItem("Save Plot");
        savePlot.addActionListener(this);
        savePlot.setEnabled(false);
        toolMenu.add(savePlot);
        
        mb.add(fileMenu);
        mb.add(toolMenu);
        setJMenuBar(mb);
        
        pedigreePanel = new JPanel();
        pedigreePanel.setBorder(new LineBorder(Color.BLACK));
        pedigreePanel.setBackground(Color.WHITE);
        pedigreePanel.setLayout(new BorderLayout());
        
        variantsPane = new JScrollPane();
        variantsPane.setBorder(new LineBorder(Color.BLACK));
        variantsPane.setBackground(Color.WHITE);
        
        filterPanel = new JPanel(new GridBagLayout());
        
        modePanel = new JPanel();
        modePanel.setLayout(new GridBagLayout());
        currentVariantCount = 0;
        totalVariantCount = 0;
        variantCount = new JLabel("Currently showing " + currentVariantCount + " of " + totalVariantCount + " variants");
        anyMode = new JRadioButton("any individual");
        anyMode.setActionCommand("anyMode");
        anyMode.setSelected(true);
        anyMode.addActionListener(this);
        selectedMode = new JRadioButton("selected individuals");
        selectedMode.setActionCommand("selectedMode");
        selectedMode.addActionListener(this);
        allMode = new JRadioButton("all individuals");
        allMode.setActionCommand("allMode");
        allMode.addActionListener(this);
        
        ButtonGroup group = new ButtonGroup();
        group.add(anyMode);
        group.add(selectedMode);
        group.add(allMode);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 10;
        c.gridwidth = 1;
        modePanel.add(variantCount, c);
        
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 10;
        c.gridwidth = 2;
        modePanel.add(new JLabel("Show variants present in ..."), c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 0;
        c.gridwidth = 1;
        modePanel.add(anyMode, c);
        
        c.gridy = 2;
        c.gridx = 1;
        modePanel.add(selectedMode, c);
        
        c.gridy = 2;
        c.gridx = 2;
        modePanel.add(allMode, c);
        
        c.gridx = 0;
        c.gridy = 0;
        filterPanel.add(modePanel, c);

        //Register a listener for the radio buttons.
        anyMode.addActionListener(this);
        selectedMode.addActionListener(this);
        allMode.addActionListener(this);
        
        ideogramPane = new JScrollPane();
        ideogramPane.setBorder(new LineBorder(Color.BLACK));
        ideogramPane.setBackground(Color.WHITE);
        ideogramPane.addComponentListener(new ComponentAdapter() {
            
            @Override
            public void componentResized(ComponentEvent e) {
                if (segments != null) {
                    drawIdeogram(segments);
                }
            }
        });
        
        logPanel = new JTextArea();
        logPanel.setEditable(false);
        logPanel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logPanel.setLineWrap(true);
        logPanel.setWrapStyleWord(true);
        JScrollPane loggingPane = new JScrollPane(logPanel);
        loggingPane.setPreferredSize(new Dimension(100, 50));
        loggingPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //Provide minimum sizes for the components
        Dimension minSizeUpper = new Dimension(100, 50);
        Dimension minSizeLower = new Dimension(100, 50);
        pedigreePanel.setMinimumSize(minSizeUpper);
        variantsPane.setMinimumSize(minSizeUpper);
        loggingPane.setMinimumSize(minSizeLower);
        ideogramPane.setMinimumSize(minSizeLower);
        //Provide prefered sizes for the components
        Dimension prefSizeUpper = new Dimension(800, 500);
        Dimension prefSizeLower = new Dimension(800, 200);
        pedigreePanel.setPreferredSize(prefSizeUpper);
        variantsPane.setPreferredSize(prefSizeUpper);
        loggingPane.setPreferredSize(prefSizeLower);
        ideogramPane.setPreferredSize(prefSizeLower);

        //Create a split pane with the two scroll panes in it.
        JSplitPane upperPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pedigreePanel, variantsPane);
        upperPane.setOneTouchExpandable(true);
        upperPane.setDividerLocation(500);
        
        JSplitPane lowerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterPanel, ideogramPane);
        lowerPane.setOneTouchExpandable(true);
        lowerPane.setDividerLocation(500);
        
        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, lowerPane);
        
        contentPanel = new JPanel();
        contentPanel.setOpaque(true);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(mainPane);
        
        addWindowListener(new WindowAdapter() {
            
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        this.setContentPane(contentPanel);
        this.pack();
        this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        File freqFile;
        if (command.equals("Open Directory")) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    try {
                        data = new DataDirectory(jfc.getSelectedFile().getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Unable to Processs the Selected Files.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    drawPedigree(data.getPed());
                    findSegments.setEnabled(true);
                } finally {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        } else if (command.equals("Export Variants")) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filename);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                PrintStream output = new PrintStream(fos);
                
                output.print("CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER");
                for (String s : fsd.getSelectedCols()) {
                    output.print("\t" + s);
                }
                output.println();
                
                for (Variant v : vcfData) {
                    output.println(join(v.getTableArray(), "\t"));
                }
                output.close();
            }
        } else if (command.equals("Export Segments")) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();
                System.out.println(filename);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filename);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                PrintStream output = new PrintStream(fos);
                for (SegmentMatch s : segments) {
                    output.println(join(s.getArray(), "\t"));
                }
                output.close();
            }
        } else if (command.equals("Find Segments")) {
            
            contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            ArrayList<Sample> selectedInds = getSelectedInds();
            
            if (selectedInds.size() > 1) {
                
                if (getSelectedSeqInds().size() > 0) {
                    fsd = new FindSegmentsDialog(selectedInds.size(), data.vcf.getMeta(), selectedCols, data.vcf.getMeta().hasCSQ());
                    fsd.setModal(true);
                    fsd.pack();
                    fsd.setVisible(true);
                    
                    if (fsd.success()) {
                        String filteringMode = getFilteringMode();
                        indIds = getIndIds(filteringMode);
                        setFreqFilter(fsd.freqFilter());
                        setFreqCutoff(fsd.getFreqCutoff());
                        setSelectedCols(fsd.getSelectedCols());
                        setMinMatches(fsd.getMinMatches());
                        
                        SampleCompare sc = new SampleCompare();
                        segments = sc.compareMulti(selectedInds);
                        if (segments.size() > 0) {
                            try {
                                if (getFreqFilter()) {
                                    File ff = new File(fsd.getFreqFile());
                                    if (ff.exists()) {
                                        setFreqFile(fsd.getFreqFile());
                                        vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedCols(), filteringMode, indIds, getFreqFile(), getFreqCutoff());
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Frequency File Missing.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {                                    
                                    vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedCols(), filteringMode, indIds, this.contentPanel);                                    
                                }
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null, "Error finding variants.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            
                            if (vcfData != null) {
                                showVariants();
                            }
                            drawIdeogram(segments);
                            savePlot.setEnabled(true);
                        } else {
                            JOptionPane.showMessageDialog(null, "No Shared Segements Found.", "Error", JOptionPane.ERROR_MESSAGE);
                            variantsPane.removeAll();
                            exportVariants.setEnabled(false);
                            exportSegments.setEnabled(false);
                            savePlot.setEnabled(false);
                            //remove the segments drawn on the idoegrams
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No Sequenced Individuals Selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } else {
                JOptionPane.showMessageDialog(null, "Too few Individuals Selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (command.equals("Save Plot")) {
            File defaultFileName = new File("olorin.png");
            jfc = new JFileChooser();
            jfc.setSelectedFile(defaultFileName);
            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File png;
                if (!jfc.getSelectedFile().toString().endsWith(".png")) {
                    png = new File(jfc.getSelectedFile().toString() + ".png");
                } else {
                    png = jfc.getSelectedFile();
                }
                BufferedImage image = new BufferedImage(ideogramPane.getWidth(), ideogramPane.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                ideogramPane.paint(g2);
                g2.dispose();
                try {
                    ImageIO.write(image, "png", png);
                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                }
            }
        } else if (command.equals("anyMode")) {
            
            refreshVariants("any");
        } else if (command.equals("selectedMode")) {
            
            refreshVariants("selected");
        } else if (command.equals("allMode")) {
            
            refreshVariants("all");
        }
    }
    
    private int getinfoOffset() {
        // create an int for the table offset to get to the info data
        // taking into account the default columns, inds and filter columns
        int infoColOffset = 7 + indIds.size();
        if (fsd.freqFilter()) {
            infoColOffset++;
        }
        return infoColOffset;
    }
    
    private ArrayList getIndIds(String fm) {
        if (fm.matches("any") || fm.matches("all")) {
            return data.vcf.getMeta().getSamples();
        } else if (fm.matches("selected")) {
            return getSelectedSeqInds();
        } else {
            return new ArrayList();
        }
    }
    
    private String getFilteringMode() {
        if (anyMode.isSelected()) {
            return "any";
        } else if (selectedMode.isSelected()) {
            return "selected";
        } else if (allMode.isSelected()) {
            return "all";
        } else {
            return "";
        }
    }
    
    private ArrayList<Sample> getSelectedInds() {
        ArrayList<Sample> selectedInds = new ArrayList<Sample>();
        Enumeration<String> e = filterList.keys();
        while (e.hasMoreElements()) {
            String sample = e.nextElement();
            Sample s = data.samples.get(sample);
            selectedInds.add(s);
        }
        return selectedInds;
    }
    
    private ArrayList<String> getSelectedSeqInds() {
        ArrayList<String> selectedSeqInds = new ArrayList<String>();
        for (String seqSample : data.vcf.getMeta().getSamples()) {
            if (filterList.containsKey(seqSample)) {
                selectedSeqInds.add(seqSample);
            }
        }
        return selectedSeqInds;
    }
    
    private EventList createFilters(int infoColOffset) {

        // create a list to hold all the matcherEditors
        matcherEditors = GlazedLists.threadSafeList(new BasicEventList());
        
        JPanel infoFilterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        
        ArrayList<Double> qualityValues = getValues(5);
        Double qualityMin;
        Double qualityMax;
        try {
            qualityMin = Collections.min(qualityValues);
            qualityMax = Collections.max(qualityValues);
        } catch (NoSuchElementException nsee) {
            qualityMin = 0d;
            qualityMax = 0d;
        }
        
        NumericMatcherEditor qme = new NumericMatcherEditor("Quality", 5, qualityMin, qualityMax);
        matcherEditors.add(qme);
        infoFilterPanel.add(qme.getWidget(), c);

        // filter for filter column?

        for (int i = 0; i < fsd.getSelectedCols().size(); i++) {
            
            c.gridy = i + 1;
            String id = fsd.getSelectedCols().get(i);
            
            String type = null;
            String number = null;
            
            if (id.startsWith("CSQ")) {
                if (id.matches("CSQ Gene")
                        || id.matches("CSQ Feature")
                        || id.matches("CSQ Consequence")
                        || id.matches("CSQ Amino Acid Change")
                        || id.matches("CSQ Sift Prediction")
                        || id.matches("CSQ PolyPhen Prediction")
                        || id.matches("CSQ Condel Prediction")) {
                    type = "String";
                } else if (id.matches("CSQ Sift Score")
                        || id.matches("CSQ PolyPhen Score")
                        || id.matches("CSQ Condel Score")
                        || id.matches("CSQ Grantham Score")
                        || id.matches("CSQ Gerp Score")) {
                    type = "Float";
                    number = "1";
                }
            } else {
                type = data.vcf.getMeta().getInfoObjects().get(id).getType();
                number = data.vcf.getMeta().getInfoObjects().get(id).getNumber();
            }
            if (type.matches("String")) {
                StringMatcherEditor sme = new StringMatcherEditor(id, i + infoColOffset);
                matcherEditors.add(sme.getTextComponentMatcherEditor());
                infoFilterPanel.add(sme.getWidget(), c);
            } else if (type.matches("Integer") && number.matches("1")) {  // if the info field has a number > 1 or '.' - can't create a simple filter
                ArrayList<Double> values = getValues(i + infoColOffset);
                Double min;
                Double max;
                try {
                    min = Collections.min(values);
                    max = Collections.max(values);
                } catch (NoSuchElementException nsee) {
                    min = 0d;
                    max = 0d;
                }
                NumericMatcherEditor nme = new NumericMatcherEditor(id, i + infoColOffset, min, max);
                matcherEditors.add(nme);
                infoFilterPanel.add(nme.getWidget(), c);
            } else if (type.matches("Float") && number.matches("1")) {
                ArrayList<Double> values = getValues(i + infoColOffset);
                Double min;
                Double max;
                try {
                    min = Collections.min(values);
                    max = Collections.max(values);
                } catch (NoSuchElementException nsee) {
                    min = 0d;
                    max = 0d;
                }
                NumericMatcherEditor nme = new NumericMatcherEditor(id, i + infoColOffset, min, max);
                matcherEditors.add(nme);
                infoFilterPanel.add(nme.getWidget(), c);
            } else if (type.matches("Flag")) {
                FlagMatcherEditor fme = new FlagMatcherEditor(id, i + infoColOffset);
                matcherEditors.add(fme);
                infoFilterPanel.add(fme.getWidget(), c);
            }
        }
        
        filterPanel.removeAll();
        c.gridx = 0;
        c.gridy = 0;
        filterPanel.add(modePanel, c);
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        filterPanel.add(new JScrollPane(infoFilterPanel), c);
        
        return matcherEditors;
    }
    
    private void drawIdeogram(ArrayList<SegmentMatch> m) {
        int chrNum = data.getChrNum();
        Collections.sort(m);
        HashMap<String, MarkerCollection> segHash = new HashMap<String, MarkerCollection>();
        for (SegmentMatch sm : m) {
            if (!segHash.containsKey(sm.getChr())) {
                segHash.put(sm.getChr(), new MarkerCollection());
            }
            Interval interval = new Interval(sm.getStart(), sm.getEnd());
            Marker marker = new Marker(interval, sm.getIds().size());
            segHash.get(sm.getChr()).add(marker);
        }

        // need to change the ideogramDB code for to accept the ucsc format files
        // which will make it easier to update for new assemblies
        IdeogramDB db = new IdeogramDB();
        String name = "resources/data/ideogram";
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream == null) {
            name = "resources/data/ideogram.gz";
            stream = getClass().getResourceAsStream(name);
            if (stream != null) {
                try {
                    stream = new GZIPInputStream(stream);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Can't Open 'resources/data/ideogram.gz'.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (stream == null) {
            System.out.println("cannot find resource name '" + name + "'");
        }
        try {
            db.read(new InputStreamReader(stream));
        } catch (FileFormatException ex) {
            Logger.getLogger(Olorin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Olorin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Vector<DataSlot> slots = new Vector<DataSlot>();

        // add one panel per chromosome
        JPanel chr_panel = new JPanel();
        chr_panel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        
        int h = ideogramPane.getHeight();
        int w = ideogramPane.getWidth();

        // create the height scale for the chromosome
        // should use a more inteligent figure than just 300
        double r = (double) h / 300;
        
        for (int i = 0; i < 22; ++i) {
            IdeogramView iv = new IdeogramView(r);
            iv.setChromosome(i + 1);
            iv.setIdeogramDB(db);
            // when creating the ideogram change the width of the chromosome and sharing blocks acording to the size of the window
            iv.setPreferredSize(new Dimension(Math.round(w / 22), h));
            
            if (segHash.containsKey(Integer.toString(i + 1))) {
                iv.setSegments(segHash.get(Integer.toString(i + 1)), minMatches);
            }
            c.gridx = i;
            c.gridy = 1;
            
            chr_panel.add(iv, c);
        }
        
        ideogramPane.setViewportView(chr_panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void drawPedigree(String csvFile) {
        
        Graph graph = new Graph();
        CsvGraphLoader loader = new CsvGraphLoader(csvFile, ",");
        loader.setSettings("Id", "Mid", "Fid");
        loader.load(graph);
        
        DefaultNodeView n = new DefaultNodeView();
        n.addHintAttribute("Id");
        n.addHintAttribute("Mid");
        n.addHintAttribute("Fid");
        n.addHintAttribute("VCF");
        n.setBorderWidth(0.1f);
        
        DefaultEdgeView e = new DefaultEdgeView();
        e.setWidth(0.1f);
        e.setColor(Color.black);
        e.setConnectChildren(true);
        
        Sugiyama s = new Sugiyama(graph, n, e);
        s.run();
        
        GraphView2D view = new GraphView2D(s.getLayoutedGraph());
        view.addRule(new ColorRule("Aff", "1", Color.white));
        view.addRule(new ColorRule("Aff", "2", Color.black));
        view.addRule(new ShapeRule("Sex", "1", new SymbolSexMale()));
        view.addRule(new ShapeRule("Sex", "2", new SymbolSexFemale()));
        view.addRule(new ShapeRule("Sex", "-1", new SymbolSexUndesignated()));
        view.setSelectionEnabled(true);
        
        view.addNodeListener(
                new NodeListener() {
                    
                    @Override
                    public void onNodeEvent(NodeEvent event) {
                        if (event.getType() == NodeEvent.SELECTED) {
                            String idSelected = (String) event.getNode().getId();
                            GraphView graph = event.getGraphView();
                            Node node = event.getNode();
                            NodeView nodeView = event.getNodeView();
                            if (filterList.containsKey(idSelected)) {
                                if (nodeView.getNode().getUserData("aff").equals("1")) {
                                    nodeView.setBorderWidth(0.1f);
                                    nodeView.setBorderColor(Color.black);
                                } else if (nodeView.getNode().getUserData("aff").equals("2")) {
                                    nodeView.setColor(Color.black);
                                }
                                filterList.remove(idSelected);
                            } else {
                                if (nodeView.getNode().getUserData("aff").equals("1")) {
                                    nodeView.setBorderWidth(1);
                                    nodeView.setBorderColor(Color.green);
                                } else if (nodeView.getNode().getUserData("aff").equals("2")) {
                                    nodeView.setColor(Color.green);
                                }
                                filterList.put(idSelected, 1);
                            }
                        }
                    }
                });
        pedigreePanel.removeAll();
        pedigreePanel.add(view.getComponent());
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    public void printLog(String text) {
        logPanel.append(text + "\n");
        logPanel.repaint();
    }
    
    public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    
    private ArrayList<Double> getValues(int i) {
        ArrayList<Double> values = new ArrayList<Double>();
        for (Variant v : vcfData) {
            String s = (String) v.getTableArray().get(i);
            //handle cases where it is not an int such as .
            if (s != null) {
                values.add(Double.parseDouble(s));
            }
        }
        return values;
    }
    
    private void setFreqFilter(boolean freqFilter) {
        this.freqFilter = freqFilter;
    }
    
    private Boolean getFreqFilter() {
        return this.freqFilter;
    }
    
    private void setFreqCutoff(double freqCutoff) {
        this.freqCutoff = freqCutoff;
    }
    
    private double getFreqCutoff() {
        return this.freqCutoff;
    }
    
    private void setSelectedCols(ArrayList<String> selectedCols) {
        this.selectedCols = selectedCols;
    }
    
    private ArrayList<String> getSelectedCols() {
        return this.selectedCols;
    }
    
    private void setMinMatches(int minMatches) {
        this.minMatches = minMatches;
    }
    
    private int getMinMatches() {
        return this.minMatches;
    }
    
    private void setFreqFile(String freqFile) {
        this.freqFile = freqFile;
    }
    
    private String getFreqFile() {
        return this.freqFile;
    }
    
    private void showVariants() {
        variants = GlazedLists.threadSafeList(new BasicEventList<Variant>());
        variants.addAll(vcfData);
        this.setTotalVariantCount(variants.size());
        this.setCurrentVariantCount(variants.size());
        this.setVariantCount();
        EventList matcherEditors = createFilters(getinfoOffset());
        FilterList filteredVariants = new FilterList(variants, new CompositeMatcherEditor(matcherEditors));
        sortedFilteredVariants = new SortedList(filteredVariants, null);
        sortedFilteredVariants.addListEventListener(this);
        table = new JTable(new EventTableModel<Variant>(sortedFilteredVariants, new VariantTableFormat(fsd.getSelectedCols(), getFreqFilter(), indIds)));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseListener(this);
        new TableComparatorChooser(table, sortedFilteredVariants, true);
        variantsPane.setViewportView(table);
        contentPanel.revalidate();
        contentPanel.repaint();
        exportVariants.setEnabled(true);
        exportSegments.setEnabled(true);
    }
    
    private void refreshVariants(String mode) {
        
        if (segments != null) {
            contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // store all the currently selected filters
            HashMap<String, ArrayList> filterProfile = new HashMap<String, ArrayList>();
            
            for (Object me : matcherEditors) {
                if (me.getClass().toString().contains("NumericMatcherEditor")) {
                    
                    NumericMatcherEditor nme = (NumericMatcherEditor) me;
                    ArrayList values = new ArrayList();
                    values.add(nme.getSign());
                    values.add(nme.getCutoff());
                    filterProfile.put(nme.getID(), values);
                } else if (me.getClass().toString().contains("StringMatcherEditor")) {
                    
                    System.out.println("sme");
                    // get the id and string
                } else if (me.getClass().toString().contains("FlagMatcherEditor")) {
                    System.out.println("fme");
                    // get the id sign and bool
                }
            }
            
            indIds = getIndIds(mode);
            try {
                if (getFreqFilter()) {
                    vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedCols(), mode, indIds, getFreqFile(), getFreqCutoff());
                } else {
                    vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedCols(), mode, indIds, this.contentPanel);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error finding variants.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            if (vcfData != null) {
                
                variants = GlazedLists.threadSafeList(new BasicEventList<Variant>());
                variants.addAll(vcfData);
                this.setTotalVariantCount(variants.size());
                this.setCurrentVariantCount(variants.size());
                this.setVariantCount();
                matcherEditors = createFilters(getinfoOffset());
                FilterList filteredVariants = new FilterList(variants, new CompositeMatcherEditor(matcherEditors));
                sortedFilteredVariants = new SortedList(filteredVariants, null);
                sortedFilteredVariants.addListEventListener(this);
                table = new JTable(new EventTableModel<Variant>(sortedFilteredVariants, new VariantTableFormat(fsd.getSelectedCols(), getFreqFilter(), indIds)));
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.addMouseListener(this);
                new TableComparatorChooser(table, sortedFilteredVariants, true);
                variantsPane.setViewportView(table);
                contentPanel.revalidate();
                contentPanel.repaint();
                exportVariants.setEnabled(true);
                exportSegments.setEnabled(true);
            }
            contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    @Override
    public void listChanged(ListEvent le) {
        this.setCurrentVariantCount(le.getSourceList().size());
        setVariantCount();
    }
    
    private void setVariantCount() {
        variantCount.setText("Currently showing " + getCurrentVariantCount() + " of " + getTotalVariantCount() + " variants");
    }
    
    public int getCurrentVariantCount() {
        return currentVariantCount;
    }
    
    public void setCurrentVariantCount(int currentVariantCount) {
        this.currentVariantCount = currentVariantCount;
    }
    
    public int getTotalVariantCount() {
        return totalVariantCount;
    }
    
    public void setTotalVariantCount(int totalVariantCount) {
        this.totalVariantCount = totalVariantCount;
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        int col = table.columnAtPoint(me.getPoint());
        if (table.getColumnName(col).matches("Total effects")) {
            int row = table.rowAtPoint(me.getPoint());
            Variant selectedVariant = (Variant) sortedFilteredVariants.get(row);
            ArrayList<VariantEffect> effects = selectedVariant.getVariantEffects();
            VariantEffectsFrame vef = new VariantEffectsFrame(effects, selectedCols);
            vef.setDefaultCloseOperation(VariantEffectsFrame.DISPOSE_ON_CLOSE);
            vef.pack();
            vef.setVisible(true);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent me) {
    }
    
    @Override
    public void mouseReleased(MouseEvent me) {
    }
    
    @Override
    public void mouseEntered(MouseEvent me) {
    }
    
    @Override
    public void mouseExited(MouseEvent me) {
    }
}
