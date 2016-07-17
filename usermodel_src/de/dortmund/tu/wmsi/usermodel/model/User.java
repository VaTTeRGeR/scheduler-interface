package de.dortmund.tu.wmsi.usermodel.model;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;

public class User extends WorkloadModelRoutine implements JobStartedListener, JobFinishedListener {

	public User() {
		super(new RoutineTimingOnce(0));
		SimulationInterface.instance().register((JobStartedListener)this);
		SimulationInterface.instance().register((JobFinishedListener)this);
	}

	@Override
	public void jobFinished(JobFinishedEvent event) {
	}

	@Override
	public void jobStarted(JobStartedEvent event) {
	}

	@Override
	protected void process(long t_now) {
		//TODO decide on when to first start
	}
	
	public void killUser() {
		SimulationInterface.instance().unregister((JobStartedListener)this);
		SimulationInterface.instance().unregister((JobFinishedListener)this);
	}
}
