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
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class GINI_EASY_LIMITED_SORT_Scheduler implements Scheduler {

	private LinkedList<Job> queueSorted;
	private LinkedList<Job> queueUnsorted;

	private Schedule schedule;
	
	private HashMap<Long, Long> waitTime;
	private HashMap<Long, Long> jobCount;
	private HashMap<Long, Double> avgwwt;
	private boolean sortedQueueDirty = false;

	private JobWaitTimeComparatorGini comparatorGini;

	private long reservation_begin = Long.MIN_VALUE;
	private Job reservation_job = null;
	
	private long res_max = -1;
	private long wait_max = -1;
	
	//private int maxSortedSize = 5; // => 276310
	private int maxSortedSize = 10; // => 285022,282675, 278466
	//private int maxSortedSize = 20; // => 292037, 272899, 275582
	//private int maxSortedSize = 30; // => 281791
	
	@Override
	public void initialize() {
		reservation_begin = Long.MIN_VALUE;
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
		if(wait_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no wait time configured");
		else if(wait_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative max wait time configured");
		
		queueSorted = new LinkedList<Job>();
		queueUnsorted = new LinkedList<Job>();
		schedule = new Schedule(res_max);
		waitTime = new HashMap<Long, Long>();
		jobCount = new HashMap<Long, Long>();
		avgwwt = new HashMap<Long, Double>();
		comparatorGini = new JobWaitTimeComparatorGini(waitTime, jobCount, avgwwt, wait_max);
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
	
	public GINI_EASY_LIMITED_SORT_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		schedule = new Schedule(res_max);
		return this;
	}
	
	public GINI_EASY_LIMITED_SORT_Scheduler setMaxWaitTime(long wait_max) {
		this.wait_max = wait_max;
		//this.wait_max = 12*60*60; // 276946,278817
		//this.wait_max = 6*60*60; // 275342, 277672
		//this.wait_max = 3*60*60; // 280477, 276323
		//this.wait_max = 30*60; // 279226, 272793
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+(queueUnsorted.size()+queueSorted.size()));
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());
		
		for(Job job : queueUnsorted) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		
		for(Job job : queueSorted) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		
		if(queueSorted.isEmpty()) {
			while(!queueUnsorted.isEmpty() && queueSorted.size() < maxSortedSize) {
				queueSorted.add(queueUnsorted.poll());
				sortedQueueDirty = true;
			}
		}
		
		for (Long user : waitTime.keySet()) {
			avgwwt.put(user, ((double)waitTime.get(user))/(double)jobCount.get(user));
		}
		
		if(sortedQueueDirty) {
			comparatorGini.prepareCompare(queueSorted);
			Collections.sort(queueSorted, comparatorGini);
			sortedQueueDirty = false;
		}
		
		if(!queueSorted.isEmpty()) {
			if(reservation_job == null) {
				reservation_begin = schedule.getNextFitTime(queueSorted.peek(), t_now);
				reservation_job = queueSorted.poll();
				sortedQueueDirty = true;
				return t_now;
			} else if (schedule.isFitToSchedule(reservation_job)) {
				schedule.addToSchedule(reservation_job, t_now);
				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				long userId = reservation_job.get(Job.USER_ID);
				
				waitTime.put(userId, Math.max(0, waitTime.getOrDefault(userId,0L)
						+ reservation_job.get(Job.WAIT_TIME)
						-StatisticalMathHelper.userAccepteableWaitTime075(reservation_job.get(Job.TIME_REQUESTED))));
				
				jobCount.put(userId, jobCount.getOrDefault(userId,0L)+1L);
				sortedQueueDirty = true;
				removeReservation();
				return t_now;
			} else {
				SimulationInterface.log("backfilling jobs from sorted queue that end before: " + reservation_begin);
				for (Job job : queueSorted) {
					if (schedule.isFitToSchedule(job) && isFinishedBeforeReservation(job, t_now)) {
						SimulationInterface.log("backfilled job: " + job.getJobId() + " running from " + t_now + " to " + (t_now + job.getRunDuration()));
						queueSorted.remove(job);
						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
						schedule.addToSchedule(job, t_now);
						long userId = job.get(Job.USER_ID);
						
						waitTime.put(userId, Math.max(0, waitTime.getOrDefault(userId,0L)
								+ job.get(Job.WAIT_TIME)
								-StatisticalMathHelper.userAccepteableWaitTime075(job.get(Job.TIME_REQUESTED))));
						
						jobCount.put(userId, jobCount.getOrDefault(userId,0L)+1L);
						sortedQueueDirty = true;
						return t_now;
					}
				}
				SimulationInterface.log("backfilling jobs from unsorted queue that end before: " + reservation_begin);
				for (Job job : queueUnsorted) {
					if (schedule.isFitToSchedule(job) && isFinishedBeforeReservation(job, t_now)) {
						SimulationInterface.log("backfilled job: " + job.getJobId() + " running from " + t_now + " to " + (t_now + job.getRunDuration()));
						queueUnsorted.remove(job);
						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
						schedule.addToSchedule(job, t_now);
						long userId = job.get(Job.USER_ID);
						
						waitTime.put(userId, Math.max(0, waitTime.getOrDefault(userId,0L)
								+ job.get(Job.WAIT_TIME)
								-StatisticalMathHelper.userAccepteableWaitTime075(job.get(Job.TIME_REQUESTED))));
						
						jobCount.put(userId, jobCount.getOrDefault(userId,0L)+1L);
						sortedQueueDirty = true;
						return t_now;
					}
				}
			}
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
	
	private boolean isFinishedBeforeReservation(Job job, long t_now) {
		return job.getRunDuration() + t_now < reservation_begin;
	}
	
	private void removeReservation() {
		reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
	}

	@Override
	public void enqueueJob(Job job) {
		if(job.get(Job.USER_ID) == Job.NOT_SET)
			throw new IllegalStateException("Job "+job.getJobId()+" has no User set!");
		queueUnsorted.add(job);
		sortedQueueDirty = true;
	}
}
