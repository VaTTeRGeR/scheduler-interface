package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.usermodel.model.userestimate.EstimateSampler;

public class EstimateSamplerTest {
	public static void main(String[] args) {
		String swfPath = "swf_input/cea_curie.swf";
		EstimateSampler sampler = new EstimateSampler(swfPath);
	}
}
