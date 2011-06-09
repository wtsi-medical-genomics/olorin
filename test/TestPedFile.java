import javax.swing.JFrame;

import org.junit.Test;

import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;

import junit.framework.TestCase;

public class TestPedFile extends TestCase {

	@Test public void testParsePed() throws Exception {
		PedFile ped = new PedFile("test_files/test.22.ped");
		
		String csvFile = ped.makeCSV();
		assertEquals("test_files/test.22.ped.csv", csvFile);
		
		Graph graph = new Graph();
		CsvGraphLoader loader = new CsvGraphLoader(csvFile, ",");
		loader.setSettings("Id", "Mid", "Fid");
		loader.load(graph);

		//Step 2
		Sugiyama s = new Sugiyama(graph);
		s.run();

		//Creates a frame
		JFrame frame = new JFrame();
		frame.setSize(800, 600);
		
		//Step 3
		GraphView2D view = new GraphView2D(s.getLayoutedGraph());		
		frame.add(view.getComponent());
		frame.setVisible(true);
	}	
}
