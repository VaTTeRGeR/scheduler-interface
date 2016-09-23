package de.dortmund.tu.wmsi_swf_example.scheduler.comparators;

import java.util.Comparator;

import de.dortmund.tu.wmsi.job.Job;

public class JobWaittimeComparator implements Comparator<Job> {

	@Override
	public int compare(Job j0, Job j1) {
		long w0 = j0.get(Job.WAIT_TIME);
		long w1 = j1.get(Job.WAIT_TIME);
		return (int)(w1-w0);
	}
}
