package de.dortmund.tu.wmsi_swf_example.model;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.FileUtil;

public class SWF_Reader_Model implements WorkloadModel {

	@Override
	public void init(String configPath) {
		SimulationInterface simface = SimulationInterface.instance();

		System.out.println("loading lines");
		String[] lines = FileUtil.loadLines(new File("C:/Users/smfoscmi/Documents/LLNL-Thunder-2007-1.1-cln.swf"));
		System.out.println("lines loaded");
		for (int i = 0; i < lines.length; i++) {
			String[] values = FileUtil.splitLine(lines[i], "\\s+");
			if(FileUtil.getLong(values[1]) < 0)
				continue;
			simface.submitJob(
					new SWFJob(FileUtil.getLong(values[1]), FileUtil.getLong(values[3]), FileUtil.getLong(values[7])));
			if (i % 1000 == 0)
				System.out.println("loading job " + i);
		}
	}

}
