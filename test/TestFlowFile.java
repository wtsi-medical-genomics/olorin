import org.junit.Test;

import junit.framework.TestCase;


public class TestFlowFile extends TestCase {

	@Test public void testParseFlow() throws Exception {
		FlowFile flow = new FlowFile("test.flow");
		
		assertEquals(16, flow.getIndNum());
	
		// get the number of markers
//		assertEquals(74, flow.getMatHap().size());
//		assertEquals(74, flow.getPatHap().size());
		
		// get a list of matching segements from two inds
		flow.findMatchingSegments("3002","3502");
				
//		assertEquals(3002, flow.getFounderCode('A'));
//		assertEquals(3002, flow.getFounderCode('B'));
//		assertEquals(3502, flow.getFounderCode('C'));
//		assertEquals(3502, flow.getFounderCode('D'));
//		assertEquals(4510, flow.getFounderCode('E'));
//		assertEquals(4510, flow.getFounderCode('F'));
//		assertFalse(flow.getFounderCode('X'));
	}	
}
