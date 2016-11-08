package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.usermodel.model.userestimate.EstimateSampler;

public class EstimateSamplerTest {
	public static void main(String[] args) {
		String swfPath = "swf_input/cea_curie.swf";
		EstimateSampler sampler = new EstimateSampler(swfPath);
		
		System.out.println();
		System.out.println("Selecting 50 estimates for runtime-bin 31");
		for (int i = 0; i < 50; i++) {
			System.out.println(sampler.randomEstimateByRuntimeBin(31));
		}
		System.out.println();

		System.out.println("Selecting 50 estimates for runtime-bin 30");
		for (int i = 0; i < 50; i++) {
			System.out.println(sampler.randomEstimateByRuntimeBin(30));
		}
		System.out.println();

		System.out.println("Selecting 50 estimates for runtime-bin 0");
		for (int i = 0; i < 50; i++) {
			System.out.println(sampler.randomEstimateByRuntimeBin(0));
		}
		System.out.println();
	}
}
