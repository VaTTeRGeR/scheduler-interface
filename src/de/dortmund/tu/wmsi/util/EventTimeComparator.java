package de.dortmund.tu.wmsi.util;

import java.util.Comparator;

import de.dortmund.tu.wmsi.event.JobFinishedEvent;

public class EventTimeComparator implements Comparator<JobFinishedEvent>{
	public int compare(JobFinishedEvent x, JobFinishedEvent y) {
		long delta = x.getTime() - y.getTime();
		long absDelta = (delta < 0) ? -delta : delta;
		return (int)(delta/absDelta);
	}
}
