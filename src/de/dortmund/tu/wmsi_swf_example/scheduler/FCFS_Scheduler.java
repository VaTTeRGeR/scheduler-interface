package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.Util;

public class FCFS_Scheduler implements Scheduler {

	private LinkedList<Job> queue = new LinkedList<Job>();
	private LinkedList<JobFinishEntry> schedule = new LinkedList<JobFinishEntry>();
	private int res_max, res_used;
	
	@Override
	public void init(String configPath) {
		Properties properties = Util.getProperties(configPath);
		res_max = Integer.parseInt(properties.getProperty("resources","1024"));
		res_used = 0;
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
		public int compareTo(JobFinishEntry jfe) {
			return (int)(end-jfe.end);
		}
	}
}
