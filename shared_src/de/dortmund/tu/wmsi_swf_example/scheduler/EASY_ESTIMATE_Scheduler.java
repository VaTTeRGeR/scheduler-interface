package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.ArrayList;
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

public class EASY_ESTIMATE_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	
	private Schedule schedule;
	
	private long res_max = -1;
	private long reservation_begin;
	private Job reservation_job;

	private HashMap<Long, Double> idToUserTimeAccuracy;
	private HashMap<Long, ArrayList<Double>> idToUserTimeAccuracySamplesList;
	private final int rollingAVGSize = 5;
	
	@Override
	public void initialize() {
		queue = new LinkedList<Job>();
		
		schedule = new Schedule(res_max);
		
		idToUserTimeAccuracy = new HashMap<Long, Double>();
		idToUserTimeAccuracySamplesList = new HashMap<Long, ArrayList<Double>>();

		reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		
		if(res_max == -1)
			throw new IllegalStateException("EASY_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("EASY_Scheduler has a negative resource count configured");
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
	}
	
	public void setMaxResources(long res_max) {
		this.res_max = res_max;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		for (Job job : queue) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		if(reservation_job != null) {
			reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
		}
		
		if(!queue.isEmpty() || reservation_job != null) {
			if (reservation_job != null && schedule.isFitToSchedule(reservation_job)) {
				schedule.addToSchedule(reservation_job, t_now);

				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				reservation_begin = Long.MIN_VALUE;
				reservation_job = null;

				return t_now;
				
			} else if (reservation_job == null) {
				reservation_begin = schedule.getNextFitTime(queue.peek(), t_now);
				reservation_job = queue.poll();
				
				return t_now;
			
			} else if(!queue.isEmpty()) {
				for (Job job : queue) {
					double accuracyFactor = idToUserTimeAccuracy.get(job.get(Job.USER_ID));
					if (schedule.isFitToSchedule(job) && ((long)(job.get(Job.TIME_REQUESTED)*accuracyFactor)) + t_now < reservation_begin) {
						schedule.addToSchedule(job, t_now);

						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

						queue.remove(job);

						return t_now;
					}
				}
			}
		}
		
		// a job is going to be finished before t_target is reached
		if(!schedule.isEmpty() && schedule.peekNextFinishedJobEntry(t_target) != null) {
			JobFinishEntry entry = schedule.pollNextFinishedJobEntry(t_target);

			updateUserTimeAccuracy(entry.job);

			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.t_end, entry.job));
			
			return entry.t_end;
		}
		
		// nothing happenend
		return t_target;
	}
	
	@Override
	public void enqueueJob(Job job) {
		final long userId = job.get(Job.USER_ID);
		//update the users time request accuracy
		ArrayList<Double> lastUserTimeAccuracyList = idToUserTimeAccuracySamplesList.get(userId);
		if(lastUserTimeAccuracyList == null) {
			lastUserTimeAccuracyList = new ArrayList<Double>();
			idToUserTimeAccuracySamplesList.put(userId, lastUserTimeAccuracyList);
			for (int i = 0; i < rollingAVGSize; i++) {
				lastUserTimeAccuracyList.add(1.0);
			}
		}
		updateUserTimeAccuracy(userId);

		queue.add(job);
	}

	private void updateUserTimeAccuracy(long userId) {
		double userLastTimeAccuracySum = 0;
		for(double value : idToUserTimeAccuracySamplesList.get(userId)) {
			userLastTimeAccuracySum += value;
		}
		idToUserTimeAccuracy.put(userId, userLastTimeAccuracySum/rollingAVGSize);
	}
	
	private void updateUserTimeAccuracy(Job job) {
		final long userId = job.get(Job.USER_ID);
		final double t_req = job.get(Job.TIME_REQUESTED);
		final double t_run = job.get(Job.RUN_TIME);
		
		if(t_run > 1) {
			ArrayList<Double> accuracyList = idToUserTimeAccuracySamplesList.get(userId);
			accuracyList.remove(0);
			accuracyList.add(t_run/t_req);
		
			updateUserTimeAccuracy(userId);
		}
	}
}
