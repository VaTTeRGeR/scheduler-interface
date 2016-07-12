package de.dortmund.tu.wmsi_tests;

import java.awt.geom.GeneralPath;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;
import de.dortmund.tu.wmsi.logger.GenericLogger;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingInterval;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingMultiple;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class TestRoutines {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();

		simface.setSimulationBeginTime(-1000);
		simface.setSimulationEndTime(5000);
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
							simface.submitJob(new SWFJob(t_now, 50, 10));
							System.out.println("Routine submitted a job.");
						} else {
							System.out.println("Routine didn't submit a job.");
						}
					}
				});
				
				//Should execute at: 0, 2500, ... etc depending on t_end
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
				simface.register(new WorkloadModelRoutine(new RoutineTimingMultiple(555,556,555,566,-10)) {
					@Override
					protected void process(long t_now) {
						System.out.println("Multiple routine executing at "+t_now+".");
					}
				});

			}
		});

		simface.setScheduler(new FCFS_Scheduler(Integer.MAX_VALUE));
		
		simface.register(new JobFinishedListener() {
			@Override
			public void jobFinished(JobFinishedEvent event) {
				System.out.println("Listener: job finished at " + event.getTime());
			}
		});

		simface.register(new JobStartedListener() {
			@Override
			public void jobStarted(JobStartedEvent event) {
				System.out.println("Listener: job started at " + event.getTime());
			}
		});
		
		simface.register(new GenericLogger());
		
		simface.simulate(null);
	}
}
