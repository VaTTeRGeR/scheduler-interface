package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.listener.JobFinishedListener;
import de.dortmund.tu.wmsi.listener.JobStartedListener;
import de.dortmund.tu.wmsi.usermodel.model.User;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface simulationInterface = SimulationInterface.instance();
		simulationInterface.setDebug(true);

		User user = new User();
		simulationInterface.register((JobStartedListener)user);
		simulationInterface.register((JobFinishedListener)user);
	}

}
