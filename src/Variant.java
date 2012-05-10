
import java.util.ArrayList;
import java.util.HashMap;

public class Variant {

    public ArrayList tableArray;
    public ArrayList<Integer> genotypes;
    private String csqString;
    private boolean freqFiltered;
    private Double freq;

    public Variant() {
    }

    // variant object with default UK10K consequence format
    public Variant(String vcfLine, ArrayList<String> selectedCols, ArrayList<Integer> selectedInds) {

        tableArray = new ArrayList();
        String values[] = vcfLine.split("\t");
        String chr_s = values[0];
        String pos_s = values[1];
        String id = new String(values[2]);
        String ref = new String(values[3]);
        String alt = new String(values[4]);
        String filter = new String(values[5]);
        String quality = new String(values[6]);

        // convert the chromosome and position into ints
        int chr_i = 0;
        int pos_i = 0;

        // remove chr string from the chomosome number if included
        if (chr_s.startsWith("chr") || chr_s.startsWith("CHR")) {
            chr_s = chr_s.substring(3);
        }

        try {
            chr_i = Integer.parseInt(chr_s);
        } catch (NumberFormatException nfe) {
            if (chr_s.matches("X")) {
                chr_i = 23;
            } else if (chr_s.matches("Y")) {
                chr_i = 24;
            } else if (chr_s.matches("MT")) {
                chr_i = 25;
            } else {
                System.out.println("can't parse chromosome '" + chr_s + "'");
            }
        }

        try {
            pos_i = Integer.parseInt(pos_s);
        } catch (NumberFormatException nfe) {
            System.out.println("position is not a number '" + pos_s + "'");
        }

        // Add the default columns
        tableArray.add(chr_i);
        tableArray.add(pos_i);
        tableArray.add(id);
        tableArray.add(ref);
        tableArray.add(alt);
        tableArray.add(filter);
        tableArray.add(quality);

        ArrayList<VariantEffect> variantEffects = new ArrayList<VariantEffect>();

        HashMap info = new HashMap();

        String infoVals[] = values[7].split(";");

        for (int i = 0; i < infoVals.length; i++) {
            if (infoVals[i].contains("=")) {
                String[] keyValuePairs = infoVals[i].split("=");
                if (keyValuePairs[0].matches("CSQ")) {
                    this.csqString = new String(keyValuePairs[1]);
                    String[] csqVals = keyValuePairs[1].split("\\+");
                    for (int j = 0; j < csqVals.length; j++) {
                        if (csqVals[j].startsWith("GERP")) {
                            String[] gerp = csqVals[j].split(",");
                            info.put("CSQ Gerp Score", gerp[1]);
                        } else {
                            variantEffects.add(new VariantEffect(csqVals[j]));
                        }
                    }
                } else {
                    info.put(keyValuePairs[0], keyValuePairs[1]);
                }
            } else {
                info.put(infoVals[i], "TRUE");
            }
        }

        // if there is not a csq string what happens?
        VariantEffect ve = this.getMostDamagingEffect(variantEffects);
        
        info.put("CSQ Gene", ve.getGene());
        info.put("CSQ Feature", ve.getFeature());
        info.put("CSQ Consequence", ve.getConsequence());
        info.put("CSQ Amino Acid Change", ve.getAaChange());
        info.put("CSQ Sift Prediction", ve.getSift());
        info.put("CSQ Sift Score", ve.getSiftScore());
        info.put("CSQ PolyPhen Prediction", ve.getPolyphen());
        info.put("CSQ PolyPhen Score", ve.getPolyphenScore());
        info.put("CSQ Condel Prediction", ve.getCondel());
        info.put("CSQ Condel Score", ve.getCondelScore());
        info.put("CSQ Grantham Score", ve.getGranthamScore());
        
        genotypes = new ArrayList<Integer>();
        ArrayList<String> genotypeStrings = new ArrayList<String>();
        for (int i = 9; i < values.length; i++) {
            genotypes.add(parseGenotypes(values[i]));
            genotypeStrings.add(parseGenotypeString(values[i]));
        }

        for (Integer indIndex : selectedInds) {
            String genotype = genotypeStrings.get(indIndex);
            tableArray.add(new String(genotype));
        }

        for (String col : selectedCols) {
            tableArray.add(info.get(col));
        }

        // the number of additional effects the variant has
        tableArray.add(variantEffects.size());
        
    }

    // variant object with consequences
    public Variant(String vcfLine, ArrayList<String> selectedCols, ArrayList<Integer> selectedInds, HashMap<String, Integer> csqIndex) {

        tableArray = new ArrayList();
        String values[] = vcfLine.split("\t");
        String chr_s = values[0];
        String pos_s = values[1];
        String id = new String(values[2]);
        String ref = new String(values[3]);
        String alt = new String(values[4]);
        String filter = new String(values[5]);
        String quality = new String(values[6]);

        // convert the chromosome and position into ints
        int chr_i = 0;
        int pos_i = 0;

        // remove chr string from the chomosome number if included
        if (chr_s.startsWith("chr") || chr_s.startsWith("CHR")) {
            chr_s = chr_s.substring(3);
        }

        try {
            chr_i = Integer.parseInt(chr_s);
        } catch (NumberFormatException nfe) {
            if (chr_s.matches("X")) {
                chr_i = 23;
            } else if (chr_s.matches("Y")) {
                chr_i = 24;
            } else if (chr_s.matches("MT")) {
                chr_i = 25;
            } else {
                System.out.println("can't parse chromosome '" + chr_s + "'");
            }
        }

        try {
            pos_i = Integer.parseInt(pos_s);
        } catch (NumberFormatException nfe) {
            System.out.println("position is not a number '" + pos_s + "'");
        }

        // Add the default columns
        tableArray.add(chr_i);
        tableArray.add(pos_i);
        tableArray.add(id);
        tableArray.add(ref);
        tableArray.add(alt);
        tableArray.add(filter);
        tableArray.add(quality);

        ArrayList<VariantEffect> variantEffects = new ArrayList<VariantEffect>();

        HashMap info = new HashMap();

        String infoVals[] = values[7].split(";");

        for (int i = 0; i < infoVals.length; i++) {
            if (infoVals[i].contains("=")) {
                String[] keyValuePairs = infoVals[i].split("=");
                if (keyValuePairs[0].matches("CSQ")) {
                    this.csqString = new String(keyValuePairs[1]);
                    String[] csqVals = keyValuePairs[1].split(",");
                    for (int j = 0; j < csqVals.length; j++) {
                        variantEffects.add(new VariantEffect(csqVals[j], csqIndex));
                    }
                } else {
                    info.put(keyValuePairs[0], keyValuePairs[1]);
                }
            } else {
                info.put(infoVals[i], "TRUE");
            }
        }

        VariantEffect ve = this.getMostDamagingEffect(variantEffects);

        info.put("CSQ Gene", ve.getGene());
        info.put("CSQ Feature", ve.getFeature());
        info.put("CSQ Consequence", ve.getConsequence());
        info.put("CSQ Amino Acid Change", ve.getAaChange());
        info.put("CSQ Sift Prediction", ve.getSift());
        info.put("CSQ Sift Score", ve.getSiftScore());
        info.put("CSQ PolyPhen Prediction", ve.getPolyphen());
        info.put("CSQ PolyPhen Score", ve.getPolyphenScore());
        info.put("CSQ Condel Prediction", ve.getCondel());
        info.put("CSQ Condel Score", ve.getCondelScore());
        info.put("CSQ Grantham Score", ve.getGranthamScore());


        genotypes = new ArrayList<Integer>();
        ArrayList<String> genotypeStrings = new ArrayList<String>();
        for (int i = 9; i < values.length; i++) {
            genotypes.add(parseGenotypes(values[i]));
            genotypeStrings.add(parseGenotypeString(values[i]));
        }

        for (Integer indIndex : selectedInds) {
            String genotype = genotypeStrings.get(indIndex);
            tableArray.add(new String(genotype));
        }

        for (String col : selectedCols) {
            tableArray.add(info.get(col));
        }

        // the number of additional effects the variant has
        tableArray.add(variantEffects.size());

    }

    private int parseGenotypes(String s) {
        String genoVals[] = s.split(":");
        //TODO record all the other vaues along with the genotype
        String alleles[] = genoVals[0].split("[\\|\\/\\\\]");
        if (alleles.length > 1) {
            String a = alleles[0];
            String b = alleles[1];
            if (a.equals(b)) {
                if (a.matches("0") || a.matches("\\.")) {
                    // homo ref                    
                    return 0;
                } else {
                    // homo alt                                                            
                    return 2;
                }
            } else {
                // het                
                return 1;
            }
        } else {
            // missing genotype from merging return as hom ref
            return 0;
        }
    }

    private String parseGenotypeString(String s) {
        String genoVals[] = s.split(":");
        //TODO record all the other vaues along with the genotype

        if (genoVals[0].matches("/.[\\|\\/\\\\]./") || genoVals[0].matches(".")) {
            // missing genotype return as hom ref
            return (String) this.tableArray.get(3) + " " + (String) this.tableArray.get(3);
        } else {
            String ab[] = genoVals[0].split("[\\|\\/\\\\]");
            String a = ab[0];
            String b = ab[1];

            if (a.matches("0")) {
                // get the ref allele
                a = (String) this.tableArray.get(3);
            } else if (a.matches("\\.")) {
                a = ".";
            } else {
                // get the alt allele
                a = ((String) this.tableArray.get(4)).split(",")[Integer.parseInt(a) - 1];
            }
            if (b.matches("0")) {
                // get the ref allele
                b = (String) this.tableArray.get(3);
            } else if (b.matches("\\.")) {
                b = ".";
            } else {
                // get the alt allele
                b = ((String) this.tableArray.get(4)).split(",")[Integer.parseInt(b) - 1];
            }
            return a + " " + b;
        }

    }

    public ArrayList getTableArray() {
        return tableArray;
    }

    public ArrayList<Integer> getGenotypes() {
        return genotypes;
    }

    public ArrayList<Integer> getGenotypes(ArrayList<Integer> indIndexes) {
        ArrayList selectedGenotypes = new ArrayList();
        for (Integer i : indIndexes) {
            selectedGenotypes.add(genotypes.get(i));
        }
        return selectedGenotypes;
    }

    void setFreq(Double f) {
        freq = f;
        freqFiltered = true;
    }

    public Double getFreq() {
        return freq;
    }

    VariantEffect getMostDamagingEffect(ArrayList<VariantEffect> variantEffects) {
        if (variantEffects.size() > 1) {

            VariantEffect mostDamaging = new VariantEffect();

            for (VariantEffect ve : variantEffects) {

                String consequence = ve.getConsequence();

                if (consequence.contains("COMPLEX_INDEL")
                        || consequence.contains("STOP_LOST")
                        || consequence.contains("FRAMESHIFT_CODING")
                        || consequence.contains("ESSENTIAL_SPLICE_SITE")
                        || consequence.contains("STOP_GAINED")
                        || consequence.contains("SPLICE_SITE")
                        || consequence.contains("NON_SYNONYMOUS_CODING")) {

                    ve.setDamageScore(3);

                } else if (consequence.contains("WITHIN_MATURE_miRNA")
                        || consequence.contains("5PRIME_UTR")
                        || consequence.contains("3PRIME_UTR")
                        || consequence.contains("SYNONYMOUS_CODING")) {
                    ve.setDamageScore(2);

                } else if (consequence.contains("INTERGENIC")
                        || consequence.contains("UPSTREAM")
                        || consequence.contains("DOWNSTREAM")
                        || consequence.contains("INTRONIC")
                        || consequence.contains("-")) {
                    ve.setDamageScore(1);
                } else {
                    ve.setDamageScore(0);
                }

                if (ve.getDamageScore() > mostDamaging.getDamageScore()) {
                    mostDamaging = ve;
                }
            }
            return mostDamaging;

        } else if (variantEffects.size() == 1) {

            return variantEffects.get(0);

        } else {
            // if there are no variant effect then create an empty object
            return new VariantEffect();
        }
    }

    ArrayList<VariantEffect> getVariantEffects() {
        
        ArrayList<VariantEffect> effects = new ArrayList<VariantEffect>();
        // if there is no format set then assume the csq string is in Sanger(UK10K) format
        String[] csqVals = csqString.split("\\+");
        for (int i = 0; i < csqVals.length; i++) {
            if (!csqVals[i].startsWith("GERP")) {
                effects.add(new VariantEffect(csqVals[i]));
            }
        }
        return effects;
    }

    ArrayList<VariantEffect> getVariantEffects(HashMap<String, Integer> csqFormat) {
        
        ArrayList<VariantEffect> effects = new ArrayList<VariantEffect>();
        // if there is a format then pass it to the variant effect
        String[] csqVals = csqString.split("\\,");
        for (int i = 0; i < csqVals.length; i++) {
            effects.add(new VariantEffect(csqVals[i], csqFormat));
        }
        return effects;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Variant v = (Variant) o;
        //...test other properties here...

        Integer chr = (Integer) this.getTableArray().get(0);
        Integer pos = (Integer) this.getTableArray().get(1);
        Integer oChr = (Integer) v.getTableArray().get(0);
        Integer oPos = (Integer) v.getTableArray().get(1);

        if (chr.equals(oChr) && pos.equals(oPos)) {
            return true;
        } else {
            return false;
        }


    }
}
