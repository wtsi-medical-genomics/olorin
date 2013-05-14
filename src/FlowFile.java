
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;

public class FlowFile {

    String filename;
    String chrom;
    BufferedReader ffr;
    HashMap<String, Chromosome> individuals;
    HashMap<String, Sample> samples;
    ArrayList<Integer> positions;
    private HashMap<String, ArrayList<Integer>> positionsByChromosome;
    boolean singleFile;

    public FlowFile(String fileName, String chrom) throws Exception {
        this.filename = filename;
        this.chrom = chrom;
        if (fileName.endsWith(".gz")) {
            ffr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        } else {
            ffr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
    }

    public FlowFile(String fileName) throws IOException {
        singleFile = true;
        this.filename = filename;
        if (fileName.endsWith(".gz")) {
            ffr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        } else {
            ffr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
    }

    public void parseFlow() {
        String line;
        individuals = new HashMap<String, Chromosome>();
        ArrayList<Segment> matHap = null;
        ArrayList<Segment> patHap = null;

        try {
            if (singleFile) {
                // assume chromosomes are numericaly sorted starting from 1
                Integer flowChr = 0;
                samples = new HashMap<String, Sample>();

                while ((line = ffr.readLine()) != null) {
                    if (!line.isEmpty()) {
                        //assume 1 family per file
                        if (!line.startsWith("FAMILY")) {
                            String[] codes = line.trim().split("\\s+");
                            // subtract 2 from the flow file length to account for the id and status                            
                            if ((codes.length - 2) != positionsByChromosome.get(flowChr.toString()).size()) {
                                JOptionPane.showMessageDialog(null, "Number of markers in .map and .flow files does not match in chromosome " + chrom, "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            String id = codes[0];
                            String status = codes[1];
                            ArrayList<Segment> seg = findSegments(codes, flowChr);
                            if (status.contains("FOUNDER")) {
                                if (matHap != null) {
                                    patHap = seg;
                                } else {
                                    matHap = seg;
                                }
                            } else if (status.contains("MATERNAL")) {
                                matHap = seg;
                            } else if (status.contains("PATERNAL")) {
                                patHap = seg;
                            }

                            if (matHap != null && patHap != null) {
                                individuals.put(id, new Chromosome(matHap, patHap));
                                matHap = null;
                                patHap = null;
                            }

                        } else if (!individuals.isEmpty()) {
                            for (String indID : individuals.keySet()) {
                                if (samples.containsKey(indID)) {
                                    Sample samp = samples.get(indID);
                                    samp.setChr(flowChr.toString(), individuals.get(indID));
                                } else {
                                    Sample samp = new Sample();
                                    samp.setChr(flowChr.toString(), individuals.get(indID));
                                    samples.put(indID, samp);
                                }
                            }
                            individuals.clear();
                            flowChr++;
                        } else {
                            // skip first chromosome - as no segments have been recorded yet
                            flowChr++;
                        }
                    }
                }

                if (!individuals.isEmpty()) {
                    for (String indID : individuals.keySet()) {
                        if (samples.containsKey(indID)) {
                            Sample samp = samples.get(indID);
                            samp.setChr(flowChr.toString(), individuals.get(indID));
                        } else {
                            Sample samp = new Sample();
                            samp.setChr(flowChr.toString(), individuals.get(indID));
                            samples.put(indID, samp);
                        }
                    }
                }

            } else {
                while ((line = ffr.readLine()) != null) {
                    if (!line.isEmpty()) {
                        //assume 1 family per file
                        if (!line.startsWith("FAMILY")) {

                            String[] codes = line.trim().split("\\s+");
                            // subtract 2 from the flow file length to account for the id and status
                            if ((codes.length - 2) != positions.size()) {
                                JOptionPane.showMessageDialog(null, "Number of markers in .map and .flow files does not match in chromosome " + chrom, "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            String id = codes[0];
                            String status = codes[1];
                            ArrayList<Segment> seg = findSegments(codes);
                            if (status.contains("FOUNDER")) {
                                if (matHap != null) {
                                    patHap = seg;
                                } else {
                                    matHap = seg;
                                }
                            } else if (status.contains("MATERNAL")) {
                                matHap = seg;
                            } else if (status.contains("PATERNAL")) {
                                patHap = seg;
                            }

                            if (matHap != null && patHap != null) {
                                individuals.put(id, new Chromosome(matHap, patHap));
                                matHap = null;
                                patHap = null;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to Parse Flow File: " + filename, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ArrayList<Segment> findSegments(String[] codes) {
        ArrayList<Segment> segs = new ArrayList<Segment>();
        String id = codes[0];
        String last = codes[2];
        int segStart = getPos(2);
        int segEnd = getPos(2);

        for (int i = 3; i < codes.length; i++) {
            String code = codes[i];
            if (code.contentEquals(last)) {
                segEnd = getPos(i);
            } else {
                // store the segment
                segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
                //reset the values for the next segment
                segStart = getPos(i);
                segEnd = getPos(i);
            }
            last = code;
        }
        //last segment
        segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
        return segs;
    }

    private ArrayList<Segment> findSegments(String[] codes, Integer flowChr) {
        ArrayList<Segment> segs = new ArrayList<Segment>();
        String id = codes[0];
        String last = codes[2];
        int segStart = getPos(2, flowChr);
        int segEnd = getPos(2, flowChr);

        for (int i = 3; i < codes.length; i++) {
            String code = codes[i];
            if (code.contentEquals(last)) {
                segEnd = getPos(i, flowChr);
            } else {
                // store the segment
                segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
                //reset the values for the next segment
                segStart = getPos(i, flowChr);
                segEnd = getPos(i, flowChr);
            }
            last = code;
        }
        //last segment
        segs.add(new Segment(segStart, segEnd, last.getBytes(), id));
        return segs;
    }

    private int getPos(int i) {
        //offset for the two extra columns in the flow file id and status
        return positions.get(i - 2);
    }

    private int getPos(int i, Integer flowChr) {
        return positionsByChromosome.get(flowChr.toString()).get(i - 2);
    }

    public HashMap<String, Chromosome> getSamples() {
        return individuals;
    }

    public HashMap<String, Sample> getSamplesAllChromosomes() {
        return samples;
    }

    public void setPos(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    public void setPos(HashMap<String, ArrayList<Integer>> positionsByChromosome) {
        this.positionsByChromosome = positionsByChromosome;
    }

}
