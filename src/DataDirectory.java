
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class DataDirectory {

    File directory;
    VCF vcf;
    String ped;
    HashMap<String, Sample> samples;
    HashMap<String, Boolean> knownChroms;
    ArrayList<String> sequencedInds;

    DataDirectory(String dir) throws Exception {

        try {
            directory = new File(dir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //what chromosomes do we have here?
        knownChroms = new HashMap<String, Boolean>();
        String fileName = null;
        for (File flowFile : directory.listFiles(new ExtensionFilter(".flow"))) {
            String[] chunks = flowFile.getName().split("\\.");
            if (fileName == null) {
                fileName = chunks[0];
            }
            if (knownChroms.get(chunks[1]) == null) {
                knownChroms.put(chunks[1], true);
                printLog("Found chromosome: " + chunks[1]);
            }
        }

        samples = new HashMap<String, Sample>();

        for (String chrom : knownChroms.keySet()) {
            String flowName = fileName + "." + chrom + ".flow";
            FlowFile flow = new FlowFile(directory.getAbsolutePath() + File.separator + flowName);
            String mapName = fileName + "." + chrom + ".map";
            MapFile map = new MapFile(directory.getAbsolutePath() + File.separator + mapName);
            flow.setPos(map.getPositions());
            flow.parseFlow();
            HashMap<String, Chromosome> h = flow.getSamples();
            for (String s : h.keySet()) {
                if (samples.containsKey(s)) {
                    Sample samp = samples.get(s);
                    samp.setChr(chrom, h.get(s));
                } else {
                    Sample samp = new Sample();
                    samp.setChr(chrom, h.get(s));
                    samples.put(s, samp);
                }
            }
            printLog("Found and parsed flow file: " + flowName);
            printLog("Found and parsed map file: " + mapName);
        }

        String vcfName = fileName + ".vcf.gz";
        vcf = new VCF(directory.getAbsolutePath() + File.separator + vcfName);
        printLog("Found and parsed vcf file: " + vcfName);

        String pedName = fileName + ".ped";
        PedFile pedFile = new PedFile(directory.getAbsolutePath() + File.separator + pedName);
        ped = pedFile.makeCSV();
        printLog("Found and parsed ped file: " + pedName);
        
        ArrayList<String> vcfSamples = vcf.getMeta().getSamples();
        HashMap<String, Integer> vcfSampleHash = new HashMap<String, Integer> ();
        int counter = 0;
        for (String s: vcfSamples) {
            counter++;
            vcfSampleHash.put(s, counter);
        }
        
        ArrayList<String> pedSamples = pedFile.getSamples();
        ArrayList<String> sequencedInds = new ArrayList<String> ();
        for (String s: pedSamples) {
            if (vcfSampleHash.containsKey(s)) {
                sequencedInds.add(s);
            }
        }
        
        if (sequencedInds.size() == 0) {
            //no matching sample ids between the vcf and ped file
            printLog("Error: No sequenced sample in the family");
        }
    }

    public void printLog(String text) {
        Olorin.logPanel.append(text + "\n");
        Olorin.logPanel.repaint();
    }

    ArrayList getSequencedInds() {
        return sequencedInds;
    }

    class ExtensionFilter implements FilenameFilter {

        String extension;

        ExtensionFilter(String extension) {
            this.extension = extension;
        }

        public boolean accept(File file, String string) {
            return string.endsWith(extension);
        }
    }

    public String getPed() {
        return ped;
    }

    public VCF returnVCF() {
        return vcf;
    }
    
    public int getChrNum() {
        return knownChroms.size();
    }
    
}
