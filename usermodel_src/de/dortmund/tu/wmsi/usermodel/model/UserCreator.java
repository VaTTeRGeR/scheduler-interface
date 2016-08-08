package de.dortmund.tu.wmsi.usermodel.model;

import java.util.LinkedList;
import java.util.List;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.util.UserModelTimeHelper;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class UserCreator {

	private static long SECONDS_PER_MINUTE = 60;
	private static long SECONDS_PER_HOUR = SECONDS_PER_MINUTE*60;
	private static long SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
	private static long SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;

	public static List<User> createUserList(String configPath) {
		List<User> users = new LinkedList<User>();
		
		PropertiesHandler properties = new PropertiesHandler(configPath);

		String usernamesPropertyString = properties.getString("setup_user", null);
		usernamesPropertyString = usernamesPropertyString.replaceAll("\\s","");
		String[] usernames = usernamesPropertyString.split(",");
		
		
		for ( String username : usernames) {
			User u = createUser(properties, username);
			users.add(u);
		}
		
		return users;
	}
	
	
	private static User createUser(PropertiesHandler properties, String username) {
		User u = new User();
		
		u.setName(properties.getString(username + ".name", null));
		u.setUserId(properties.getInt(username + ".userId", Integer.MIN_VALUE));
		u.setPresenceRatio(properties.getDouble(username + ".presenceRatio", Double.NaN));
		u.setDayDistributionString(properties.getString(username + ".dayDistribution", null));
		u.setStartOfDayMu(properties.getDouble(username + ".startOfDayMu", Double.NaN));
		u.setStartOfDaySigma(properties.getDouble(username + ".startOfDaySigma", Double.NaN));
		u.setWorkLengthLessOneHour(properties.getDouble(username + ".workLengthLessOneHour", Double.NaN));
		u.setWorkLengthMu(properties.getDouble(username + ".workLengthMu", Double.NaN));
		u.setWorkLengthSigma(properties.getDouble(username + ".workLengthSigma", Double.NaN));
		u.setBatchSizeOne(properties.getDouble(username + ".batchSizeOne", Double.NaN));
		u.setBatchSizeMu(properties.getDouble(username + ".batchSizeMu", Double.NaN));
		u.setBatchSizeSigma(properties.getDouble(username + ".batchSizeSigma", Double.NaN));
		u.setInterarrivalTimeMu(properties.getDouble(username + ".interarrivalTimeMu", Double.NaN));
		u.setInterarrivalTimeSigma(properties.getDouble(username + ".interarrivalTimeSigma", Double.NaN));
		u.setDistributionString(properties.getString(username + ".coreDistribution", null));
		u.setRuntimeMuhatString(properties.getString(username + ".runtimeMu", null));
		u.setRuntimeSigmahatString(properties.getString(username + ".runtimeSigma", null));
		u.setRuntimeString(properties.getString(username + ".runtimeDistribution", null));
		u.setThinkTimeB(properties.getDouble(username + ".thinkTimeB", Double.NaN));
		u.setThinkTimeM(properties.getDouble(username + ".thinkTimeM", Double.NaN));
		
		u.setNumberOfProvidedResources(properties.getInt("site.numberOfProvidedResources", 0));
		
		SimulationInterface si = SimulationInterface.instance();
		long simSeconds = si.getSimulationEndTime()-si.getSimulationBeginTime();
		
		u.setNumberOfSimulatedWeeks((int)UserModelTimeHelper.toWeeks(simSeconds));

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
