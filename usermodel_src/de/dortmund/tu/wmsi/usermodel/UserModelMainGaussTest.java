package de.dortmund.tu.wmsi.usermodel;

import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;

public class UserModelMainGaussTest {

	public static void main(String[] args) {
		SimulationInterface si;
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_gini_easy.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
	}

}
