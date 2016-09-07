package de.dortmund.tu.wmsi_swf_example;

import de.dortmund.tu.wmsi.SimulationInterface;

public class Test_SWF_FCFS {
	public static void main(String[] args) {
		SimulationInterface.instance().configure("swfmodel_config/default.properties");
		SimulationInterface.instance().simulate();
	}
}
