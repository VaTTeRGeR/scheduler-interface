package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.Event;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class TestSimpleScheduler {
	public static void main(String[] args) {
		SimulationInterface simface = new SimulationInterface();
		simface.setWorkloadModel(new WorkloadModel() {
			
			@Override
			public void init(String configPath) {
			}
		});

		simface.setScheduler(new Scheduler() {
			@Override
			public long simulateUntil(long t) {
				return t;
			}
			
			@Override
			public void init(String configPath) {
			}
			
			@Override
			public void enqueueJob(Job job) {
			}
		});
		
		simface.register(new JobFinishedListener() {
			
			@Override
			public void jobFinished(Event event) {
				System.out.println("job finished at " + event.getTime());
			}
		});
		simface.simulate(null);
	}
}
