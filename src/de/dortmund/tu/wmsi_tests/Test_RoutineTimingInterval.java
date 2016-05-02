package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.routine.timing.RoutineTimingInterval;

public class Test_RoutineTimingInterval {
	public static void main(String[] args) {
		RoutineTimingInterval tei = new RoutineTimingInterval(5, 2);
		tei.update(Long.MIN_VALUE);
		System.out.println(Long.MIN_VALUE+" -> "+tei.getNextTime());
		for (long i = 0; i < Long.MAX_VALUE; i+=100001L) {
			tei.update(i);
			System.out.println(i+" -> "+tei.getNextTime());
		}
	}
}
