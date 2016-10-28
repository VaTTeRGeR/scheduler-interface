package de.dortmund.tu.wmsi_tests;

import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class PolynomialFitAccuracyTest {
	public static void main(String[] args) {
		WeightedObservedPoints points = new WeightedObservedPoints();
		for(double i = 0f; i < 8d*Math.PI; i += 0.1*Math.PI) {
			points.add(i, Math.sin(i));
		}
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(20);
		List<WeightedObservedPoint> pointCollection = points.toList();
		double[] coefficients = fitter.fit(pointCollection);
		//System.out.println(Arrays.toString(coefficients));
		PolynomialFunction.Parametric func = new PolynomialFunction.Parametric();
		for(double i = 0f; i < 8d*Math.PI; i += 0.1*Math.PI) {
			System.out.print("("+i+","+func.value(i, coefficients)+")");
		}
		System.out.println();
		System.out.println("http://www.shodor.org/interactivate/activities/SimplePlot/");
	}
}
