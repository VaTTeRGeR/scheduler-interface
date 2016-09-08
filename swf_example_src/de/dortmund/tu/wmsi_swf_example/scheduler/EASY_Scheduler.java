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

	private LinkedList<Job> queue = new LinkedList<Job>();
	private Schedule schedule;
	private long res_max = -1;
	private long reservation_begin = Long.MIN_VALUE;
	
	@Override
	public void initialize() {
		schedule = new Schedule(res_max);
		reservation_begin = Long.MIN_VALUE;
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
	
	public EASY_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());
		
		for (Job job : queue) {
			job.set(Job.WAIT_TIME, t_now - job.get(Job.SUBMIT_TIME));
		}
		
		if(!queue.isEmpty()) {
			if (schedule.isFitToSchedule(queue.peek())) {
				SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, queue.peek()));
				schedule.addToSchedule(queue.poll(), t_now);
				reservation_begin = Long.MIN_VALUE;
				return t_now;
			} else if (reservation_begin == Long.MIN_VALUE) {
				reservation_begin = schedule.getNextFitTime(queue.peek(), t_now);
			} else {
				SimulationInterface.log("backfilling jobs that end before: " + reservation_begin);
				for (Job job : queue) {
					if (schedule.isFitToSchedule(job) && job.getRunDuration() + t_now < reservation_begin && !job.equals(queue.peek())) {
						SimulationInterface.log("backfilled job: " + job.getJobId() + " running from "+t_now+" to "+(t_now+job.getRunDuration()));
						queue.remove(job);
						schedule.addToSchedule(job, t_now);
						SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
						return t_now;
					}
				}
			}
		}
		
		// a job is going to be finished before t_target is reached
		if(!schedule.isEmpty() && schedule.peekNextFinishedJobEntry(t_target) != null) {
			
			JobFinishEntry entry = schedule.pollNextFinishedJobEntry(t_target);

			SimulationInterface.instance().submitEvent(new JobFinishedEvent(entry.t_end, entry.job));
			
			SimulationInterface.log("finished job "+entry.job.getJobId()+" at "+entry.t_end);
			SimulationInterface.log("freeing "+entry.job.getResourcesRequested()+" resources");
			
			return entry.t_end;
		}
		
		SimulationInterface.log("scheduler idled");

		// nothing happenend
		return t_target;
	}
	
	@Override
	public void enqueueJob(Job job) {
		queue.add(job);
	}
}
