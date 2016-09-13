package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si;
		
		for (int i = 0; i < 100; i++) {
			si = SimulationInterface.instance();

			si.configure("usermodel_config/simulation_easy_server.properties");
			si.simulate();
			
			SimulationInterface.destroy();
		}

		for (int i = 0; i < 100; i++) {
			si = SimulationInterface.instance();

			si.configure("usermodel_config/simulation_gini_easy_limited_sort_server.properties");
			si.simulate();
			
			SimulationInterface.destroy();
		}
	}
}
