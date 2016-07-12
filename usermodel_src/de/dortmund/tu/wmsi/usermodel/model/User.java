package de.dortmund.tu.wmsi.usermodel.model;

import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;

public class User implements JobStartedListener, JobFinishedListener {

	@Override
	public void jobFinished(JobFinishedEvent event) {
	}

	@Override
	public void jobStarted(JobStartedEvent event) {
	}

}
