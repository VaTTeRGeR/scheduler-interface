package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.usermodel.model.userestimate.EstimateSampler;

public class EstimateSamplerTest {
	public static void main(String[] args) {
		String swfPath = "swf_input/cea_curie.swf";
		EstimateSampler sampler = new EstimateSampler(swfPath, 32, 0.95);
		
		System.out.println();
		System.out.println("runtime -> estimate");
		for (int i = 0; i < 20; i++) {
			System.out.println((32*i*i)+" -> "+sampler.randomEstimateByRuntime(32*i*i));
		}
		System.out.println();

		System.out.println("runtime -> estimate");
		for (int i = 0; i < 20; i++) {
			System.out.println("0 -> "+sampler.randomEstimateByRuntime(0));
		}
		System.out.println();
		System.out.println();
		System.out.println("estimate -> average runtime");
		for (long i = 0; i <= 100000; i += 1000) {
			System.out.println(i+" -> "+sampler.averageRuntimeByEstimate(i));
		}
		System.out.println();

	}
}
