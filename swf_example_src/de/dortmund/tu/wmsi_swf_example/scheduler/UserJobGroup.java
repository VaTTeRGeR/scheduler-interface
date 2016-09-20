package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.LinkedList;

import de.dortmund.tu.wmsi.job.Job;

public class UserJobGroup {
	public long user = -1;
	public long t_last_submit = -1;
	public long t_threshold = -1;
	public LinkedList<Job> jobs = new LinkedList<Job>();
	
	public UserJobGroup(long user, long t_threshold) {
		this.user = user;
		this.t_threshold = t_threshold;
	}
	
	public void putJob(Job job) {
		jobs.add(job);
		t_last_submit = job.get(Job.SUBMIT_TIME);
	}
	
	public boolean canPutJob(Job job) {
		return job.get(Job.USER_ID) == user && job.get(Job.SUBMIT_TIME) - t_last_submit <= t_threshold;
	}
	
	public Job pollJob() {
		return jobs.removeFirst();
	}
}
