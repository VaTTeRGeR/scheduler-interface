package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class BACKFILL_Scheduler implements Scheduler {

	private LinkedList<Job> queue = new LinkedList<Job>();
	private LinkedList<JobFinishEntry> schedule = new LinkedList<JobFinishEntry>();
	private LinkedList<Long> reservationTime = new LinkedList<Long>();
	private LinkedList<Job> reservationJob = new LinkedList<Job>();
	private long res_max = -1, res_used = 0;

	@Override
	public void initialize() {
		res_used = 0;
		if(res_max == -1)
			throw new IllegalStateException("BACKFILL_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("BACKFILL_Scheduler has a negative resource count configured");
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
	}
	
	public BACKFILL_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(res_used+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.size());
		
		if(!queue.isEmpty()) {
			if(isJobFits(queue.peek())) {
				addToSchedule(queue.poll(), t_now);
				return t_now;
			} else if(!hasReservation(queue.peek())) {
				reserve(queue.peek(), getReservationTime(queue.peek()));
			} else {
				long endTimeLimit = 0; //TODO add
				SimulationInterface.log("backfilling jobs");
				for (Job job : queue) {
					if (isJobFits(job) && job.getRunDuration() + t_now < endTimeLimit) {
						SimulationInterface.log("backfilled job: " + job.getJobId() + " running from "+t_now+" to "+(t_now+job.getRunDuration()));
						queue.remove(job);
						addToSchedule(job, t_now);
						return t_now;
					}
				}
			}
		}
		
		// a job is going to be finished before t_target is reached
		if(!schedule.isEmpty() && t_target >= schedule.peek().end) {
			
			JobFinishEntry entry = schedule.poll();
			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.end, entry.job));
			
			res_used -= entry.job.getResourcesRequested();
			
			SimulationInterface.log("finished job "+entry.job.getJobId()+" at "+entry.end);
			SimulationInterface.log("freeing "+entry.job.getResourcesRequested()+" resources");
			
			return entry.end;
		}
		
		SimulationInterface.log("scheduler idled");

		// nothing happenend
		return t_target;
	}
	
	private void removeReservation(Job job) {
		reservationTime.remove(reservationJob.indexOf(job));
		reservationJob.remove(job);
	}
	
	private void reserve(Job job, long t_reserve){
		if(t_reserve < Long.MAX_VALUE) {
			reservationJob.add(job);
			reservationTime.add(t_reserve);
		}
	}
	
	private boolean hasReservation(Job job) {
		return reservationJob.contains(job);
	}

	private long getReservationTime(Job job) {
		return reservationTime.get(reservationJob.indexOf(job));
	}
	
	private boolean isJobFits(Job job) {
		return res_max >= res_used + job.getResourcesRequested();
	}
	
	private long getFitTime(Job job) {
		long res_left = job.getResourcesRequested() - (res_max - res_used);
		if(schedule.isEmpty())
			return SimulationInterface.instance().getCurrentTime();
		for (JobFinishEntry jfe : schedule) {
			res_left -= jfe.job.getResourcesRequested();
			if (res_left <= 0) {
				return jfe.end;
			}
		}
		return Long.MAX_VALUE;
	}
	
	private void addToSchedule(Job job, long t_now) {
		if(!isJobFits(job))
			throw new IllegalStateException("Job "+job+" didn't fit on the schedule, but was added to it");
		schedule.add(new JobFinishEntry(t_now + job.getRunDuration(), job));
		res_used += job.getResourcesRequested();

		Collections.sort(schedule);

		SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));

		SimulationInterface.log("moved job "+job.getJobId()+" from queue to schedule");
		SimulationInterface.log("binding "+job.getResourcesRequested()+" resources");
	}
	
	@Override
	public void enqueueJob(Job job) {
		if(hasReservation(job))
			throw new IllegalStateException("Job "+job.getJobId()+" already added to the scheduler");
		queue.add(job);
		reserve(job, getFitTime(job));
	}

	private class JobFinishEntry implements Comparable<JobFinishEntry>{
		private long end;
		private Job job;

		public JobFinishEntry(long end, Job job) {
			this.end = end;
			this.job = job;
		}
		
		@Override
		public int compareTo(JobFinishEntry other) {
			return (int)(end-other.end);
		}
	}
}
