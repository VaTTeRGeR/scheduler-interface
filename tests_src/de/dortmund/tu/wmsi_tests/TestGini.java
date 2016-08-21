package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.util.GiniUtil;

public class TestGini {
	public static void main(String[] args) {
		double[] v;
		v = new double[]{2,2,2,2,2,2};
		System.out.println("g1: "+GiniUtil.getGiniCoefficient(v));
		v = new double[]{3,2,3,2,2,2};
		System.out.println("g2: "+GiniUtil.getGiniCoefficient(v));
		v = new double[]{1,2,3,4,5,6};
		System.out.println("g3: "+GiniUtil.getGiniCoefficient(v));
		v = new double[]{0,0,0,0,0,1};
		System.out.println("g4: "+GiniUtil.getGiniCoefficient(v));

		v = new double[]{0.05,1.188,3,23};
		System.out.println("wikipedia: "+GiniUtil.getGiniCoefficient(v) +" should be ~ 0.6455");
	}
}
