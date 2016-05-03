package de.dortmund.tu.wmsi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.dortmund.tu.wmsi.event.Event;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.EventTimeComparator;
import de.dortmund.tu.wmsi.util.JobSubmitComparator;

public class Interface {

	private static Interface instance = null;

	private WorkloadModel model = null;
	private Scheduler scheduler = null;

	private final ArrayList<JobFinishedListener> listeners = new ArrayList<JobFinishedListener>();
	private final ArrayList<WorkloadModelRoutine> routines = new ArrayList<WorkloadModelRoutine>();
	private final LinkedList<Job> jobs = new LinkedList<Job>();
	private final LinkedList<Event> events = new LinkedList<Event>();

	private final JobSubmitComparator jobComparator = new JobSubmitComparator();
	private final EventTimeComparator eventComparator = new EventTimeComparator();

	private long t_now;
	private long t_next;

	private long t_begin;
	private long t_end;

	private final long[] times = new long[4];
	private final int END = 0;
	private final int ROUTINE = 1;
	private final int SUBMIT = 2;

	public Interface() {
		instance = this;
	}

	// ** INTERFACE PUBLIC METHODS **//

	public static Interface instance() {
		if (instance == null)
			throw new NullPointerException("Create an instance of Interface before calling the static access method.");
		else
			return instance;
	}

	public void simulate(String configPath) {
		scheduler.init(configPath); // TODO real config path
		model.init(configPath); // TODO real config path

		t_begin = 0;
		t_end = Long.MAX_VALUE;

		t_now = t_begin;
		t_next = t_end;
		
		while (t_now < t_end) { // simulate until the t_end reached
			WorkloadModelRoutine nextRoutine = getNextRoutine();
			Job nextJob = jobs.peek();

			times[END] = t_end;
			times[ROUTINE] = nextRoutine.getNextExecutionTime(t_now);
			times[SUBMIT] = nextJob != null ? nextJob.getSubmitTime() : Long.MAX_VALUE;

			int winner = getIndexOfSmallest(times);

			t_next = times[winner]; // t_next = min{t_end,t_routine,t_submit}
			
			long t_scheduler = t_next;
			if(t_next > t_now) {
				t_scheduler = scheduler.simulateUntil(t_next);
			}
			if(t_scheduler < t_next || !events.isEmpty()) { // Event dazwischen
				Event event = null;
				while((event = events.poll()) != null) {
					for (int j = 0; j < listeners.size(); j++) {
						listeners.get(j).jobFinished(event);
					}
				}
			} else if(t_scheduler == t_next) { // Kein Event, durchgelaufen
				if(winner == ROUTINE) {
					executeRoutine(nextRoutine);
				} else if(winner == SUBMIT) {
					scheduler.enqueueJob(jobs.poll());
				}
			} else {
				throw new IllegalStateException("Scheduler cannot simulate ahead of t_next");
			}
			//TODO hier weiter machen :D
		}
	}

	// ** INTERFACE INNER METHODS **//

	private int getIndexOfSmallest(long[] array) {
		int minIndex = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] < array[minIndex]) {
				minIndex = i;
			}
		}
		return minIndex;
	}

	private void executeRoutine(WorkloadModelRoutine routine) {
		routine.process(t_now);
	}

	private WorkloadModelRoutine getNextRoutine() {
		WorkloadModelRoutine best = null;
		WorkloadModelRoutine other = null;
		long bestTime = Long.MAX_VALUE, otherTime = Long.MAX_VALUE;

		final int n = routines.size();
		for (int i = 0; i < n; i++) {
			other = routines.get(i);
			otherTime = other.getNextExecutionTime(t_now);
			if (otherTime >= t_now && otherTime < bestTime) {
				best = other;
				bestTime = best.getNextExecutionTime(t_now);
			}
		}
		return best;
	}

	// ** MODEL METHODS **//

	public void setWorkloadModel(WorkloadModel model) {
		this.model = model;
	}

	public void register(JobFinishedListener listener) {
		listeners.add(listener);
	}

	public void register(WorkloadModelRoutine routine) {
		routines.add(routine);
	}

	public void unregister(JobFinishedListener listener) {
		listeners.remove(listener);
	}

	public void unregister(WorkloadModelRoutine routine) {
		routines.remove(routine);
	}

	public void submitJob(Job job) {
		jobs.add(job);
		jobs.sort(jobComparator);
	}

	// ** SCHEDULER METHODS **//

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void submitEvent(Event event) {
		events.add(event);
		events.sort(eventComparator);
	}
}
