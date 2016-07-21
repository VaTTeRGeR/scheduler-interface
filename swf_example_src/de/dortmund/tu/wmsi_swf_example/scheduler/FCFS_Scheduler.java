package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class FCFS_Scheduler implements Scheduler<Job> {

	private LinkedList<Job> queue = new LinkedList<Job>();
	private LinkedList<JobFinishEntry> schedule = new LinkedList<JobFinishEntry>();
	private int res_max = -1, res_used = 0;
	
	public FCFS_Scheduler setMaxResources(int res_max) {
		this.res_max = res_max;
		return this;
	}
	
	@Override
	public void initialize() {
		res_used = 0;
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getInt("resources", 1024));
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(res_used+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.size());
		
		//try to process a job from the queue
		if(!queue.isEmpty() && res_max >= res_used + queue.peek().getResourcesRequested()) {
			Job job = queue.poll();
			
			schedule.add(new JobFinishEntry(t_now + job.getRunDuration(), job));
			Collections.sort(schedule);

			SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
			
			res_used += job.getResourcesRequested();

			SimulationInterface.log("moved job "+job.getJobId()+" from queue to schedule");
			SimulationInterface.log("binding "+job.getResourcesRequested()+" resources");
			
			return t_now;
		}
		
		// a job is going to be finished before t_target is reached
		if(!schedule.isEmpty() && t_target >= schedule.peek().end) {
			
			JobFinishEntry entry = schedule.poll();
			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.end, entry.job));
			
			res_used -= entry.job.getResourcesRequested();
			
			SimulationInterface.log("finished job "+entry.job.getJobId()+" at "+entry.end);
			SimulationInterface.log("freeing "+entry.job.getResourcesRequested()+" resources");
			
			return (t_now = entry.end);
		}
		
		SimulationInterface.log("scheduler idled");

		// nothing happenend
		return (t_now = t_target);
	}
	
	@Override
	public void enqueueJob(Job job) {
		queue.add(job);
	}

	private class JobFinishEntry implements Comparable<JobFinishEntry>{
		private long end;
		private Job job;

		public JobFinishEntry(long end, Job job) {
			this.end = end;
			this.job = job;
		}
		
		@Override
		public int compareTo(JobFinishEntry other) {
			return (int)(end-other.end);
		}
	}
}
