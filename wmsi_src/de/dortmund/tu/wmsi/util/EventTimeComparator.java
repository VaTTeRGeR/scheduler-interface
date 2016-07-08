package de.dortmund.tu.wmsi.util;

import java.util.Comparator;

import de.dortmund.tu.wmsi.event.JobEvent;

public class EventTimeComparator implements Comparator<JobEvent>{
	public int compare(JobEvent x, JobEvent y) {
		long delta = x.getTime() - y.getTime();
		long absDelta = (delta < 0) ? -delta : delta;
		return delta == 0L ? 0 : (int)(delta/absDelta);
	}
}
