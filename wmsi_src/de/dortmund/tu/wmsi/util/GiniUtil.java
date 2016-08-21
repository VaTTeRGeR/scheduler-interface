package de.dortmund.tu.wmsi.util;

public class GiniUtil {
	public static double getGiniCoefficient(double[] values) {
		if (values.length < 1)
			return 0; // not computable
		if (values.length == 1)
			return 0;
		
		double relVars = 0;
		double descMean = mean(values);

		if (descMean == 0.0)
			return 0; // only possible if all data is zero
		
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				relVars += (Math.abs(values[i] - values[j]));
			}
		}
		
		relVars = relVars / (values.length * values.length * 2.0);
		
		return (relVars / descMean);
	}
	
	private static double mean(double[] values) {
		double sum = 0;
		double len = values.length;
		
		if(len < 1) return 0;
		
		for (double d : values) {
			sum += d;
		}
		
		return sum/len;
	}

	public static double getGiniCoefficient(long[] values) {
		if (values.length < 1)
			return 0; // not computable
		if (values.length == 1)
			return 0;
		
		double relVars = 0;
		double descMean = mean(values);

		if (descMean == 0.0)
			return 0; // only possible if all data is zero
		
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				relVars += (Math.abs(values[i] - values[j]));
			}
		}
		
		relVars = relVars / (values.length * values.length * 2.0);
		
		return (long)(relVars / descMean);
	}
	
	private static double mean(long[] values) {
		long sum = 0;
		int len = values.length;

		if(len < 1) return 0;

		for (long l : values) {
			sum += l;
		}
		
		return sum/len;
	}
}