package de.dortmund.tu.wmsi.event;

@Deprecated
public class Event {
	private long t = 0;
	
	public Event(long time) {
		t = time;
	}

	public long getTime() {
		return t;
	}
}
