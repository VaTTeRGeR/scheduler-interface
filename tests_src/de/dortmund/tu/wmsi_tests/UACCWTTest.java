package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class UACCWTTest {
	public static void main(String[] args) {
		long pj;
		pj = 3600/2; // 30min
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj)/3600d);
		pj = 1*3600; // 1h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj)/3600d);
		pj = 10*3600; // 10h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj)/3600d);
		pj = 100*3600; // 100h
		System.out.println("UACCWT: " + StatisticalMathHelper.userAccepteableWaitTime075(pj)/3600d);
	}
}
