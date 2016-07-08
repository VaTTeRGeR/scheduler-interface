package de.dortmund.tu.wmsi_tests;

import java.util.Collections;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingInterval;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingMultiple;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;
import de.dortmund.tu.wmsi.scheduler.Scheduler;

public class TestSimpleRoutine {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();
		simface.setSimulationBeginTime(0);
		simface.setSimulationEndTime(10000);
		simface.setDebug(false);
		simface.setWorkloadModel(new WorkloadModel() {
			@Override
			public void init(String configPath) {
				//Should execute at: 0, 1000, 2000, etc
				simface.register(new WorkloadModelRoutine(new RoutineTimingInterval(0, 1000)) {
					
					@Override
					protected void process(long t_now) {
						System.out.println("Random job submit Routine executed at "+t_now+".");
						if(Math.random() > 0.5) {
							simface.submitJob(new SWFJob(t_now, 50, Long.MAX_VALUE));
							System.out.println("Routine submitted a job.");
						} else {
							System.out.println("Routine didn't submit a job.");
						}
					}
				});
				
				//Should execute at: 0, 2500, 5000, etc
				simface.register(new WorkloadModelRoutine(new RoutineTimingInterval(0, 2500)) {
					
					@Override
					protected void process(long t_now) {
						System.out.println("Interval routine executing at "+t_now+".");
					}
				});

				//Should execute at 1999
				simface.register(new WorkloadModelRoutine(new RoutineTimingOnce(1999)) {
					@Override
					protected void process(long t_now) {
						System.out.println("Empty routine executing at "+t_now+".");
					}
				});

				//Should not execute
				simface.register(new WorkloadModelRoutine(new RoutineTimingOnce(-1999)) {
					@Override
					protected void process(long t_now) {
						System.out.println("Empty routine executing at "+t_now+".");
					}
				});

				//Should not execute
				simface.register(new WorkloadModelRoutine(new RoutineTimingOnce(Long.MAX_VALUE-1L)) {
					@Override
					protected void process(long t_now) {
						System.out.println("Empty routine executing at "+t_now+".");
					}
				});

				//will execute at: 555, 556, 566
				simface.register(new WorkloadModelRoutine(new RoutineTimingMultiple(555,556,555,566,10001)) {
					@Override
					protected void process(long t_now) {
						System.out.println("Multiple routine executing at "+t_now+".");
					}
				});

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
				queue.push(job.getSubmitTime()+job.getRunDuration());
				Collections.sort(queue);
			}
		});
		
		simface.register(new JobFinishedListener() {
			@Override
			public void jobFinished(JobFinishedEvent event) {
				System.out.println("Listener: job finished at " + event.getTime());
			}
		});
		
		simface.simulate(null);
	}
}
