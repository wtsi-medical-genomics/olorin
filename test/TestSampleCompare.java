import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestSampleCompare extends TestCase {

	HashMap<String, Chromosome> samples;	

	@Before public void setUp() throws Exception {
		FlowFile flow = new FlowFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.22.flow", "22");
		MapFile map   = new MapFile("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test.22.map", "22");
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
		int count = 0;
		while (i.hasNext()) {
			SegmentMatch match = i.next();
			count++;
			if (match.getCodeString().equals("G")) {
				assertEquals(15437138, match.getStart());
				assertEquals(16172740, match.getEnd());
				assertEquals(2, match.getIds().size());
			} else if (match.getCodeString().equals("C")) {
				assertEquals(16041178, match.getStart());
				assertEquals(16162813, match.getEnd());
				assertEquals(2, match.getIds().size());
			} else if (match.getCodeString().equals("A")) {
				assertEquals(16271554, match.getStart());
				assertEquals(16317630, match.getEnd());
				assertEquals(2, match.getIds().size());
			}
			
		}
		assertEquals(3, count);
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
			count++;
		}
		assertEquals(4, count);

		// test running compareMulti again after adding a sample
		selected.add(new Sample ("22", samples.get("5075")));
		System.out.println(selected.size());
		Iterator<SegmentMatch> j = sc.compareMulti(selected).iterator();
		int count2 = 0;
		while (j.hasNext()) {
			SegmentMatch match = j.next();
			count2++;
		}
		assertEquals(6, count2);
		
	}

}
