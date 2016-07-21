package de.dortmund.tu.wmsi.dynamicUserModel;

import java.util.Arrays;

import de.irf.it.rmg.core.teikoku.runtime.events.JobStartedEvent;
import de.irf.it.rmg.core.teikoku.workload.swf.SWFJob;
import de.irf.it.rmg.util.time.DateHelper;
import de.irf.it.rmg.util.time.TimeHelper;


public class ThinktimeLogger {

	private long[] currentBatchStart;
	private long[] currentBatchEnd;
	
	private int numberOfThinktimes;
	private double currentAverageThinkTime;
	
	public ThinktimeLogger(int numberOfUsers) {
		currentBatchStart = new long[numberOfUsers];
		currentBatchEnd = new long[numberOfUsers];

		Arrays.fill(currentBatchStart, 0);
		Arrays.fill(currentBatchEnd, 0);

		currentAverageThinkTime = 0;
	}
	
	public void receiveStartedEvent(JobStartedEvent event) {
		short userIDOfJob = ((SWFJob) event.getStartedJob()).getUserID();
		long submittime = DateHelper.convertToSeconds(TimeHelper.toLongValue(((SWFJob) event.getStartedJob()).getReleaseTime()));
		long waittime = ((SWFJob) event.getStartedJob()).getWaitTime();
		long runtime = ((SWFJob) event.getStartedJob()).getRunTime();
		
		
	}

}
