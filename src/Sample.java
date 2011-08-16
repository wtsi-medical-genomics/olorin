

public class Sample {

	// for the moment just work with a single chromosome
	public Haplotype matHap;
	public Haplotype patHap;
	
	public Sample(Haplotype m, Haplotype p) {
		matHap = m;
		patHap = p;
	}

	public Sample() {

	}

	public Haplotype getMat() {
		return matHap;
	}

	public Haplotype getPat() {
		return patHap;
	}
	
}