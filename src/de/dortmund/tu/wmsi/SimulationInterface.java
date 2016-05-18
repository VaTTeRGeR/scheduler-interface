package de.dortmund.tu.wmsi;

import java.util.ArrayList;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.EventTimeComparator;
import de.dortmund.tu.wmsi.util.JobSubmitComparator;

public class SimulationInterface {

	private static SimulationInterface instance = null;

	private WorkloadModel model = null;
	private Scheduler scheduler = null;

	private final ArrayList<JobFinishedListener> listeners = new ArrayList<JobFinishedListener>();
	private final ArrayList<WorkloadModelRoutine> routines = new ArrayList<WorkloadModelRoutine>();
	private final LinkedList<Job> jobs = new LinkedList<Job>();
	private final LinkedList<JobFinishedEvent> events = new LinkedList<JobFinishedEvent>();

	private final JobSubmitComparator jobComparator = new JobSubmitComparator();
	private final EventTimeComparator eventComparator = new EventTimeComparator();

	private long t_now;
	private long t_next;

	private long t_begin;
	private long t_end;

	private final long[] times = new long[3];
	private final int	END = 0, ROUTINE = 1, SUBMIT = 2;

	private SimulationInterface() {
		instance = this;
	}

	// ** INTERFACE PUBLIC METHODS **//
	
	public static SimulationInterface instance() {
		if (instance == null)
			return (instance = new SimulationInterface());
		else
			return instance;
	}
	
	public static void destroy() {
		if (instance == null)
			throw new NullPointerException("Create an instance of SimulationInterface before calling the destroy method.");
		else
			instance = null;
	}
	
	public void setSimulationBeginTime(long begin) {
		t_begin = begin;
	}

	public void setSimulationEndTime(long end) {
		t_end = end;
	}

	public void simulate(String configPath) {
		scheduler.init(configPath); // TODO real config path
		model.init(configPath); // TODO real config path

		t_now = t_begin;
		t_next = t_end;
		
		log("simulation from "+t_begin+" to "+t_end);

		while (t_now < t_end) { // simulate until t_now >= t_end
			logNewLine();
			log("new iteration");
			WorkloadModelRoutine nextRoutine = getNextRoutine();
			Job nextJob = jobs.peek();

			times[END] = t_end;
			times[ROUTINE] = nextRoutine != null ? nextRoutine.getNextExecutionTime(t_now) : Long.MAX_VALUE;
			times[SUBMIT] = nextJob != null ? nextJob.getSubmitTime() : Long.MAX_VALUE;

			int winner = getIndexOfSmallestLong(times);

			switch (winner) {
			case 0:
				log("t_end is the next step target");
				break;

			case 1:
				log("a routine execution is the next step target");
				break;

			case 2:
				log("a job submit is the next step target");
				break;

			default:
				throw new IllegalArgumentException("winner has to be in [0, 1, 2]");
			}

			t_next = times[winner]; // t_next = firstMin{t_end,t_routine,t_submit}
			
			long t_scheduler = t_next;
			
			if(t_next > t_now) {
				log("trying to simulate scheduler until "+t_next);
				t_scheduler = scheduler.simulateUntil(t_next);
				log("scheduler got simulated until "+t_scheduler);
				t_now = t_scheduler;
			} else if(t_next < t_now) {
				throw new IllegalStateException("t_now cannot be ahead of t_next");
			} else {
				log("skipping scheduler simulation. no time advance");
			}
			
			if(t_scheduler < t_next || !events.isEmpty()) { // Event dazwischen
				log("scheduler event.");
				JobFinishedEvent event = null;
				while((event = events.poll()) != null) {
					for (int j = 0; j < listeners.size(); j++) {
						log("contacting listener "+j);
						listeners.get(j).jobFinished(event);
					}
				}
			} else if(t_scheduler == t_next) { // Kein Event, durchgelaufen
				log("no scheduler event");
				if(winner == ROUTINE) {
					log("executing routine");
					executeRoutine(nextRoutine);
				} else if(winner == SUBMIT) {
					log("passing job "+jobs.peek().getJobId()+" to scheduler");
					scheduler.enqueueJob(jobs.poll());
				} else {
					log("no routine or submit");
				}
			} else {
				throw new IllegalStateException("Scheduler cannot simulate ahead of t_next");
			}
		}
		logNewLine();
		log("simulation finished");
	}

	// ** INTERFACE INNER METHODS **//
	
	private void log(String message) {
		System.out.println(new StringBuilder("t_now = ").append(t_now).append("  -  ").append(message).toString());
	}

	private void logNewLine() {
		System.out.println();
	}

	private int getIndexOfSmallestLong(long[] array) {
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

	public void submitEvent(JobFinishedEvent event) {
		events.add(event);
		events.sort(eventComparator);
	}
}
