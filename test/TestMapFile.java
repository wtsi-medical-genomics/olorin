import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestMapFile extends TestCase {
	
	@Test public void testParseMap() throws Exception {
		MapFile map = new MapFile("test_files/test.22.map");
		Vector<Integer> pos = map.getPositions();
		assertEquals(74, pos.size());	
		assertEquals(15437138, pos.firstElement().intValue());
		assertEquals(16317630, pos.lastElement().intValue());
	}
	
}

