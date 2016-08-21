package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si;
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_fcfs.properties");
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_easy.properties");
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_gini.properties");
		si.simulate();
		
		SimulationInterface.destroy();
	}

}
