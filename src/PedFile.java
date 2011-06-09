import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class PedFile {
	
	String fileName;
	
	public PedFile(String fn) throws Exception {
		fileName = fn;	
	}
	
	public String makeCSV() throws IOException {
		String csvFileName = fileName + ".csv";
		PrintStream output = new PrintStream(new FileOutputStream(new File(csvFileName)));
		output.println("Id,Fid,Mid,Sex,aff");
		BufferedReader pfr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String line;
		while((line=pfr.readLine()) != null) {
			String values[] = line.split("\\s+");
			String ID    = values[1];
			String patID = values[2];
			String matID = values[3];
			String sex   = values[4];
			String aff   = values[5];
			output.println(ID +","+ patID +","+ matID +","+ sex +","+ aff);
		}
		output.close();
		return csvFileName;
	}
}
