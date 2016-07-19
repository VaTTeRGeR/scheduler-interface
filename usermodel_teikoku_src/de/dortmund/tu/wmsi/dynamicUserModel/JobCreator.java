package de.dortmund.tu.wmsi.dynamicUserModel;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

import de.dortmund.tu.wmsi.job.SWFJob;

public class JobCreator {
	
	private User user;
	
	private int numberOfGeneratedJobs;
	
	private static int PROCESSORS[] = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 8192*2, 8192*4, 8192*8, 8192*16, 8192*32, 8192*64 };
	
	private int numberOfProvidedResources;
	
	private double coreDistribution[];
	private double averageRuntime[];
	private double runtimeMuhat[];
	private double runtimeSigmahat[];
	

	public JobCreator(User user) {
		this.user = user;
		
		this.numberOfProvidedResources 	= user.getNumberOfProvidedResources();
		
		initializeCoreDistribution(user.getDistributionString());
		initializeRuntimeDistribution(user.getRuntimeString());
		initializeMuhat(user.getRuntimeMuhatString());
		initializeSigmahat(user.getRuntimeSigmahatString());
		
		System.out.println("Job Creator started for User: " + user.getUserId());
	}

	
	/**
	 * Initializing method for Core Distribution
	 * 
	 * @param coreString
	 */
	private void initializeCoreDistribution(String coreString) {
		List<String> singleCoreDistribution = Arrays
				.asList(coreString.split(","));
		
		this.coreDistribution = new double[singleCoreDistribution.size()];
		
		int currentDistributionIndex = 0;
		for (String currentDistributionString : singleCoreDistribution) {
			this.coreDistribution[currentDistributionIndex] = Double.parseDouble(currentDistributionString);
			++currentDistributionIndex;
		}
	}
	
	/**
	 * Initializing method for runtime distribution
	 * 
	 * @param runtimeString
	 */
	private void initializeRuntimeDistribution(String runtimeString) {
		List<String> singleRuntimeDistribution = Arrays
				.asList(runtimeString.split(","));
	
		averageRuntime = new double[singleRuntimeDistribution.size()];
		
		int currentAverageIndex = 0;
		for (String currentAverageString : singleRuntimeDistribution) {
			averageRuntime[currentAverageIndex] = Double.parseDouble(currentAverageString);
			++currentAverageIndex;
		}
	}

	/**
	 * Initializing method for runtime muhat values
	 * 
	 * @param runtimeMuhatString
	 */
	private void initializeMuhat(String runtimeMuhatString) {
		System.out.println("User: " + user.getUserId());
		
		List<String> singleMuhat = Arrays
				.asList(runtimeMuhatString.split(","));

		runtimeMuhat = new double[singleMuhat.size()];

		int currentMuhatIndex = 0;
		for (String currentMuhatString : singleMuhat) {
			runtimeMuhat[currentMuhatIndex] = Double.parseDouble(currentMuhatString);
			++currentMuhatIndex;
		}
	}

	/**
	 * Initializing method for runtime sigma values
	 * 
	 * @param runtimeSigmahatString
	 */
	private void initializeSigmahat(String runtimeSigmahatString) {
		List<String> singleSigmahat = Arrays
				.asList(runtimeSigmahatString.split(","));

		runtimeSigmahat = new double[singleSigmahat.size()];

		int currentSigmahatIndex = 0;
		for (String currentSigmahatString : singleSigmahat) {
			runtimeSigmahat[currentSigmahatIndex] = Double
					.parseDouble(currentSigmahatString);
			++currentSigmahatIndex;
		}
	}
	
	
	/**
	 * Create 
	 * @return
	 */
	public SWFJob createJob() {
		SWFJob j = this.createBasicJob();
		this.sampleJob(j);
		
		return j;
	}
	
	
	/**
	 * Sample job according to proveded parameters.
	 * 
	 * @param j
	 * @return
	 */
	public SWFJob sampleJob(SWFJob j) {
		int numberOfProcessors 	= this.sampleNumberOfProcessors();	
		long jobLength 			= this.sampleJobLength(numberOfProcessors);
		
		if (numberOfProcessors > numberOfProvidedResources) {
			numberOfProcessors = numberOfProvidedResources;
		}
		
		j.set(SWFJob.RESOURCES_REQUESTED, numberOfProcessors);
		j.set(SWFJob.RESOURCES_ALLOCATED, numberOfProcessors);
		
		j.setRunTime(jobLength);
		j.setRequestedTime(jobLength); //important for EASY //TODO add runtimeModel
		
		return j;
	}
	
	
	/**
	 * Sample job size according to sampling parameters
	 * @return
	 */
	private int sampleNumberOfProcessors() {
		double random = Math.random();
		
		int processors = 1;
		
		boolean found = false;
		for (int i = 0; i < coreDistribution.length  && !found; ++i) {
			random -= coreDistribution[i];
			if (random < 0) {
				found = true;
				processors =  PROCESSORS[i];
			}
		}
		return processors;
	}
	
	/**
	 * Sample Job length according to sampling parameters
	 * 
	 * @param processors
	 * @return
	 */
	private long sampleJobLength(int processors) {	
		int index = (int)Math.round(Math.log( processors ) / Math.log( 2.0 ));
		
		long result = 1;
		
		if ( this.runtimeMuhat[index] != 0 && this.runtimeSigmahat[index] != 0) {
			NormalDistribution normDistribution = new NormalDistribution(this.runtimeMuhat[index], this.runtimeSigmahat[index]);
			result = (long)normDistribution.sample();
		}
		if ( result > 0 ) {
			return result;
		}
		return (long)averageRuntime[index]+1;
	}
	

	
	/**
	 * Helping method to create an empty SWFJob
	 * 
	 * @return SWFJob with basic properties.
	 */
	public SWFJob createBasicJob() {
		SWFJob j = new SWFJob(null);
		
		j.setUserID(this.user.getUserId());
		j.setJobNumber(++numberOfGeneratedJobs);

		j.setStatus((byte) -1);
		j.setGroupID((short) -1);
		j.setExecutableApplicationNumber(-1);
		j.setQueueNumber((byte) -1);
		j.setPartitionNumber((byte) -1);
		j.setPrecedingJobNumber(-1);
		j.setThinkTimeFromPrecedingJob(-1);

		return j;
	}
	
	
}

	
	
