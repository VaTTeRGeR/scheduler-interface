package de.dortmund.tu.wmsi.job;

public class SWFJob extends Job {
	
	long[] values;

	public SWFJob(long submitTime, long runDuration, long resourcesRequested) {
		values = new long[]{submitTime, runDuration, resourcesRequested};
	}
	
	@Override
	public long getSubmitTime() {
		return values[0];
	}

	@Override
	public long getRunDuration() {
		return values[1];
	}

	@Override
	public long getResourcesRequested() {
		return values[2];
	}
	
}
