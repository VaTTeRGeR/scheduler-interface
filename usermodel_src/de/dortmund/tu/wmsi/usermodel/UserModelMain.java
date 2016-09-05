package de.dortmund.tu.wmsi.usermodel;

import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;

public class UserModelMain {

	public static void main(String[] args) {
		SimulationInterface si;
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_fcfs.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_easy.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_gini.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_gini_pure.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
		si = SimulationInterface.instance();

		si.configure("usermodel_config/simulation_gini_easy.properties");
		si.setSimulationEndTime(TimeUnit.DAYS.toSeconds(7*10));
		si.simulate();
		
		SimulationInterface.destroy();
	}

}
