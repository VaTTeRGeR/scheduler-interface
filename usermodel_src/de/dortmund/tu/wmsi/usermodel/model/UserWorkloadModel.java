package de.dortmund.tu.wmsi.usermodel.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class UserWorkloadModel implements WorkloadModel{
	
	public UserWorkloadModel() {
	}
	
	@Override
	public void initialize() {
	}
	
	@Override
	public void configure(String configPath) {
		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		long weeks = properties.getLong("weeks", 0);
		
		if(properties.getBoolean("startnow", true)) {
			Date now = new Date();
			SimulationInterface.instance().setSimulationBeginTime(now.getTime()/1000L);
			SimulationInterface.instance().setSimulationEndTime((now.getTime()/1000L) + TimeUnit.DAYS.toSeconds(7L*weeks));
		} else {
			SimulationInterface.instance().setSimulationBeginTime(0L);
			SimulationInterface.instance().setSimulationEndTime(TimeUnit.DAYS.toSeconds(7L*weeks));
		}
	}
}
