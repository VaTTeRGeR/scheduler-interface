package de.dortmund.tu.wmsi.usermodel.model.userestimate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.util.SWFFileUtil;

public class EstimateSampler {
	private int								numBins;
	
	private ArrayList<ArrayList<Long>>		runtimeHits; // [estimateIndex, runtimeIndex]
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
					//System.out.println("("+t_estimate+":"+t_run+") added to existing List of Size "+runtimeList.size());
				} else {
					runtimeList = new ArrayList<Long>();
					runtimeList.add(t_run);
					estimateToRuntimes.put(t_estimate, runtimeList);
					//System.out.println("("+t_estimate+":"+t_run+") added to new List");
				}
				
			} else {
				//System.out.println("Job "+getValue(line, Job.JOB_ID)+" was not loaded, invalid values");
			}
		}
	}

	private void build() {
		numBins = 32;
		binSize = biggestTimeSeconds / ((double)numBins);
		
		runtimeHits = new ArrayList<ArrayList<Long>>(numBins);
		
		for (int i = 0; i < numBins; i++) {
			ArrayList<Long> runtimeHitList = new ArrayList<Long>(numBins);
			for (int j = 0; j < numBins; j++) {
				runtimeHitList.add(j, 1L);
				System.out.println("Created bin ["+i+"|"+j+"]");
			}
			runtimeHits.add(i, runtimeHitList);
		}
	
		for(Long estimate : estimateToRuntimes.keySet()) {
			System.out.println("Sorting in estimate "+estimate+".");
			ArrayList<Long> runtimesOfEstimate = estimateToRuntimes.get(estimate);
			for (Long runtime : runtimesOfEstimate) {
				System.out.println("Sorting in runtime "+runtime+" for estimate "+estimate+" into bin ["+getBinIndex(estimate)+"|"+getBinIndex(runtime)+"]");
				ArrayList<Long> runtimeHitRates = runtimeHits.get(getBinIndex(estimate));
				Long runTimeHitRateNew = runtimeHitRates.get(getBinIndex(runtime));
				runtimeHitRates.set(getBinIndex(runtime), runTimeHitRateNew + 1L);
			}
		}
	}
	
	private int getBinIndex(long seconds) {
		int binId = (int)(seconds / binSize);
		binId = Math.max(0, binId);
		binId = Math.min(numBins - 1, binId);
		
		return binId;
	}

	private static long getValue(String[] line, int index) {
		return Long.valueOf((line[index]));
	}
}
