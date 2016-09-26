package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.Comparator;
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
import de.dortmund.tu.wmsi_swf_example.scheduler.comparators.JobExceedWaitTimeComparatorAbsolute;
import de.dortmund.tu.wmsi_swf_example.scheduler.comparators.JobWaittimeComparator;
import de.dortmund.tu.wmsi_swf_example.scheduler.comparators.JobAccWTComparator;

public class ABSOLUTE_OVERTIME_SPREAD_SCALING_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	private LinkedList<Job> backfillQueue; //Stores the jobs that could be backfilled

	private Schedule schedule;
	
	private Comparator<Job> comparator;
	private Comparator<Job> comparatorBackfill;

	private long reservation_begin = Long.MIN_VALUE;
	private Job reservation_job = null;
	
	private long res_max = -1;
	
	private long t_last_execution = Long.MIN_VALUE;
	
	@Override
	public void initialize() {
		t_last_execution = Long.MIN_VALUE;
		reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
		
		queue = new LinkedList<Job>();
		backfillQueue = new LinkedList<Job>();
		schedule = new Schedule(res_max);
		comparator = new JobExceedWaitTimeComparatorAbsolute();
		comparatorBackfill = new JobAccWTComparator();
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
	}
	
	public ABSOLUTE_OVERTIME_SPREAD_SCALING_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		schedule = new Schedule(res_max);
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+(queue.size()));
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());
		
		for(Job job : queue) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		
		if(t_now > t_last_execution) {
			t_last_execution = t_now;
			
			Collections.sort(queue, comparator);
			
			HashMap<Long, LinkedList<Integer>> indexListMap = new HashMap<Long, LinkedList<Integer>>();

			int i = 0;
			for(Job job : queue) {
				long userId = job.get(Job.USER_ID);
				LinkedList<Integer> indexList = indexListMap.getOrDefault(userId, new LinkedList<Integer>());
				indexList.add(i);
				indexListMap.put(userId, indexList);
				
				i++;
			}
			
			final HashMap<Long, Integer> medianIndexMap = new HashMap<Long, Integer>(64);
			
			for(long userId : indexListMap.keySet()) {
				LinkedList<Integer> indexList = indexListMap.getOrDefault(userId, new LinkedList<Integer>());
				medianIndexMap.put(userId, indexList.get((int)Math.ceil((((double)indexList.size())-1d)/2d)));
			}
			Collections.sort(queue, new Comparator<Job>() {
				@Override
				public int compare(Job o1, Job o2) {
					return medianIndexMap.get(o1.get(Job.USER_ID)) - medianIndexMap.get(o2.get(Job.USER_ID));
				}
			});
		}
		
		/*for (Job job : queue) {
			long accWaitTime = job.get(Job.TIME_REQUESTED) + StatisticalMathHelper.userAccepteableWaitTime075(job.get(Job.TIME_REQUESTED));
			long delta = accWaitTime - job.get(Job.WAIT_TIME);
			System.out.println("WT: "+job.get(Job.WAIT_TIME)+" - USER_TIME: "+job.get(Job.TIME_REQUESTED)+" - ACCWT: "+accWaitTime+" - DELTA: "+delta);
		}
		System.out.println();*/

		
		if(!queue.isEmpty()) {
			if(reservation_job == null) {
				reservation_begin = schedule.getNextFitTime(queue.peek(), t_now);
				reservation_job = queue.poll();
				return t_now;
			} else if (schedule.isFitToSchedule(reservation_job)) {
				reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
				schedule.addToSchedule(reservation_job, t_now);
				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				reservation_begin = Long.MIN_VALUE;
				reservation_job = null;
				return t_now;
			} else {
				SimulationInterface.log("backfilling jobs from unsorted queue that end before: " + reservation_begin);
				for (Job job : queue) {
					if (schedule.isFitToSchedule(job) && isFinishedBeforeReservation(job, t_now)) {
						backfillQueue.add(job);
					}
				}
				backfillQueue.sort(comparatorBackfill);
				if(!backfillQueue.isEmpty()) {
					Job job = backfillQueue.peek();
					backfillQueue.clear();
					SimulationInterface.log("backfilled job: " + job.getJobId() + " running from " + t_now + " to "+ (t_now + job.getRunDuration()));
					queue.remove(job);
					SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
					schedule.addToSchedule(job, t_now);
					return t_now;
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

	@Override
	public void enqueueJob(Job job) {
		if(job.get(Job.USER_ID) == Job.NOT_SET)
			throw new IllegalStateException("Job "+job.getJobId()+" has no User set!");
		queue.add(job);
		t_last_execution = Long.MIN_VALUE;
	}
}
