package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Comparator;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;

public class JobGroupExceedWaitTimeComparatorAbsolute implements Comparator<UserJobGroup> {

	@Override
	public int compare(UserJobGroup j0List, UserJobGroup j1List) {
		long i0 = 0;
		long deltaSum0 = 0;

		for(Job j0 : j0List.jobs) {
			long accWaitTime0 = j0.get(Job.TIME_REQUESTED)+StatisticalMathHelper.userAccepteableWaitTime(j0.get(Job.TIME_REQUESTED));
			deltaSum0 += accWaitTime0-j0.get(Job.WAIT_TIME);
			i0++;
		}

		long i1 = 0;
		long deltaSum1 = 0;
		
		for(Job j1 : j1List.jobs) {
			long accWaitTime1 = j1.get(Job.TIME_REQUESTED)+StatisticalMathHelper.userAccepteableWaitTime(j1.get(Job.TIME_REQUESTED));
			deltaSum1 += accWaitTime1-j1.get(Job.WAIT_TIME);
			i1++;
		}

		return (int)((deltaSum0/i0)-(deltaSum1/i1));
	}
}
