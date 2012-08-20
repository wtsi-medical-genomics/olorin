
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

class VariantEffect {

    public String feature;
    public String gene;
    public String consequence;
    public String sift;
    public String siftScore;
    public String polyphen;
    public String polyphenScore;
    public String condel;
    public String condelScore;
    public String granthamScore;
    public String aaChange;
    private int damageScore;

    VariantEffect() {
    }

    VariantEffect(String string) {
        String effectVals[] = null;
        effectVals = string.split(":");
        // a variant effect should have a minimum of three fields
        // feature, gene and consequence
        if (effectVals.length >= 3) {
            setFeature(effectVals[0]);
            setGene(effectVals[1]);
            setConsequence(effectVals[2]);

            for (int i = 3; i < effectVals.length; i++) {
                String effect = effectVals[i];
                Matcher matcher = Olorin.variantEffectPattern.matcher(effect);

                if (matcher.find()) {
                    String name = matcher.group(1);
                    String impact = matcher.group(2);
                    String score = matcher.group(3);

                    if (name.matches("SIFT")) {
                        setSift(impact);
                        setSiftScore(score);
                    } else if (name.matches("PolyPhen")) {
                        setPolyphen(impact);
                        setPolyphenScore(score);
                    } else if (name.matches("Condel")) {
                        setCondel(impact);
                        setCondelScore(score);
                    }
                } else if (effect.contains(">")) {
                    setAaChange(effect);
                } else if (effect.startsWith("Grantham")) {
                    setGranthamScore(effect.split(",")[1]);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Badly formatted consequence string" + string, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // constructor for VEP format variant effect objects
    VariantEffect(String string, HashMap<String, Integer> csqIndex) {

        Vector<String> effectVals = missingSplit(string, "|");
        // a variant effect should have a minimum of three fields
        // feature, gene and consequence
        if (effectVals.size() >= 3) {
            if (csqIndex.containsKey("Gene")) {
                setGene(effectVals.get(csqIndex.get("Gene")));
            }
            if (csqIndex.containsKey("Feature")) {
                setFeature(effectVals.get(csqIndex.get("Feature")));
            }
            if (csqIndex.containsKey("Consequence")) {
                setConsequence(effectVals.get(csqIndex.get("Consequence")));
            }
            if (csqIndex.containsKey("Amino_acids")) {
                setAaChange(effectVals.get(csqIndex.get("Amino_acids")));
            }
            if (csqIndex.containsKey("SIFT")) {
                // assume SIFT has been run with the both command for prediction and score
                if (!effectVals.get(csqIndex.get("SIFT")).isEmpty()) {
                    String[] vals = effectVals.get(csqIndex.get("SIFT")).split("[\\(\\)]");
                    setSift(vals[0]);
                    setSiftScore(vals[1]);
                }
            }

            if (csqIndex.containsKey("PolyPhen")) {
                // assume PolyPhen has been run with the both command for prediction and score
                if (!effectVals.get(csqIndex.get("PolyPhen")).isEmpty()) {
                    String[] vals = effectVals.get(csqIndex.get("PolyPhen")).split("[\\(\\)]");
                    setPolyphen(vals[0]);
                    setPolyphenScore(vals[1]);
                }
            }
            if (csqIndex.containsKey("Condel")) {
                // assume Condel has been run with the both command for prediction and score
                if (!effectVals.get(csqIndex.get("Condel")).isEmpty()) {
                    String[] vals = effectVals.get(csqIndex.get("Condel")).split("[\\(\\)]");
                    setCondel(vals[0]);
                    setCondelScore(vals[1]);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Badly formatted consequence string" + string, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getAaChange() {
        if (aaChange != null) {
            return aaChange;
        } else {
            return null;
        }
    }

    public void setAaChange(String aaChange) {
        this.aaChange = aaChange;
    }

    public String getCondel() {
        if (condel != null) {
            return condel;
        } else {
            return null;
        }
    }

    public void setCondel(String condel) {
        this.condel = condel;
    }

    public String getCondelScore() {
        if (condelScore != null) {
            return condelScore;
        } else {
            return null;
        }
    }

    public void setCondelScore(String condelScore) {
        this.condelScore = condelScore;
    }

    public String getConsequence() {
        if (consequence != null) {
            return consequence;
        } else {
            return null;
        }
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public String getFeature() {
        if (feature != null) {
            return feature;
        } else {
            return null;
        }
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getGene() {
        if (gene != null) {
            return gene;
        } else {
            return null;
        }
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getGranthamScore() {
        if (granthamScore != null) {
            return granthamScore;
        } else {
            return null;
        }
    }

    public void setGranthamScore(String granthamScore) {
        this.granthamScore = granthamScore;
    }

    public String getPolyphen() {
        if (polyphen != null) {
            return polyphen;
        } else {
            return null;
        }
    }

    public void setPolyphen(String polyphen) {
        this.polyphen = polyphen;
    }

    public String getPolyphenScore() {
        if (polyphenScore != null) {
            return polyphenScore;
        } else {
            return null;
        }
    }

    public void setPolyphenScore(String polyphenScore) {
        this.polyphenScore = polyphenScore;
    }

    public String getSift() {
        if (sift != null) {
            return sift;
        } else {
            return null;
        }
    }

    public void setSift(String sift) {
        this.sift = sift;
    }

    public String getSiftScore() {
        if (siftScore != null) {
            return siftScore;
        } else {
            return null;
        }
    }

    public void setSiftScore(String siftScore) {
        this.siftScore = siftScore;
    }

    ArrayList getValues(ArrayList<String> selectedCols) {
        ArrayList tableValues = new ArrayList();
        for (String col : selectedCols) {
            if (col.startsWith("CSQ")) {
                if (col.matches("CSQ Gene")) {
                    tableValues.add(this.getGene());
                }
                if (col.matches("CSQ Feature")) {
                    tableValues.add(this.getFeature());
                }
                if (col.matches("CSQ Consequence")) {
                    tableValues.add(this.getConsequence());
                }
                if (col.matches("CSQ Amino Acid Change")) {
                    tableValues.add(this.getAaChange());
                }
                if (col.matches("CSQ Sift Prediction")) {
                    tableValues.add(this.getSift());
                }
                if (col.matches("CSQ Sift Score")) {
                    tableValues.add(this.getSiftScore());
                }
                if (col.matches("CSQ PolyPhen Prediction")) {
                    tableValues.add(this.getPolyphen());
                }
                if (col.matches("CSQ PolyPhen Score")) {
                    tableValues.add(this.getPolyphenScore());
                }
                if (col.matches("CSQ Condel Prediction")) {
                    tableValues.add(this.getCondel());
                }
                if (col.matches("CSQ Condel Score")) {
                    tableValues.add(this.getCondelScore());
                }
                if (col.matches("CSQ Grantham Score")) {
                    tableValues.add(this.getGranthamScore());
                }

            }
        }
        return tableValues;
    }

    void setDamageScore(int damageScore) {
        this.damageScore = damageScore;
    }

    int getDamageScore() {
        try {
            Integer s = Integer.valueOf(damageScore);
            return s;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    Vector<String> missingSplit(String input, String delimiter) {
        boolean wasDelimiter = false;
        String token = null;
        Vector v = new Vector();
        StringTokenizer st = new StringTokenizer(input, delimiter, true);
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.equals(delimiter)) {
                if (wasDelimiter) {
                    token = "";
                } else {
                    token = null;
                }
                wasDelimiter = true;
            } else {
                wasDelimiter = false;
            }
            if (token != null) {
                v.addElement(token);
            }
        }
        if (wasDelimiter) {
            token = "";
        } else {
            token = null;
        }
        if (token != null) {
            v.addElement(token);
        }
        return v;
    }
}
