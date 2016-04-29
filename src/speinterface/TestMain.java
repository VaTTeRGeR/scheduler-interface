package speinterface;

import speinterface.util.TimerExecuteInterval;

public class TestMain {
	public static void main(String[] args) {
		TimerExecuteInterval tei = new TimerExecuteInterval(5, 2);
		tei.update(Long.MIN_VALUE);
		System.out.println(Long.MIN_VALUE+" -> "+tei.getNextTime());
		for (long i = 0; i < Long.MAX_VALUE; i+=100001L) {
			tei.update(i);
			System.out.println(i+" -> "+tei.getNextTime());
		}
	}
}
