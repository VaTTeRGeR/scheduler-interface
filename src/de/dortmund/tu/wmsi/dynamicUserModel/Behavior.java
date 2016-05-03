package de.dortmund.tu.wmsi.dynamicUserModel;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.LogisticDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Behavior {
	
	private static long SECONDS_PER_HOUR = 3600;
	private static long SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
	private static long SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;
	

	User user;
	
	private int numberOfSimulatedWeeks;
	
	private double thinkTimeM;
	private double thinkTimeB;
	
	private DayCreator daycreator;
	
	private double[] dayDistribution;
	private double presenceRatio;
	
	private long[] startOfDays;
	private long[] lengthOfDays;
	
	

	
	public Behavior(User user) {
		this.user = user;
				
		this.numberOfSimulatedWeeks = user.getNumberOfSimulatedWeeks();
		this.presenceRatio 			= user.getPresenceRatio();
		this.daycreator				= user.getDaycreator();
		this.thinkTimeM 			= user.getThinkTimeM();
		this.thinkTimeB 			= user.getThinkTimeB();
		
		this.initializeDayDistribution(user.getDayDistributionString());
		this.initializeDays();
	}

	/**
	 * Initialize daily distribution
	 * 
	 * @param dayDistributionString
	 */
	private void initializeDayDistribution(String dayDistributionString) {
		List<String> singleDayDistribution = Arrays
				.asList(dayDistributionString.split(","));
		this.dayDistribution = new double[singleDayDistribution.size()];
		int currentDistributionIndex = 0;
		for (String currentDistributionString : singleDayDistribution) {
			this.dayDistribution[currentDistributionIndex] = Double.parseDouble(currentDistributionString);
			++currentDistributionIndex;
		}
	}
	/**
	 * Initialize whether active or not
	 */
	private void initializeDays() {
		startOfDays 	= new long[numberOfSimulatedWeeks*7];
		lengthOfDays 	= new long[numberOfSimulatedWeeks*7];

		for (int i = 0 ; i < numberOfSimulatedWeeks ; i++) { 		//weeks
			
			double p = Math.random();
			if (user.getUserId() == 184) {
				System.out.println("User: " + user.getUserId() + " Sampled value: " + p + " presenceRatio: " + presenceRatio);
			}
			boolean activeWeek = (p < presenceRatio);
			
			for (int d = 0; d < 7; d++) { 							//days
				
				boolean activeDay = (Math.random() <= dayDistribution[d]);
				
				if (activeWeek && activeDay) {
					startOfDays[i*7+d] 		= daycreator.sampleStartOfDay();
					lengthOfDays[i*7+d] 	= daycreator.sampleLengthOfDay();
				}
				else {
					startOfDays[i*7+d] 		= -1;
					lengthOfDays[i*7+d] 	= -1;
				}
			}
			
		}
		//System.out.println("start of days: " + Arrays.toString(startOfDays));
		//System.out.println("lengths of days: " + Arrays.toString(lengthOfDays));
	}
	



	/**
	 * 
	 * @param timestamp
	 * @return
	 */
	public long startTimeNextSession(long timestamp) {
		int dayOfStart 	= (int)Math.floor(timestamp/SECONDS_PER_DAY);
		long timeOnDay 	= timestamp-dayOfStart*SECONDS_PER_DAY;
		
		if (dayOfStart > numberOfSimulatedWeeks*7-1) {
			return -1;
		}
		
		if (timeOnDay-startOfDays[dayOfStart] < lengthOfDays[dayOfStart]) {	//Job finished before day ended
			System.out.println("start session on same day at " + timestamp);
			
			return timestamp;
		}
		else {
			int d = dayOfStart+1;
			while (d < numberOfSimulatedWeeks*7 && startOfDays[d] == -1) {
				d++;
			}
			if (d < numberOfSimulatedWeeks*7) {
				return d*SECONDS_PER_DAY + startOfDays[d];
			}
			else {
				System.out.println("end of simulation of user " + user.getUserId());
				return -1;
			}
		}
	}
	
	
	
	
	/**
	 * Takes values of last finished job and calculates when to submit next batch
	 * return -1 if session terminates
	 * 
	 * @param timestamp
	 * @return
	 */
	public long startTimeNextBatch(long submittime, long responsetime) {
		long t = -1;
		
		long thinktime = this.thinktime(responsetime);
		
		if (this.stillActive(submittime,submittime+thinktime)) {
			t = submittime+responsetime+thinktime;
		}
			
		//System.out.println("ST in startTimeNextBatch: "+ submittime);
		//System.out.println("RT in startTimeNextBatch: "+ responsetime);
		//System.out.println("TT in startTimeNextBatch: "+ thinktime);
		//System.out.println("Time in startTimeNextBatch: "+ t);
		
		return t;
	}
	

	/**
	 * 
	 * @param responsetime
	 * @return
	 */
	public long thinktime(long responsetime) {
		long thinktimeBase = (long)Math.floor(thinkTimeM*responsetime+thinkTimeB);
		
		NormalDistribution d = new NormalDistribution(thinktimeBase, thinktimeBase/2);
		
		double probability = Math.random();
		long thinktime = (int)d.inverseCumulativeProbability(probability);
		while (thinktime < 0) {
			probability = Math.random();
			thinktime = (int)d.inverseCumulativeProbability(probability);
		}
		
		return thinktime;
	}
	
	
	/**
	 * 
	 * @param starttime
	 * @param endtime
	 * @return
	 */
	public boolean stillActive(long starttime, long endtime) {
		int dayOfStart 	= (int)Math.floor(starttime/SECONDS_PER_DAY);
		long timeOnDay 	= endtime-dayOfStart*SECONDS_PER_DAY;
		
		if (dayOfStart > numberOfSimulatedWeeks*7-1) {
			return false;
		}
		
		if (timeOnDay-startOfDays[dayOfStart] < lengthOfDays[dayOfStart]) {	//Job finished before day ended
			return true;
		}
		
		return false;
	}

	
	
	

//	/**
//	 * Returns a boolean value, whether or not a Session continues after a job finished according to Zakay 2012(?).
//	 * true: continue session
//	 * false: end session
//	 * 
//	 * @param responseTime
//	 * @return
//	 */
//	public boolean continueSession(long responseTime) {
//		double randomValue = Math.random();
//		double continueProbability = 0.8/((0.05*(responseTime/60)) + 1);
//		return randomValue <= continueProbability;
//	}
//	
//	/**
//	 * 
//	 * @param timestamp
//	 * @return
//	 */
//	public long nextTimeActive(long timestamp) {
//		return timestamp+1000;
//	}
//	
//	/**
//	 * 
//	 * @param day
//	 * @param secondsAtDay
//	 * @return
//	 */
//	public boolean isActiveAtTime(long timestamp) {
//		//TODO vernünftig machen
//		return (Math.random() <= 0.5);
//	}
//	
//	/**
//	 * 
//	 * @param day
//	 * @return
//	 */
//	private long firstSubmittalAtDay(int day) {
//		return startOfDay;
//	}
//	
//	/**
//	 * 
//	 * @param day
//	 * @return
//	 */
//	private long lastSubmittalAtDay(int day) {
//		return endOfDay;
//	}


	
}
