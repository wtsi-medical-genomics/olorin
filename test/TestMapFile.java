import java.util.ArrayList;
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
        
        // test that badly formatted positions (eg containing . or ,) give the correct warning
	
}

