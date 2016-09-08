package de.dortmund.tu.wmsi_swf_example.scheduler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.util.GiniUtil;

public class JobWaitTimeComparatorGini implements Comparator<Job> {
	private HashMap<Long, Long> waitTime;
	private HashMap<Long, Long> jobCount;
	private HashMap<Long, Double> avgWeightedWaitTime;

	private HashMap<Job, Long> jobToGini = new HashMap<Job, Long>();
	private LinkedList<Double> awwt = new LinkedList<Double>();

	private long wait_max;

	public JobWaitTimeComparatorGini(
			HashMap<Long, Long> waitTime,
			HashMap<Long, Long> jobCount,
			HashMap<Long, Double> avgwwt,
			long wait_max)
	{
		this.waitTime = waitTime;
		this.jobCount = jobCount;
		this.avgWeightedWaitTime = avgwwt;
		this.wait_max = wait_max;
	}

	public void prepareCompare(LinkedList<Job> queue) {
		jobToGini.clear();
		for (Job job : queue) {
			awwt.clear();
			for (Long userId : waitTime.keySet()) {
				if(userId == job.get(Job.USER_ID)) {
					long wt = waitTime.get(userId) + job.get(Job.WAIT_TIME);
					long jc = jobCount.get(userId) + 1;
					awwt.add(((double)wt)/(double)jc);
				} else {
					awwt.add(avgWeightedWaitTime.get(userId));
				}
			}
			int i = 0;
			double[] awwtValues = new double[awwt.size()];
			for (Double value : awwt) {
				awwtValues[i++] = value;
			}
			jobToGini.put(job, (long)(GiniUtil.getGiniCoefficient(awwtValues)*100000d));
			//System.out.println("Gini if Job "+job.getJobId()+ " taken: "+(long)(GiniUtil.getGiniCoefficient(awwtValues)*100000d) + " / "+GiniUtil.getGiniCoefficient(awwtValues));
		}
	}
	
	@Override
	public int compare(Job j0, Job j1) {
		if(wait_max > 0 && (j0.get(Job.WAIT_TIME) > wait_max || j1.get(Job.WAIT_TIME) > wait_max))
			return (int)(j1.get(Job.WAIT_TIME)-j0.get(Job.WAIT_TIME));
		else
			return (int)(jobToGini.get(j0)-jobToGini.get(j1));
	}
}
