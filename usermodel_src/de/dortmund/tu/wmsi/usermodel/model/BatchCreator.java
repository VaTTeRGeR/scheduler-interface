package de.dortmund.tu.wmsi.usermodel.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.LogisticDistribution;

import de.dortmund.tu.wmsi.job.SWFJob;

public class BatchCreator {

	User user;

	JobCreator jobcreator; 
	
	double batchSizeOne;
	
	double batchSizeMu;
	double batchSizeSigma; 
	
	double interarrivalTimeMu;
	double interarrivalTimeSigma;


	public BatchCreator(User u) {
		this.user = u;

		//TODO MATH
		this.jobcreator 			= u.getJobcreator(); 
		this.batchSizeOne 			= u.getBatchSizeOne();
		this.batchSizeMu 			= u.getBatchSizeMu();
		this.batchSizeSigma 		= u.getBatchSizeSigma();
		this.interarrivalTimeMu 	= u.getInterarrivalTimeMu();
		this.interarrivalTimeSigma 	= u.getBatchSizeSigma();
	}


	/**
	 * Create a batch starting at t_start = 0
	 * 
	 * @return
	 */
	public List<SWFJob> createBatch() {
		return createBatch(0);
	}
	
	
	/**
	 * Create a batch starting at t_start
	 * 
	 * @return
	 */
	public List<SWFJob> createBatch(long t_start) {
		List<SWFJob> batch = new LinkedList<SWFJob>();

		SWFJob j = jobcreator.createJob();
		
		j.set(SWFJob.SUBMIT_TIME, t_start);
		batch.add(j);
		
		System.out.println("Add job to batch at time: " + j.getSubmitTime());
		int CORES = (int)j.get(SWFJob.RESOURCES_REQUESTED);

		int batchsize = this.sampleBatchSize();
		System.out.println("User: " + user.getUserID() + " batchsize: " + batchsize);
		
		for (int i = 1; i <= batchsize-1; i++) {
			t_start += this.sampleInterArrivalTime();
			
			j = jobcreator.createJob();
			j.set(SWFJob.SUBMIT_TIME, t_start);
			j.set(SWFJob.RESOURCES_REQUESTED, CORES);
			batch.add(j);
			
			System.out.println("Add job to batch at time: " + j.getSubmitTime());
		}	

		return batch;
	}


	/**
	 * 
	 * @return
	 */
	private int sampleBatchSize() {
		int size = 0;
		
		double probability = Math.random();
		if ( probability < this.batchSizeOne ) {
			size = 1;
		}
		else {
			LogisticDistribution d = new LogisticDistribution(this.batchSizeMu, this.batchSizeSigma);
			
			probability = Math.random();
			size = (int)d.inverseCumulativeProbability(probability);
			while (size < 1) {
				probability = Math.random();
				size = (int)d.inverseCumulativeProbability(probability);
			}		
		}
		return size;
	}

	/**
	 * Sample interarrival time
	 * 
	 * @return
	 */
	private int sampleInterArrivalTime() {
		LogisticDistribution d = new LogisticDistribution(this.interarrivalTimeMu, this.interarrivalTimeSigma);
		double probability = Math.random();
		int time = (int)d.inverseCumulativeProbability(probability);
		
		if (time < 0 ) {
			time = 10;
		}
		//TODO TODO TODO TODO TODO DUB DI DU DI DU
		
		//while (time < 0) {
		//	probability = Math.random();
		//	time = (int)d.inverseCumulativeProbability(probability);
		//}		
		
		return time;

	}


}
