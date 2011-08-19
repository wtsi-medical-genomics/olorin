import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class VariantTest extends TestCase {

	// example vcf lines taken from the 1000g website	  
	// Genotype data are given for three samples, two of which are phased and the third unphased, 
	// with per sample genotype quality, depth and haplotype qualities (the latter only for the phased samples) given as well as the genotypes

	String var1;
	String var2;
	String var3;
	String var4;
	
	@Before public void setUp() throws Exception {
		var1 = "20	14370	rs6054257	G	A	29	PASS	NS=3;DP=14;AF=0.5;DB;H2	GT:GQ:DP:HQ	0|0:48:1:51,51	1|0:48:8:51,51	1/1:43:5:.,.";
		var2 = "20	17330	.	T	A	3	q10	NS=3;DP=11;AF=0.017	GT:GQ:DP:HQ	0|0:49:3:58,50	0|1:3:5:65,3	0/0:41:3";
		var3 = "20	1110696	rs6040355	A	G,T	67	PASS	NS=2;DP=10;AF=0.333,0.667;AA=T;DB	GT:GQ:DP:HQ	1|2:21:6:23,27	2|1:2:0:18,2	2/2:35:4";
		var4 = "20	1230237	.	T	.	47	PASS	NS=3;DP=13;AA=T	GT:GQ:DP:HQ	0|0:54:7:56,60	0|0:48:4:51,51	0/0:61:2";
	}

	private String vcfLine = "";
	
	@Test public void testVariant1() throws Exception {
		// a good simple SNP,
		Variant var = new Variant(var1);

		assertEquals(20, var.getChr());
		assertEquals(14370, var.getPos());
		assertEquals("rs6054257", var.getId());
		assertEquals("G", var.getRef());
		assertEquals("A", var.getAlt());
		assertEquals("29", var.getQual());
		assertEquals("PASS", var.getFilter());
		assertEquals(5, var.getInfo().size());
		assertEquals(3, var.getGenotypes().size());

	}

	@Test public void testVariant2() throws Exception {
		// a good simple SNP,
		Variant var = new Variant(var2);

		assertEquals(20, var.getChr());
		assertEquals(17330, var.getPos());
		assertEquals(".", var.getId());
		assertEquals("T", var.getRef());
		assertEquals("A", var.getAlt());
		assertEquals("3", var.getQual());
		assertEquals("q10", var.getFilter());
		assertEquals(3, var.getInfo().size());
		assertEquals(3, var.getGenotypes().size());

	}

	@Test public void testVariant3() throws Exception {
		// a good simple SNP,
		Variant var = new Variant(var3);

		assertEquals(20, var.getChr());
		assertEquals(1110696, var.getPos());
		assertEquals("rs6040355", var.getId());
		assertEquals("A", var.getRef());
		assertEquals("G,T", var.getAlt());
		assertEquals("67", var.getQual());
		assertEquals("PASS", var.getFilter());
		assertEquals(5, var.getInfo().size());
		assertEquals(3, var.getGenotypes().size());

	}

	@Test public void testVariant4() throws Exception {
		// a good simple SNP,
		Variant var = new Variant(var4);

		assertEquals(20, var.getChr());
		assertEquals(1230237, var.getPos());
		assertEquals(".", var.getId());
		assertEquals("T", var.getRef());
		assertEquals(".", var.getAlt());
		assertEquals("47", var.getQual());
		assertEquals("PASS", var.getFilter());
		assertEquals(3, var.getInfo().size());
		assertEquals(3, var.getGenotypes().size());

	}
}
