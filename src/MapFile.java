
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;

public class MapFile {

    String chrom;
    BufferedReader mfr;
    ArrayList<Integer> positions;

    public MapFile(String fileName, String chrom) throws IOException {
        this.chrom = chrom;
        if (fileName.endsWith(".gz")) {
            mfr = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        } else {
            mfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
        parseMap();
    }

    private void parseMap() throws IOException {
        String line;
        positions = new ArrayList<Integer>();
        while ((line = mfr.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] values = line.trim().split("\\s+");
                try {
                    positions.add(new Integer(Integer.parseInt(values[3])));
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Unrecognised position format " + values[3] + " in chromosome " + chrom, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }
}
