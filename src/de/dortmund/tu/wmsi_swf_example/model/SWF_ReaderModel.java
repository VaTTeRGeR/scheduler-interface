package de.dortmund.tu.wmsi_swf_example.model;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.model.WorkloadModel;

public class SWF_ReaderModel implements WorkloadModel {

	@Override
	public void init(String configPath) {
		SimulationInterface simface = SimulationInterface.instance();
		simface.submitJob(new SWFJob(0, 0, 0));
	}

}
