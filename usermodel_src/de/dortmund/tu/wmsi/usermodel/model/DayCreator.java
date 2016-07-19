package de.dortmund.tu.wmsi.usermodel.model;

import org.apache.commons.math3.distribution.LogisticDistribution;

public class DayCreator {

	User user;
	
	double startOfDayMu;
	double startOfDaySigma;
	double workLengthLessOneHour;
	double workLengthMu;
	double workLengthSigma;
	

	public DayCreator(User user) {
		this.user = user;
		
		this.startOfDayMu 			= user.getStartOfDayMu();
		this.startOfDaySigma 		= user.getStartOfDaySigma();
		this.workLengthLessOneHour 	= user.getWorkLengthLessOneHour();
		this.workLengthMu 			= user.getWorkLengthMu();
		this.workLengthSigma 		= user.getWorkLengthSigma();

	}
	
	
	public int sampleStartOfDay() {
		int time = 0;
		
		LogisticDistribution d = new LogisticDistribution(this.startOfDayMu, this.startOfDaySigma);
		
		double probability = Math.random();
		time = (int)d.inverseCumulativeProbability(probability);
		while (time < 0 || time > 24*3600) {
			probability = Math.random();
			time = (int)d.inverseCumulativeProbability(probability);
		}
		
		return time;
	}

	
	public int sampleLengthOfDay() {
		int time = 0;
		
		double lessOneHourProbability = Math.random();
		if (lessOneHourProbability <= workLengthLessOneHour) {	//one hour job
			time = 3600;
		}
		else {	//more than one hour
			LogisticDistribution d = new LogisticDistribution(this.workLengthMu, this.workLengthSigma);
			
			double probability = Math.random();
			time = (int)d.inverseCumulativeProbability(probability);
			while (time < 0 || time > 24*3600) {
				probability = Math.random();
				time = (int)d.inverseCumulativeProbability(probability);
			}
		}
		
		return time;
	}
	
	
}
