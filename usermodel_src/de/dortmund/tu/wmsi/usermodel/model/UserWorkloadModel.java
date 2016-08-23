package de.dortmund.tu.wmsi.usermodel.model;


import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.model.WorkloadModel;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class UserWorkloadModel implements WorkloadModel{
	
	List<User> users;
	
	@Override
	public void configure(String configPath) {
		PropertiesHandler properties = new PropertiesHandler(configPath);
		
		long weeks = properties.getLong("simulation.weeks", 0);
		
		long t_start = 0L;
		if(properties.getBoolean("simulation.useCurrentDate", false))
			t_start = new Date().getTime()/1000L;
		else
			t_start = properties.getLong("simulation.startTime", 0);
			
		SimulationInterface.instance().setSimulationBeginTime(t_start);
		SimulationInterface.instance().setSimulationEndTime(t_start + TimeUnit.DAYS.toSeconds(7L*weeks));

		users = UserCreator.createUserList(properties.getString("model.user_config", null));
	}

	@Override
	public void initialize() {
		for(User user : users)
			user.initialize();
	}
}
