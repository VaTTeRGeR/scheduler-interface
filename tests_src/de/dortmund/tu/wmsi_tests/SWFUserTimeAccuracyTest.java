package de.dortmund.tu.wmsi_tests;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.util.SWFFileUtil;

public class SWFUserTimeAccuracyTest {
	public static void main(String[] args) {
		String swfPath = "swf_input/cea_curie.swf";
		if(swfPath == null || !(new File(swfPath).exists())) {
			System.out.println("SWF-File: " + swfPath + " does not exist!");
			return;
		}
		
		String[] lines = SWFFileUtil.loadLines(new File(swfPath));
		
		WeightedObservedPoints points = new WeightedObservedPoints();
		
		for (int i = 0; i < lines.length; i++) {
			String[] line = lines[i].split("\\s+");
			if(getValue(line, Job.SUBMIT_TIME) != -1 &&
			   getValue(line, Job.RUN_TIME) != -1 &&
			   getValue(line, Job.RESOURCES_ALLOCATED) != -1 &&
			   getValue(line, Job.USER_ID) != -1 &&
			   getValue(line, Job.RESOURCES_REQUESTED) != -1 &&
			   getValue(line, Job.TIME_REQUESTED) != -1 &&
			   getValue(line, Job.RUN_TIME) != -1
			) {
				points.add(getValue(line, Job.TIME_REQUESTED), getValue(line, Job.RUN_TIME));
				//System.out.println("("+getValue(line, Job.TIME_REQUESTED)+":"+getValue(line, Job.RUN_TIME)+")");
			} else {
				//System.out.println("Job "+getValue(line, Job.JOB_ID)+" was not loaded, invalid values");
			}
		}
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
		List<WeightedObservedPoint> pointCollection = points.toList();
		double[] coefficients = fitter.fit(pointCollection);
		//System.out.println(Arrays.toString(coefficients));
		PolynomialFunction.Parametric func = new PolynomialFunction.Parametric();
		for (int i = 0; i < 50000; i+=100) {
			System.out.print("("+i+","+func.value(i, coefficients)+")");
		}
		System.out.println("http://www.shodor.org/interactivate/activities/SimplePlot/");
	}
	
	private static long getValue(String[] line, int index) {
		return Long.valueOf((line[index]));
	}
}
