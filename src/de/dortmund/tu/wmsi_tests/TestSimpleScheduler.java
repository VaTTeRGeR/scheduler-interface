package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.Event;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class TestSimpleScheduler {
	public static void main(String[] args) {
		SimulationInterface simface = new SimulationInterface();
		simface.setSimulationBeginTime(0);
		simface.setSimulationEndTime(1000);
		simface.setWorkloadModel(new WorkloadModel() {
			
			@Override
			public void init(String configPath) {
				SimulationInterface.instance().submitJob(new SWFJob(500, 20, 0));;
			}
		});

		simface.setScheduler(new Scheduler() {
			
			private long jobFinish = Long.MAX_VALUE;
			
			@Override
			public long simulateUntil(long t) {
				long t_sim = t;
				
				if(jobFinish < t) {
					t_sim = jobFinish;
					jobFinish = t;
					SimulationInterface.instance().submitEvent(new Event(t_sim));
					return t_sim;
				} else {
					return t;
				}
			}
			
			@Override
			public void init(String configPath) {
				
			}
			
			@Override
			public void enqueueJob(Job job) {
				jobFinish = job.getSubmitTime()+job.getRunDuration();
			}
		});
		
		simface.register(new JobFinishedListener() {
			
			@Override
			public void jobFinished(Event event) {
				System.out.println("Listener: job finished at " + event.getTime());
			}
		});
		
		simface.simulate(null);
	}
}
