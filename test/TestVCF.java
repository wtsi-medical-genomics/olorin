import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class TestVCF extends TestCase {

	private VCF vcf;
	private Hashtable<Integer, Hashtable> vcfVariants;
	
	// create a fixture to avoid duplication of code
	@Before public void setUp() throws Exception {
		vcf = new VCF("test_files/test.22.vcf.gz");
		vcfVariants = vcf.getVariants();
	}
	
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
	
	// test that the vcf is parsed ok
	@Test public void testGetVariantTotal() throws Exception {
		assertEquals(7293, vcf.getVariantTotal());
	}
	
	// filtering out common variants using Luke's 1KG freq file 
	// file format: chr start end ceu asn yri
	// i think the freq file is based on build36 and the test vcf on b37 - this needs to be fixed
	@Test public void testFlagCommon() throws Exception {	
		vcf.flagCommon("test_files/test.freq.22.compressed.gz",0.05); 
		assertTrue((Boolean) vcfVariants.get(22).get(21156252));
		assertTrue((Boolean) vcfVariants.get(22).get(23503569));
		assertTrue((Boolean) vcfVariants.get(22).get(23741878));
	}
	
	@Test public void testFlagCommonTabix() throws Exception {
		vcf.flagCommonTabix("test_files/test.freq.22.tabix.gz",0.05);
		assertTrue((Boolean) vcfVariants.get(22).get(21156252));
		assertTrue((Boolean) vcfVariants.get(22).get(23503569));
		assertTrue((Boolean) vcfVariants.get(22).get(23741878));
	}
	
	@Test public void testGetInds() throws Exception {
		// get an array of sample ids
	}
	
//	@Test public void testReturnRareVariants() throws Exception {
//		Hashtable<Integer, Hashtable> rareVariants = vcf.returnRare()
//		// return id of all the variants that are not flagged as common
//		assertEquals(1, rareVariants.size());
//		assertEquals(7293, rareVariants.get(22).size());
//
//		//if more detail on variant is required then use tabix to return details from the vcf		
//	}
	
	
	
	// create a get method that only returns a single variant - or dies
	// this should generate a variant object
	// parse the vcf line, the 9 information fields and the remanining genotypes

	
	
	
//	// Given a bgziped tabix indexed file the next two tests should pass
//	@Test public void testCompressed() throws Exception {
//		assertTrue(vcf.compressed());
//	}
//	
//	@Test public void testIndexed() throws Exception {
//		assertTrue(vcf.indexed());
//	}
//	
//	//given an uncompressed and unindexed file the next two tests should fail
//	@Test public void testCompressed() throws Exception {
//		assertFalse(vcf.compressed());
//	}
//	
//	@Test public void testIndexed() throws Exception {
//		assertFalse(vcf.indexed());
//	}
//	
//	// future feature: if the file is not compressed and indexed then 'convert' the file to make it bgzip.tabix 
//	// check if the file is sorted first if not sort it?
//	// compress with bgzip
//	// index with tabix
//		
//	//test creating a tabix object
//	@Test public void testTabixObject() throws Exception {
//		
//		//better test would be to test if the tabix object is a valid TabixReader object
//		assertTrue(vcf.tabixObject());
//		
//	}
//	
//	@Test public void testVcfFileFormat() throws Exception {
//		String expectedFileFormat = "VCFv4.0";
//		assertEquals(expectedFileFormat, vcf.getFileFormat());
//	}
//	
//	@Test public void testInfoLines() throws Exception {
//		int expectedInfoLines = 8;
//		assertEquals(expectedInfoLines, vcf.getInfoLines.size());
//	}
//	
//	@Test public void testFilterLines() throws Exception {
//		int expectedFilterLines = 0;
//		assertEquals(expectedFilterLines, vcf.getFilterLines.size());
//	}
//
//	@Test public void testFormatLines() throws Exception {
//		int expectedFormatLines = 6;
//		assertEquals(expectedFormatLines, vcf.getFormatLines.size());
//	}
//	
//	@Test public void testGetInds() throws Exception {
//		int expectedIndTotal = 104;
//		assertEquals(expectedIndTotal, vcf.getInds.size());
//	}
//	
//	// first line of the test vcf
//	@Test public void testNextLine() throws Exception {
//		int expectedLineSize = 13;
//		int expectedLineChr  = 1;
//		int expectedLinePos  = 721694;
//		
//		Vector line = vcf.nextLine();
//		assertEquals(expectedLineSize, line.size());
//		assertEquals(expectedLineChr, line.get(0));
//		assertEquals(expectedLinePos, line.get(1));
//	}
//	
//	//second line of the test vcf
//	@Test public void testAnotherLine() throws Exception {
//		int expectedLineSize = 13;
//		int expectedLineChr  = 1;
//		int expectedLinePos  = 721757;
//		
//		Vector line = vcf.nextLine();
//		assertEquals(expectedLineSize, line.size());
//		assertEquals(expectedLineChr, line.get(0));
//		assertEquals(expectedLinePos, line.get(1));
//	}

//	@Test public void testGetVariants() throws Exception {
//	int expectedChr      = 1;
//	int expectedPos1     = 1000643;
//	int expectedPos2     = 1000857;
//	int expectedPos3     = 1000894;
//	int expectedPos4     = 1000910;
//			
//	TabixReader.Iterator iter = vcf.getVariants(1, 1000000, 1001000);
//	
//	String line1 = iter.next();
//	String line2 = iter.next();
//	String line3 = iter.next();
//	String line4 = iter.next();
//	
//	String tokens1[]=line1.split("[\t]");
//	String tokens2[]=line2.split("[\t]");
//	String tokens3[]=line3.split("[\t]");
//	String tokens4[]=line4.split("[\t]");
//	
//	assertEquals(expectedChr,  Integer.parseInt(tokens1[0]));
//	assertEquals(expectedPos1, Integer.parseInt(tokens1[1]));
//	
//	assertEquals(expectedChr,  Integer.parseInt(tokens2[0]));
//	assertEquals(expectedPos2, Integer.parseInt(tokens2[1]));
//	
//	assertEquals(expectedChr,  Integer.parseInt(tokens3[0]));
//	assertEquals(expectedPos3, Integer.parseInt(tokens3[1]));
//	
//	assertEquals(expectedChr,  Integer.parseInt(tokens4[0]));
//	assertEquals(expectedPos4, Integer.parseInt(tokens4[1]));
//	
//	// make sure the iterator does not contain any more lines
//	
//}

	
}
