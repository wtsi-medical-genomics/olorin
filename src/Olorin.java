
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ideogram.Marker;
import ideogram.MarkerCollection;
import ideogram.db.IdeogramDB;
import ideogram.input.DataSlot;
import ideogram.tree.Interval;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
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
import java.awt.*;
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
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeListener;
import util.FileFormatException;

public class Olorin extends JFrame implements ActionListener {

    FindSegmentsDialog3 fsd;
    JFileChooser jfc;
    DataDirectory data;
    ProgressMonitor pm;
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
    private ArrayList<Boolean> selectedCols = new ArrayList();
    private ArrayList<Variant> vcfData;
    private ArrayList<SegmentMatch> segments;
    FlowFile flow;
    GridBagConstraints c;

    public static void main(String[] args) {

        new Olorin();

    }
    private JPopupMenu popmenu;

    Olorin() {
        super("Olorin");

        this.setLayout(new BorderLayout());

        jfc = new JFileChooser("user.dir");
        JMenuBar mb = new JMenuBar();

        int menumask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        c = new GridBagConstraints();

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

        modePanel = new JPanel(new GridBagLayout());

        anyMode = new JRadioButton("any individual");
        anyMode.setActionCommand("anyMode");
        anyMode.setSelected(true);
        selectedMode = new JRadioButton("selected individuals");
        selectedMode.setActionCommand("selectedMode");
        allMode = new JRadioButton("all individuals");
        allMode.setActionCommand("allMode");

        ButtonGroup group = new ButtonGroup();
        group.add(anyMode);
        group.add(selectedMode);
        group.add(allMode);

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

        modePanel.add(new JLabel("Show variants present in ..."), c);
        c.gridx = 0;
        c.gridy = 1;
        modePanel.add(anyMode, c);
        c.gridx = 1;
        modePanel.add(selectedMode, c);
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
                for (String s : fsd.getSelectedInfo()) {
                    output.print("\t" + s);
                }
                output.println();

                for (Variant v : vcfData) {
                    output.println(join(v.getArray(fsd.getSelectedInfo()), "\t"));
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
            ArrayList<Sample> selected = new ArrayList<Sample>();
            Enumeration<String> e = filterList.keys();
            while (e.hasMoreElements()) {
                String sample = e.nextElement();
                Sample s = data.samples.get(sample);
                selected.add(s);
            }

            if (selected.size() > 1) {
                //check that at least one of the selected inds is in the vcf
                ArrayList<String> seqSamples = data.vcf.getMeta().getSamples();
                System.out.println("seqSample size: " + seqSamples.size());
                ArrayList<String> seqSamplesSelected = new ArrayList<String>();
                for (String seqSample : seqSamples) {
                    if (filterList.containsKey(seqSample)) {
                        seqSamplesSelected.add(seqSample);
                    }
                }

                if (seqSamplesSelected.size() > 0) {
                    fsd = new FindSegmentsDialog3(selected.size(), data.vcf.getMeta(), selectedCols);
                    fsd.setModal(true);
                    fsd.pack();
                    fsd.setVisible(true);
                    if (fsd.success()) {
                        selectedCols = fsd.getSelectedCols();
                        minMatches = fsd.getMinMatches();
                        SampleCompare sc = new SampleCompare();
                        segments = sc.compareMulti(selected);
                        if (segments.size() > 0) {
                            if (fsd.freqFilter()) {
                                File ff = new File(fsd.getFreqFile());
                                if (ff.exists()) {
                                    try {
                                        vcfData = data.vcf.getVariants(segments, minMatches, fsd.getFreqFile(), fsd.getFreqCutoff());
                                    } catch (Exception ex) {
                                        Logger.getLogger(Olorin.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                    if (vcfData != null) {
                                        variantsTable = new JTable(new vcfTableModel(vcfData, fsd.getSelectedInfo(), fsd.getFreqFile()));
                                        variantsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                                        TableColumnAdjuster tca = new TableColumnAdjuster(variantsTable);
                                        tca.setColumnDataIncluded(true);
                                        tca.adjustColumns();
                                        variantsTable.setFillsViewportHeight(true);
                                        variantsTable.setAutoCreateRowSorter(true);
                                        variantsPane.setViewportView(variantsTable);
                                        contentPanel.revalidate();
                                        contentPanel.repaint();
                                        exportVariants.setEnabled(true);
                                        exportSegments.setEnabled(true);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "Frequency File Missing.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                try {
                                    vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedInfo());
//                                    vcfData = data.vcf.getVariants(segments, minMatches, fsd.getSelectedInfo(), seqSamplesSelected);
                                } catch (IOException e1) {
                                    JOptionPane.showMessageDialog(null, "Unable to Read from VCF.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                if (vcfData != null) {
                                    EventList<Variant> variants = GlazedLists.threadSafeList(new BasicEventList<Variant>());
                                    variants.addAll(vcfData);

                                    // create a list to hold all the matcherEditors
                                    EventList matcherEditors = GlazedLists.threadSafeList(new BasicEventList());

                                    JPanel infoFilterPanel = new JPanel(new GridBagLayout());
                                    c.gridx = 0;
                                    c.gridy = 0;

                                    NumericMatcherEditor qme = new NumericMatcherEditor("Quality", 5, 0d, 10000d);
                                    matcherEditors.add(qme);
                                    infoFilterPanel.add(qme.getWidget(), c);
//                                    c.gridy = 1;
//                                    StringMatcherEditor fme = new StringMatcherEditor("Filter", 6);
//                                    matcherEditors.add(fme.getTextComponentMatcherEditor());
//                                    infoFilterPanel.add(fme.getWidget(), c);

                                    // for each of the user selected info fields create a filter
                                    for (int i = 0; i < fsd.getSelectedInfo().size(); i++) {
                                        c.gridy = i + 1;
                                        String id = fsd.getSelectedInfo().get(i);
                                        String type = data.vcf.getMeta().getInfoObjects().get(id).getType();
                                        String number = data.vcf.getMeta().getInfoObjects().get(id).getNumber();
                                        
                                        

                                        if (id.matches("CSQ")) {
                                            // create filters for the consequence field
                                        } else {
                                            if (type.matches("String")) {
                                                StringMatcherEditor sme = new StringMatcherEditor(id, i + 7);
                                                matcherEditors.add(sme.getTextComponentMatcherEditor());
                                                infoFilterPanel.add(sme.getWidget(), c);
                                            } else if (type.matches("Integer") && number.matches("1")) {  // if the info field has a number > 1 or '.' - can't create a simple filter
                                                ArrayList<Double> values = getValues(i + 7);
                                                Double min;
                                                Double max;
                                                try {
                                                    min = Collections.min(values);
                                                    max = Collections.max(values);
                                                } catch (NoSuchElementException nsee) {
                                                    min = 0d;
                                                    max = 0d;
                                                }
                                                NumericMatcherEditor nme = new NumericMatcherEditor(id, i + 7, min, max);
                                                matcherEditors.add(nme);
                                                infoFilterPanel.add(nme.getWidget(), c);
                                            } else if (type.matches("Float") && number.matches("1")) {
                                                ArrayList<Double> values = getValues(i + 7);
                                                Double min;
                                                Double max;
                                                try {
                                                    min = Collections.min(values);
                                                    max = Collections.max(values);
                                                } catch (NoSuchElementException nsee) {
                                                    min = 0d;
                                                    max = 0d;
                                                }
                                                NumericMatcherEditor nme = new NumericMatcherEditor(id, i + 7, min, max);
                                                matcherEditors.add(nme);
                                                infoFilterPanel.add(nme.getWidget(), c);
                                            } else if (type.matches("Flag")) {
                                                FlagMatcherEditor fme = new FlagMatcherEditor(id, i + 7);
                                                matcherEditors.add(fme);
                                                infoFilterPanel.add(fme.getWidget(), c);
                                            }
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

                                    FilterList filteredVariants = new FilterList(variants, new CompositeMatcherEditor(matcherEditors));
                                    SortedList sortedFilteredVariants = new SortedList(filteredVariants, null);
                                    JTable t = new JTable(new EventTableModel<Variant>(sortedFilteredVariants, new VariantTableFormat(fsd.getSelectedInfo())));
                                    t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                                    new TableComparatorChooser(t, sortedFilteredVariants, true);

                                    variantsPane.setViewportView(t);
                                    contentPanel.revalidate();
                                    contentPanel.repaint();
                                    exportVariants.setEnabled(true);
                                    exportSegments.setEnabled(true);
                                }
                            }
                            try {
                                try {
                                    drawIdeogram(segments);
                                } catch (FileFormatException ex) {
                                    Logger.getLogger(Olorin.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(Olorin.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            savePlot.setEnabled(true);
                        } else {
                            // no segments found
                            // if a table has already been created then remove it
                            variantsPane.removeAll();
                            //remove any created plots
                            exportVariants.setEnabled(false);
                            exportSegments.setEnabled(false);
                            savePlot.setEnabled(false);
                            //remove the segments drawn on the idoegrams
                            // do that here!
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No Sequenced Individuals Selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(null, "No Individuals Selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
        }
    }

    private void drawIdeogram(ArrayList<SegmentMatch> m) throws IOException, FileFormatException {
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
                stream = new GZIPInputStream(stream);
            }
        }
        if (stream == null) {
            System.out.println("cannot find resource name '" + name + "'");
        }
        db.read(new InputStreamReader(stream));

        Vector<DataSlot> slots = new Vector<DataSlot>();

        // add one panel per chromosome
        JPanel chr_panel = new JPanel();
        chr_panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        int n2 = chrNum / 2;

// chromosomes in a single row
        for (int i = 0; i < chrNum; ++i) {
            IdeogramView iv = new IdeogramView();
            iv.setChromosome(i + 1);
            iv.setIdeogramDB(db);

            // when creating the ideogram change the width of the chromosome and sharing blocks acording to the size of the window
            iv.setPreferredSize(new Dimension(Math.round(ideogramPane.getWidth() / chrNum), 300));

            if (segHash.containsKey(Integer.toString(i + 1))) {
                iv.setSegments(segHash.get(Integer.toString(i + 1)), minMatches);
            }
            c.gridx = i;
            c.gridy = 1;

            chr_panel.add(iv, c);
        }

// chromosomes in two rows        
//        for (int i = 0; i < chrNum; ++i) {
//            IdeogramView iv = new IdeogramView();
//            iv.setChromosome(i + 1);
//            iv.setIdeogramDB(db);
//            
//            // when creating the ideogram change the width of the chromosome and sharing blocks acording to the size of the window
//            
//            if (i >= n2) {
//                iv.setPreferredSize(new Dimension(Math.round(ideogramPane.getWidth()/n2), 180));
//            } else {
//                iv.setPreferredSize(new Dimension(Math.round(ideogramPane.getWidth()/n2), 300));
//            }
//            if (segHash.containsKey(Integer.toString(i+1))) {
//                iv.setSegments(segHash.get(Integer.toString(i+1)));
//            }            
//            c.gridx = i % n2;
//            c.gridy = i / n2;
//            
//            chr_panel.add(iv, c);
//        }

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
        view.addRule(new ShapeRule("Sex", "1", new SymbolSexFemale()));
        view.addRule(new ShapeRule("Sex", "2", new SymbolSexMale()));
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
            if (!s.matches(".")) {
                values.add(Double.parseDouble(s));
            }
        }
        return values;
    }
    
    private ArrayList<Integer> getIntValues(int i) {
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (Variant v : vcfData) {
            String s = (String) v.getTableArray().get(i);
            //handle cases where it is not an int such as .
            if (!s.matches(".")) {
                values.add(Integer.parseInt(s));
            }
        }
        return values;
    }

    private ArrayList<Float> getFloatValues(int i) {
        ArrayList<Float> values = new ArrayList<Float>();
        for (Variant v : vcfData) {
            String s = (String) v.getTableArray().get(i);
            //handle cases where it is not an int such as .
            if (!s.matches(".")) {
                values.add(Float.parseFloat(s));
            }
        }
        return values;
    }

    class StringMatcherEditor {

        private JPanel widget;
        private TextComponentMatcherEditor tme;

        public StringMatcherEditor(String id, final int i) {
            widget = new JPanel();
            widget.add(new JLabel(id + ":"));
            JTextField filterField = new JTextField(5);
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
    }

    private static class NumericMatcherEditor extends AbstractMatcherEditor implements ActionListener, ChangeListener {

        private JComboBox signChooser;
        private JTextField valueField;
        private JSlider slider;
        private String id;
        private int i;
        private JPanel widget;
        private ArrayList<String> selectedCols;

        private NumericMatcherEditor(String id, int i, Double min, Double max) {
            this.id = id;
            this.i = i;
            this.widget = new JPanel();

            widget.add(new JLabel(id + ":"));

            this.signChooser = new JComboBox(new Object[]{">", "<"});
            this.signChooser.addActionListener(this);
            widget.add(signChooser);

            this.valueField = new JTextField(5);
            valueField.setText("0.0");
            this.valueField.addActionListener(this);
            widget.add(valueField);

            this.slider = new JSlider(min.intValue(), max.intValue());
            this.slider.setMajorTickSpacing(max.intValue() / 2);
            this.slider.setPaintLabels(true);
            this.slider.addChangeListener(this);
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
            this.valueField.setText(Integer.toString(this.slider.getValue()));
            final String sign = (String) this.signChooser.getSelectedItem();
            final String cutoff = (String) this.valueField.getText();

            if (sign == null || cutoff == null) {
                this.fireMatchAll();
            } else {
                this.fireChanged(new NumericMatcher(i, sign, cutoff));
            }
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
                if (!value.matches(".")) {
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
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private static class FlagMatcherEditor extends AbstractMatcherEditor implements ActionListener {

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

    public static class VariantTableFormat implements TableFormat {

        private ArrayList<String> usrSelectedCols;
        private ArrayList<String> cols;

        VariantTableFormat(ArrayList<String> selectedCols) {
            usrSelectedCols = selectedCols;
            cols = new ArrayList();
            cols.add("Chromosome");
            cols.add("Position");
            cols.add("ID");
            cols.add("Ref");
            cols.add("Alt");
            cols.add("Quality");
            cols.add("Filter");
            cols.addAll(usrSelectedCols);
        }

        @Override
        public int getColumnCount() {
            return cols.size();
        }

        @Override
        public String getColumnName(int i) {
            return cols.get(i);
        }

        @Override
        public Object getColumnValue(Object e, int i) {
            Variant v = (Variant) e;
            return v.getArray(usrSelectedCols).get(i);
        }
    }
}
