import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestSampleCompare extends TestCase {

	Hashtable<String, Sample> samples;	
	
	@Before public void setUp() throws Exception {
		FlowFile flow = new FlowFile("test_files/test.22.flow");
		MapFile map = new MapFile("test_files/test.22.map");
		flow.setPos(map.getPositions());
		flow.parseFlow();
		samples = flow.getSamples();
	}
	
	@Test public void testParseFlow() throws Exception {
		
		Sample ind1 = samples.get("5069");
		Sample ind2 = samples.get("5065");
		
		SampleCompare sc = new SampleCompare(ind1, ind2);
		
		Vector<Segment> matches = sc.getResults();
		
		assertEquals(3, matches.size());
		
		Enumeration<Segment> e = matches.elements();
		Segment match1 = e.nextElement();
		Segment match2 = e.nextElement();
		Segment match3 = e.nextElement();
		
		assertEquals(16041178, match1.getStart());
		assertEquals(16162813, match1.getEnd());
		assertEquals('C', match1.getCode());
		
		assertEquals(16271554, match2.getStart());
		assertEquals(16317630, match2.getEnd());
		assertEquals('A', match2.getCode());
		
		assertEquals(15437138, match3.getStart());
		assertEquals(16172740, match3.getEnd());
		assertEquals('G', match3.getCode());
		
	}
}
