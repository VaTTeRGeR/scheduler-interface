package de.dortmund.tu.wmsi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.event.JobEvent;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.JobIDCounter;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;
import de.dortmund.tu.wmsi.logger.Logger;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.scheduler.Scheduler;
import de.dortmund.tu.wmsi.util.EventTimeComparator;
import de.dortmund.tu.wmsi.util.JobSubmitComparator;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class SimulationInterface {

	private static SimulationInterface instance = null;

	private WorkloadModel model = null;
	private Scheduler scheduler = null;
	private Logger logger = null;

	private final ArrayList<JobFinishedListener> finishedListeners = new ArrayList<JobFinishedListener>();
	private final ArrayList<JobStartedListener> startedListeners = new ArrayList<JobStartedListener>();
	private final ArrayList<WorkloadModelRoutine> routines = new ArrayList<WorkloadModelRoutine>();
	private final LinkedList<Job> jobs = new LinkedList<Job>();
	private final LinkedList<JobEvent> events = new LinkedList<JobEvent>();

	private final JobSubmitComparator jobComparator = new JobSubmitComparator();
	private final EventTimeComparator eventComparator = new EventTimeComparator();

	private long t_now;
	private long t_next;

	private long t_begin = Long.MIN_VALUE;
	private long t_end = Long.MAX_VALUE;

	private boolean jobsDirty = false;

	private final long[] times = new long[3];
	private final int END = 0, ROUTINE = 1, SUBMIT = 2;

	private static boolean debug = true;

	private SimulationInterface() {
		instance = this;
	}

	// ** INTERFACE PUBLIC METHODS **//
	/**
	 * Returns an instance of SimulationInterface
	*/
	public static SimulationInterface instance() {
		if (instance == null) {
			return (instance = new SimulationInterface());
		} else {
			return instance;
		}
	}
	
	public static void destroy() {
		if (instance == null)
			throw new IllegalStateException("Create an instance of SimulationInterface before calling the destroy method.");
		else
			instance = null;
	}
	
	// ** SETTERS ** //
	
	public void setSimulationBeginTime(long begin) {
		t_begin = begin;
	}

	public void setSimulationEndTime(long end) {
		t_end = end;
	}

	public void setDebug(boolean debug) {
		SimulationInterface.debug = debug;
	}

	// ** GETTERS ** //
	
	public long getSimulationBeginTime() {
		return t_begin;
	}

	public long getSimulationEndTime() {
		return t_end;
	}

	public long getCurrentTime() {
		return t_now;
	}

	public boolean getDebug() {
		return SimulationInterface.debug;
	}

	// ** SIMULATE METHOD ** //
	
	public void configure(String configPath) {
		if(configPath != null) {
			PropertiesHandler properties = new PropertiesHandler(configPath);

			String config_path = properties.getString("config_path", new String());

			setSimulationBeginTime(properties.getLong("start_time", 0));
			setSimulationEndTime(properties.getLong("end_time", Long.MAX_VALUE));

			setDebug(properties.getBoolean("debug", false));

			try {
				scheduler = (Scheduler) Class.forName(properties.getString("scheduler_package", null) + "." + properties.getString("scheduler", null)).newInstance();
				setScheduler(scheduler);
				scheduler.configure(config_path + properties.getString("scheduler_config", null));
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				log("Scheduler properties error or class not present");
				return;
			}

			try {
				model = (WorkloadModel) Class.forName(properties.getString("model_package", null) + "." + properties.getString("model", null)).newInstance();
				setWorkloadModel(model);
				model.configure(config_path + properties.getString("model_config" , null));
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				log("Model properties error or class not present");
				return;
			}

			if (properties.has("logger")) {
				try {
					logger = (Logger) Class
							.forName(properties.getString("logger_package", null) + "." + properties.getString("logger", null))
							.newInstance();
					register(logger);
					logger.configure(config_path + properties.getString("logger_config", null));
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
					log("Logger properties error or class not present");
				}
			}
		} else {
			throw new IllegalStateException("Cannot configure if no config path is given");
		}
	}

	public void simulate() {
		JobIDCounter.resetID();

		t_now = t_begin;
		t_next = t_end;
		times[END] = t_end;

		if(model == null)
			throw new IllegalStateException("Cannot simulate without a workload model");
		else
			model.initialize();
			
		if(scheduler == null)
			throw new IllegalStateException("Cannot simulate without a scheduler");
		else
			scheduler.initialize();
			
		if(logger == null)
			log("No logger set");
		else
			logger.initialize();
		
		log("simulation from "+t_begin+" to "+t_end);

		while (t_now <= t_end) { // simulate until t_now >= t_end
			logNewLine();
			log("new iteration");
			
			//Remove finished Routines
			for (int i = 0; i < routines.size(); i++) {
				if(routines.get(i).getNextExecutionTime(t_now) == Long.MAX_VALUE) {
					log("Removing a Routine that last executed at "+routines.get(i).getLastExecutionTime());
					routines.remove(i--);
				}
			}
			
			WorkloadModelRoutine nextRoutine = getNextRoutine();
			
			if(jobsDirty) {
				Collections.sort(jobs,jobComparator);
				jobsDirty = false;
			}
				
			Job nextJob = jobs.peek();

			times[ROUTINE] = nextRoutine != null ? nextRoutine.getNextExecutionTime(t_now) : Long.MAX_VALUE;
			times[SUBMIT] = nextJob != null ? nextJob.getSubmitTime() : Long.MAX_VALUE;

			int winner = getIndexOfMin();

			switch (winner) {
			case 0:
				log("t_end at "+times[winner]+" is the next step target.");
				break;

			case 1:
				log("a routine execution at "+times[winner]+" is the next step target.");
				break;

			case 2:
				log("a job submit at "+times[winner]+" is the next step target");
				break;

			default:
				throw new IllegalArgumentException("winner has to be in [0, 1, 2]");
			}

			t_next = times[winner]; // t_next = firstMin{t_end,t_routine,t_submit}

			checkState();
			
			log("trying to simulate scheduler from "+t_now+" to "+t_next);
			long t_scheduler = scheduler.simulateUntil(t_now, t_next);
			t_now = t_scheduler;
			log("scheduler got simulated until "+t_scheduler);
			
			checkState();

			if(!events.isEmpty()) { // Event dazwischen
				log("scheduler event.");
				JobEvent event = null;
				while((event = events.poll()) != null) {
					if(event instanceof JobFinishedEvent) {
						for (int j = 0; j < finishedListeners.size(); j++) {
							log("contacting job finished listener "+j+".");
							finishedListeners.get(j).jobFinished((JobFinishedEvent)event);
						}
					} else if(event instanceof JobStartedEvent) {
						for (int j = 0; j < startedListeners.size(); j++) {
							log("contacting job started listener "+j+".");
							startedListeners.get(j).jobStarted((JobStartedEvent)event);
						}
					}
				}
			} else if(t_scheduler == t_next) { // Kein Event, durchgelaufen
				log("no scheduler event queued");
				if(winner == ROUTINE) {
					log("executing routine");
					nextRoutine.startProcessing(t_now);
				} else if(winner == SUBMIT) {
					log("passing job "+jobs.peek().getJobId()+" to scheduler");
					scheduler.enqueueJob(jobs.poll());
				} else {
					log("no routine execution or job submit");
				}
			} else if(t_next < t_scheduler) {
				throw new IllegalStateException("Scheduler cannot simulate ahead of t_next");
			}
			
			if(winner == END && t_now == t_end) {
				log("t_end has been reached and routines have been finished.");
				break;
			}
		}
		logNewLine();
		log("simulation finished");
	}
	
	// ** LOG METHODS ** //
	
	public static void log(String message) {
		if(debug)
			System.out.println(new StringBuilder("[t_now = ").append(instance().t_now).append("]  -  ").append(message).toString());
	}

	public static void logNewLine() {
		if(debug)
			System.out.println();
	}

	// ** INTERFACE INNER METHODS ** //
	
	private void checkState() {
		if(times[END] < t_now)
			throw new IllegalStateException("t_end cannot be before t_now.");
		if(times[ROUTINE] < t_now)
			throw new IllegalStateException("the next routine execution time cannot be before t_now.");
		if(times[SUBMIT] < t_now)
			throw new IllegalStateException("the next submit cannot be before t_now: "+times[SUBMIT]+" < "+t_now);
		if(t_now > t_next)
			throw new IllegalStateException("t_now cannot be ahead of t_next.");
		if(t_begin > t_now)
			throw new IllegalStateException("t_now cannot be lower than t_begin.");
		if(t_end < t_now)
			throw new IllegalStateException("t_now cannot be higher than t_end.");
	}

	/**
	 *@return END if all are equal, else returns the first that is "<=" the rest.
	 **/
	private int getIndexOfMin() {
		int minIndex = END;
		for (int i = 0; i < times.length; i++) {
			if (times[i] < times[minIndex]) {
				minIndex = i;
			}
		}
		return minIndex;
	}

	private WorkloadModelRoutine getNextRoutine() {
		WorkloadModelRoutine best = null;
		long bestTime = Long.MAX_VALUE;
		
		WorkloadModelRoutine other = null;
		long otherTime, otherLastExecution;

		final int n = routines.size();
		for (int i = 0; i < n; i++) {
			other = routines.get(i);
			otherTime = other.getNextExecutionTime(t_now);
			otherLastExecution = other.getLastExecutionTime();
			if (((otherTime == t_now && otherLastExecution < t_now) || otherTime > t_now) && otherTime < bestTime) {
				SimulationInterface.log("Routine was last executed at " + otherLastExecution + ", next execution at " + otherTime);
				best = other;
				bestTime = otherTime;
			}
		}
		return best;
	}

	// ** REGISTER METHODS ** //
	
	public void register(Logger logger) {
		unregisterLogger();
		
		this.logger = logger;
		startedListeners.add(logger);
		finishedListeners.add(logger);
	}

	public void register(JobFinishedListener listener) {
		finishedListeners.add(listener);
	}

	public void register(JobStartedListener listener) {
		startedListeners.add(listener);
	}

	public void register(WorkloadModelRoutine routine) {
		routines.add(routine);
	}

	// ** UNREGISTER METHODS ** //
	
	public void unregisterLogger() {
		unregister((JobStartedListener)logger);
		unregister((JobFinishedListener)logger);
		logger = null;
	}

	public void unregister(JobFinishedListener listener) {
		finishedListeners.remove(listener);
	}

	public void unregister(JobStartedListener listener) {
		startedListeners.remove(listener);
	}

	public void unregister(WorkloadModelRoutine routine) {
		routines.remove(routine);
	}

	// ** WORKLOAD METHODS ** //

	public void setWorkloadModel(WorkloadModel model) {
		this.model = model;
	}
	
	public void submitJob(Job job) {
		if(!job.isValid()) {
			throw new IllegalStateException("job "+job.getJobId()+" did not pass validity check.");
		} else {
			jobs.add(job);
			jobsDirty = true;
		}
	}

	// ** SCHEDULER METHODS ** //

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void submitEvent(JobEvent event) {
		events.add(event);
		events.sort(eventComparator);
	}
}
