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

public class BATCH_PRIORITY_FAIR_ESTIMATE_BACKFILL_ONLY_Scheduler implements Scheduler {

	private LinkedList<Job> queueJobFCFSSorted;
	private LinkedList<Job> queueJobPrioSorted;
	private LinkedList<Long> queuePriority;

	private HashMap<Long, Long> idToUserLastSubmit;
	private HashMap<Long, Long> idToUserLastPriority;
	private long batch_id_max;
	
	private Schedule schedule;
	
	private long res_max = -1;

	private long reservation_begin;
	private Job reservation_job;
	
	private long t_threshold;

	private HashMap<Long, Double> idToUserTimeAccuracy;
	private HashMap<Long, ArrayList<Double>> idToUserTimeAccuracySamplesList;
	private final int rollingAVGSize = 5;
	
	@Override
	public void initialize() {
		queueJobFCFSSorted = new LinkedList<Job>();
		queueJobPrioSorted = new LinkedList<Job>();
		queuePriority = new LinkedList<Long>();
		schedule = new Schedule(res_max);
		batch_id_max = 0;
		idToUserLastSubmit = new HashMap<Long, Long>();
		idToUserLastPriority = new HashMap<Long, Long>();
		idToUserTimeAccuracy = new HashMap<Long, Double>();
		idToUserTimeAccuracySamplesList = new HashMap<Long, ArrayList<Double>>();
		reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		t_threshold = 60 * 60;
		if(res_max == -1)
			throw new IllegalStateException("BATCH_PRIORITY_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("BATCH_PRIORITY_Scheduler has a negative resource count configured");
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
		for (Job job : queueJobPrioSorted) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		if(reservation_job != null) {
			reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
		}
		
		if(!queueJobPrioSorted.isEmpty() || reservation_job != null) {
			if (reservation_job != null && schedule.isFitToSchedule(reservation_job)) {
				schedule.addToSchedule(reservation_job, t_now);

				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				reservation_begin = Long.MIN_VALUE;
				reservation_job = null;

				return t_now;
				
			} else if (reservation_job == null) {
				reservation_job = queueJobFCFSSorted.peek();
				reservation_begin = schedule.getNextFitTime(reservation_job, t_now);

				queueJobFCFSSorted.removeFirst();
				
				queueJobPrioSorted.remove(reservation_job);
				queuePriority.remove(reservation_job);
				
				return t_now;
			
			} else if(!queueJobPrioSorted.isEmpty()) {
				for (Job job : queueJobPrioSorted) {
					double accuracyFactor = idToUserTimeAccuracy.get(job.get(Job.USER_ID));
					if (schedule.isFitToSchedule(job) && ((long)(job.get(Job.TIME_REQUESTED)*accuracyFactor)) + t_now < reservation_begin) {
						schedule.addToSchedule(job, t_now);

						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

						queueJobFCFSSorted.remove(job);
						
						int removeIndex = queueJobPrioSorted.indexOf(job);
						queuePriority.remove(removeIndex);
						queueJobPrioSorted.remove(removeIndex);

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
		queueJobFCFSSorted.add(job);
		
		final long userId = job.get(Job.USER_ID);
		final long t_now = SimulationInterface.instance().getCurrentTime();

		final long t_last_submit = idToUserLastSubmit.getOrDefault(userId, Long.MIN_VALUE);
		idToUserLastSubmit.put(userId, job.get(Job.SUBMIT_TIME));

		final boolean isUserInQueue = isUserInQueue(userId);
		final boolean isUserInBatch = t_now - t_last_submit < t_threshold;
		
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

		long jobPriority;
		
		if(isUserInBatch) {
			if(isUserInQueue) {
				jobPriority = getLastPriorityOfUser(userId);
			} else if(idToUserLastPriority.containsKey(userId)){
				jobPriority = idToUserLastPriority.get(userId);
			} else {
				jobPriority = batch_id_max++;
				idToUserLastPriority.put(userId, jobPriority);
			}
		} else {
			jobPriority = batch_id_max++;
			idToUserLastPriority.put(userId, jobPriority);
		}
		
		for (int i = 0; i< queueJobPrioSorted.size(); i++) {
			if(jobPriority < queuePriority.get(i)) {
				queueJobPrioSorted.add(i, job);
				queuePriority.add(i, jobPriority);
				return;
			}
		}
		queueJobPrioSorted.add(job);
		queuePriority.add(jobPriority);
	}
	
	//recalculate the users accuracy of time requested
	private void updateUserTimeAccuracy(long userId) {
		double userLastTimeAccuracySum = 0;
		for(double value : idToUserTimeAccuracySamplesList.get(userId)) {
			userLastTimeAccuracySum += value;
		}
		idToUserTimeAccuracy.put(userId, userLastTimeAccuracySum/rollingAVGSize);
	}
	
	//recalculate the users accuracy of time requested after popping his oldest job and pushing the new one to the rolling average
	private void updateUserTimeAccuracy(Job job) {
		final long userId = job.get(Job.USER_ID);
		final double t_req = job.get(Job.TIME_REQUESTED);
		final double t_run = job.get(Job.RUN_TIME);
		
		//exclude jobs that "failed" (t_run == 1)
		if(t_run > 1) {
			ArrayList<Double> accuracyList = idToUserTimeAccuracySamplesList.get(userId);
			accuracyList.remove(0);
			accuracyList.add(t_run/t_req);
		
			updateUserTimeAccuracy(userId);
		}
	}
	
	private boolean isUserInQueue(long userId) {
		for (Job job : queueJobPrioSorted) {
			if(job.get(Job.USER_ID) == userId)
				return true;
		}
		return false;
	}
	
	private long getLastPriorityOfUser(long userId){
		for(int i = queueJobPrioSorted.size() - 1; i >= 0; i--) {
			Job job = queueJobPrioSorted.get(i);
			if(job.get(Job.USER_ID) == userId) {
				return queuePriority.get(i);
			}
		}
		throw new IllegalAccessError("Cannot determine last priority of user "+userId+", he is not in the queue.");
	}
	
	/*private String getQueueString() {
		StringBuilder builder = new StringBuilder();
		for (Job job : queueJob) {
			long jobId = job.get(Job.JOB_ID);
			long userId = job.get(Job.USER_ID);
			long up = idToUserMap.get(userId);
			builder.append("(");
			builder.append(jobId);
			builder.append(",");
			builder.append(userId);
			builder.append(",");
			builder.append(up);
			builder.append(")");
		}
		return builder.toString();
	}*/
}
