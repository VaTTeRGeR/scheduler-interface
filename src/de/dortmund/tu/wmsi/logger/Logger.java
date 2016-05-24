package de.dortmund.tu.wmsi.logger;

import de.dortmund.tu.wmsi.listener.JobFinishedListener;

public interface Logger extends JobFinishedListener {
	public void init();
}
