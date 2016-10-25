package de.dortmund.tu.wmsi.usermodel.util;

import java.util.Arrays;
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
	
	public static long median(long[] m) {
		if(m == null || m.length == 0)
			return 0;
		
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	        return m[middle];
	    } else {
	        return (m[middle-1] + m[middle]) / 2;
	    }
	}

	public static long lowerQuantile(long[] m) {
		if(m == null || m.length == 0)
			return 0;
		
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	    	int quantile = middle/2;
	        return m[quantile];
	    } else {
	    	int quantile = (middle-1)/2;
	        return m[quantile];
	    }
	}

	public static double lowerQuantile(double[] m) {
		if(m == null || m.length == 0)
			return 0;
		
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	    	int quantile = middle/2;
	        return m[quantile];
	    } else {
	    	int quantile = (middle-1)/2;
	        return m[quantile];
	    }
	}

	public static long upperQuantile(long[] m) {
		if(m == null || m.length == 0)
			return 0;
		
	    int middle = m.length/2;
    	int quantile = middle+(middle/2);
        return m[quantile];
	}

	public static double upperQuantile(double[] m) {
		if(m == null || m.length == 0)
			return 0;
		
	    int middle = m.length/2;
    	int quantile = middle+(middle/2);
        return m[quantile];
	}

	public static long[] getBoxPlotValues(long[] m) {
		if(m == null || m.length == 0)
			return new long[]{0,0,0,0,0};

		Arrays.sort(m);
		return new long[]{m[0],lowerQuantile(m),median(m),upperQuantile(m),m[m.length-1]};
	}
}
