package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Comparator;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.job.Job;

public class UserJobGroup {
	private static Comparator<Job> c = new JobExceedWaitTimeComparatorAbsolute();
	
	public long user = -1;
	public LinkedList<Job> jobs = new LinkedList<Job>();
	
	public UserJobGroup(long user) {
		this.user = user;
	}
	
	public void putJob(Job job) {
		jobs.add(job);
	}
	
	public boolean canPutJob(Job job) {
		return job.get(Job.USER_ID) == user;
	}
	
	public Job pollJob() {
		return jobs.removeFirst();
	}
	
	public void sort(){
		jobs.sort(c);
	}
}
