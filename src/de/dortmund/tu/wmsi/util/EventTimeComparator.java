package de.dortmund.tu.wmsi.util;

import java.util.Comparator;

import de.dortmund.tu.wmsi.event.Event;

public class EventTimeComparator implements Comparator<Event>{
	public int compare(Event x, Event y) {
		long delta = x.getTime() - y.getTime();
		long absDelta = (delta < 0) ? -1*delta : delta;
		return (int)(delta/absDelta);
	}
}
