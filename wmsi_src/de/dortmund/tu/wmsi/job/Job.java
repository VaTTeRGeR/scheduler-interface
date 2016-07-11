package de.dortmund.tu.wmsi.job;

public abstract class Job {

	private static int idcounter = 0;
	private int id = -1;
	
	public Job() {
		setId();
	}
	
	private synchronized void setId(){
		id = idcounter++;
	}
	
	public int getJobId() {
		return id;
	}
	
	public boolean isValid() {
		return getSubmitTime()>=0 && getResourcesRequested()>0 && getRunDuration() >= 0;
	};

	public abstract long getSubmitTime();
	public abstract long getRunDuration();
	public abstract long getResourcesRequested();
}