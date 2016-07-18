package de.dortmund.tu.wmsi.usermodel.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;

public class User implements JobStartedListener, JobFinishedListener {
	
	LinkedList<Job> jobs = new LinkedList<Job>();

	public User() {
		SimulationInterface.instance().register((JobStartedListener)this);
		SimulationInterface.instance().register((JobFinishedListener)this);
		
		createBatch(SimulationInterface.instance().getSimulationBeginTime());
	}
	
	private void createBatch(long t_submit) {
		for (int i = 0; i < 5; i++) {
			Job job = new SWFJob(t_submit, 1, 1);

			jobs.add(job);
			SimulationInterface.instance().submitJob(job);
		}
	}

	@Override
	public void jobFinished(JobFinishedEvent event) {
		jobs.remove(event.getJob());
		if(jobs.isEmpty()) {
			System.out.println("New Batch at " + new Date((event.getTime()+TimeUnit.HOURS.toSeconds(6))*1000L));
			createBatch(event.getTime()+TimeUnit.HOURS.toSeconds(6));
		}
	}

	@Override
	public void jobStarted(JobStartedEvent event) {
	}

	public void kill() {
		SimulationInterface.instance().unregister((JobStartedListener)this);
		SimulationInterface.instance().unregister((JobFinishedListener)this);
	}
}
