package de.dortmund.tu.wmsi.util;

import java.util.Comparator;

import de.dortmund.tu.wmsi.job.Job;

public class JobFinishComparator implements Comparator<Job>{
	public int compare(Job x, Job y) {
		long delta = x.getSubmitTime() + x.getRunDuration() - y.getSubmitTime() + y.getRunDuration();
		long absDelta = (delta < 0L) ? -1L*delta : delta;
		return delta == 0L ? 0 : (int)(delta/absDelta);
	}
}
