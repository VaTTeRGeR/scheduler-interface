package de.dortmund.tu.wmsi_tests;

import de.dortmund.tu.wmsi.SimulationInterface;

public class Test_SWF_FCFS {
	public static void main(String[] args) {
		SimulationInterface.instance().simulate("config/default.properties");
	}
}