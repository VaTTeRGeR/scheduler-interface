package de.dortmund.tu.wmsi.logger;

import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;

public interface Logger extends JobFinishedListener, JobStartedListener {
	public void initialize();
	public void configure(String configPath);
}
