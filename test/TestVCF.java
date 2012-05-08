
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TestVCF extends TestCase {

    private VCF vcf;
    private VCF vepVcf;
    private Collection<SegmentMatch> matches;
    private ArrayList<String> selectedCols;
    private ArrayList<String> selectedInds;
    ArrayList<SegmentMatch> segs;
    ArrayList<SegmentMatch> emptySegs;
    ArrayList<SegmentMatch> dupSegs;
    ArrayList<SegmentMatch> selectedSegs;

    @Before
    @Override
    public void setUp() throws Exception {

        vcf = new VCF("/Users/jm20/work/workspace/Oberon_svn/test/test_files/test3.vcf.gz");
        
        selectedCols = new ArrayList<String>();
        
        // Ids in the test vcf file
        // 522     1163    1171    526     524
        selectedInds = new ArrayList<String>();
        selectedInds.add("522");
        selectedInds.add("1171");
        selectedInds.add("524");
        
        // create a list containing a single segment with 10 variants        
        segs = new ArrayList<SegmentMatch>();
        segs.add(new SegmentMatch("1", 69511, 880238, "A".getBytes(), selectedInds));
        
        // create a list containing a single segment with 0 variants
        emptySegs = new ArrayList<SegmentMatch>();
        emptySegs.add(new SegmentMatch("1", 69500, 69500, "A".getBytes(), selectedInds));
        
        // create a list containing two overlapping segments with a toal of 15 unique variants
        dupSegs = new ArrayList<SegmentMatch>();
        dupSegs.add(new SegmentMatch("1", 753474, 881627, "A".getBytes(), selectedInds));
        dupSegs.add(new SegmentMatch("1", 871159, 884101, "A".getBytes(), selectedInds));
        
        // create a list containing a segment that passes the matching chromosome cutoff but does not contain any of the sequenced inds
        // in the selected mode this should not return any variants 
        
        ArrayList<String> segmentInds = new ArrayList<String>();
        
        segmentInds.add("1");
        segmentInds.add("2");
        segmentInds.add("3");            
        
        selectedSegs = new ArrayList<SegmentMatch>();
        selectedSegs.add(new SegmentMatch("1", 69511, 880238, "A".getBytes(), segmentInds));
        
    }

    @Test
    public void testFailsBadVCF() {
        try {
            VCF badVCF = new VCF("/Users/jm20/work/workspace/Oberon_svn/test/test_files/bad/test.bad.vcf");
        } catch (Exception e) {
            assertEquals("VCF needs to be compressed using bgzip", e.getMessage());
        }

        try {
            VCF badVCF = new VCF("/Users/jm20/work/workspace/Oberon_svn/test/test_files/bad/test.bad.compressed.vcf.gz");
        } catch (Exception e) {
            assertEquals("VCF needs to be indexed using tabix", e.getMessage());
        }
    }

    @Test
    public void testMissingVariants() throws Exception {
        ArrayList<Variant> vcfData = vcf.getVariants(emptySegs, 3, selectedCols, "any", selectedInds, null);
        assertEquals(0, vcfData.size());        
    }
    
    @Test
    public void testGetAnyVariants() throws Exception {
        ArrayList<Variant> vcfData = vcf.getVariants(segs, 3, selectedCols, "any", selectedInds, null);
        assertEquals(10, vcfData.size());
    }
    
    @Test
    public void testGetAllVariants() throws Exception {
        ArrayList<Variant> vcfData = vcf.getVariants(segs, 3, selectedCols, "all", selectedInds, null);
        assertEquals(1, vcfData.size());
    }
    
    @Test
    public void testGetSelectedVariants() throws Exception {
        ArrayList<Variant> vcfData = vcf.getVariants(segs, 3, selectedCols, "selected", selectedInds, null);
        assertEquals(2, vcfData.size());
    }
    
    @Test
    public void testRemoveDuplicates() throws Exception {       
        ArrayList<Variant> vcfData = vcf.getVariants(dupSegs, 3, selectedCols, "any", selectedInds, null);
        assertEquals(15, vcfData.size());
    }
    
    // test that only vars are retrieved from segments where at leat one of the inds in the segment is sequenced and selected
    @Test
    public void testGetFilteredVariants() throws Exception {
        ArrayList<Variant> vcfData = vcf.getVariants(selectedSegs, 3, selectedCols, "selected", selectedInds, null);
        assertEquals(0, vcfData.size());
    }
    
    // testing filtering by frequency? - is this a feature we want to continue with or should we rely on the vcf or query the 1000g website?
    
}
