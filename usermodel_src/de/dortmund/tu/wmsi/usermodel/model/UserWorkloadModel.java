package de.dortmund.tu.wmsi.usermodel.model;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.Util;

public class UserWorkloadModel implements WorkloadModel{
	
	public UserWorkloadModel() {
		Date now = new Date();
		SimulationInterface.instance().setSimulationBeginTime(now.getTime()/1000L);
		SimulationInterface.instance().setSimulationEndTime((now.getTime()/1000L) + TimeUnit.DAYS.toSeconds(7));
	}
	
	@Override
	public void initialize() {
		//TODO create Users etc
	}
	
	@Override
	public void configure(String configPath) {
		if(configPath != null) {
			Properties properties = Util.getProperties(configPath);
			//TODO load config
		}
	}
}
