package de.dortmund.tu.wmsi_swf_example.model;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.Util;

public class SWF_Reader_Model implements WorkloadModel {

	@Override
	public void init(String configPath) {
		SimulationInterface simface = SimulationInterface.instance();

		System.out.println("loading lines");
		String[] lines = Util.loadLines(new File("C:/Users/smfoscmi/Documents/test.swf"));
		System.out.println("lines loaded");
		for (int i = 0; i < lines.length; i++) {
			String[] values = lines[i].split("\\s+");
			
			if(Util.getLong(values[1]) >= 0 && Util.getLong(values[3]) >= 0 && Util.getLong(values[7]) >= 0) {
				simface.submitJob( new SWFJob( Util.getLong(values[1]), Util.getLong(values[3]), Util.getLong(values[7]) ) );
			}
			if (i % 1000 == 0)
				System.out.println("loading job " + i);
		}
	}

}
