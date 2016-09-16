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

	static double c1 = 1.7;
	static double c2 = 29.51 * 60.0; // 29.51 minutes in seconds
	
	public static void setUserAccepteableWaitTimeParameters(double c1, double c2) {
		StatisticalMathHelper.c1 = c1;
		StatisticalMathHelper.c2 = c2;
	}
	
	public static long userAccepteableWaitTime(long run_time){
		final double pj = (double)run_time;
		return Math.max((long)((linearRegression(pj, c1, c2)-1.0)*pj), 0);
	}
}
