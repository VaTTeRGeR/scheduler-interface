package de.dortmund.tu.wmsi.dynamicUserModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class UserModelCreator {

	private static UserModelCreator model;
	
	public static void main(String[] args) {
		String configPath = "/Users/stephan/Library/Mobile Documents/com~apple~CloudDocs/Projects_GRK/Dynamic Scheduling Framework/scheduler-interface/config/CTC_easy.properties";
		
		model = new UserModelCreator();
		
		List<User> resultList = model.createUserList(configPath);
	}
	
	
	
	public List<User> createUserList(String configPath) {
		List<User> resultList = new LinkedList<User>();
		
		Properties properties = new Properties();
		//load properties
		try {
			properties.load(new FileInputStream(configPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//iteration for all users and append to user list
		String usernamesPropertyString = properties.getProperty("setup_user");
		usernamesPropertyString = usernamesPropertyString.replaceAll("\\s","");
		String[] usernames = usernamesPropertyString.split(",");
		
		
		for ( String username : usernames) {
			User u = createUser(properties, username);
			this.createUser(properties, username);

			resultList.add(u);
			System.out.println(u.getBatchSizeMu());
		}
		
		return resultList;
	}
	
	
	private User createUser(Properties properties, String username) {
		User u = new User();
		
		u.setBatchSizeMu(Double.parseDouble(properties.getProperty(username + ".batchSizeMu")));
		
		return u;
	}
	
	/*user1.name = 1
			user1.userId = 1
			user1.presenceRatio = 0.00000 
			user1.dayDistribution = 0.684, 0.684, 0.632, 0.368, 1.000, 1.000, 0.263
			user1.startOfDayMu = 62452.58 
			user1.startOfDaySigma = 8252.68 
			user1.workLengthLessOneHour = 0.0 
			user1.workLengthMu = 24819.14 
			user1.workLengthSigma = 10357.54
			user1.batchSizeOne		= 0.76119 
			user1.batchSizeMu		= 2.31350 
			user1.batchSizeSigma		= 0.60308 
			user1.interarrivalTimeMu	= 360.00 
			user1.interarrivalTimeSigma = 305.35
			user1.coreDistribution = 1.00000 
			user1.runtimeMu = 13147.59091 
			user1.runtimeSigma = 19111.74779 
			user1.runtimeDistribution = 13147.59091
			user1.thinkTimeM = 0.4826 
			user1.thinkTimeB = 1779*/
	
}
