package de.dortmund.tu.wmsi_swf_example.scheduler.comparators;

import java.util.Comparator;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi_swf_example.scheduler.UserJobGroup;

public class JobGroupMaxWaitTimeComparator implements Comparator<UserJobGroup> {

	@Override
	public int compare(UserJobGroup j0Group, UserJobGroup j1Group) {

		Job j0 = j0Group.jobs.peek();
		Job j1 = j1Group.jobs.peek();
		
		double w0 = j0.get(Job.WAIT_TIME)/((double)j0.get(Job.RESOURCES_REQUESTED)/4.0);
		double w1 = j1.get(Job.WAIT_TIME)/((double)j1.get(Job.RESOURCES_REQUESTED)/4.0);

		return (int)((w1-w0)*1000d);
	}
}
