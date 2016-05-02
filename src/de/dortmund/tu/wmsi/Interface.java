package de.dortmund.tu.wmsi;

import de.dortmund.tu.wmsi.event.Event;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class Interface {
	public void loadInterfaceConfig(String path) {
		//TODO load config and apply config data
	}

	public void setWorkloadModel(WorkloadModel model) {
		//TODO set WorkloadModel
	}

	public void register(JobFinishedListener listener) {
		//TODO register JFL
	}

	public void register(WorkloadModelRoutine listener) {
		//TODO register WLMR
	}

	public void submitJob(Job job) {
		//TODO put job in queue for submission
	}

	public void setScheduler(Scheduler scheduler) {
		//TODO set Scheduler
	}

	public void submitEvent(Event event) {
		//TODO queue and route Event to JFL
	}
}
