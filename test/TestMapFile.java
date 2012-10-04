import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestMapFile extends TestCase {
	
	@Test public void testParseMap() throws Exception {
            MapFile map = new MapFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.22.map", "22");
            ArrayList<Integer> pos = map.getPositions();
            assertEquals(74, pos.size());	
            assertEquals(15437138, pos.get(0).intValue());
            assertEquals(16317630, pos.get(73).intValue());
	}
        
        @Test public void testMultiChrMap() throws Exception {
            MapFile map = new MapFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/fam88.map");
            HashMap<String, ArrayList<Integer>> positionsByChromosome = map.getPositionsByChromosome();
            assertEquals(22, positionsByChromosome.size());
            
            // test the number of snps for each chromosome
            assertEquals(2776, positionsByChromosome.get("1").size());
            assertEquals(2751, positionsByChromosome.get("2").size());
            assertEquals(2440, positionsByChromosome.get("3").size());
            assertEquals(1971, positionsByChromosome.get("4").size());
            assertEquals(2077, positionsByChromosome.get("5").size());
            assertEquals(2268, positionsByChromosome.get("6").size());
            assertEquals(1814, positionsByChromosome.get("7").size());
            assertEquals(2031, positionsByChromosome.get("8").size());
            assertEquals(1663, positionsByChromosome.get("9").size());
            assertEquals(1688, positionsByChromosome.get("10").size());
            assertEquals(1671, positionsByChromosome.get("11").size());
            assertEquals(1590, positionsByChromosome.get("12").size());
            assertEquals(1289, positionsByChromosome.get("13").size());
            assertEquals(1132, positionsByChromosome.get("14").size());
            assertEquals(1013, positionsByChromosome.get("15").size());
            assertEquals(1065, positionsByChromosome.get("16").size());
            assertEquals(1067, positionsByChromosome.get("17").size());
            assertEquals(1127, positionsByChromosome.get("18").size());
            assertEquals(798, positionsByChromosome.get("19").size());
            assertEquals(1016, positionsByChromosome.get("20").size());
            assertEquals(632, positionsByChromosome.get("21").size());
            assertEquals(617, positionsByChromosome.get("22").size());
            
            //test some of the position values
//            assertEquals(1111657, positionsByChromosome.get("1").get(0).intValue());

        }
        
        // test that badly formatted positions (eg containing . or ,) give the correct warning
	
}

