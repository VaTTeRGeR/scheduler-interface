package de.dortmund.tu.wmsi.usermodel;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si = SimulationInterface.instance();
		for (int i = 0; i < 1; i++) {
			si.configure("usermodel_config/simulation_easy_avg.properties");
			si.simulate();
		}
		
		AVGWTLogger.resetLog();

		for (int i = 0; i < 1; i++) {
			si.configure("usermodel_config/simulation_overtime_easy_absolute.properties");
			si.simulate();
		}
	}
}
