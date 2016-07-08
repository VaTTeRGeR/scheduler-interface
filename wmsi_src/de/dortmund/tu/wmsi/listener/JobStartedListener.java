package de.dortmund.tu.wmsi.listener;

import de.dortmund.tu.wmsi.event.JobStartedEvent;

public interface JobStartedListener {
	public void jobStarted(JobStartedEvent event);
}
