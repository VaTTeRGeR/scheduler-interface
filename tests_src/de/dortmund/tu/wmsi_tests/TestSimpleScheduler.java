package de.dortmund.tu.wmsi_tests;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class TestSimpleScheduler {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();
		simface.setSimulationBeginTime(0);
		simface.setSimulationEndTime(Long.MAX_VALUE);
		simface.setWorkloadModel(new WorkloadModel() {
			
			@Override
			public void init(String configPath) {
				SimulationInterface.instance().submitJob(new SWFJob(50, 900, 1)); // long job 50 -> 950
				SimulationInterface.instance().submitJob(new SWFJob(500, 20, 1)); // short job 500 -> 520
			}
		});

		simface.setScheduler(new Scheduler() {
			
			private LinkedList<Long> queue = new LinkedList<Long>();
			
			@Override
			public long simulateUntil(long t_now, long t_target) {
				if(!queue.isEmpty() && queue.peek() < t_target) {
					long t_job = queue.poll();
					SimulationInterface.instance().submitEvent(new JobFinishedEvent(t_job, null));
					return t_job;
				} else {
					return t_target;
				}
			}
			
			@Override
			public void init(String configPath) {
				
			}
			
			@Override
			public void enqueueJob(Job job) {
				SimulationInterface.instance().submitEvent(new JobStartedEvent(job.getSubmitTime(), null));
				queue.push(job.getSubmitTime()+job.getRunDuration());
				Collections.sort(queue);
			}
		});
		
		simface.register(new JobFinishedListener() {
			boolean submitDone = false;
			@Override
			public void jobFinished(JobFinishedEvent event) {
				System.out.println("Listener: job finished at " + event.getTime());
				if(!submitDone) {
					SimulationInterface.instance().submitJob(new SWFJob(event.getTime()+10, 50, 1));
					submitDone = true;
				}
			}
		});
		
		simface.simulate(null);
	}
}
