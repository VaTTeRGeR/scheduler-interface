package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class FCFS_Scheduler implements Scheduler {

	private LinkedList<Job> queue = new LinkedList<Job>();
	private LinkedList<JobFinishEntry> schedule = new LinkedList<JobFinishEntry>();
	private int res_max, res_used;
	private long t_now;
	
	@Override
	public void init(String configPath) {
		res_max = 4096;
		res_used = 0;
		t_now = 0;
	}
	

	@Override
	public long simulateUntil(long t) {
		
		//try to process a job
		if(!queue.isEmpty() && res_max >= res_used + queue.peek().getResourcesRequested()) {
			Job job = queue.poll();
			schedule.add(new JobFinishEntry(t_now + job.getRunDuration(), job));
			Collections.sort(schedule);
			
			res_used += job.getResourcesRequested();
			return t_now;
		}
		
		// a job is going to be finished before t is reached
		if(!schedule.isEmpty() && t >= schedule.peek().end) {
			JobFinishEntry entry = schedule.poll();
			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.end, entry.job));
			res_used -= entry.job.getResourcesRequested();
			return (t_now = entry.end);
		}
		
		// nothing happenend
		return (t_now = t);
	}
	
	@Override
	public void enqueueJob(Job job) {
		queue.push(job);
	}

	private class JobFinishEntry implements Comparable<JobFinishEntry>{
		private long end;
		private Job job;

		public JobFinishEntry(long end, Job job) {
			this.end = end;
			this.job = job;
		}
		
		@Override
		public int compareTo(JobFinishEntry jfe) {
			return (int)(end-jfe.end);
		}
	}
}
