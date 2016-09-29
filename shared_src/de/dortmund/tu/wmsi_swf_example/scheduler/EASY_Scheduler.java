package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Schedule;
import de.dortmund.tu.wmsi.scheduler.Schedule.JobFinishEntry;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class EASY_Scheduler implements Scheduler {

	private LinkedList<Job> queue;
	
	private Schedule schedule;
	
	private long res_max = -1;
	private long reservation_begin;
	private Job reservation_job;
	
	@Override
	public void initialize() {
		queue = new LinkedList<Job>();
		schedule = new Schedule(res_max);
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
	
	@Override
	public void enqueueJob(Job job) {
		queue.add(job);
	}
}
