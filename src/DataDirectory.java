import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;
import java.util.Vector;


public class DataDirectory {

	File directory;
	VCF vcf;
	String ped; 
	Hashtable<String, Sample> samples;
	
	DataDirectory(String dir) throws Exception {

		try {						
			directory = new File(dir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//what chromosomes do we have here?
        Hashtable<String,Boolean> knownChroms = new Hashtable<String,Boolean>();
        String fileName = null;
        for (File flowFile : directory.listFiles(new ExtensionFilter(".flow"))){
            String[] chunks = flowFile.getName().split("\\.");
            if(fileName == null){
            	fileName = chunks[0];
            }
            if (knownChroms.get(chunks[1]) == null){
                knownChroms.put(chunks[1],true);
                printLog("Found chromosome: " + chunks[1]);
            }
        }

        samples = new Hashtable<String, Sample> ();
        
        for (String chrom : knownChroms.keySet()){
        	String flowName = fileName + "." + chrom + ".flow";
        	FlowFile flow = new FlowFile(directory.getAbsolutePath() + File.separator + flowName);   	
        	String mapName = fileName + "." + chrom + ".map";
        	MapFile map   = new MapFile(directory.getAbsolutePath() + File.separator + mapName);
    		flow.setPos(map.getPositions());
    		flow.parseFlow();
    		Hashtable<String, Chromosome> h = flow.getSamples();
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
        }
		
        String vcfName = fileName + ".vcf.gz";
		vcf = new VCF(directory.getAbsolutePath() + File.separator + vcfName);
		
		String pedName = fileName + ".ped";
		PedFile pedFile = new PedFile(directory.getAbsolutePath() + File.separator + pedName);
		ped = pedFile.makeCSV();
		
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

	public String getPed() {
		return ped;
	}
	
	public VCF returnVCF () {
		return vcf;
	}

}
