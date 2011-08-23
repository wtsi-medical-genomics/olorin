import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestSampleCompare extends TestCase {

	Hashtable<String, Chromosome> samples;	

	@Before public void setUp() throws Exception {
		FlowFile flow = new FlowFile("test_files/test.22.flow");
		MapFile map   = new MapFile("test_files/test.22.map");
		flow.setPos(map.getPositions());
		flow.parseFlow(); 
		samples = flow.getSamples(); 
	}

	@Test public void testCompareMultiTwo() throws Exception {
		ArrayList<Sample> selected = new ArrayList<Sample> ();
		selected.add(new Sample("22", samples.get("5069")));
		selected.add(new Sample("22", samples.get("5065")));
		SampleCompare sc = new SampleCompare();
		Iterator<SegmentMatch> i = sc.compareMulti(selected).iterator();
		SegmentMatch match1 = i.next();
		SegmentMatch match2 = i.next();
		SegmentMatch match3 = i.next();

		assertEquals(15437138, match3.getStart());
		assertEquals(16172740, match3.getEnd());
		assertEquals("G", match3.getCodeString());
		assertEquals(2, match3.getIds().size());

		assertEquals(16041178, match1.getStart());
		assertEquals(16162813, match1.getEnd());
		assertEquals("C", match1.getCodeString());
		assertEquals(2, match1.getIds().size());

		assertEquals(16271554, match2.getStart());
		assertEquals(16317630, match2.getEnd());
		assertEquals("A", match2.getCodeString());
		assertEquals(2, match2.getIds().size());
	}

	@Test public void testCompareMulti2() throws Exception {		
		ArrayList<Sample> selected = new ArrayList<Sample> ();
		selected.add(new Sample ("22", samples.get("4038")));
		selected.add(new Sample ("22", samples.get("4019")));
		selected.add(new Sample ("22", samples.get("5062")));
		selected.add(new Sample ("22", samples.get("5061")));
		SampleCompare sc = new SampleCompare();
		Iterator<SegmentMatch> i = sc.compareMulti(selected).iterator();
		int count = 0;
		while (i.hasNext()) {
			SegmentMatch match = i.next();
			System.out.println(match.getStart() + " " + match.getEnd() + " " + match.getCodeString() + " " + match.getIds());
			count++;
		}
		assertEquals(4, count);
	}

}
