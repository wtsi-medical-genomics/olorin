import javax.swing.*;
import javax.swing.border.LineBorder;

import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;
import pedviz.view.NodeEvent;
import pedviz.view.NodeListener;
import pedviz.view.rules.ColorRule;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;
import pedviz.view.symbols.SymbolSexUndesignated;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

public class GUI extends JFrame implements ActionListener {
	
	JFileChooser jfc;
	ProgressMonitor pm;
	
	JPanel pedigreePanel;
	JPanel contentPanel;
	JTextArea logPanel;
	
	// for initial vcf filtering
	String freqFile = "test_files/test.freq.22.compressed.gz";
	Double commonCutoff = 0.05;
	
	private JMenu fileMenu;
	private JMenuItem openDirectory;
		
	public static void main(String[] args) {

		new GUI();

	}

	GUI() {
		super("Oberon");

		jfc = new JFileChooser("user.dir");
		JMenuBar mb = new JMenuBar();

		int menumask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		fileMenu = new JMenu("File");
		openDirectory = new JMenuItem("Open Directory");
		openDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,menumask));
		openDirectory.addActionListener(this);
		fileMenu.add(openDirectory);
		
		mb.add(fileMenu);
		setJMenuBar(mb);

		pedigreePanel = new JPanel();
		pedigreePanel.setPreferredSize(new Dimension(350, 350));
		pedigreePanel.setBorder(new LineBorder(Color.BLACK));
		pedigreePanel.setBackground(Color.WHITE);
		pedigreePanel.setLayout(new BoxLayout(pedigreePanel, BoxLayout.Y_AXIS));
		
		JPanel variantsPanel = new JPanel();
		
		variantsPanel.setPreferredSize(new Dimension(700, 350));
		variantsPanel.setBorder(new LineBorder(Color.BLACK));
		variantsPanel.setBackground(Color.WHITE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.add(pedigreePanel);
		mainPanel.add(variantsPanel);
		
		logPanel = new JTextArea();
		logPanel.setEditable(false);
		logPanel.setFont(new Font("Monospaced",Font.PLAIN,12));
		logPanel.setLineWrap(true);
		logPanel.setWrapStyleWord(true);
        JScrollPane scrollzor = new JScrollPane(logPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollzor.setSize(new Dimension(1050,100));
       
        contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(mainPanel);
		contentPanel.add(logPanel);
		
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
		if (command.equals("Open Directory")) {
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {						
						// create a data directory class to hold all these files like evoker
						File directory = new File(jfc.getSelectedFile().getAbsolutePath());
						File[] vcfFiles  = directory.listFiles(new ExtensionFilter(".vcf.gz"));
						File[] flowFiles = directory.listFiles(new ExtensionFilter(".flow"));
						File[] pedFiles  = directory.listFiles(new ExtensionFilter(".ped"));
				        
						if (vcfFiles.length > 1) {
							printLog("Error: multiple vcf files found");
						} else if (vcfFiles.length < 1) {
							printLog("Error: no vcf file found");	
						} else {
							File vcfFile = vcfFiles[0];
							VCF vcf = null;
							try {
								vcf = new VCF(vcfFile.getAbsolutePath());
							} catch (Exception e) {
								printLog(e.getMessage());
							}
							if (vcf.open) {
								printLog("Found VCF: " + vcfFile.getAbsolutePath());
								printLog("Processed VCF: found " + vcf.getVariantTotal() + " variants");
								vcf.flagCommon(freqFile, commonCutoff);
								printLog("Removed " + vcf.getFlaggedTotal() + " common variants (" + commonCutoff + ")");
							}
						}

						if (flowFiles.length > 1) {
							printLog("Error: multiple flow files found");
						} else if (flowFiles.length < 1) {
							printLog("Error: no ped file found");
						} else {
							File flowFile = flowFiles[0];
							FlowFile flow = new FlowFile(flowFile.getAbsolutePath());
							printLog("Found flow: " + flowFile.getAbsolutePath());
						}
						
						if (pedFiles.length > 1) {
							printLog("Error: multiple ped files found");
						} else if (pedFiles.length < 1) {
							printLog("Error: no ped file found");
						} else {
							File pedFile = pedFiles[0];
							PedFile ped = new PedFile(pedFile.getAbsolutePath());
							printLog("Found ped: " + pedFile.getAbsolutePath());
							String csvFile = ped.makeCSV();
							drawPedigree(csvFile);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} finally {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}
	
	private void drawPedigree(String csvFile) {
		
		Graph graph = new Graph();
		CsvGraphLoader loader = new CsvGraphLoader(csvFile, ",");
		loader.setSettings("Id", "Mid", "Fid");
		loader.load(graph);
		
		Sugiyama s = new Sugiyama(graph);
		s.run();
		
		GraphView2D view = new GraphView2D(s.getLayoutedGraph());
		view.addRule(new ColorRule("aff", "1",  Color.black));
		view.addRule(new ColorRule("aff", "2",  Color.red));
		view.addRule(new ShapeRule("Sex",  "1",  new SymbolSexFemale()));
		view.addRule(new ShapeRule("Sex",  "2",  new SymbolSexMale()));
		view.addRule(new ShapeRule("Sex",  "-1", new SymbolSexUndesignated()));
		view.setSelectionEnabled(true);
		view.addNodeListener(
				new NodeListener() {
					public void onNodeEvent(NodeEvent event) {
						switch (event.getType()) {
						case NodeEvent.DESELECTED:
							printLog("Node deselected");
							break;
						case NodeEvent.SELECTED:
							printLog("Node selected");
							break;
						}
					}
				}	
		);
		
		pedigreePanel.add(view.getComponent());
		contentPanel.revalidate();
		contentPanel.repaint();
	}
	
	public void printLog(String text){
		logPanel.append(text+"\n");
    }
	
	class ExtensionFilter implements FilenameFilter{

		String extension;

		ExtensionFilter(String extension){
			this.extension = extension;
		}

		public boolean accept(File file, String string) {
			return string.endsWith(extension);
		}
	}
}
