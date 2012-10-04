
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class DataDirectory {

    File directory;
    VCF vcf;
    String ped;
    HashMap<String, Sample> samples;
    HashMap<String, Boolean> knownChroms;
    ArrayList<String> sequencedInds;
    boolean isLoaded = false;

    DataDirectory(String dir) {
        loadData(dir);
    }

    private void loadData(String dir) {

        printLog("Trying to load data in directory: " + dir);
        try {
            directory = new File(dir);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to Open the Selected Directory:\n'" + dir + "'\n'" + e.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        printLog("Opened directory: " + dir);

        //what chromosomes do we have here?
        knownChroms = new HashMap<String, Boolean>();
        String fileName = null;
        File[] flowFiles = directory.listFiles(new ExtensionFilter(".flow"));

        if (flowFiles.length > 1) {
            // flow and map files are arranged by chromosome
            for (File flowFile : flowFiles) {
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
                printLog("Trying to open .flow file: " + flowName);
                FlowFile flow = null;
                try {
                    flow = new FlowFile(directory.getAbsolutePath() + File.separator + flowName, chrom);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Unable to Open .flow file:\n'" + flowName + "'\n'" + ex.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                printLog("Opened .flow file: " + flowName);

                String mapName = fileName + "." + chrom + ".map";
                printLog("Trying to open .map file: " + mapName);
                MapFile map = null;
                try {
                    map = new MapFile(directory.getAbsolutePath() + File.separator + mapName, chrom);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Unable to Open .map file:\n'" + mapName + "'\n'" + ex.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                printLog("Open .map file: " + mapName);

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
                printLog("Parsed flow file: " + flowName);
                printLog("Parsed map file: " + mapName);
            }

        } else if (flowFiles.length == 1) {
            // single flow file found - switch to single file mode
            printLog("Using single file input mode");
            String flowName = flowFiles[0].getName();
            String[] chunks = flowFiles[0].getName().split("\\.");
            fileName = chunks[0];

            // open the map file and check what chromosomes are in the file

            String mapName = fileName + ".map";
            printLog("Trying to open .map file: " + mapName);
            MapFile map = null;
            try {
                map = new MapFile(directory.getAbsolutePath() + File.separator + mapName);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Unable to Open .map file:\n'" + mapName + "'\n'" + ex.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            printLog("Opened .map file: " + mapName);


            printLog("Trying to open .flow file: " + flowName);
            FlowFile flow = null;
            try {
                flow = new FlowFile(directory.getAbsolutePath() + File.separator + flowName);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unable to Open .flow file:\n'" + flowName + "'\n'" + ex.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            printLog("Opened .flow file: " + flowName);

            flow.setPos(map.getPositionsByChromosome());
            flow.parseFlow();
            samples = flow.getSamplesAllChromosomes();
            printLog("Parsed flow file: " + flowName);
            printLog("Parsed map file: " + mapName);



        } else {
            // no flow files found - not a valid olorin input directory
            JOptionPane.showMessageDialog(null, "No flow files found in the directory:\n'" + dir + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
            return;
        }


        String vcfName = fileName + ".vcf.gz";
        printLog("Trying to open vcf file: " + vcfName);
        try {
            vcf = new VCF(directory.getAbsolutePath() + File.separator + vcfName);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to process VCF:\n'" + fileName + ".vcf.gz'\n'" + e.toString() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        printLog("Parsed vcf file: " + vcfName);

        // pass to the pedfile object the list of inds in the vcf so the VCF column can be added to the pedigree
        String pedName = fileName + ".ped";
        printLog("Trying to open .ped file: " + pedName);
        PedFile pedFile = null;
        try {
            pedFile = new PedFile(directory.getAbsolutePath() + File.separator + pedName);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to Open .ped file:\n'" + pedName + "'\n'" + e.getMessage() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ped = pedFile.makeCSV(vcf.getMeta().getSampleHash());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to Write the .ped.csv file:\n'" + e.toString() + "'", "Loading input data failed.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        printLog("Found and parsed ped file: " + pedName);

        isLoaded = true;


    }

    public void printLog(String text) {
        System.out.println(text);
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
