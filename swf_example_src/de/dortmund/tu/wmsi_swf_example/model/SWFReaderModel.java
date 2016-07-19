package de.dortmund.tu.wmsi_swf_example.model;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.PropertiesHandler;
import de.dortmund.tu.wmsi.util.SWFFileUtil;

public class SWFReaderModel implements WorkloadModel {

	PropertiesHandler properties;
	
	@Override
	public void configure(String configPath) {
		SimulationInterface.log("loading: "+configPath);
		
		properties = new PropertiesHandler(configPath);
	}

	@Override
	public void initialize() {
		String swfPath = properties.getString("swf_file", null);
		if(swfPath == null || !(new File(swfPath).exists())) {
			SimulationInterface.log("SWF-File: " + swfPath + " does not exist!");
			return;
		}
		
		SimulationInterface simface = SimulationInterface.instance();

		SimulationInterface.log("loading lines");
		
		String[] lines = SWFFileUtil.loadLines(new File(swfPath));
		
		SimulationInterface.log(lines.length+" lines loaded");
		
		for (int i = 0; i < lines.length; i++) {
			String[] values = lines[i].split("\\s+");
			if(Long.parseLong(values[SWFJob.SUBMIT_TIME]) >= 0 && Long.parseLong(values[SWFJob.RUN_TIME]) >= 0 && Long.parseLong(values[SWFJob.RESOURCES_ALLOCATED]) >= 0) {
				simface.submitJob(new SWFJob(Integer.valueOf((values[SWFJob.JOB_ID])), Long.parseLong(values[SWFJob.SUBMIT_TIME]), Long.parseLong(values[SWFJob.RUN_TIME]), Long.parseLong(values[SWFJob.RESOURCES_ALLOCATED])));
			}
		}
		
		SimulationInterface.log("jobs created");
	}

}
