package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi_swf_example.model.SWF_Reader_Model;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class Test_SWF_FCFS {
	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();

		simface.setSimulationBeginTime(0);
		simface.setSimulationEndTime(Long.MAX_VALUE);

		simface.setWorkloadModel(new SWF_Reader_Model());
		simface.setScheduler(new FCFS_Scheduler());
		
		simface.simulate("no config support yet");
	}
}
