package de.dortmund.tu.wmsi.job;

public class JobIDCounter {
	private static int JOB_ID_COUNTER = 0;
	
	public static int nextID() {
		return JOB_ID_COUNTER++;
	}
	
	public static void resetID(){
		JOB_ID_COUNTER = 0;
	}
}
