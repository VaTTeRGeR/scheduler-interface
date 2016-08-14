package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi_swf_example.logger.SWFLogger;
import de.dortmund.tu.wmsi_swf_example.scheduler.EASY_Scheduler;

public class TestEASY {
	public static void main(String[] args) {
		final SimulationInterface simface = SimulationInterface.instance();

		simface.setSimulationBeginTime(0);
		simface.setSimulationEndTime(1000);
		simface.setDebug(true);
		
		simface.setWorkloadModel(new WorkloadModel() {
			@Override
			public void configure(String configPath) {}

			@Override
			public void initialize() {
				simface.submitJob(new Job(0, 100, 50));
				simface.submitJob(new Job(0, 10, 70));
				simface.submitJob(new Job(0, 10, 10));
				simface.submitJob(new Job(0, 10, 10));
				simface.submitJob(new Job(0, 10, 10));
				simface.submitJob(new Job(0, 10, 10));
				simface.submitJob(new Job(0, 200, 10));
			}
		});

		simface.setScheduler(new EASY_Scheduler().setMaxResources(100));
		
		SWFLogger logger = new SWFLogger();
		logger.configure("swfmodel_config/swf_logger.properties");
		simface.register(logger);
		
		simface.simulate();
	}
}
