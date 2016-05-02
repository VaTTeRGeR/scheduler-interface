package de.irf.it.rmg.research.workload.usermodel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.irf.it.rmg.core.teikoku.job.Job;
import de.irf.it.rmg.core.teikoku.runtime.events.JobStartedEvent;
import de.irf.it.rmg.core.teikoku.workload.swf.SWFJob;
import de.irf.it.rmg.research.workload.util.UserModelTimeHelper;
import de.irf.it.rmg.util.time.Clock;
import de.irf.it.rmg.util.time.TimeHelper;

public class Session {
	

	//private JobCreator jobcreator;
	private User user;
	
	private BatchCreator batchcreator;
	private Behavior behavior;
	
	private Queue<Job> jobQueue;
	private Queue<Job> notFinishedJobs;
	
	private boolean terminated;
	
	long startTimeOfCurrentBatch = -1;


	public Session(User user) {
		//this.jobcreator 	= user.getJobcreator();
		this.user = user;
		
		this.behavior 		= user.getBehavior();
		this.batchcreator 	= user.getBatchcreator();

		this.terminated = false;

		this.jobQueue 			= new LinkedList<Job>();
		this.notFinishedJobs 	= new LinkedList<Job>();
	}

	/**
	 * Start the session at t_start
	 * 
	 * @param t_start
	 */
	public void start(long t_start) {
		this.addBatchToSession(t_start);
	}
	
	/**
	 * Add a new batch to session starting at t_startBatch.
	 * 
	 * @param t_startBatch
	 */
	private void addBatchToSession(long t_startBatch) {
		System.out.println("NEW BATCH ADDED AT " + UserModelTimeHelper.time(t_startBatch) + " USER: " + user.getUserId());
		
		List<SWFJob> batch = batchcreator.createBatch(t_startBatch);
		
		startTimeOfCurrentBatch = batch.get(0).getSubmitTime();
		
		this.jobQueue.addAll(batch);
		this.notFinishedJobs.addAll(batch);
	}
	
	
	/**
	 * Receive information from class User when a job started.
	 * If job is from this session, delete it from currently pending bjobs in batch.
	 * If it was the last job waiting for, decide to submit new batch according 
	 * to behavior or terminate session.
	 * 
	 * @param event
	 */
	public void deliverEvent(JobStartedEvent event) {
		long now = TimeHelper.toLongValue(Clock.instance().now()) / 1000;
		SWFJob runningJob = (SWFJob) event.getStartedJob();
		
		long submittime = runningJob.getSubmitTime();
		long responsetime = now - submittime + (long)runningJob.getRequestedTime();

		System.out.println("Starting job size: " +runningJob.getRequestedNumberOfProcessors() + " at time: " + now + " finishing at: " + (now+ runningJob.getRequestedTime()) );
		
		boolean foundDependency = false;
		for (Job j: this.notFinishedJobs) {		//look for job in dependency list						
			if (j == runningJob) {
				foundDependency = true;
			}
		}
		if (foundDependency) {					//delete job if it was found
			this.notFinishedJobs.remove(runningJob);

			if (this.notFinishedJobs.isEmpty()) {	//session not terminated and last job in batch finished
				long starttimeNextBatch = behavior.startTimeNextBatch(submittime,responsetime); 

				if (starttimeNextBatch == -1) {		//terminate
					this.terminated = true;
				}
				else { 								//continue session
					System.out.println("Continue Session. USER: " + user.getUserId());
					
					addBatchToSession(starttimeNextBatch);
				}
			}
		}
		
	}
	
	//Getters and Setters
	public Queue<Job> getJobQueue() {
		return this.jobQueue;
	}

	public boolean isTerminated() {
		return this.terminated;
	}
	
}
