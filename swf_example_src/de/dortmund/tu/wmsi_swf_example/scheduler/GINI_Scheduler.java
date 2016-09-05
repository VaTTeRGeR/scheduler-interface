package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.GiniUtil;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class GINI_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	private LinkedList<JobFinishEntry> schedule;
	private HashMap<Long, Long> waitTime;
	private HashMap<Long, Long> jobCount;
	private HashMap<Long, Double> avgWeightedWaitTime;
	private long res_max = -1, res_used = 0, wait_max = -1;
	private boolean awwtDirty = false;
	
	@Override
	public void initialize() {
		res_used = 0;
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
		if(wait_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no wait time configured");
		else if(wait_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative max wait time configured");
		
		queue = new LinkedList<Job>();
		schedule = new LinkedList<JobFinishEntry>();
		waitTime = new HashMap<Long, Long>();
		jobCount = new HashMap<Long, Long>();
		avgWeightedWaitTime = new HashMap<Long, Double>();
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
		setMaxWaitTime(properties.getLong("wait_threshold", -1));
	}
	
	public GINI_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		return this;
	}
	
	public GINI_Scheduler setMaxWaitTime(long wait_max) {
		this.wait_max = wait_max;
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(res_used+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.size());
		
		for(Job job : queue) {
			job.set(Job.WAIT_TIME, t_now-job.get(Job.SUBMIT_TIME));
		}
		
		for (Long user : waitTime.keySet()) {
			avgWeightedWaitTime.put(user, ((double)waitTime.get(user))/(double)jobCount.get(user));
		}

		if(awwtDirty) {
			jwtcGini.prepareCompare();
			awwtDirty = false;
		}
		Collections.sort(queue, jwtcGini);
		
		
		//try to process a job from the queue
		if(!queue.isEmpty() && res_max >= res_used + queue.peek().getResourcesRequested()) {
			Job job = queue.poll();
			long userId = job.get(Job.USER_ID);
			
			waitTime.put(userId, waitTime.getOrDefault(userId,0L)+job.get(Job.WAIT_TIME));
			jobCount.put(userId, jobCount.getOrDefault(userId,0L)+1L);
			
			schedule.add(new JobFinishEntry(t_now + job.getRunDuration(), job));
			Collections.sort(schedule);
			
			res_used += job.getResourcesRequested();

			SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
			

			SimulationInterface.log("moved job "+job.getJobId()+" from queue to schedule");
			SimulationInterface.log("binding "+job.getResourcesRequested()+" resources");
			
			awwtDirty = true;
			
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
		if(job.get(Job.USER_ID) == Job.NOT_SET)
			throw new IllegalStateException("Job "+job.getJobId()+" has no User set!");
		queue.add(job);
		awwtDirty = true;
	}

	private JobWaitTimeComparatorGini jwtcGini = new JobWaitTimeComparatorGini();
	
	private class JobWaitTimeComparatorGini implements Comparator<Job> {
		private HashMap<Job, Long> jobToGini = new HashMap<Job, Long>();
		private LinkedList<Double> awwt = new LinkedList<Double>();

		private void prepareCompare() {
			jobToGini.clear();
			for (Job job : queue) {
				awwt.clear();
				for (Long userId : waitTime.keySet()) {
					if(userId == job.get(Job.USER_ID)) {
						long wt = waitTime.get(userId) + job.get(Job.WAIT_TIME);
						long jc = jobCount.get(userId) + 1;
						awwt.add(((double)wt)/(double)jc);
					} else {
						awwt.add(avgWeightedWaitTime.get(userId));
					}
				}
				int i = 0;
				double[] awwtValues = new double[awwt.size()];
				for (Double value : awwt) {
					awwtValues[i++] = value;
				}
				jobToGini.put(job, (long)(GiniUtil.getGiniCoefficient(awwtValues)*100000d));
				//System.out.println("Gini if Job "+job.getJobId()+ " taken: "+(long)(GiniUtil.getGiniCoefficient(awwtValues)*100000d) + " / "+GiniUtil.getGiniCoefficient(awwtValues));
			}
		}
		
		@Override
		public int compare(Job j0, Job j1) {
			if(wait_max > 0 && (j0.get(Job.WAIT_TIME) > wait_max || j1.get(Job.WAIT_TIME) > wait_max))
				return (int)(j1.get(Job.WAIT_TIME)-j0.get(Job.WAIT_TIME));
			else
				return (int)(jobToGini.get(j0)-jobToGini.get(j1));
		}
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
