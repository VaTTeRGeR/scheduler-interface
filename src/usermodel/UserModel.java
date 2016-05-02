package de.irf.it.rmg.research.workload.usermodel;


import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.irf.it.rmg.core.teikoku.RuntimeEnvironment;
import de.irf.it.rmg.core.teikoku.exceptions.WorkloadException;
import de.irf.it.rmg.core.teikoku.job.Job;
import de.irf.it.rmg.research.workload.util.JobLengthComparator;
import de.irf.it.rmg.sim.kuiga.annotations.InvalidAnnotationException;
import de.irf.it.rmg.util.Initializable;
import de.irf.it.rmg.util.exceptions.InitializationException;
import de.irf.it.rmg.core.teikoku.runtime.configuration.spring.PropertyHelper;
import de.irf.it.rmg.core.teikoku.runtime.events.JobStartedEvent;
import de.irf.it.rmg.core.teikoku.submission.Executor;
import de.irf.it.rmg.core.teikoku.workload.WorkloadFilter;
import de.irf.it.rmg.core.teikoku.workload.WorkloadSource;
import de.irf.it.rmg.core.teikoku.workload.swf.SWFJob;

public class UserModel implements WorkloadSource, Initializable {
	
	private PriorityQueue<SWFJob> jobQueue;
	
	private List<User> userList;
	private String userNames;
	
	private Executor executor;
	
	private int numberOfSubmittedJobs;
	private int currentJobID;
	
	public static int MAX_NUMBER_JOBS = 2500000;
	public static int MAX_NUMBER_OF_WEEKS = 100;
	
	private boolean initialized;
	
	public UserModel() {
		numberOfSubmittedJobs = 0;
		currentJobID = 1;

		Comparator<SWFJob> comparator = new JobLengthComparator();
		this.jobQueue = new PriorityQueue<SWFJob>(10,comparator);
		
		try {
			new UserModelEventHandler(this);
		}
		catch (InvalidAnnotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("UserModel gestartet");

		this.userList = new LinkedList<User>();
	}

	
	
	public void initialize() throws InitializationException {
		System.out.println("SYSTEM : " + this);
		this.initialized = true;
		List<String> singleUserNames = Arrays.asList(this.userNames.split(","));
		for (String userName : singleUserNames) {
			PropertyHelper.setActualUserName(userName.trim());
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]
							{ "file:" + RuntimeEnvironment.SITE_CONFIG_XML });
			User user = ctx.getBean("user", de.irf.it.rmg.research.workload.usermodel.User.class);
			this.userList.add(user);	
		}
	}

	@Override
	public void initialize(WorkloadFilter[] filters)
			throws InitializationException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Job inspectNextJob() throws WorkloadException {
		if (numberOfSubmittedJobs < MAX_NUMBER_JOBS) {
			//Initial dummy job
			if (numberOfSubmittedJobs == 0) {
				SWFJob j = this.createBootstrapDummy();
				jobQueue.add(j);
				currentJobID++;
				numberOfSubmittedJobs++;
			}
			return jobQueue.peek();
		}
		return null;
	}


	@Override
	public Job fetchNextJob() throws WorkloadException {
		if (numberOfSubmittedJobs < MAX_NUMBER_JOBS && !jobQueue.isEmpty()) {
			numberOfSubmittedJobs++;
			return jobQueue.poll();
		}
		return null;
	}

	public void receiveStartedEvent(JobStartedEvent event) {
		this.informUsers(event);
		this.getJobsFromUsers();	
	}


	private void getJobsFromUsers() {
		for (User u : this.userList) {
			try {
				while (u.inspectNextJob() != null) {
					((SWFJob) u.inspectNextJob()).setJobNumber(currentJobID++);
					jobQueue.add((SWFJob)u.fetchNextJob());
				}
			} catch (WorkloadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void informUsers(JobStartedEvent event) {
		for (User u : this.userList) {
			u.receiveStartedEvent(event);
		}
	}

	private SWFJob createBootstrapDummy() {
		SWFJob j = new SWFJob(null);
		j.setJobNumber(currentJobID);
		j.setSubmitTime(0);
		//j.setWaitTime(200);
		j.setRunTime(1);
		j.setNumberOfAllocatedProcessors(1);
		j.setAverageCPUTimeUsed(-1);
		j.setUsedMemory(100);
		j.setRequestedNumberOfProcessors(1);
		j.setRequestedTime(1);
		j.setRequestedMemory(10);
		j.setStatus((byte)1);
		j.setUserID((short)-1);
		j.setGroupID((short)2);
		j.setExecutableApplicationNumber(1);
		j.setQueueNumber((byte)1);
		j.setPartitionNumber((byte)1);
		j.setPrecedingJobNumber(-1);
		j.setThinkTimeFromPrecedingJob(-1);
		return j;
	}
	
	//Getters and Setters
	public String getUserNames() {
		return userNames;
	}

	public void setUserNames(String userNames) {
		this.userNames = userNames;
	}
	
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public PriorityQueue<SWFJob> getJobQueue() {
		return jobQueue;
	}

	public Executor getExecutor() {
		return executor;
	}
		
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
}
