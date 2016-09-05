package de.dortmund.tu.wmsi.usermodel.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.LogisticDistribution;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.Job;

public class BatchCreator {

	User user;

	JobCreator jobcreator; 
	
	double batchSizeOne;
	
	double batchSizeMu;
	double batchSizeSigma; 
	
	double interarrivalTimeMu;
	double interarrivalTimeSigma;

	Random random = new Random();

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
	public List<Job> createBatch() {
		return createBatch(SimulationInterface.instance().getSimulationBeginTime());
	}
	
	
	/**
	 * Create a batch starting at t_start
	 * 
	 * @return
	 */
	public List<Job> createBatch(long t_start) {
		List<Job> batch = new LinkedList<Job>();

		Job j = jobcreator.createJob();
		
		j.set(Job.SUBMIT_TIME, t_start);
		batch.add(j);
		
		//System.out.println("Add job to batch at time: " + j.getSubmitTime());
		int CORES = (int)j.get(Job.RESOURCES_REQUESTED);

		int batchsize = this.sampleBatchSize();
		//System.out.println("User: " + user.getUserId() + " batchsize: " + batchsize);
		
		for (int i = 1; i < batchsize; i++) {
			t_start += this.sampleInterArrivalTime();
			
			j = jobcreator.createJob();
			j.set(Job.SUBMIT_TIME, t_start);
			j.set(Job.RESOURCES_REQUESTED, CORES);
			j.set(Job.USER_ID, user.getUserId());
			
			double gaussRandom = random.nextGaussian()/4d+0.75d;
			gaussRandom = Math.min(gaussRandom, 1);
			gaussRandom = Math.max(gaussRandom, 0);
			
			long t_run = j.get(Job.TIME_REQUESTED);
			
			j.set(Job.RUN_TIME, (long)(t_run * gaussRandom));
			
			if(j.get(Job.RUN_TIME) >= 1)
				batch.add(j);
			
			//System.out.println("Add job to batch at time: " + j.getSubmitTime() + " with "+j.get(Job.RESOURCES_REQUESTED)+" resources");
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
