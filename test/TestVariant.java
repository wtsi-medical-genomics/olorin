
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TestVariant extends TestCase {

    // example vcf lines taken from the 1000g website	  
    // Genotype data are given for three samples, two of which are phased and the third unphased, 
    // with per sample genotype quality, depth and haplotype qualities (the latter only for the phased samples) given as well as the genotypes
    // need to take example lines from a UK10K pipeline VCF
    // missing genotypes
    String var1 = "20	14370	rs6054257	G	A	29	PASS	NS=3;DP=14;AF=0.5;DB;H2	GT:GQ:DP:HQ	0|0:48:1:51,51	1|0:48:8:51,51	1/1:43:5:.,.";
    String var2 = "20	17330	.	T	A	3	q10	NS=3;DP=11;AF=0.017	GT:GQ:DP:HQ	0|0:49:3:58,50	0|1:3:5:65,3	0/0:41:3";
    String var3 = "CHR20	1110696	rs6040355	A	G,T	67	PASS	NS=2;DP=10;AF=0.333,0.667;AA=T;DB	GT:GQ:DP:HQ	1|2:21:6:23,27	2|1:2:0:18,2	2/2:35:4";
    String var4 = "chr20	1230237	.	T\t.	47	PASS	NS=3;DP=13;AA=T	GT:GQ:DP:HQ	0|0:54:7:56,60	0|0:48:4:51,51	0/0:61:2";

    ArrayList<String> selectedCols;
    ArrayList<Integer> selectedInds;

    @Before
    public void setUp() throws Exception {
        selectedCols = new ArrayList();
        selectedCols.add("NS");
        selectedCols.add("DP");
        selectedCols.add("AF");
        selectedCols.add("DB");
        selectedCols.add("H2");
        selectedCols.add("CSQ");

        selectedInds = new ArrayList();
        selectedInds.add(0);
        selectedInds.add(1);
        selectedInds.add(2);
    }
    private String vcfLine = "";

    @Test
    public void testVariant1() throws Exception {
        // a good simple SNP,        
        Variant var = new Variant(var1, selectedCols, selectedInds);
        //required fields
        assertEquals(20, var.getTableArray().get(0));
        assertEquals(14370, var.getTableArray().get(1));
        assertEquals("rs6054257", var.getTableArray().get(2));
        assertEquals("G", var.getTableArray().get(3));
        assertEquals("A", var.getTableArray().get(4));
        assertEquals("29", var.getTableArray().get(5));
        assertEquals("PASS", var.getTableArray().get(6));
        //Genotypes
        assertEquals("G G", var.getTableArray().get(7));
        assertEquals("A G", var.getTableArray().get(8));
        assertEquals("A A", var.getTableArray().get(9));
        //Info fields        
        assertEquals("3", var.getTableArray().get(10));
        assertEquals("14", var.getTableArray().get(11));
        assertEquals("0.5", var.getTableArray().get(12));
        assertEquals("TRUE", var.getTableArray().get(13));
        assertEquals("TRUE", var.getTableArray().get(14));
    }

    @Test
    public void testVariant2() throws Exception {
        // a good simple SNP,
        Variant var = new Variant(var2, selectedCols, selectedInds);

        assertEquals(20, var.getTableArray().get(0));
        assertEquals(17330, var.getTableArray().get(1));
        assertEquals(".", var.getTableArray().get(2));
        assertEquals("T", var.getTableArray().get(3));
        assertEquals("A", var.getTableArray().get(4));
        assertEquals("3", var.getTableArray().get(5));
        assertEquals("q10", var.getTableArray().get(6));
        //Genotypes
        assertEquals("T T", var.getTableArray().get(7));
        assertEquals("T A", var.getTableArray().get(8));
        assertEquals("T T", var.getTableArray().get(9));
        //Info fields        
        assertEquals("3", var.getTableArray().get(10));
        assertEquals("11", var.getTableArray().get(11));
        assertEquals("0.017", var.getTableArray().get(12));
        assertEquals(null, var.getTableArray().get(13));
        assertEquals(null, var.getTableArray().get(14));
    }

    @Test
    public void testVariant3() throws Exception {
        // a good simple SNP,
        Variant var = new Variant(var3, selectedCols, selectedInds);

        assertEquals(20, var.getTableArray().get(0));
        assertEquals(1110696, var.getTableArray().get(1));
        assertEquals("rs6040355", var.getTableArray().get(2));
        assertEquals("A", var.getTableArray().get(3));
        assertEquals("G,T", var.getTableArray().get(4));
        assertEquals("67", var.getTableArray().get(5));
        assertEquals("PASS", var.getTableArray().get(6));
        //Genotypes
        assertEquals("G T", var.getTableArray().get(7));
        assertEquals("T G", var.getTableArray().get(8));
        assertEquals("T T", var.getTableArray().get(9));
        //Info fields        
        assertEquals("2", var.getTableArray().get(10));
        assertEquals("10", var.getTableArray().get(11));
        assertEquals("0.333,0.667", var.getTableArray().get(12));
        assertEquals("TRUE", var.getTableArray().get(13));
        assertEquals(null, var.getTableArray().get(14));
    }

    @Test
    public void testVariant4() throws Exception {
        // a good simple SNP,
        Variant var = new Variant(var4, selectedCols, selectedInds);

        assertEquals(20, var.getTableArray().get(0));
        assertEquals(1230237, var.getTableArray().get(1));
        assertEquals(".", var.getTableArray().get(2));
        assertEquals("T", var.getTableArray().get(3));
        assertEquals(".", var.getTableArray().get(4));
        assertEquals("47", var.getTableArray().get(5));
        assertEquals("PASS", var.getTableArray().get(6));
        //Genotypes
        assertEquals("T T", var.getTableArray().get(7));
        assertEquals("T T", var.getTableArray().get(8));
        assertEquals("T T", var.getTableArray().get(9));
        //Info fields        
        assertEquals("3", var.getTableArray().get(10));
        assertEquals("13", var.getTableArray().get(11));
        assertEquals(null, var.getTableArray().get(12));
        assertEquals(null, var.getTableArray().get(13));
        assertEquals(null, var.getTableArray().get(14));
    }
    
    public void testVariant5() throws Exception {
        // SNP with a Sanger CSQ string
        String csqType = "SANGER";
        String var5 = "1\t1574076\trs9442413\tG\tA\t133.37\tPASS\tAC=1;AF=0.50;AF_AFR=0.054878;AF_AMR=0.279006;AF_ASN=0.159091;AF_EUR=0.318898;AF_MAX=0.318898;AN=2;BaseQRankSum=-1.704;CSQ=ENST00000317673:ENSG00000248333:WITHIN_NON_CODING_GENE,INTRONIC+ENST00000340677:ENSG00000248333:WITHIN_NON_CODING_GENE,INTRONIC+ENST00000341832:ENSG00000248333:WITHIN_NON_CODING_GENE,INTRONIC+ENST00000356026:MMP23B:DOWNSTREAM+ENST00000378675:MMP23B:DOWNSTREAM+ENST00000407249:ENSG00000248333:WITHIN_NON_CODING_GENE,INTRONIC+ENST00000412415:MMP23B:DOWNSTREAM+ENST00000435358:MMP23B:DOWNSTREAM+ENST00000479814:MMP23B:DOWNSTREAM+ENST00000486400:MMP23B:DOWNSTREAM+ENST00000489782:MMP23B:DOWNSTREAM+ENST00000490017:MMP23B:DOWNSTREAM+ENST00000503792:MMP23B:DOWNSTREAM+ENST00000512731:MMP23B:DOWNSTREAM+ENST00000513088:ENSG00000248333:WITHIN_NON_CODING_GENE,INTRONIC+GERP,-0.29;DB;DP=24;Dels=0.00;FS=5.166;HRun=0;HaplotypeScore=2.4820;MQ0=0;MQ=30.87;MQRankSum=-0.033;QD=5.56;ReadPosRankSum=1.102;SB=-24.67;SF=0;VQSLOD=6.8457;culprit=MQRankSum;dbSNP=119\tGT:GQ:DP:PL:AD\t1/1:99:24:163,0,454:16,8\t.\t.";
        Variant var = new Variant(var5, selectedCols, selectedInds);

        assertEquals(1, var.getTableArray().get(0));
        assertEquals(1574076, var.getTableArray().get(1));
        assertEquals("rs9442413", var.getTableArray().get(2));
        assertEquals("G", var.getTableArray().get(3));
        assertEquals("A", var.getTableArray().get(4));
        assertEquals("133.37", var.getTableArray().get(5));
        assertEquals("PASS", var.getTableArray().get(6));
        //Genotypes
        assertEquals("A A", var.getTableArray().get(7));
        assertEquals("G G", var.getTableArray().get(8));
        assertEquals("G G", var.getTableArray().get(9));
        //Info fields        
        assertEquals(null, var.getTableArray().get(10));
        assertEquals("24", var.getTableArray().get(11));
        assertEquals("0.50", var.getTableArray().get(12));
        assertEquals("TRUE", var.getTableArray().get(13));
        assertEquals(null, var.getTableArray().get(14));
        // CSQ fields       
        ArrayList<VariantEffect> variantEffects = var.getVariantEffects();
        VariantEffect ve = var.getMostDamagingEffect(variantEffects);
        assertEquals(15, variantEffects.size());
        assertEquals("ENSG00000248333", ve.getGene());
        assertEquals("ENST00000317673", ve.getFeature());
        assertEquals(null, ve.getAaChange());
        assertEquals("WITHIN_NON_CODING_GENE,INTRONIC", ve.getConsequence());
        assertEquals(null, ve.getCondel());
        assertEquals(null, ve.getCondelScore());
        assertEquals(null, ve.getPolyphen());
        assertEquals(null, ve.getPolyphenScore());
        assertEquals(null, ve.getSift());
        assertEquals(null, ve.getSiftScore());
        assertEquals(null, ve.getGranthamScore());
        assertEquals(1, ve.getDamageScore());

    }

    public void testVariant6() throws Exception {
        // SNP with a VEP CSQ string
        String csqMeta = "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|PolyPhen|SIFT|Condel\">";
        String var6 = "1\t50666515\trs2494876\tC\tT\t2228.51\tPASS\tAC=8;AF=0.50;AF_AFR=0.373984;AF_AMR=0.886740;AF_ASN=0.650350;AF_EUR=0.893701;AF_MAX=0.893701;AN=10;BaseQRankSum=-4.455;CSQ=T|ENSG00000162374|ENST00000371821|Transcript|NON_SYNONYMOUS_CODING|1138|823|275|P/S|Ccc/Tcc||benign(0)|tolerated(0.54)|neutral(0.007),T|ENSG00000162374|ENST00000371819|Transcript|SPLICE_SITE&INTRONIC|||||||||,T|ENSG00000162374|ENST00000357083|Transcript|SPLICE_SITE&INTRONIC|||||||||,T|ENSG00000162374|ENST00000371827|Transcript|SPLICE_SITE&INTRONIC|||||||||,T|ENSG00000162374|ENST00000371823|Transcript|NON_SYNONYMOUS_CODING|1032|808|270|P/S|Ccc/Tcc||benign(0.001)|tolerated(0.86)|neutral(0.001)\tGT:GQ:DP:PL:AD\t0/1:99:133:1493,0,2231:76,56\t1/1:99:69:2271,187,0:0,68\t1/1:99:79:2659,217,0:0,79\t0/1:99:101:1647,0,1375:46,55\t1/1:99:91:3231,253,0:0,91";
      
        VCFMeta vcfMeta = new VCFMeta();
        vcfMeta.add(csqMeta);
        String csqType = vcfMeta.getCsqType();
        HashMap csqFormat = vcfMeta.getCsqIndex();
        Variant var = new Variant(var6, selectedCols, selectedInds, csqFormat);

        assertEquals(1, var.getTableArray().get(0));
        assertEquals(50666515, var.getTableArray().get(1));
        assertEquals("rs2494876", var.getTableArray().get(2));
        assertEquals("C", var.getTableArray().get(3));
        assertEquals("T", var.getTableArray().get(4));
        assertEquals("2228.51", var.getTableArray().get(5));
        assertEquals("PASS", var.getTableArray().get(6));
        //Genotypes
        assertEquals("C T", var.getTableArray().get(7));
        assertEquals("T T", var.getTableArray().get(8));
        assertEquals("T T", var.getTableArray().get(9));
        //Info fields        
        assertEquals("0.50", var.getTableArray().get(12));

        ArrayList<VariantEffect> variantEffects = var.getVariantEffects(csqFormat);
        VariantEffect ve = var.getMostDamagingEffect(variantEffects);
        assertEquals(5, variantEffects.size());
        assertEquals("ENSG00000162374", ve.getGene());
        assertEquals("ENST00000371821", ve.getFeature());
        assertEquals("P/S", ve.getAaChange());
        assertEquals("NON_SYNONYMOUS_CODING", ve.getConsequence());
        assertEquals("neutral", ve.getCondel());
        assertEquals("0.007", ve.getCondelScore());
        assertEquals("benign", ve.getPolyphen());
        assertEquals("0", ve.getPolyphenScore());
        assertEquals("tolerated", ve.getSift());
        assertEquals("0.54", ve.getSiftScore());
        assertEquals(null, ve.getGranthamScore());
        assertEquals(3, ve.getDamageScore());
    }
}
