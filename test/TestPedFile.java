
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import javax.swing.JFrame;

import org.junit.Test;

import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;

import junit.framework.TestCase;

public class TestPedFile extends TestCase {

    @Test
    public void testParsePed() throws Exception {
        PedFile ped = new PedFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.ped");

        HashMap<String, Integer> vcfSampleHash = new HashMap<String, Integer>();
        vcfSampleHash.put("4010", 1);
        String csvFile = ped.makeCSV(vcfSampleHash);
        assertEquals("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.ped.csv", csvFile);

        BufferedReader pfr = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
        String header = pfr.readLine();
        String line = pfr.readLine();
        String values[] = line.split("\\,");        
        assertEquals(values[0], "4010");
        assertEquals(values[1], "3502");
        assertEquals(values[2], "3002");
        assertEquals(values[3], "2");
        assertEquals(values[4], "2");
        assertEquals(values[5], "Yes");                
    }
}
