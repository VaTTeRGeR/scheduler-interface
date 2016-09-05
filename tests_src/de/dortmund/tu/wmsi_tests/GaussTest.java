package de.dortmund.tu.wmsi_tests;

import java.util.Random;

public class GaussTest {
	public static void main(String[] args) {
		Random random = new Random();
		for (int i = 0; i < 1000; i++) {
			double g = random.nextGaussian()/4d+0.75d;
			g = Math.min(g, 1);
			g = Math.max(g, 0);
			System.out.println(g);
		}
	}
}
