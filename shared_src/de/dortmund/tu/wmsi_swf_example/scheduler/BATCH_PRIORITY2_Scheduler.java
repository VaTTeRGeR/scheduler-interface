package de.dortmund.tu.wmsi_swf_example.scheduler;

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

public class BATCH_PRIORITY2_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	private HashMap<Long, UserPriority> idToUserMap;
	
	private Schedule schedule;
	
	private long res_max = -1;

	private long reservation_begin;
	private Job reservation_job;
	
	private long t_threshold;
	
	@Override
	public void initialize() {
		queue = new LinkedList<Job>();
		schedule = new Schedule(res_max);
		idToUserMap = new HashMap<Long, UserPriority>();
		reservation_begin = Long.MIN_VALUE;
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
					if (schedule.isFitToSchedule(job) && job.get(Job.TIME_REQUESTED) + t_now < reservation_begin) {
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

			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.t_end, entry.job));
			
			return entry.t_end;
		}
		
		// nothing happenend
		return t_target;
	}
	
	private void updatePriorities(long t_now) {
		for (UserPriority up : idToUserMap.values()) {
			if(up.t_last_submit + t_threshold < t_now && up.priority > 0) {
				for (UserPriority upOther : idToUserMap.values()) {
					if(upOther.priority > up.priority) {
						upOther.priority--;
					}
				}
				up.priority = 0;
			} else if(up.priority == 0 && up.t_last_submit + t_threshold >= t_now) {
				up.priority = getMaxPriority() + 1;
			}
		}
	}
	
	@Override
	public void enqueueJob(Job job) {
		final long userId = job.get(Job.USER_ID);
		final boolean isUserInQueue = isUserInQueue(userId);

		UserPriority up = idToUserMap.get(userId);
		if(up == null) {
			idToUserMap.put(userId, up = new UserPriority());
		}
		up.t_last_submit = job.get(Job.SUBMIT_TIME);
		
		updatePriorities(SimulationInterface.instance().getCurrentTime());
		
		if(up.priority > 0 && isUserInQueue) {
			Job prevJob = queue.peek();
			for (int i = 1; i < queue.size(); i++) {
				Job otherJob = queue.get(i);
				if(otherJob.get(Job.USER_ID) != userId && prevJob.get(Job.USER_ID) == userId) {

					long prevJobPrio = idToUserMap.get(prevJob.get(Job.USER_ID)).priority;
					System.out.println(getQueueString());
					System.out.println("Added job "+job.get(Job.JOB_ID)+" with prio "+up.priority+" to batch of prio "+prevJobPrio);
					
					queue.add(queue.indexOf(otherJob), job);

					System.out.println(getQueueString());
					System.out.println();
					
					return;
				}
				prevJob = otherJob;
			}
		} else if(up.priority > 0 && !isUserInQueue) {
			for (Job otherJob : queue) {
				long otherJobPrio = idToUserMap.get(otherJob.get(Job.USER_ID)).priority;
				if(otherJobPrio > up.priority || otherJobPrio == 0) {
					System.out.println(getQueueString());
					System.out.println("Inserted job "+job.get(Job.JOB_ID)+" with prio "+up.priority+" before job with prio "+otherJobPrio);
					
					queue.add(queue.indexOf(otherJob), job);
					
					System.out.println(getQueueString());
					System.out.println();
					return;
				}
			}
		}
		System.out.println(getQueueString());
		System.out.println("Added job "+job.get(Job.JOB_ID)+" with prio "+up.priority+" at the end of the queue("+queue.size()+")");
		
		queue.add(job);
		
		System.out.println(getQueueString());
		System.out.println();
	}
	
	private String getQueueString() {
		StringBuilder builder = new StringBuilder();
		for (Job job : queue) {
			long jobId = job.get(Job.JOB_ID);
			long userId = job.get(Job.USER_ID);
			int up = idToUserMap.get(userId).priority;
			builder.append("(");
			builder.append(jobId);
			builder.append(",");
			builder.append(userId);
			builder.append(",");
			builder.append(up);
			builder.append(")");
		}
		return builder.toString();
	}
	
	private boolean isUserInQueue(long userId) {
		for (Job job : queue) {
			if(job.get(Job.USER_ID) == userId)
				return true;
		}
		return false;
	}
	
	private int getMaxPriority() {
		int max = 0;
		for (UserPriority up : idToUserMap.values()) {
			if(up.priority > max)
				max = up.priority;
		}
		return max;
	}
	
	private class UserPriority {
		private int priority;
		private long t_last_submit;
		
		public UserPriority() {
			priority = 0;
			t_last_submit = Long.MIN_VALUE;
		}
	}
}
