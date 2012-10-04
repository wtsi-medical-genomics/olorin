
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;

public class MapFile {

    String chrom;
    BufferedReader mfr;
    ArrayList<Integer> positions;
    HashMap<String, ArrayList<Integer>> positionsByChromosome;

    public MapFile(String fileName, String chrom) throws IOException {
        this.chrom = chrom;
        if (fileName.endsWith(".gz")) {
            mfr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        } else {
            mfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
        try {
            parseMap();
        } catch (IOException ex) {
            throw ex;
        }
    }

    public MapFile(String fileName) throws IOException {
        if (fileName.endsWith(".gz")) {
            mfr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        } else {
            mfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
        try {
            parseMultiChrMap();
        } catch (IOException ex) {
            throw ex;
        }
    }

    private void parseMap() throws IOException, IOException {
        String line;
        positions = new ArrayList<Integer>();
        while ((line = mfr.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] values = line.trim().split("\\s+");
                if (values.length < 4) {
                    throw new IOException(".map file format error.\nOlorin expects a file containing at least 'Chromosome', 'Marker', 'Genetic distance', 'Base-pair position' columns");
                } else {
                    try {
                        positions.add(new Integer(Integer.parseInt(values[3])));
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Unrecognised position format " + values[3] + " in chromosome " + chrom, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void parseMultiChrMap() throws IOException, IOException {
        String line;

        // hash of position arrays?        
        positionsByChromosome = new HashMap<String, ArrayList<Integer>>();
        positions = new ArrayList<Integer>();
        String lastChromosome = null;

        // get the first chromosome         
        if ((line = mfr.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] values = line.trim().split("\\s+");
                // check the file has the correct number of columns
                if (values.length < 4) {
                    throw new IOException(".map file format error.\nOlorin expects a file containing at least 'Chromosome', 'Marker', 'Genetic distance', 'Base-pair position' columns");
                } else {
                    lastChromosome = values[0];
                    try {
                        positions.add(new Integer(Integer.parseInt(values[3])));
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Unrecognised position format " + values[3] + " in .map file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        while ((line = mfr.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] values = line.trim().split("\\s+");
                String chromosome = values[0];
                if (chromosome.matches(lastChromosome)) {
                    try {
                        positions.add(new Integer(Integer.parseInt(values[3])));
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Unrecognised position format " + values[3] + " in .map file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    positionsByChromosome.put(lastChromosome, new ArrayList<Integer>(positions));
                    positions.clear();
                    try {
                        positions.add(new Integer(Integer.parseInt(values[3])));
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Unrecognised position format " + values[3] + " in .map file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    lastChromosome = chromosome;
                }
            }
        }

        if (!positions.isEmpty()) {
            positionsByChromosome.put(lastChromosome, new ArrayList<Integer>(positions));
        }
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    HashMap<String, ArrayList<Integer>> getPositionsByChromosome() {
        return positionsByChromosome;
    }
}
