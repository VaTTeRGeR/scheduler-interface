package de.dortmund.tu.wmsi.util;

import java.util.Comparator;

import de.dortmund.tu.wmsi.job.Job;

public class JobSubmitComparator implements Comparator<Job>{
	public int compare(Job x, Job y) {
		long delta = x.getSubmitTime() - y.getSubmitTime();
		long absDelta = (delta < 0) ? -1*delta : delta;
		return (int)(delta/absDelta);
	}
}
