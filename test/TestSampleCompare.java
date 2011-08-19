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
	
	@Test public void sampleCompare() throws Exception {
		
		Sample ind1 = samples.get("5069");
		Sample ind2 = samples.get("5065");
		
		SampleCompare sc = new SampleCompare(ind1, ind2);
		
		Vector<SegmentMatch> matches = sc.getResults();
		
		assertEquals(3, matches.size());
		
		Enumeration<SegmentMatch> e = matches.elements();
		SegmentMatch match1 = e.nextElement();
		SegmentMatch match2 = e.nextElement();
		SegmentMatch match3 = e.nextElement();
		
		assertEquals(16041178, match1.getStart());
		assertEquals(16162813, match1.getEnd());
		assertEquals('C', match1.getCode());
		assertEquals("5069", match1.getIds().get(0));
		assertEquals("5065", match1.getIds().get(1));
		
		assertEquals(16271554, match2.getStart());
		assertEquals(16317630, match2.getEnd());
		assertEquals('A', match2.getCode());
		assertEquals("5069", match2.getIds().get(0));
		assertEquals("5065", match2.getIds().get(1));
		
		assertEquals(15437138, match3.getStart());
		assertEquals(16172740, match3.getEnd());
		assertEquals('G', match3.getCode());
		assertEquals("5069", match3.getIds().get(0));
		assertEquals("5065", match3.getIds().get(1));
	}
	
	@Test public void compareList() throws Exception {
		
		Vector<Sample> selected = new Vector<Sample> ();
		selected.add(samples.get("5069"));
		selected.add(samples.get("5065"));
		selected.add(samples.get("5064"));
		selected.add(samples.get("4037"));
		
		SampleComapre sc = new SampleCompare(selected);
		
		Vector<SegmentMatch> matches = sc.getResults();
		
		assertEquals(1, matches.size());
		
		Enumeration<SegmentMatch> e = matches.elements();
		SegmentMatch match = e.nextElement();
		
		assertEquals(16041178, match.getStart());
		assertEquals(16162813, match.getEnd());
		assertEquals('C', match.getCode());
		assertEquals("5069", match.getIds().get(0));
		assertEquals("5065", match.getIds().get(1));
		
		
		// add up the size of the genome selected and warn the user if it is too high
		
		
	}

//	@Test public void matchCompare() throws Exception {
//	
//	// test comparing sets of matched segments to create an overall set
//	
//			
//}
	
//	@Test public void multipleCompare() throws Exception {
//		
//		// test comparing multiple samples
//		
//		// compare the first two
//		
//		// compare the matched segments to the next sample
//		
//		// and so on
//				
//	}
}
