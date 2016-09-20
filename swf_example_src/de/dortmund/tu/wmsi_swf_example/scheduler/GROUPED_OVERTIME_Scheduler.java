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
import de.dortmund.tu.wmsi_swf_example.scheduler.comparators.JobGroupExceedWaitTimeComparatorAbsolute;

public class GROUPED_OVERTIME_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	private LinkedList<UserJobGroup> groupQueue;
	private HashMap<Job, UserJobGroup> jobToGroupMap;

	private Schedule schedule;
	
	private Comparator<Job> comparatorJob;
	private Comparator<UserJobGroup> comparatorJobGroup;

	private long reservation_begin = Long.MIN_VALUE;
	private Job reservation_job = null;
	private UserJobGroup reservation_jobGroup = null;
	
	private long res_max = -1;
	
	private long t_last_sort = Long.MIN_VALUE;
	
	@Override
	public void initialize() {
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
		
		queue = new LinkedList<Job>();
		groupQueue = new LinkedList<UserJobGroup>();
		jobToGroupMap = new HashMap<Job, UserJobGroup>();
		
		schedule = new Schedule(res_max);
		
		comparatorJob = new JobExceedWaitTimeComparatorAbsolute();
		comparatorJobGroup = new JobGroupExceedWaitTimeComparatorAbsolute();
		
		reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		reservation_jobGroup = null;

		t_last_sort = Long.MIN_VALUE;
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
	}
	
	public GROUPED_OVERTIME_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		schedule = new Schedule(res_max);
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+(queue.size()));
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());

		if(t_now > t_last_sort) {
			t_last_sort = t_now;
			
			Collections.sort(groupQueue, comparatorJobGroup);

			if(!groupQueue.isEmpty() && reservation_jobGroup == null) {
				reservation_jobGroup = groupQueue.poll();
				for (Job job : reservation_jobGroup.jobs) {
					job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
				}
				reservation_jobGroup.sort(comparatorJob);
				
				System.out.println("New Job Group selected: "+reservation_jobGroup.user);
			}
			
			queue.clear();
			
			if(reservation_jobGroup != null) {
				for (Job job : reservation_jobGroup.jobs) {
					queue.add(job);
				}
			}
			
			for (UserJobGroup ujg : groupQueue) {
				for (Job job : ujg.jobs) {
					job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
				}
				
				ujg.sort(comparatorJob);
				
				for (Job job : ujg.jobs) {
					queue.add(job);
				}
			}
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

				UserJobGroup ujg = jobToGroupMap.get(reservation_job);
				ujg.jobs.remove(reservation_job);
				if(ujg.jobs.isEmpty()) {
					groupQueue.remove(ujg);
					reservation_jobGroup = null;
				}
				jobToGroupMap.remove(reservation_job);
				
				return t_now;
			} else if (schedule.isFitToSchedule(reservation_job)) {
				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				System.out.println("Scheduling reserved job of user: "+reservation_job.get(Job.USER_ID));
				
				schedule.addToSchedule(reservation_job, t_now);

				reservation_begin = Long.MIN_VALUE;
				reservation_job = null;
				
				return t_now;
			} else {
				SimulationInterface.log("backfilling jobs from unsorted queue that end before: " + reservation_begin);
				for (Job job : queue) {
					if (schedule.isFitToSchedule(job) && isFinishedBeforeReservation(job, t_now)) {
						SimulationInterface.log("backfilled job: " + job.getJobId() + " running from " + t_now + " to " + (t_now + job.getRunDuration()));
						
						queue.remove(job);

						UserJobGroup ujg = jobToGroupMap.get(job);
						ujg.jobs.remove(job);
						if(ujg.jobs.isEmpty()) {
							groupQueue.remove(ujg);
						}
						jobToGroupMap.remove(job);
						
						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
						
						System.out.println("Backfilling job of user: "+job.get(Job.USER_ID));
						
						schedule.addToSchedule(job, t_now);
						
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

	@Override
	public void enqueueJob(Job job) {
		if(job.get(Job.USER_ID) == Job.NOT_SET)
			throw new IllegalStateException("Job "+job.getJobId()+" has no User set!");
		t_last_sort = Long.MIN_VALUE;
		for (UserJobGroup ujg : groupQueue) {
			if(ujg.canPutJob(job)) {
				ujg.putJob(job);
				jobToGroupMap.put(job, ujg);
				return;
			}
		}
		UserJobGroup ujg = new UserJobGroup(job.get(Job.USER_ID));
		ujg.putJob(job);
		jobToGroupMap.put(job, ujg);
		groupQueue.add(ujg);
	}
}
