package de.dortmund.tu.wmsi.scheduler;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.job.Job;

public class Schedule {
	private long resources_used;
	private long resources_available;
	private LinkedList<JobFinishEntry> scheduleReal;
	private LinkedList<JobFinishEntry> scheduleUser;
	
	public Schedule(long resources_available) {
		scheduleReal = new LinkedList<JobFinishEntry>();
		scheduleUser = new LinkedList<JobFinishEntry>();
		this.resources_used = 0;
		this.resources_available = resources_available;
	}
	
	public void addToSchedule(Job job, long t_start){
		if(!isFitToSchedule(job))
			throw new IllegalStateException("Cannot add job("+job.get(Job.RESOURCES_REQUESTED)+") to the Schedule: "+resources_used+" of "+resources_available+ " resources in use");
		
		scheduleReal.add(new JobFinishEntry(job, t_start, t_start + job.get(Job.RUN_TIME)));
		scheduleUser.add(new JobFinishEntry(job, t_start, t_start + job.get(Job.TIME_REQUESTED)));
		
		sortSchedule();
		
		resources_used += job.get(Job.RESOURCES_REQUESTED);
		
	}
	
	private void sortSchedule(){
		Collections.sort(scheduleReal);
		Collections.sort(scheduleUser);
	}
	
	public JobFinishEntry pollNextFinishedJobEntry(long t_max){
		JobFinishEntry jfe = scheduleReal.getFirst();
		if(!isEmpty() && jfe.t_end <= t_max) {
			scheduleReal.removeFirst();
			scheduleUser.remove(jfe);
			sortSchedule();
			resources_used -= jfe.job.get(Job.RESOURCES_REQUESTED);
			return jfe;
		} else {
			return null;
		}
	}
	
	public JobFinishEntry peekNextFinishedJobEntry(long t_max){
		JobFinishEntry jfe = scheduleReal.getFirst();
		if(!isEmpty() && jfe.t_end <= t_max) {
			return jfe;
		} else {
			return null;
		}
	}
	
	public boolean isFitToSchedule(Job job) {
		return resources_used + job.get(Job.RESOURCES_REQUESTED) <= resources_available;
	}
	
	public long getNextFitTime(Job job, long t_now){
		if(isFitToSchedule(job))
			return t_now;
		
		long res_used_temp = resources_used;
		for (JobFinishEntry jobFinishEntry : scheduleUser) {
			res_used_temp -= jobFinishEntry.job.get(Job.RESOURCES_REQUESTED);
			if(resources_available - res_used_temp  >= job.get(Job.RESOURCES_REQUESTED))
				return jobFinishEntry.t_end;
		}
		return Long.MAX_VALUE;
	}
	
	public void clear(){
		scheduleReal.clear();
		scheduleUser.clear();
		resources_used = 0;
	}
	
	public long getResourcesUsed() {
		return resources_used;
	}

	public long getResourcesAvailable() {
		return resources_available;
	}

	public int getScheduleSize() {
		return scheduleReal.size();
	}

	public boolean isEmpty() {
		return scheduleReal.isEmpty();
	}

	public class JobFinishEntry implements Comparable<JobFinishEntry>{
		public final long t_begin;
		public final long t_end;
		public final Job job;

		public JobFinishEntry(Job job, long t_begin, long t_end) {
			this.t_begin = t_begin;
			this.t_end = t_end;
			this.job = job;
		}
		
		@Override
		public int compareTo(JobFinishEntry other) {
			return (int)(t_end-other.t_end);
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((JobFinishEntry)obj).job.equals(job);
		}
	}
}
