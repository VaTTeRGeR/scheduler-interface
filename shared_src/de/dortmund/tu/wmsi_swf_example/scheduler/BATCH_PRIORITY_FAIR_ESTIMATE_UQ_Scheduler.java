package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Schedule;
import de.dortmund.tu.wmsi.scheduler.Schedule.JobFinishEntry;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class BATCH_PRIORITY_FAIR_ESTIMATE_UQ_Scheduler implements Scheduler {
	
	//The current schedule of the scheduler
	private Schedule schedule;

	//The schedulers queue
	private LinkedList<Job> queueJob;
	//The priorities of the jobs in the schedulers queue
	private LinkedList<Long> queuePriority;
	//map (userId to time of last submit)
	private HashMap<Long, Long> idToUserLastSubmit;
	//map (userId to last user priority)
	private HashMap<Long, Long> idToUserLastPriority;
	
	//id that is given to a new user or a user that just got out of batch
	private long batch_id_max;
	
	//the number of available resources
	private long res_max = -1;

	//EASY job reservation time
	private long t_reservation_begin;
	//EASY job reservation
	private Job reservation_job;
	
	//user batch threshold
	private long t_threshold;

	//map (userId to user)
	private HashMap<Long, Double> idToUserTimeAccuracy;
	//map (userId to ArrayList) of values for generating the avg accuracy coefficient per user
	private HashMap<Long, ArrayList<Double>> idToUserTimeAccuracySamplesList;
	//The number of values to keep for generating the avg accuracy coefficient per user
	private final int rollingAVGSize = 10;
	
	//everything gets thrown away for the garbage collector and is renewed, is called after configure
	@Override
	public void initialize() {
		queueJob = new LinkedList<Job>();
		queuePriority = new LinkedList<Long>();
		schedule = new Schedule(res_max);
		batch_id_max = 0;
		idToUserLastSubmit = new HashMap<Long, Long>();
		idToUserLastPriority = new HashMap<Long, Long>();
		idToUserTimeAccuracy = new HashMap<Long, Double>();
		idToUserTimeAccuracySamplesList = new HashMap<Long, ArrayList<Double>>();
		t_reservation_begin = Long.MIN_VALUE;
		reservation_job = null;
		t_threshold = 20 * 60;
		if(res_max == -1)
			throw new IllegalStateException("BATCH_PRIORITY_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("BATCH_PRIORITY_Scheduler has a negative resource count configured");
	}

	//reads the config file, gets called before initialize()
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
	
	//the simulation step function
	@Override
	public long simulateUntil(long t_now, long t_target) {
		//update wait time of jobs in queue
		for (Job job : queueJob) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}

		//update wait time of reserved job if possible
		if(reservation_job != null) {
			reservation_job.set(Job.WAIT_TIME, t_now - reservation_job.get(Job.SUBMIT_TIME));
		}
		
		//if the queue contains something or the reservation is set and needs to be checked for scheduling
		if(!queueJob.isEmpty() || reservation_job != null) {
			//this ensures that the reservation time is pushed back
			t_reservation_begin = Math.max(t_now, t_reservation_begin);
			
			//schedule the reserved job as soon as possible
			if (reservation_job != null && schedule.isFitToSchedule(reservation_job)) {
				schedule.addToSchedule(reservation_job, t_now);

				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, reservation_job));
				
				//clear the reservation
				t_reservation_begin = Long.MIN_VALUE;
				reservation_job = null;

				return t_now;
				
			//set a new reservation in none is present
			} else if (reservation_job == null) {
				t_reservation_begin = schedule.getNextFitTime(queueJob.peek(), t_now);
				reservation_job = queueJob.peek();

				//the reserved job is not handled by the queue anymore
				queuePriority.removeFirst();
				queueJob.removeFirst();
				
				return t_now;
			
			//try some backfilling
			} else if(!queueJob.isEmpty()) {
				for (Job job : queueJob) {
					double accuracyFactor = idToUserTimeAccuracy.get(job.get(Job.USER_ID));
					//the job is backfilled if its requested_time*accuracyFactor+t_now is below the reservation_begin and the resources are availeable
					if (schedule.isFitToSchedule(job) && ((long)(job.get(Job.TIME_REQUESTED)*accuracyFactor)) + t_now < t_reservation_begin) {
						schedule.addToSchedule(job, t_now);

						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

						//remove the backfilled job from the queue
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

		//last submit time of user is updated
		final long t_last_submit = idToUserLastSubmit.getOrDefault(userId, Long.MIN_VALUE);
		idToUserLastSubmit.put(userId, job.get(Job.SUBMIT_TIME));

		//true if user has a job in the queue
		final boolean isUserInQueue = isUserInQueue(userId);
		//true if user is still in batch timing
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
		
		//if user is still in a batch
		if(isUserInBatch) {
			//if user has a job in the queue just add it after that job
			if(isUserInQueue) {
				jobPriority = getLastPriorityOfUser(userId);
			//if user has no job in queue but has submitted before
			} else if(idToUserLastPriority.containsKey(userId)){
				jobPriority = idToUserLastPriority.get(userId);
			//this case should actually not be reacheable, but it is a valid fallback
			} else {
				jobPriority = batch_id_max++;
				idToUserLastPriority.put(userId, jobPriority);
			}
		//if user is not in a batch just give him worst priority (=> max_prio) and increment the max priority
		} else {
			jobPriority = batch_id_max++;
			idToUserLastPriority.put(userId, jobPriority);
		}
		
		//sort it into the queue or just add it at the end
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
	
	//recalculate the users accuracy of time requested
	private void updateUserTimeAccuracy(long userId) {
		ArrayList<Double> userAccuracyHistory = idToUserTimeAccuracySamplesList.get(userId);
		double[] values = new double[userAccuracyHistory.size()];
		int i = 0;
		for (double value : userAccuracyHistory) {
			values[i++] = value;
		}
		Arrays.sort(values);
		idToUserTimeAccuracy.put(userId, StatisticalMathHelper.upperQuantile(values));
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
	
	//debug string of the queue
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
