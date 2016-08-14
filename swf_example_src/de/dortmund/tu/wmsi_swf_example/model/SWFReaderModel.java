package de.dortmund.tu.wmsi_swf_example.model;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.job.Job;
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
			if(Long.parseLong(values[Job.SUBMIT_TIME]) >= 0 && Long.parseLong(values[Job.RUN_TIME]) >= 0 && Long.parseLong(values[Job.RESOURCES_ALLOCATED]) >= 0) {
				simface.submitJob(new Job(Long.valueOf((values[Job.JOB_ID])), Long.parseLong(values[Job.SUBMIT_TIME]), Long.parseLong(values[Job.RUN_TIME]), Long.parseLong(values[Job.RESOURCES_ALLOCATED])));
			}
		}
		
		SimulationInterface.log("jobs created");
	}

}
