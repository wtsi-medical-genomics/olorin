import javax.swing.JFrame;

import org.junit.Test;

import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;

import junit.framework.TestCase;

public class TestPedFile extends TestCase {

	@Test public void testParsePed() throws Exception {
		PedFile ped = new PedFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.ped");
		
		String csvFile = ped.makeCSV();
		assertEquals("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.ped.csv", csvFile);
		
		// read the csv file and make sure it is what we expect
                
	}	
}
