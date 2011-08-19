import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class TestVCF extends TestCase {

	private VCF vcf;
	private Vector<SegmentMatch> matches;
	private Vector<Variant> vcfData;
	
	@Before public void setUp() throws Exception {
		
		FlowFile flow = new FlowFile("test_files/test.22.flow");
		MapFile map = new MapFile("test_files/test.22.map");
		flow.setPos(map.getPositions());
		flow.parseFlow();
		Hashtable<String, Sample> samples = flow.getSamples();
		matches = new SampleCompare(samples.get("5069"), samples.get("5065")).getResults(); 
		vcf = new VCF("test_files/test.22.vcf.gz");
	}
	
	//create a VCF meta test to make sure the header is parsed correctly
	
	@Test public void testFailsBadVCF() {
		try {
			VCF badVCF = new VCF("test_files/bad/test.bad.vcf");
		} catch (Exception e) {
			assertEquals("VCF needs to be compressed using bgzip", e.getMessage());
		}
		
		try {
			VCF badVCF = new VCF("test_files/bad/test.bad.compressed.vcf.gz");
		} catch (Exception e) {
			assertEquals("VCF needs to be indexed using tabix", e.getMessage());		
		}
	}
	
	// future feature: if the file is not compressed and indexed then 'convert' the file to make it bgzip.tabix 
	
	@Test public void testGetVariants() throws Exception {
		vcfData = vcf.getVariants(matches, 2);
		assertEquals(15, vcfData.size());
	}
	
	@Test public void testGetFilteredVariants() throws Exception {
		vcf.loadFreqFile("test_files/test.freq.22.tabix.gz");
		vcfData = vcf.getVariants(matches, 2, 0.05);
		assertEquals(10, vcfData.size());
	}
			
}
