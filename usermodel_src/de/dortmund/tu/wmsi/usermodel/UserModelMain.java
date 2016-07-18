package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.model.UserWorkloadModel;
import de.dortmund.tu.wmsi_swf_example.scheduler.FCFS_Scheduler;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si = SimulationInterface.instance();

		si.setDebug(false);

		si.setScheduler(new FCFS_Scheduler());
		si.setWorkloadModel(new UserWorkloadModel());
		
		si.simulate(null);
	}

}
