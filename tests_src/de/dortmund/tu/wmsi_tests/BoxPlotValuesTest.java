package de.dortmund.tu.wmsi_tests;

import java.util.Arrays;

import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class BoxPlotValuesTest {
	public static void main(String[] args) {
		long[] m = new long[]{1};
		System.out.println("values: "+Arrays.toString(m));
		System.out.println("boxplot: "+Arrays.toString(StatisticalMathHelper.getBoxPlotValues(m)));
		
		m = new long[]{1,2,3};
		System.out.println("values: "+Arrays.toString(m));
		System.out.println("boxplot: "+Arrays.toString(StatisticalMathHelper.getBoxPlotValues(m)));

		m = new long[]{1,1,1,1,5};
		System.out.println("values: "+Arrays.toString(m));
		System.out.println("boxplot: "+Arrays.toString(StatisticalMathHelper.getBoxPlotValues(m)));

		m = new long[]{1,2};
		System.out.println("values: "+Arrays.toString(m));
		System.out.println("boxplot: "+Arrays.toString(StatisticalMathHelper.getBoxPlotValues(m)));

		m = new long[]{};
		System.out.println("values: "+Arrays.toString(m));
		System.out.println("boxplot: "+Arrays.toString(StatisticalMathHelper.getBoxPlotValues(m)));
	}
}
