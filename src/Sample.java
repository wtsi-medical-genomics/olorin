

public class Sample {

	// for the moment just work with a single chromosome
	public Haplotype matHap;
	public Haplotype patHap;
	public String id;
	
	public Sample(Haplotype m, Haplotype p, String i) {
		matHap = m;
		patHap = p;
		id = i;
	}

	public Sample() {

	}

	public Haplotype getMat() {
		return matHap;
	}

	public Haplotype getPat() {
		return patHap;
	}
	
	public String getId() {
		return id;
	}
	
}