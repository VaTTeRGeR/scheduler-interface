package de.dortmund.tu.wmsi.usermodel.model.userestimate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.util.SWFFileUtil;

public class EstimateSampler {
	private int								numBins;
	
	private ArrayList<ArrayList<Long>>		runtimes; // [estimateIndex, runtimeIndex]
	private Map<Long, ArrayList<Long>>		estimateToRuntimes; 

	private long							biggestTimeSeconds;
	private double 							binSize;
	
	public EstimateSampler(String swfPath) {
		
		loadSwfData(swfPath);
		build();
	}
	
	private void loadSwfData(String swfPath) {
		estimateToRuntimes = new HashMap<Long, ArrayList<Long>>();
		
		if(swfPath == null || !(new File(swfPath).exists())) {
			throw new IllegalStateException("SWF File "+swfPath+" does not exist.");
		}
		
		String[] lines = SWFFileUtil.loadLines(new File(swfPath));
		
		for (int i = 0; i < lines.length; i++) {
			
			String[] line = lines[i].split("\\s+");
			
			if(getValue(line, Job.SUBMIT_TIME)			!= -1 &&
			   getValue(line, Job.RUN_TIME)				!= -1 &&
			   getValue(line, Job.RESOURCES_ALLOCATED)	!= -1 &&
			   getValue(line, Job.USER_ID)				!= -1 &&
			   getValue(line, Job.RESOURCES_REQUESTED)	!= -1 &&
			   getValue(line, Job.TIME_REQUESTED)		!= -1 &&
			   getValue(line, Job.RUN_TIME)				!= -1
			) {
				long t_run = getValue(line, Job.RUN_TIME);
				long t_estimate = getValue(line, Job.TIME_REQUESTED);

				if(t_run > biggestTimeSeconds) {
					biggestTimeSeconds = t_run;
					System.out.println("Biggest Value is now "+biggestTimeSeconds);
				}
				
				if(t_estimate > biggestTimeSeconds) {
					biggestTimeSeconds = t_estimate;
					System.out.println("Biggest Value is now "+biggestTimeSeconds);
				}

				ArrayList<Long> runtimeList = null;
				if(estimateToRuntimes.containsKey(t_estimate)) {
					runtimeList = estimateToRuntimes.get(t_estimate);
					runtimeList.add(t_run);
					System.out.println("("+t_estimate+":"+t_run+") added to existing List of Size "+runtimeList.size());
				} else {
					runtimeList = new ArrayList<Long>();
					runtimeList.add(t_run);
					estimateToRuntimes.put(t_estimate, runtimeList);
					System.out.println("("+t_estimate+":"+t_run+") added to new List");
				}
				
			} else {
				System.out.println("Job "+getValue(line, Job.JOB_ID)+" was not loaded, invalid values");
			}
		}
	}

	private void build() {
		final double biggestTimeHours = ((double)biggestTimeSeconds)/3600D;
		numBins = (int)(biggestTimeHours + 0.5D);
		binSize = biggestTimeSeconds/((double)numBins);
		
		runtimes = new ArrayList<ArrayList<Long>>(numBins);
		for (int i = 0; i < numBins; i++) {
			runtimes.add(i, new ArrayList<Long>());
		}

		System.out.println("Created "+numBins+" bins with a size of "+binSize+".");
	}
	
	private int secondsToBinIndex(long time) {
		return (int)( ((double)time)/((double)binSize) + 0.5D);
	}

	private static long getValue(String[] line, int index) {
		return Long.valueOf((line[index]));
	}
}
