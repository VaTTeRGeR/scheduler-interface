package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Comparator;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class JobExceedWaitTimeComparator implements Comparator<Job> {

	@Override
	public int compare(Job j0, Job j1) {
		long accWaitTime0 = StatisticalMathHelper.userAccepteableWaitTime075(j0.get(Job.RUN_TIME));
		long accWaitTime1 = StatisticalMathHelper.userAccepteableWaitTime075(j1.get(Job.RUN_TIME));
		long delta0 = accWaitTime0-j0.get(Job.WAIT_TIME);
		long delta1 = accWaitTime1-j1.get(Job.WAIT_TIME);
		return (int)(delta0-delta1);
	}
}
