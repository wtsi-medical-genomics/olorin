import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestFlowFile extends TestCase {

	FlowFile flow;
	
	@Before public void setUp() throws Exception {
		flow = new FlowFile("test_files/test.22.flow");
		MapFile map = new MapFile("test_files/test.22.map");
		flow.setPos(map.getPositions());
	}
	
	@Test public void testParseFlow() throws Exception {
		
		flow.parseFlow();
		Hashtable<String, Chromosome> samples = flow.getSamples();
		
		assertEquals(16, samples.size());
		
		Chromosome ind1 = samples.get("3000");
		
		int matSegNum = ind1.getMatSegNum();
		int patSegNum = ind1.getPatSegNum();
		
		Iterator<Segment> ind1Mat = ind1.getMatIt();
		Iterator<Segment> ind1Pat = ind1.getPatIt();
		
		Segment ind1MatSeg1 = ind1Mat.next();
		Segment ind1PatSeg1 = ind1Pat.next();
		
		int matStart = ind1MatSeg1.getStart();
		int matEnd   = ind1MatSeg1.getEnd();
		String matCode = new String(ind1MatSeg1.getCode());
		
		int patStart = ind1PatSeg1.getStart();
		int patEnd   = ind1PatSeg1.getEnd();
		String patCode = new String(ind1PatSeg1.getCode());
		
		assertEquals(1, matSegNum);
		assertEquals(1, patSegNum);
		
		assertEquals(15437138, matStart);
		assertEquals(16317630, matEnd);
		assertEquals("A", matCode);
		
		assertEquals(15437138, patStart);
		assertEquals(16317630, patEnd);
		assertEquals("B", patCode);
		
		Chromosome ind2 = samples.get("5065");
		
		int matSegNum2 = ind2.getMatSegNum();
		int patSegNum2 = ind2.getPatSegNum();
		
		Iterator<Segment> ind2Mat = ind2.getMatIt();
		Iterator<Segment> ind2Pat = ind2.getPatIt();
		
		Segment ind2MatSeg1 = ind2Mat.next();
		Segment ind2MatSeg2 = ind2Mat.next();
		Segment ind2MatSeg3 = ind2Mat.next();
		Segment ind2PatSeg1 = ind2Pat.next();
		
		int matStart2_1 = ind2MatSeg1.getStart();
		int matEnd2_1   = ind2MatSeg1.getEnd();
		byte[] matCode2_1 = ind2MatSeg1.getCode();
		
		int matStart2_2 = ind2MatSeg2.getStart();
		int matEnd2_2   = ind2MatSeg2.getEnd();
		byte[] matCode2_2 = ind2MatSeg2.getCode();
		
		int matStart2_3 = ind2MatSeg3.getStart();
		int matEnd2_3   = ind2MatSeg3.getEnd();
		byte[] matCode2_3 = ind2MatSeg3.getCode();
		
		int patStart2 = ind2PatSeg1.getStart();
		int patEnd2   = ind2PatSeg1.getEnd();
		byte[] patCode2 = ind2PatSeg1.getCode();
						
		assertEquals(3, matSegNum2);
		assertEquals(1, patSegNum2);
		
		assertEquals(15437138, matStart2_1);
		assertEquals(16003408, matEnd2_1);
		assertEquals("A", new String(matCode2_1));
		
		assertEquals(16041178, matStart2_2);
		assertEquals(16162813, matEnd2_2);
		assertEquals("C", new String(matCode2_2));
		
		assertEquals(16163945, matStart2_3);
		assertEquals(16317630, matEnd2_3);
		assertEquals("A", new String(matCode2_3));
		
		assertEquals(15437138, patStart2);
		assertEquals(16317630, patEnd2);
		assertEquals("G", new String(patCode2));
	}	
}
