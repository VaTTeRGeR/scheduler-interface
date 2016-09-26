package de.dortmund.tu.wmsi_swf_example.scheduler.comparators;

import java.util.Comparator;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class JobAccWTComparator implements Comparator<Job> {

	@Override
	public int compare(Job j0, Job j1) {
		long v0 = j0.get(Job.WAIT_TIME) - StatisticalMathHelper.userAccepteableWaitTime(j0.get(Job.TIME_REQUESTED));
		long v1 = j1.get(Job.WAIT_TIME) - StatisticalMathHelper.userAccepteableWaitTime(j1.get(Job.TIME_REQUESTED));
		return (int)(v1-v0);
	}
}
