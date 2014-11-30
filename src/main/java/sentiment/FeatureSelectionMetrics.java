package sentiment;
public class FeatureSelectionMetrics extends FeatureOccurrenceCounter {

	private Double mi;

	private Double chiSquare;

	private Double ig;
	
	private Double bi;

	public FeatureSelectionMetrics() {
		super();
	}

	public FeatureSelectionMetrics(double tp, double fn, double fp, double tn) {
		super(tp, fn, fp, tn);
	}


	public Double getMI() {
		calculateMutualInformation();
		return mi;
	}


	public Double getChiSquare() {
		calculateChiSquare();
		return chiSquare;
	}


	public Double getIG() {
		calculateInformationGain();
		return ig;
	}
	
	
	public Double getBiNormal() {
		calculateBiNormalSeperation();
		return bi;
	}


	private void calculateMutualInformation() {
		if (tp == 0 || fn == 0 || fp == 0 || tn == 0) {
			// Boundary cases
			mi = null;
			return;
		}

		calculateSum();
		double gPos = getGoldStandardPositives();
		double gNeg = getGoldStandardNegatives();
		double fPos = getPredictedPositives();
		double fNeg = getPredictedNegatives();

		mi = (tp / n) * log2((n * tp) / (gPos * fPos)) + (fp / n)
				* log2((n * fp) / (gNeg * fPos)) + (fn / n)
				* log2((n * fn) / (gPos * fNeg)) + (tn / n)
				* log2((n * tn) / (gNeg * fNeg));
	}


	private void calculateChiSquare() {
		if (tp + fp == 0 || tp + fn == 0 || fn + tn == 0 || fp + tn == 0) {
			// Boundary cases.
			chiSquare = null;
			return;
		}

		calculateSum();
		// An arithmetically simpler way of computing chi-square
		chiSquare = (n * Math.pow((tp * tn - fn * fp), 2))
				/ ((tp + fp) * (tp + fn) * (fn + tn) * (fp + tn));
	}

	private void calculateInformationGain() {
		if (tp == 0 || fn == 0 || fp == 0 || tn == 0) {
			// Boundary cases
			ig = null;
			return;
		}

		calculateSum();
		double gPos = getGoldStandardPositives();
		double gNeg = getGoldStandardNegatives();
		double fPos = getPredictedPositives();
		double fNeg = getPredictedNegatives();

		// Information gain = (entropy when a feature is absent) - (entropy when
		// a feature is present)
		ig = -(gPos / n) * log2(gPos / n) - (gNeg / n) * log2(gNeg / n)
				+ (tp / n) * log2(tp / fPos) + (fp / n) * log2(fp / fPos)
				+ (fn / n) * log2(fn / fNeg) + (tn / n) * log2(tn / fNeg);
	}
	
	private void calculateBiNormalSeperation() {
		if (tp == 0 || fn == 0 || fp == 0 || tn == 0) {
			// Boundary cases
			ig = null;
			return;
		}
		
		double gPos = getGoldStandardPositives();
		double gNeg = getGoldStandardNegatives();
		
		double f1 = StatUtil.getInvCDF(tp/gPos, false);
		double f2 = StatUtil.getInvCDF(fp/gNeg, false);
		
		bi = Math.abs(f1-f2);
	}

	private double log2(double value) {
		return (Math.log(value) / Math.log(2));
	}
	
}
