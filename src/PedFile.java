
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;

// Class to reformat the input ped format file for use with the pedViz library
public class PedFile {

    String fileName;
    BufferedReader pfr;
    ArrayList<String> samples;

    public PedFile(String fn) throws FileNotFoundException {
        fileName = fn;
        pfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        samples = new ArrayList();
    }

    public ArrayList<String> getSamples() {
        return samples;
    }

    String makeCSV(HashMap<String, Integer> vcfSampleHash) throws FileNotFoundException, IOException {
        String csvFileName = fileName + ".csv";
        PrintStream output = new PrintStream(new FileOutputStream(new File(csvFileName)));
        output.println("Id,Fid,Mid,Sex,Aff,VCF");
        String line;
        while ((line = pfr.readLine()) != null) {
            if (!line.isEmpty()) {
                String values[] = line.split("\\s+");
                String ID = values[1];
                String patID = values[2];
                String matID = values[3];
                String sex = values[4];
                String aff = values[5];
                String vcf;
                if (vcfSampleHash.containsKey(ID)) {
                    vcf = "Yes";
                } else {
                    vcf = "No";
                }
                output.println(ID + "," + patID + "," + matID + "," + sex + "," + aff + "," + vcf);
                samples.add(ID);
            }
        }
        output.close();
        return csvFileName;
    }
}
