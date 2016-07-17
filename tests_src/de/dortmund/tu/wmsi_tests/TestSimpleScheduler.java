package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.logger.GenericLogger;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class TestSimpleScheduler {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();
		
		simface.setDebug(false);

		simface.setWorkloadModel(new WorkloadModel() {
			@Override
			public void init(String configPath) {
				SimulationInterface.instance().submitJob(new SWFJob(0, 1, 2)); // long job 0 -> 100
				SimulationInterface.instance().submitJob(new SWFJob(0, 1, 4)); // long job 100 -> 200
				SimulationInterface.instance().submitJob(new SWFJob(0, 1, 1)); // long job 200 -> 300
				SimulationInterface.instance().submitJob(new SWFJob(0, 1, 5)); // short job 300 -> 350
			}
		});

		simface.setScheduler(new FCFS_Scheduler(5));

		simface.register(new GenericLogger());

		simface.simulate(null);
	}
}
