package de.dortmund.tu.wmsi.usermodel.model;

import java.util.LinkedList;
import java.util.List;

import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class UserModelCreator {

	public static List<User> createUserList(String configPath) {
		List<User> resultList = new LinkedList<User>();
		
		PropertiesHandler properties = new PropertiesHandler(configPath);

		//iteration for all users and append to user list
		String usernamesPropertyString = properties.getString("setup_user", null);
		usernamesPropertyString = usernamesPropertyString.replaceAll("\\s","");
		String[] usernames = usernamesPropertyString.split(",");
		
		
		for ( String username : usernames) {
			User u = createUser(properties, username);

			resultList.add(u);
			System.out.println(u.getBatchSizeMu());
		}
		
		return resultList;
	}
	
	
	private static User createUser(PropertiesHandler properties, String username) {
		User u = new User();
		
		u.setName(username);
		u.setUserId(Integer.parseInt(username.substring(4)));
		u.setNumberOfProvidedResources(properties.getInt("site.numberOfProvidedResources", 0));
		u.setBatchSizeMu(properties.getDouble(username + ".batchSizeMu", 0));
		u.setDistributionString(properties.getString(username + ".coreDistribution", null));
		u.setDayDistributionString(properties.getString(username + ".dayDistribution", null));
		u.setRuntimeString(properties.getString(username + ".runtimeDistribution", null));
		u.setRuntimeMuhatString(properties.getString(username + ".runtimeMu", null));
		u.setRuntimeSigmahatString(properties.getString(username + ".runtimeSigma", null));
		
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
