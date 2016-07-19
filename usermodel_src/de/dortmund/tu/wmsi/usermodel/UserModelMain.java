package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.model.UserWorkloadModel;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si = SimulationInterface.instance();

		si.setDebug(true);

		FCFS_Scheduler scheduler = new FCFS_Scheduler();
		scheduler.setMaxResources(10);
		
		si.setScheduler(scheduler);
		
		UserWorkloadModel model = new UserWorkloadModel();
		model.configure("config/usermodel.properties");
		si.setWorkloadModel(model);
		
		si.simulate();
	}

}
