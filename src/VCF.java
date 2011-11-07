
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class VCF {
    
    TabixReader tabixVCF;
    VCFMeta meta;
    
    public VCF(final String fileName) throws Exception {
        File vcfIndex = new File(fileName + ".tbi");
        if (fileName.endsWith("gz")) {
            if (vcfIndex.exists()) {
                tabixVCF = new TabixReader(fileName);
                this.parseHeader();
            } else {
                throw new Exception("VCF needs to be indexed using tabix");
            }
        } else {
            throw new Exception("VCF needs to be compressed using bgzip");
        }
    }
    
    private void parseHeader() throws IOException {
        meta = new VCFMeta();
        String line = tabixVCF.readLine();
        while (line.startsWith("#")) {
            meta.add(line);
            line = tabixVCF.readLine();
        }
    }

    public ArrayList<Variant> getVariants(ArrayList<SegmentMatch> matches, int matchNum, ArrayList<String> selectedCols, String filteringMode, ArrayList<String> indIds) throws IOException {
        
        // convert the ind id into a column index in the file
        ArrayList<Integer> indIndexes = new ArrayList<Integer>();
        
        for (String id : indIds) {
            indIndexes.add(meta.sampleHash.get(id));
        }

        ArrayList<Variant> variants = new ArrayList<Variant>();
        
        for (SegmentMatch m : matches) {
            
            if (m.getIds().size() >= matchNum) {
                if (tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd()) != null) {
                    TabixReader.Iterator i = tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd());
                    if (i.next() != null) {
                        String vcfLine = i.next();
                        while (vcfLine != null) {
                            
                            Variant v = new Variant(vcfLine, selectedCols, indIndexes);
                            
                            if (filteringMode.matches("any")) {
                                int altCount = 0;
                                for (Integer geno : v.getGenotypes()) {
                                    altCount += geno;
                                }
                                
                                if (altCount >= 1) {
                                    variants.add(v);
                                }
                                
                            } else if (filteringMode.matches("selected")) {                              
                                int altCount = 0;
                                for (Integer geno : v.getGenotypes()) {
                                    if (geno >= 1) {
                                        altCount++;
                                    }
                                }
                                
                                if (altCount == indIndexes.size()) {
                                    variants.add(v);
                                }
                                
                            } else if (filteringMode.matches("all")) {
                                int altCount = 0;
                                for (Integer geno : v.getGenotypes()) {
                                    if (geno >= 1) {
                                        altCount++;
                                    }
                                }
                                
                                if (altCount == meta.getSampleHash().size()) {
                                    variants.add(v);
                                }
                                
                            } else {
                                // invalid filtering mode
                            }
                            
                            vcfLine = i.next();
                        }
                    }
                }
            }
        }
        return variants;
    }
    
//    public ArrayList<Variant> getVariants(ArrayList<SegmentMatch> matches, int matchNum, String ff, double cutoff) throws NumberFormatException, IOException {
//        
//        TabixReader tabixFreq = new TabixReader(ff);
//        ArrayList<Variant> variants = new ArrayList<Variant>();
//        for (SegmentMatch m : matches) {
//            if (m.getIds().size() >= matchNum) {
//                if (tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd()) != null) {
//                    TabixReader.Iterator vcfi = tabixVCF.query(m.getChr() + ":" + m.getStart() + "-" + m.getEnd());
//                    String vcfLine = vcfi.next();
//                    while (vcfLine != null) {
//                        Variant var = new Variant(vcfLine);
//                        // for each variant check if it exists in the freq file
//                        // if it does is it below the frequency cutoff?
//                        if (tabixFreq.query(m.getChr() + ":" + var.getPos() + "-" + var.getPos()) != null) {
//                            TabixReader.Iterator f = tabixFreq.query(m.getChr() + ":" + var.getPos() + "-" + var.getPos());
//                            String freqLine = f.next();
//                            if (freqLine != null) {
//                                String[] vals = freqLine.split("\\t+");
//                                ArrayList<Double> freqVals = new ArrayList<Double>();
//                                int nAlleles = Integer.parseInt(vals[2]);
//                                for (int i = 4; i < nAlleles + 4; i++) {
//                                    String[] frequency = vals[i].split(":");
//                                    Double freqVal = Double.parseDouble(frequency[1]);
//                                    freqVals.add(freqVal);
//                                }
//                                Double minFreq = Collections.min(freqVals);
//                                if (minFreq < cutoff) {
//                                    var.setFreq(minFreq);
//                                    variants.add(var);
//                                }      
//                            }
//                            // what if there is more than 1 line?
//                        } else {
//                            // variant not in freq file - so include
//                            var.setFreq(0.0);
//                            variants.add(var);
//                        }
//                        vcfLine = vcfi.next();
//                    }
//                }
//            }
//        }
//        return variants;
//    }
    
    public VCFMeta getMeta() {
        return meta;
    }
}
