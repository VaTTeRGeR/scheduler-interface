package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.logger.GenericLogger;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class TestSimpleScheduler {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();
		
		simface.setDebug(false);

		simface.setWorkloadModel(new WorkloadModel() {
			@Override
			public void configure(String configPath) {
			}

			@Override
			public void initialize() {
				SimulationInterface.instance().submitJob(new Job(0, 1, 2)); // 0 -> 1
				SimulationInterface.instance().submitJob(new Job(0, 1, 4)); // 1 -> 2
				SimulationInterface.instance().submitJob(new Job(0, 1, 1)); // 1 -> 2
				SimulationInterface.instance().submitJob(new Job(0, 1, 5)); // 2 -> 3
			}
		});

		simface.setScheduler(new FCFS_Scheduler().setMaxResources(5));

		simface.register(new GenericLogger());

		simface.simulate();
	}
}
