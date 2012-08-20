
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;

public class VCFMeta {

    ArrayList<HashMap<String, String>> info;
    HashMap<String, Info> infoObjects;
    ArrayList<String> samples;
    HashMap<String, Integer> sampleHash;
    ArrayList<String> infoIds;
    private boolean csq = false;
    String csqType;
    HashMap<String, Integer> csqIndex;
    String fileFormat;
    
    // replace the arraylists with info, filter and format objects?
    public VCFMeta() {
        infoIds = new ArrayList<String>();
        samples = new ArrayList<String>();
        sampleHash = new HashMap<String, Integer>();
        info = new ArrayList<HashMap<String, String>>();
        infoObjects = new HashMap<String, Info>();
        
        
        // set filter as an info field
        HashMap filterHM = new HashMap<String, String>();
        filterHM.put("ID", "Filter");
        filterHM.put("Number", "1");
        filterHM.put("Type", "String");
        filterHM.put("Description", "PASS if this position has passed all filters, if the site has not passed all filters, a semicolon-separated list of codes for filters that fail.");
        
        info.add(filterHM);
        
        // set quality as an info field
        HashMap qualityHM = new HashMap<String, String>();
        qualityHM.put("ID", "Quality");
        qualityHM.put("Number", "1");
        qualityHM.put("Type", "Float");
        qualityHM.put("Description", "phred-scaled quality score for the assertion made in ALT");
                
        info.add(qualityHM);
        
        Info filterOB = new Info();
        filterOB.setId("Filter");
        filterOB.setType("String");
        filterOB.setDescription("PASS if this position has passed all filters, if the site has not passed all filters, a semicolon-separated list of codes for filters that fail.");
        filterOB.setNumber("1");
        infoObjects.put("Filter", filterOB);
        
        Info qualityOB = new Info();
        qualityOB.setId("Quality");        
        qualityOB.setType("Integer");
        qualityOB.setDescription("phred-scaled quality score for the assertion made in ALT");
        qualityOB.setNumber("1");
        infoObjects.put("Quality", qualityOB);
        
    }

    public void add(String s) {
        if (s.startsWith("##")) {
            s = s.substring(2);
            if (s.startsWith("INFO")) {
                String id = null;
                String[] values = s.trim().split("[<>]");
                //split on the comma only if that comma has zero, or an even number of quotes in ahead of it
                // for explaination see here http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
                String[] values2 = values[1].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                HashMap ht = new HashMap<String, String>();
                Info inf = new Info();
                for (String val : values2) {
                    if (val.contains("=")) {
                        String[] values3 = val.split("=");
                        if (values3[0].matches("ID")) {
                            id = values3[1];
                            inf.setId(id);
                            infoIds.add(values3[1]);
                            ht.put(values3[0], values3[1]);
                        } else if (values3[0].matches("Number")) {
                            inf.setNumber(values3[1]);
                            ht.put(values3[0], values3[1]);
                        } else if (values3[0].matches("Type")) {
                            inf.setType(values3[1]);
                            ht.put(values3[0], values3[1]);
                        } else if (values3[0].matches("Description")) {
                            String subStr = values3[1].substring(1, values3[1].length() - 1);
                            inf.setDescription(subStr);
                            ht.put(values3[0], subStr);
                        }
                    } else {
                        ht.put(val, "true");
                    }
                }
                if (ht.get("ID").toString().matches("CSQ")) {                    
                    
                    if (ht.get("Description").toString().startsWith("Consequence of the ALT alleles")) {                        
                        csq = true;
                        csqType = "SANGER";
                        // the sanger csq info description does not currently explain the format of the csq string
                    } else if (ht.get("Description").toString().startsWith("Consequence type as predicted by VEP")) {                        
                        csq = true;
                        csqType = "VEP";                     
                        String[] csqInfoString = ht.get("Description").toString().split("Format:");
                        if (csqInfoString.length == 2){                            
                            String[] csqFormat = csqInfoString[1].split("\\|");                            
                            csqIndex = new HashMap();
                            for(int i=0; i<csqFormat.length; i++ ) {                                
                                csqIndex.put(csqFormat[i], i);
                            }                                                        
                        } else {                            
                            JOptionPane.showMessageDialog(null, "The CSQ format is not recognised", JOptionPane.MESSAGE_PROPERTY, JOptionPane.ERROR_MESSAGE);
                        }                        
                    } else {
                        JOptionPane.showMessageDialog(null, "The CSQ string style is not recognised", JOptionPane.MESSAGE_PROPERTY, JOptionPane.ERROR_MESSAGE);
                    }
                }
                info.add(ht);
                infoObjects.put(id, inf);
            } else if (s.startsWith("fileformat")) {
                String[] values = s.split("=");               
                setFileFormat(values[1]);                               
            }
            
        } else if (s.startsWith("#")) {
            s = s.substring(1);
            String[] values = s.trim().split("\t");
            for (int i = 9; i < values.length; i++) {
                samples.add(values[i]);
                sampleHash.put(values[i], i - 9);
            }
        }
    }

    public HashMap<String, Info> getInfoObjects() {
        return infoObjects;
    }

    private void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }
    
    public String getFileFormat() {
        return fileFormat;
    }

    public ArrayList<HashMap<String, String>> getInfo() {
        return info;
    }

    public ArrayList<String> getSamples() {
        return samples;
    }

    public ArrayList<String> getInfoIds() {
        return infoIds;
    }

    public HashMap<String, Integer> getSampleHash() {
        return sampleHash;
    }

    boolean hasCSQ() {
        return csq;
    }

    String getCsqType() {
        return csqType;
    }
    
    HashMap<String, Integer> getCsqIndex () {        
        return csqIndex;        
    }

    
    public static class Info {

        String id;
        String number;
        String type;
        String description;

        public Info() {
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        public String getNumber() {
            return number;
        }

        public String getType() {
            return type;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
