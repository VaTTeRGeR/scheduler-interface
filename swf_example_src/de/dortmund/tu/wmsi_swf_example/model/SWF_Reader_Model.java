package de.dortmund.tu.wmsi_swf_example.model;

import java.io.File;
import java.util.Properties;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.Util;

public class SWF_Reader_Model implements WorkloadModel {

	@Override
	public void init(String configPath) {
		SimulationInterface.log("loading: "+configPath);
		
		Properties properties = Util.getProperties(configPath);

		String swfPath = properties.getProperty("swf_file");
		if(swfPath == null || !(new File(swfPath).exists())) {
			SimulationInterface.log("SWF-File: " + swfPath + " does not exist!");
			return;
		}
		
		SimulationInterface simface = SimulationInterface.instance();

		SimulationInterface.log("loading lines");
		
		String[] lines = Util.loadLines(new File(swfPath));
		
		SimulationInterface.log(lines.length+" lines loaded");
		
		for (int i = 0; i < lines.length; i++) {
			String[] values = lines[i].split("\\s+");
			if(Util.getLong(values[SWFJob.SUBMIT_TIME]) >= 0 && Util.getLong(values[SWFJob.RUN_TIME]) >= 0 && Util.getLong(values[SWFJob.RESOURCES_ALLOCATED]) >= 0) {
				simface.submitJob(new SWFJob(Integer.valueOf((values[SWFJob.JOB_NUMBER])), Util.getLong(values[SWFJob.SUBMIT_TIME]), Util.getLong(values[SWFJob.RUN_TIME]), Util.getLong(values[SWFJob.RESOURCES_ALLOCATED])));
			}
		}
		
		SimulationInterface.log("jobs created");
	}

}
