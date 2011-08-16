import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;


public class DataDirectory {

	VCF vcfFile;
	Vector<Sample> samples;
	String pedFile;
	FreqFile freqFile;
	
	
	DataDirectory(String dir) {

		try {						
			File directory = new File(dir);
			File[] vcfFiles  = directory.listFiles(new ExtensionFilter(".vcf.gz"));
			File[] flowFiles = directory.listFiles(new ExtensionFilter(".flow"));
			File[] pedFiles  = directory.listFiles(new ExtensionFilter(".ped"));
			File[] mapFiles  = directory.listFiles(new ExtensionFilter(".map"));

			if (vcfFiles.length == 1) {
				File vcfFile = vcfFiles[0];
				VCF vcf = null;
				try {
					vcf = new VCF(vcfFile.getAbsolutePath());
				} catch (Exception e) {
					printLog(e.getMessage());
				}
				printLog("Found VCF: " + vcfFile.getAbsolutePath());
				printLog("Processed VCF: found " + vcf.getVariantTotal() + " variants");
			}

			if (flowFiles.length == 1) {
				File flowFile = flowFiles[0];
				sample = new FlowFile(flowFile.getAbsolutePath()).getSamples();
				printLog("Found flow: " + flowFile.getAbsolutePath());
			}

			if (pedFiles.length == 1) {
				File pedFile = pedFiles[0];
				PedFile ped = new PedFile(pedFile.getAbsolutePath());
				printLog("Found ped: " + pedFile.getAbsolutePath());
				String csvFile = ped.makeCSV();
				drawPedigree(csvFile);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printLog(String text){
		GUI.logPanel.append(text+"\n");
    }
	
	class ExtensionFilter implements FilenameFilter{

		String extension;

		ExtensionFilter(String extension){
			this.extension = extension;
		}

		public boolean accept(File file, String string) {
			return string.endsWith(extension);
		}
	}

}
