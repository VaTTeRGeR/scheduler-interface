package de.dortmund.tu.wmsi.usermodel.util;

import java.util.Random;

public class StatisticalMathHelper {
	private static Random random = new Random();
	
	public static double normalDistributedSD(double mean, double standardDeviation) {
		return mean + random.nextGaussian() * standardDeviation;
	}

	public static double normalDistributedV(double mean, double variance) {
		return mean + random.nextGaussian() * Math.sqrt(variance);
	}
	
	public static double linearRegression(double pj, double c1, double c2){
		final double temp = (c1*pj + c2) - pj;
		return Math.max(temp/pj, 1d);
	}

	public static long userAccepteableWaitTime075(long run_time){
		final double	m1 = 1,
						m2 = 1;
		final double c1 = 1.7 * m1;
		final double c2 = 29.51 * 60.0 * m2; // 29.51 minutes in seconds
		final double pj = (double)run_time;
		return Math.max((long)((linearRegression(pj, c1, c2)-1.0)*pj), 0);
	}
}
