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
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi.util.PropertiesHandler;
import de.dortmund.tu.wmsi_swf_example.scheduler.comparators.JobGroupMaxWaitTimeComparator;

public class GROUPED_WAITTIME_Scheduler implements Scheduler {

	private LinkedList<Job> queue; //Stores the sorted jobs
	private LinkedList<UserJobGroup> groupQueue; //stores the groups except the primary group
	private HashMap<Job, UserJobGroup> jobToGroupMap; //allows for removing jobs from groups

	private Schedule schedule; //the current schedule
	
	private Comparator<UserJobGroup> comparatorJobGroup; //

	private long reservation_begin = Long.MIN_VALUE;
	private Job reservation_job = null;
	private UserJobGroup reservation_jobGroup = null;
	
	private long res_max = -1;
	private long t_threshold = -1;
	
	private long t_last_sort = Long.MIN_VALUE;

	private static final boolean debugQueue = false;
	private static final boolean debugGroup = false;
	
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
		
		comparatorJobGroup = new JobGroupMaxWaitTimeComparator();
		
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
	
		t_threshold = 20*60; // 30 min max interarrival time
	}
	
	public GROUPED_WAITTIME_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		schedule = new Schedule(res_max);
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+(queue.size()));
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());

		if(true && t_now > t_last_sort) {
			t_last_sort = t_now;
			
			if(!groupQueue.isEmpty() && reservation_jobGroup == null) {
				reservation_jobGroup = groupQueue.poll();
				if(debugGroup)
					System.out.println("New primary Job Group selected: "+reservation_jobGroup.user+" - Job "+reservation_jobGroup.jobs.peek());
			}

			if(reservation_jobGroup != null) {
				for (Job job : reservation_jobGroup.jobs) {
					job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
				}
			}
			for (UserJobGroup ujg : groupQueue) {
				for (Job job : ujg.jobs) {
					job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
				}
			}
			
			Collections.sort(groupQueue, comparatorJobGroup);
			
			queue.clear();
			
			if(reservation_jobGroup != null) {
				for (Job job : reservation_jobGroup.jobs) {
					queue.add(job);
				}
			}
			for (UserJobGroup ujg : groupQueue) {
				for (Job job : ujg.jobs) {
					queue.add(job);
				}
			}
		}
		

		
		if(!queue.isEmpty()) {
			
			if(debugQueue) {
				System.out.println();
				System.out.println("----------------------------------------------");
				System.out.println();
				long prev_userid = queue.peek().get(Job.USER_ID);
				for (Job job : queue) {
					if (prev_userid != job.get(Job.USER_ID)) {
						System.out.println();
					}
					prev_userid = job.get(Job.USER_ID);
					long accWaitTime = job.get(Job.TIME_REQUESTED)
							+ StatisticalMathHelper.userAccepteableWaitTime(job.get(Job.TIME_REQUESTED));
					long delta = accWaitTime - job.get(Job.WAIT_TIME);
					System.out.println("WT: " + job.get(Job.WAIT_TIME) + " - USER_TIME: " + job.get(Job.TIME_REQUESTED)
							+ " - ACCWT: " + accWaitTime + " - DELTA: " + delta);
				}
				System.out.println();
				System.out.println("----------------------------------------------");
				System.out.println();
			}

			if(reservation_job == null && reservation_jobGroup != null) { // A new reserved job can be chosen if a primary group is set
				reservation_begin = schedule.getNextFitTime(queue.peek(), t_now);
				reservation_job = queue.poll();

				UserJobGroup ujg = jobToGroupMap.get(reservation_job);
				ujg.jobs.remove(reservation_job);
				if(ujg.jobs.isEmpty()) {
					groupQueue.remove(ujg);
					reservation_jobGroup = null;
				}
				jobToGroupMap.remove(reservation_job);
				
				if(debugGroup)
					System.out.println("Reserving primary spot for user: "+reservation_job.get(Job.USER_ID)+" - Job "+reservation_job);

				return t_now;
			} else if (reservation_job != null && schedule.isFitToSchedule(reservation_job)) { // primary job gets scheduled
				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				if(debugGroup)
					System.out.println("Scheduling reserved job of user: "+reservation_job.get(Job.USER_ID)+" - Job "+reservation_job);
				
				reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
				
				schedule.addToSchedule(reservation_job, t_now);

				reservation_begin = Long.MIN_VALUE;
				reservation_job = null;
				
				return t_now;
			} else { // backfilling from all groups. order is: high to low priority
				SimulationInterface.log("backfilling jobs from queue that end before: " + reservation_begin);
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
						
						if(debugGroup)
							System.out.println("Backfilling job of user: "+job.get(Job.USER_ID)+" - Job "+job);
						
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
		return job.get(Job.TIME_REQUESTED) + t_now < reservation_begin;
	}

	@Override
	public void enqueueJob(Job job) {
		if(job.get(Job.USER_ID) == Job.NOT_SET)
			throw new IllegalStateException("Job "+job.getJobId()+" has no User set!");
		
		t_last_sort = Long.MIN_VALUE;
		
		if(reservation_jobGroup != null && reservation_jobGroup.canPutJob(job)) {
			reservation_jobGroup.putJob(job);
			jobToGroupMap.put(job, reservation_jobGroup);
			return;
		}
		
		for (UserJobGroup ujg : groupQueue) {
			if(ujg.canPutJob(job)) {
				ujg.putJob(job);
				jobToGroupMap.put(job, ujg);
				return;
			}
		}
		
		UserJobGroup ujg = new UserJobGroup(job.get(Job.USER_ID), t_threshold);
		ujg.putJob(job);
		jobToGroupMap.put(job, ujg);
		groupQueue.add(ujg);
	}
}
