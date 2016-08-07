package de.dortmund.tu.wmsi.usermodel.model;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.listener.JobStartedListener;

public class User implements JobStartedListener {
	//General Setup
	private int numberOfProvidedResources;
	private int numberOfSimulatedWeeks;

	//General User
	private String name;
	private int id;

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
	
	private BatchCreator batchCreator;
	
	private Behavior behavior;
	
	private DayCreator daycreator;

	private Session session;
	
	private boolean initialized;
	
	public void initialize() {
		SimulationInterface.instance().register(this);

		jobcreator = new JobCreator(this);
		batchCreator = new BatchCreator(this);
		behavior = new Behavior(this);
		daycreator = new DayCreator(this);
		session = null;

		think();
		
		//initialized = true;
	}
	
	private void think() {
		if(session == null) {
			long t_nextSession = behavior.startTimeNextSession(SimulationInterface.instance().getCurrentTime());
			if(t_nextSession != -1) {
				session = new Session(this);
				session.start(t_nextSession);
			} else {
				kill();
			}
		}
	}
	
	@Override
	public void jobStarted(JobStartedEvent event) {
		session.deliverEvent(event);
		
		if(session.isTerminated())
			session = null;
		
		think();
	}
	
	public void kill() {
		SimulationInterface.instance().unregister(this);
	}

	/** Getters and Setters **/

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


	public int getUserId() {
		return id;
	}


	public void setUserId(int userId) {
		this.id = userId;
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
		return batchCreator;
	}


	public void setBatchcreator(BatchCreator batchcreator) {
		this.batchCreator = batchcreator;
	}


	public DayCreator getDaycreator() {
		return daycreator;
	}


	public void setDaycreator(DayCreator daycreator) {
		this.daycreator = daycreator;
	}
}
