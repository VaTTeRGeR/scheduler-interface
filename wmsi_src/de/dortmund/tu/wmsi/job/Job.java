package de.dortmund.tu.wmsi.job;

public abstract class Job {

	private int id = -1;
	
	public Job(int id) {
		this.id = id;
	}
	
	public int getJobId() {
		return id;
	}
	
	public boolean isValid() {
		return getResourcesRequested() > 0 && getRunDuration() >= 0 && id >= 0;
	};

	public abstract long getSubmitTime();
	public abstract long getRunDuration();
	public abstract long getResourcesRequested();
}
