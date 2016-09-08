package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Schedule;
import de.dortmund.tu.wmsi.scheduler.Schedule.JobFinishEntry;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class GINI_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	
	private Schedule schedule;
	
	private HashMap<Long, Long> waitTime;
	private HashMap<Long, Long> jobCount;
	private HashMap<Long, Double> avgwwt;
	private boolean awwtDirty = false;
	
	private JobWaitTimeComparatorGini jwtcGini;

	private long res_max = -1, wait_max = -1;
	
	@Override
	public void initialize() {
		if(res_max == -1)
			throw new IllegalStateException("GINI_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("GINI_Scheduler has a negative resource count configured");
		if(wait_max == -1)
			throw new IllegalStateException("GINI_Scheduler has no wait time configured");
		else if(wait_max < 0)
			throw new IllegalStateException("GINI_Scheduler has a negative max wait time configured");
		
		queue = new LinkedList<Job>();
		schedule = new Schedule(res_max);
		waitTime = new HashMap<Long, Long>();
		jobCount = new HashMap<Long, Long>();
		avgwwt = new HashMap<Long, Double>();
		jwtcGini = new JobWaitTimeComparatorGini(waitTime, jobCount, avgwwt, wait_max);
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
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());
		
		for(Job job : queue) {
			job.set(Job.WAIT_TIME, t_now-job.get(Job.SUBMIT_TIME));
		}
		
		for (Long user : waitTime.keySet()) {
			avgwwt.put(user, ((double)waitTime.get(user))/(double)jobCount.get(user));
		}

		if(awwtDirty) {
			jwtcGini.prepareCompare(queue);
			awwtDirty = false;
		}
		Collections.sort(queue, jwtcGini);
		
		
		//try to process a job from the queue
		if(!queue.isEmpty() && schedule.isFitToSchedule(queue.peek())) {
			Job job = queue.poll();
			long userId = job.get(Job.USER_ID);
			
			waitTime.put(userId, waitTime.getOrDefault(userId,0L)+job.get(Job.WAIT_TIME));
			jobCount.put(userId, jobCount.getOrDefault(userId,0L)+1L);
			
			schedule.addToSchedule(job, t_now);
			
			SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

			SimulationInterface.log("moved job "+job.getJobId()+" from queue to schedule");
			SimulationInterface.log("binding "+job.getResourcesRequested()+" resources");
			
			awwtDirty = true;
			
			return t_now;
		}
		
		// a job is going to be finished before t_target is reached
		if(!schedule.isEmpty() && schedule.peekNextFinishedJobEntry(t_target) != null) {
			
			JobFinishEntry jfe = schedule.pollNextFinishedJobEntry(t_target);
			
			SimulationInterface.instance().submitEvent(new JobFinishedEvent(jfe.t_end, jfe.job));
			
			SimulationInterface.log("finished job "+jfe.job.getJobId()+" at "+jfe.t_end);
			SimulationInterface.log("freeing "+jfe.job.getResourcesRequested()+" resources");
			
			return (t_now = jfe.t_end);
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
}
