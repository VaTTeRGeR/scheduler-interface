package de.dortmund.tu.wmsi.routine.timing;

public interface RoutineTiming {
	public long getNextTime();
	public void update(long time);
}
