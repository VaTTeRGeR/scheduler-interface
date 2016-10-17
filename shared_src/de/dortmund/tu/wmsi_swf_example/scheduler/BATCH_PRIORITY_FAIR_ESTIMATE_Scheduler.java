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

public class BATCH_PRIORITY_FAIR_ESTIMATE_Scheduler implements Scheduler {

	private LinkedList<Job> queueJob;
	private LinkedList<Long> queuePriority;
	private HashMap<Long, Long> idToUserLastSubmit;
	private HashMap<Long, Long> idToUserLastPriority;
	private HashMap<Long, Double> idToUserLastTimeAccuracy;
	private HashMap<Long, ArrayList<Double>> idToUserLastTimeAccuracyList;
	private long batch_id_max;
	
	private Schedule schedule;
	
	private long res_max = -1;

	private long t_reservation_begin;
	private Job reservation_job;
	
	private long t_threshold;

	private int userTimeAccuracyAverageSize = 5;
	
	@Override
	public void initialize() {
		queueJob = new LinkedList<Job>();
		queuePriority = new LinkedList<Long>();
		schedule = new Schedule(res_max);
		batch_id_max = 0;
		idToUserLastSubmit = new HashMap<Long, Long>();
		idToUserLastPriority = new HashMap<Long, Long>();
		idToUserLastTimeAccuracy = new HashMap<Long, Double>();
		idToUserLastTimeAccuracyList = new HashMap<Long, ArrayList<Double>>();
		t_reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		t_threshold = 20 * 60;
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
		for (Job job : queueJob) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}

		if(reservation_job != null) {
			reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
		}
		
		if(!queueJob.isEmpty() || reservation_job != null) {
			t_reservation_begin = Math.max(t_now, t_reservation_begin);
			if (reservation_job != null && schedule.isFitToSchedule(reservation_job)) {
				schedule.addToSchedule(reservation_job, t_now);

				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				t_reservation_begin = Long.MIN_VALUE;
				reservation_job = null;

				return t_now;
				
			} else if (reservation_job == null) {
				t_reservation_begin = schedule.getNextFitTime(queueJob.peek(), t_now);
				reservation_job = queueJob.peek();
				queuePriority.removeFirst();
				queueJob.removeFirst();
				
				return t_now;
			
			} else if(!queueJob.isEmpty()) {
				for (Job job : queueJob) {
					double accuracyFactor = idToUserLastTimeAccuracy.get(job.get(Job.USER_ID));
					if (schedule.isFitToSchedule(job) && ((long)(job.get(Job.TIME_REQUESTED)*accuracyFactor)) + t_now < t_reservation_begin) {
						schedule.addToSchedule(job, t_now);

						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

						int removeIndex = queueJob.indexOf(job);
						queuePriority.remove(removeIndex);
						queueJob.remove(removeIndex);

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
		final long t_now = SimulationInterface.instance().getCurrentTime();

		final long t_last_submit = idToUserLastSubmit.getOrDefault(userId, Long.MIN_VALUE);
		idToUserLastSubmit.put(userId, job.get(Job.SUBMIT_TIME));

		final boolean isUserInQueue = isUserInQueue(userId);
		final boolean isUserInBatch = t_now - t_last_submit < t_threshold;
		
		ArrayList<Double> lastUserTimeAccuracyList = idToUserLastTimeAccuracyList.get(userId);
		if(lastUserTimeAccuracyList == null) {
			lastUserTimeAccuracyList = new ArrayList<Double>();
			idToUserLastTimeAccuracyList.put(userId, lastUserTimeAccuracyList);
			for (int i = 0; i < userTimeAccuracyAverageSize; i++) {
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
		
		for (int i = 0; i< queueJob.size(); i++) {
			if(jobPriority < queuePriority.get(i)) {
				queueJob.add(i, job);
				queuePriority.add(i, jobPriority);
				return;
			}
		}
		queueJob.add(job);
		queuePriority.add(jobPriority);
	}
	
	private void updateUserTimeAccuracy(long userId) {
		double userLastTimeAccuracySum = 0;
		for(double value : idToUserLastTimeAccuracyList.get(userId)) {
			userLastTimeAccuracySum += value;
		}
		idToUserLastTimeAccuracy.put(userId, userLastTimeAccuracySum/userTimeAccuracyAverageSize);
	}
	
	private void updateUserTimeAccuracy(Job job) {
		final long userId = job.get(Job.USER_ID);
		final double t_req = job.get(Job.TIME_REQUESTED);
		final double t_run = job.get(Job.RUN_TIME);
		
		ArrayList<Double> accuracyList = idToUserLastTimeAccuracyList.get(userId);
		accuracyList.remove(0);
		accuracyList.add(t_run/t_req);
		
		updateUserTimeAccuracy(userId);
	}
	
	private boolean isUserInQueue(long userId) {
		for (Job job : queueJob) {
			if(job.get(Job.USER_ID) == userId)
				return true;
		}
		return false;
	}
	
	private long getLastPriorityOfUser(long userId){
		for(int i = queueJob.size() - 1; i >= 0; i--) {
			Job job = queueJob.get(i);
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
