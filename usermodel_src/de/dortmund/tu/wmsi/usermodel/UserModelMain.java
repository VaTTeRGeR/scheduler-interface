package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si = SimulationInterface.instance();
		si.configure("usermodel_config/simulation.properties");
		si.simulate();
	}

}
