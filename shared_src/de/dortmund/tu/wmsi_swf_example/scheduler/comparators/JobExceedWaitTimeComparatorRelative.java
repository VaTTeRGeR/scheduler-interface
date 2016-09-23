package de.dortmund.tu.wmsi_swf_example.scheduler.comparators;

import java.util.Comparator;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class JobExceedWaitTimeComparatorRelative implements Comparator<Job> {

	@Override
	public int compare(Job j0, Job j1) {
		long accWaitTime0 = j0.get(Job.TIME_REQUESTED)+StatisticalMathHelper.userAccepteableWaitTime(j0.get(Job.TIME_REQUESTED));
		long accWaitTime1 = j1.get(Job.TIME_REQUESTED)+StatisticalMathHelper.userAccepteableWaitTime(j1.get(Job.TIME_REQUESTED));
		long delta0 = accWaitTime0/Math.max(j0.get(Job.WAIT_TIME), 1);
		long delta1 = accWaitTime1/Math.max(j1.get(Job.WAIT_TIME), 1);
		return (int)(delta0-delta1);
	}
}
