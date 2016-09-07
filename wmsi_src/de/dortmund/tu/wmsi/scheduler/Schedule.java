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
		this.resources_used = 0;
		this.resources_available = resources_available;
	}
	
	public void addToSchedule(Job job, long t_start){
		if(resources_used + job.get(Job.RESOURCES_REQUESTED) > resources_available)
			throw new IllegalStateException("Cannot add job("+job.get(Job.RESOURCES_REQUESTED)+") to the Schedule: "+resources_used+" of "+resources_available+ " resources in use");
		
		scheduleReal.add(new JobFinishEntry(job, t_start + job.get(Job.RUN_TIME)));
		scheduleReal.add(new JobFinishEntry(job, t_start + job.get(Job.TIME_REQUESTED)));
		
		Collections.sort(scheduleReal);
		Collections.sort(scheduleUser);
		
		resources_used += job.get(Job.RESOURCES_REQUESTED);
		
	}
	
	public Job pullNextFinishedJob(long t_max){
		JobFinishEntry job = scheduleReal.getFirst();
		if(job.t_end <= t_max) {
			scheduleReal.removeFirst();
			return job.job;
		} else {
			return null;
		}
	}
	
	public Job peekNextFinishedJob(long t_max){
		JobFinishEntry job = scheduleReal.getFirst();
		if(job.t_end <= t_max) {
			return job.job;
		} else {
			return null;
		}
	}
	
	public boolean canRunNow(Job job) {
		return resources_used + job.get(Job.RESOURCES_REQUESTED) <= resources_available;
	}
	
	public long getNextFitTime(Job job, long t_now){
		if(canRunNow(job))
			return t_now;
		
		long res_used_temp = resources_used;
		for (JobFinishEntry jobFinishEntry : scheduleUser) {//TODO sort by user finish time
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
	
	private class JobFinishEntry implements Comparable<JobFinishEntry>{
		private long t_end;
		private Job job;

		public JobFinishEntry(Job job, long t_end) {
			this.t_end = t_end;
			this.job = job;
		}
		
		//SORT ASCENDING BY REAL FINISH TIME
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
