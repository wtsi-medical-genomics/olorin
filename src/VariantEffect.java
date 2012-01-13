
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String effectVals[] = string.split(":");
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
}
