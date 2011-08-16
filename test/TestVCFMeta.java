import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TestVCFMeta extends TestCase {

	
	VCFMeta metaInfo = new VCFMeta();
	
	@Before public void setUp() throws Exception {
		// load exmaple data taken from the 1000g site
		metaInfo.add("##fileformat=VCFv4.1");
		metaInfo.add("##fileDate=20090805");
		metaInfo.add("##source=myImputationProgramV3.1");
		metaInfo.add("##reference=file:///seq/references/1000GenomesPilot-NCBI36.fasta");
		metaInfo.add("##contig=<ID=20,length=62435964,assembly=B36,md5=f126cdf8a6e0c7f379d618ff66beb2da,species="Homo sapiens",taxonomy=x>");
		metaInfo.add("##phasing=partial");
		metaInfo.add("##INFO=<ID=NS,Number=1,Type=Integer,Description="Number of Samples With Data">");
		metaInfo.add("##INFO=<ID=DP,Number=1,Type=Integer,Description="Total Depth">");
		metaInfo.add("##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency">");
		metaInfo.add("##INFO=<ID=AA,Number=1,Type=String,Description="Ancestral Allele">");
		metaInfo.add("##INFO=<ID=DB,Number=0,Type=Flag,Description="dbSNP membership, build 129">");
		metaInfo.add("##INFO=<ID=H2,Number=0,Type=Flag,Description="HapMap2 membership">");
		metaInfo.add("##FILTER=<ID=q10,Description="Quality below 10">");
		metaInfo.add("##FILTER=<ID=s50,Description="Less than 50% of samples have data">");
		metaInfo.add("##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">");
		metaInfo.add("##FORMAT=<ID=GQ,Number=1,Type=Integer,Description="Genotype Quality">");
		metaInfo.add("##FORMAT=<ID=DP,Number=1,Type=Integer,Description="Read Depth">");
		metaInfo.add("##FORMAT=<ID=HQ,Number=2,Type=Integer,Description="Haplotype Quality">");
		metaInfo.add("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	NA00001	NA00002	NA00003");
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test public void testFileFormat() throws Exception {
		
		
	}
	
	@Test public void testINFO() throws Exception {
		VCFMeta meta = new VCFMeta(testINFO);
		assertEquals("INFO", meta.getMetaType());
		assertEquals("H2", meta.getID());
		assertEquals(0, meta.getNumber());
		assertEquals("Flag", meta.getType());
		assertEquals("\"HapMap2 membership\"", meta.getDescription());
	}

	@Test public void testFILTER() throws Exception {
		VCFMeta meta = new VCFMeta(testFILTER);
		assertEquals("FILTER", meta.getMetaType());
		assertEquals("q10", meta.getID());
		assertEquals("\"Quality below 10\"", meta.getDescription());
	}

	@Test public void testFORMAT() throws Exception {
		VCFMeta meta = new VCFMeta(testFORMAT);
		assertEquals("FORMAT", meta.getMetaType());
		assertEquals("GT", meta.getID());
		assertEquals(1, meta.getNumber());
		assertEquals("String", meta.getType());
		assertEquals("\"Genotype\"", meta.getDescription());
	}
}
