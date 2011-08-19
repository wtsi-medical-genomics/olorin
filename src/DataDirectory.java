import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;
import java.util.Vector;


public class DataDirectory {

	File[] vcfFiles;
	File[] flowFiles;
	File[] pedFiles;
	File[] mapFiles; 

	//look at the evoker code for this
	
	DataDirectory(String dir) {

		try {						
			File directory = new File(dir);
			vcfFiles  = directory.listFiles(new ExtensionFilter(".vcf.gz"));
			flowFiles = directory.listFiles(new ExtensionFilter(".flow"));
			pedFiles  = directory.listFiles(new ExtensionFilter(".ped"));
			mapFiles  = directory.listFiles(new ExtensionFilter(".map"));
			printLog("Found " + vcfFiles.length " VCF files");
			printLog("Found " + flowFiles.length " .flow files");
			printLog("Found " + pedFiles.length " .ped files");
			printLog("Found " + mapFiles.length " .map files");	
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
