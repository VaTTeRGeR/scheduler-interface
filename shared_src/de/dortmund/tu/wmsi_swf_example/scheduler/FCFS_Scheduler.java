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

public class FCFS_Scheduler implements Scheduler {

	private LinkedList<Job> queue = new LinkedList<Job>();
	private Schedule schedule;
	private long res_max = -1;
	
	@Override
	public void initialize() {
		if(res_max == -1)
			throw new IllegalStateException("FCFS_Scheduler has no resource count configured");
		else if(res_max < 0)
			throw new IllegalStateException("FCFS_Scheduler has a negative resource count configured");
		schedule = new Schedule(res_max);
	}

	@Override
	public void configure(String configPath) {
		if(configPath == null)
			throw new NullPointerException("configPath should not be null");
		
		SimulationInterface.log("loading: "+configPath);

		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		setMaxResources(properties.getLong("resources", Long.MAX_VALUE));
	}
	
	public FCFS_Scheduler setMaxResources(long res_max) {
		this.res_max = res_max;
		schedule = new Schedule(res_max);
		return this;
	}
	
	@Override
	public long simulateUntil(long t_now, long t_target) {
		SimulationInterface.log(schedule.getResourcesUsed()+"/"+res_max+" resources in use");
		SimulationInterface.log("queue size: "+queue.size());
		SimulationInterface.log("schedule size: "+schedule.getScheduleSize());
		
		//try to process a job from the queue
		if(!queue.isEmpty() && schedule.isFitToSchedule(queue.peek())){
			Job job = queue.poll();
			
			schedule.addToSchedule(job, t_now);

			SimulationInterface.instance().submitEvent(new JobStartedEvent(t_now, job));
			
			SimulationInterface.log("moved job "+job.getJobId()+" from queue to schedule");
			SimulationInterface.log("binding "+job.getResourcesRequested()+" resources");
			
			return t_now;
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
	
	@Override
	public void enqueueJob(Job job) {
		queue.add(job);
		if(job.get(Job.RESOURCES_REQUESTED) < 1)
			throw new IllegalStateException("Job cannot use less than one resource ("+job.get(Job.RESOURCES_REQUESTED)+")");
		if(job.get(Job.RUN_TIME) < 1)
			throw new IllegalStateException("Job cannot run less than one second ("+job.get(Job.RUN_TIME)+")");
	}
}
