
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TestVCFMeta extends TestCase {

    VCFMeta metaInfo = new VCFMeta();

    @Before
    public void setUp() throws Exception {
        // exmaple data taken from the 1000g site
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
        metaInfo.add("##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence of the ALT alleles from Ensembl 64 VEP v2.2\">");
        metaInfo.add("##FILTER=<ID=q10,Description=\"Quality below 10\">");
        metaInfo.add("##FILTER=<ID=s50,Description=\"Less than 50% of samples have data\">");
        metaInfo.add("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
        metaInfo.add("##FORMAT=<ID=GQ,Number=1,Type=Integer,Description=\"Genotype Quality\">");
        metaInfo.add("##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Read Depth\">");
        metaInfo.add("##FORMAT=<ID=HQ,Number=2,Type=Integer,Description=\"Haplotype Quality\">");
        metaInfo.add("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	NA00001	NA00002	NA00003");
    }

    @Test
    public void testFileFormat() throws Exception {
        assertEquals("VCFv4.1", metaInfo.getFileFormat());
    }

    // not bothering with date,source,reference file,contig and phasing at the moment
    @Test
    public void testINFO() throws Exception {
        assertEquals(9, metaInfo.getInfo().size());
        assertEquals("NS", metaInfo.getInfo().get(2).get("ID"));
        assertEquals("1", metaInfo.getInfo().get(2).get("Number"));
        assertEquals("Integer", metaInfo.getInfo().get(2).get("Type"));
        assertEquals("Number of Samples With Data", metaInfo.getInfo().get(2).get("Description"));
        // all the other info lines
    }

    @Test
    public void testCSQ() throws Exception {
        assertTrue(metaInfo.hasCSQ());
        assertEquals("SANGER", metaInfo.getCsqType());
    }

    @Test
    public void testGetSampleNames() throws Exception {
        assertEquals(3, metaInfo.getSamples().size());
        assertEquals("NA00001", metaInfo.getSamples().get(0));
        assertEquals("NA00002", metaInfo.getSamples().get(1));
        assertEquals("NA00003", metaInfo.getSamples().get(2));
    }

    @Test
    public void testVepCSQ() throws Exception {
        VCFMeta metaInfoVep = new VCFMeta();
        metaInfoVep.add("##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|PolyPhen|SIFT|Condel\">");
        assertTrue(metaInfo.hasCSQ());
        assertEquals("VEP", metaInfoVep.getCsqType());  
        HashMap<String, Integer> csqIndex = metaInfoVep.getCsqIndex();        
        assertEquals((Integer) 1, csqIndex.get("Gene"));
        assertEquals((Integer) 2,    csqIndex.get("Feature"));
        assertEquals((Integer) 4,    csqIndex.get("Consequence"));
        assertEquals((Integer) 11,   csqIndex.get("PolyPhen"));
        assertEquals((Integer) 12,   csqIndex.get("SIFT"));
        assertEquals((Integer) 13,   csqIndex.get("Condel"));
        assertEquals((Integer) null, csqIndex.get("false"));
    }
    
    
    @Test
    public void testCSQFail() throws Exception {
        // test an ungeconised CSQ string
        VCFMeta metaInfoVep = new VCFMeta();
        metaInfoVep.add("##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type\">");
    }
    
    @Test
    public void testCSQFail2() throws Exception {
        // test an ungeconised CSQ string format
        VCFMeta metaInfoVep = new VCFMeta();
        metaInfoVep.add("##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP.\">");
    }
    
}
