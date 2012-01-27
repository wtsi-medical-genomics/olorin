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
		metaInfo.add("##contig=<ID=20,length=62435964,assembly=B36,md5=f126cdf8a6e0c7f379d618ff66beb2da,species=\"Homo sapiens\",taxonomy=x>");
		metaInfo.add("##phasing=partial");
		metaInfo.add("##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">");
		metaInfo.add("##INFO=<ID=DP,Number=1,Type=Integer,Description=\"Total Depth\">");
		metaInfo.add("##INFO=<ID=AF,Number=A,Type=Float,Description=\"Allele Frequency\">");
		metaInfo.add("##INFO=<ID=AA,Number=1,Type=String,Description=\"Ancestral Allele\">");
		metaInfo.add("##INFO=<ID=DB,Number=0,Type=Flag,Description=\"dbSNP membership, build 129\">");
		metaInfo.add("##INFO=<ID=H2,Number=0,Type=Flag,Description=\"HapMap2 membership\">");
		metaInfo.add("##FILTER=<ID=q10,Description=\"Quality below 10\">");
		metaInfo.add("##FILTER=<ID=s50,Description=\"Less than 50% of samples have data\">");
		metaInfo.add("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
		metaInfo.add("##FORMAT=<ID=GQ,Number=1,Type=Integer,Description=\"Genotype Quality\">");
		metaInfo.add("##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Read Depth\">");
		metaInfo.add("##FORMAT=<ID=HQ,Number=2,Type=Integer,Description=\"Haplotype Quality\">");
		metaInfo.add("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	NA00001	NA00002	NA00003");
	}
	
	@Test public void testFileFormat() throws Exception {
		assertEquals("VCFv4.1", metaInfo.getFileFormat());
	}
	
	// not bothering with date,source,reference file,contig and phasing at the moment
	
	@Test public void testINFO() throws Exception {
		assertEquals(6, metaInfo.getInfo().size());
		assertEquals("NS", metaInfo.getInfo().get(0).get("ID"));
		assertEquals("1", metaInfo.getInfo().get(0).get("Number"));
		assertEquals("Integer", metaInfo.getInfo().get(0).get("Type"));
		assertEquals("Number of Samples With Data", metaInfo.getInfo().get(0).get("Description"));
		// all the other info lines
	}

	@Test public void testFILTER() throws Exception {
		assertEquals(2, metaInfo.getFilter().size());
		assertEquals("q10", metaInfo.getFilter().get(0).get("ID"));
		assertEquals("Quality below 10", metaInfo.getFilter().get(0).get("Description"));
		assertEquals("s50", metaInfo.getFilter().get(1).get("ID"));
		assertEquals("Less than 50% of samples have data", metaInfo.getFilter().get(1).get("Description"));
	}

	@Test public void testFORMAT() throws Exception {
		assertEquals(4, metaInfo.getFormat().size());
		assertEquals("GT", metaInfo.getFormat().get(0).get("ID"));
		assertEquals("1", metaInfo.getFormat().get(0).get("Number"));
		assertEquals("String", metaInfo.getFormat().get(0).get("Type"));
		assertEquals("Genotype", metaInfo.getFormat().get(0).get("Description"));
		// all the other format lines
	}
	
	@Test public void testGetSampleNames() throws Exception {
		assertEquals(3, metaInfo.getSamples().size());
		assertEquals("NA00001", metaInfo.getSamples().get(0));
		assertEquals("NA00002", metaInfo.getSamples().get(1));
		assertEquals("NA00003", metaInfo.getSamples().get(2));
	}
}

