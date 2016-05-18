package de.dortmund.tu.wmsi.listener;

import de.dortmund.tu.wmsi.event.JobFinishedEvent;

public interface JobFinishedListener {
	public void jobFinished(JobFinishedEvent event);
}
