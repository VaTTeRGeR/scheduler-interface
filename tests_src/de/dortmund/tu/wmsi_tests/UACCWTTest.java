package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class UACCWTTest {
	public static void main(String[] args) {
		double pj;
		pj = 0.5; // 30min
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj*60d)/60d);
		pj = 1; // 1h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj*60d)/60d);
		pj = 10; // 10h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj*60d)/60d);
		pj = 100; // 100h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj*60d)/60d);
	}
}
