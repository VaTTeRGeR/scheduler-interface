package de.irf.it.rmg.research.workload.usermodel;

import java.util.Comparator;

//import org.apache.commons.math3.distribution;

import java.util.PriorityQueue;

import org.apache.commons.math3.distribution.LogisticDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import de.irf.it.rmg.core.teikoku.exceptions.WorkloadException;
import de.irf.it.rmg.core.teikoku.job.Job;
import de.irf.it.rmg.core.teikoku.runtime.events.JobStartedEvent;
import de.irf.it.rmg.core.teikoku.workload.swf.SWFJob;
import de.irf.it.rmg.research.workload.util.JobLengthComparator;
import de.irf.it.rmg.util.exceptions.InitializationException;
import de.irf.it.rmg.util.time.Clock;
import de.irf.it.rmg.util.time.TimeHelper;

public class User {

	//General Setup
	private int numberOfProvidedResources;
	private int numberOfSimulatedWeeks;

	//General User
	private String name;
	private short userId;

	//Batch Model
	private double batchSizeOne;
	private double batchSizeMu;
	private double batchSizeSigma;
	private double interarrivalTimeMu;
	private double interarrivalTimeSigma;

	//Job Model
	private String distributionString;
	private String runtimeString;
	private String runtimeMuhatString;
	private String runtimeSigmahatString;

	//Week Model
	private double presenceRatio;
	private String dayDistributionString;
	
	//Day Model
	private double startOfDayMu;
	private double startOfDaySigma;

	private double workLengthLessOneHour;
	private double workLengthMu;
	private double workLengthSigma;
	
	//Think Time
	private double thinkTimeM;
	private double thinkTimeB;
	
	
	private JobCreator jobcreator;
	
	private BatchCreator batchcreator;
	
	private Behavior behavior;
	
	private DayCreator daycreator;

	private Session session;
	
	
	private PriorityQueue<SWFJob> jobQueue;
	
	private boolean initialized;
	
	
	public void initialize() throws InitializationException {
		System.out.println("[WL] Initzializing user ID: " + userId);

		Comparator<SWFJob> comparator = new JobLengthComparator();
		this.jobQueue = new PriorityQueue<SWFJob>(10,comparator);

		this.jobcreator 	= new JobCreator(this);
		this.batchcreator 	= new BatchCreator(this);
		this.daycreator		= new DayCreator(this);
		this.behavior		= new Behavior(this);
		this.session 		= new Session(this);
		
		this.initialized = true;

		System.out.println("user init: " + this.userId);		
	}


	public void receiveStartedEvent(JobStartedEvent event) {
		short userIDOfJob = ((SWFJob) event.getStartedJob()).getUserID();
		long now = TimeHelper.toLongValue(Clock.instance().now()) / 1000;

		//if userId = -1, detect bootstrap job and start first session
		if (userIDOfJob == -1) {
			
			long t_start = this.behavior.startTimeNextSession(now);
			if (t_start < 0) {
				System.out.println("Start was smaller 0: " + t_start);
				t_start = 0;
			}
			session.start(t_start);
		}

		//event contains own user id, deliver to session
		else if (userIDOfJob == this.userId) {
			//ask behavior if active and ask session if not terminated.
			
			//else create new session starting at behavior.nextStart()
			
			session.deliverEvent(event);
			
			if (session.isTerminated()) {
				//take time from finished event 
				SWFJob runningJob = (SWFJob) event.getStartedJob();
				
				//long submittime = runningJob.getSubmitTime();
				//long responsetime = runningJob.getWaitTime() + (long)runningJob.getRequestedTime();

				
				long t_start = this.behavior.startTimeNextSession(now+(long)runningJob.getRequestedTime());
				System.out.println("SESSION TERMINATED, creating new session starting at: " + t_start + ", now was: " + now);
				
				if (t_start > -1) {
					session = new Session(this);
					session.start(t_start);
				}
			}
		}

		//session terminated -> start new session.
		

		this.getJobsFromSession();	
	}

	
	
	private void getJobsFromSession() {
		double earliestFinishingTime = 0;
		long now = TimeHelper.toLongValue(Clock.instance().now()) / 1000;

		while (!session.getJobQueue().isEmpty()) {
			SWFJob j = (SWFJob)session.getJobQueue().remove();
			earliestFinishingTime = max(earliestFinishingTime,j.getSubmitTime() + j.getRequestedTime());
			this.jobQueue.add(j);

			//System.out.println("SUBMITTIME " + j.getSubmitTime());
		}
	}

	
	private double max(double v1, double v2) {
		if (v1 > v2) return v2;
		return v1;
	}


	public Job inspectNextJob() throws WorkloadException {
		return jobQueue.peek();
	}

	public Job fetchNextJob() throws WorkloadException {
		if (!jobQueue.isEmpty()) {
			return jobQueue.poll();
		}
		return null;
	}




	//Getters and Setters
	public PriorityQueue<SWFJob> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(PriorityQueue<SWFJob> jobQueue) {
		this.jobQueue = jobQueue;
	}

	
	public int getNumberOfProvidedResources() {
		return numberOfProvidedResources;
	}


	public void setNumberOfProvidedResources(int numberOfProvidedResources) {
		this.numberOfProvidedResources = numberOfProvidedResources;
	}


	public int getNumberOfSimulatedWeeks() {
		return numberOfSimulatedWeeks;
	}


	public void setNumberOfSimulatedWeeks(int numberOfSimulatedWeeks) {
		this.numberOfSimulatedWeeks = numberOfSimulatedWeeks;
	}


	public String getDayDistributionString() {
		return dayDistributionString;
	}


	public void setDayDistributionString(String dayDistributionString) {
		this.dayDistributionString = dayDistributionString;
	}


	public String getDistributionString() {
		return distributionString;
	}


	public void setDistributionString(String distributionString) {
		this.distributionString = distributionString;
	}


	public String getRuntimeString() {
		return runtimeString;
	}


	public void setRuntimeString(String runtimeString) {
		this.runtimeString = runtimeString;
	}


	public String getRuntimeMuhatString() {
		return runtimeMuhatString;
	}


	public void setRuntimeMuhatString(String runtimeMuhatString) {
		this.runtimeMuhatString = runtimeMuhatString;
	}


	public String getRuntimeSigmahatString() {
		return runtimeSigmahatString;
	}


	public void setRuntimeSigmahatString(String runtimeSigmahatString) {
		this.runtimeSigmahatString = runtimeSigmahatString;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public short getUserId() {
		return userId;
	}


	public void setUserId(short userId) {
		this.userId = userId;
	}


	public double getPresenceRatio() {
		return presenceRatio;
	}


	public void setPresenceRatio(double presenceRatio) {
		this.presenceRatio = presenceRatio;
	}


	public double getStartOfDayMu() {
		return startOfDayMu;
	}


	public void setStartOfDayMu(double startOfDayMu) {
		this.startOfDayMu = startOfDayMu;
	}


	public double getStartOfDaySigma() {
		return startOfDaySigma;
	}


	public void setStartOfDaySigma(double startOfDaySigma) {
		this.startOfDaySigma = startOfDaySigma;
	}


	public double getWorkLengthLessOneHour() {
		return workLengthLessOneHour;
	}


	public void setWorkLengthLessOneHour(double workLengthLessOneHour) {
		this.workLengthLessOneHour = workLengthLessOneHour;
	}


	public double getWorkLengthMu() {
		return workLengthMu;
	}


	public void setWorkLengthMu(double workLengthMu) {
		this.workLengthMu = workLengthMu;
	}


	public double getWorkLengthSigma() {
		return workLengthSigma;
	}


	public void setWorkLengthSigma(double workLengthSigma) {
		this.workLengthSigma = workLengthSigma;
	}


	public double getBatchSizeOne() {
		return batchSizeOne;
	}


	public void setBatchSizeOne(double batchSizeOne) {
		this.batchSizeOne = batchSizeOne;
	}


	public double getBatchSizeMu() {
		return batchSizeMu;
	}


	public void setBatchSizeMu(double batchSizeMu) {
		this.batchSizeMu = batchSizeMu;
	}


	public double getBatchSizeSigma() {
		return batchSizeSigma;
	}


	public void setBatchSizeSigma(double batchSizeSigma) {
		this.batchSizeSigma = batchSizeSigma;
	}


	public double getInterarrivalTimeMu() {
		return interarrivalTimeMu;
	}


	public void setInterarrivalTimeMu(double interarrivalTimeMu) {
		this.interarrivalTimeMu = interarrivalTimeMu;
	}


	public double getInterarrivalTimeSigma() {
		return interarrivalTimeSigma;
	}


	public void setInterarrivalTimeSigma(double interarrivalTimeSigma) {
		this.interarrivalTimeSigma = interarrivalTimeSigma;
	}


	public double getThinkTimeM() {
		return thinkTimeM;
	}


	public void setThinkTimeM(double thinkTimeM) {
		this.thinkTimeM = thinkTimeM;
	}


	public double getThinkTimeB() {
		return thinkTimeB;
	}


	public void setThinkTimeB(double thinkTimeB) {
		this.thinkTimeB = thinkTimeB;
	}


	public JobCreator getJobcreator() {
		return jobcreator;
	}


	public void setJobcreator(JobCreator jobcreator) {
		this.jobcreator = jobcreator;
	}


	public Behavior getBehavior() {
		return behavior;
	}


	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}


	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}


	public boolean isInitialized() {
		return initialized;
	}


	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}


	public BatchCreator getBatchcreator() {
		return batchcreator;
	}


	public void setBatchcreator(BatchCreator batchcreator) {
		this.batchcreator = batchcreator;
	}


	public DayCreator getDaycreator() {
		return daycreator;
	}


	public void setDaycreator(DayCreator daycreator) {
		this.daycreator = daycreator;
	}
}
