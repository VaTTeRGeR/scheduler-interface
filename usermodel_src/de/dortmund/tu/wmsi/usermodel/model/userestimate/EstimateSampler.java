package de.dortmund.tu.wmsi.usermodel.model.userestimate;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.util.SWFFileUtil;

public class EstimateSampler {
	private int								numBins;
	private long							numSamples;
	private double 							binSize;
	private long							biggestTimeSeconds;
	
	private long[][]						estimateToHitBins;			// [estimateIndex, runtimeIndex]
	private long[]							estimateTotalHits;			// [estimateIndex]
	private long[]							runtimeTotalHits;			// [estimateIndex]
	private Map<Long, ArrayList<Long>>		estimateToRuntimeSamples;	// [estimateIndex]

	
	public EstimateSampler(String swfPath) {
		loadSwfData(swfPath);
		build();
	}
	
	private void loadSwfData(String swfPath) {
		estimateToRuntimeSamples = new HashMap<Long, ArrayList<Long>>();
		numSamples = 0;
		
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
				if(estimateToRuntimeSamples.containsKey(t_estimate)) {
					runtimeList = estimateToRuntimeSamples.get(t_estimate);
					runtimeList.add(t_run);
					//System.out.println("("+t_estimate+":"+t_run+") added to existing List of Size "+runtimeList.size());
				} else {
					runtimeList = new ArrayList<Long>();
					runtimeList.add(t_run);
					estimateToRuntimeSamples.put(t_estimate, runtimeList);
					//System.out.println("("+t_estimate+":"+t_run+") added to new List");
				}
				numSamples++;
			} else {
				//System.out.println("Job "+getValue(line, Job.JOB_ID)+" was not loaded, invalid values");
			}
		}
	}

	private void build() {
		numBins = 32;
		binSize = biggestTimeSeconds / ((double)numBins);
		
		estimateToHitBins = new long[numBins][numBins];
		estimateTotalHits = new long[numBins];
		runtimeTotalHits = new long[numBins];
		
		for (int i = 0; i < numBins; i++) {
			for (int j = 0; j < numBins; j++) {
				if(i >= j) {
					estimateToHitBins[i][j] = 1;
				} else {
					estimateToHitBins[i][j] = 0;
				}
				System.out.println("Created bin ["+i+"|"+j+"] with "+estimateToHitBins[i][j]+" bins.");
			}
		}
	
		for (int i = 0; i < numBins; i++) {
			estimateTotalHits[i] = i+1;
			System.out.println("Estimate "+i+" starts with a total of "+(i+1)+" hits.");
		}
		
		for (int i = 0; i < numBins; i++) {
			runtimeTotalHits[i] = numBins - i;
			System.out.println("Runtime "+i+" starts with a total of "+(i+1)+" hits.");
		}
		
		for(Long estimate : estimateToRuntimeSamples.keySet()) {
			//System.out.println("Sorting in estimate "+estimate+".");
			ArrayList<Long> runtimesOfEstimate = estimateToRuntimeSamples.get(estimate);
			for (Long runtime : runtimesOfEstimate) {
				int eIndex = getBinIndex(estimate);
				int rIndex = getBinIndex(runtime);

				if(eIndex >= rIndex) {
					estimateToHitBins[eIndex][rIndex]++;
					//System.out.println("Sorting in runtime "+runtime+" for estimate "+estimate+" into bin ["+eIndex+"|"+rIndex+"]");
				} else {
					estimateToHitBins[eIndex][eIndex]++;
					//System.out.println("Sorting in runtime "+runtime+" for estimate "+estimate+" into bin ["+eIndex+"|"+eIndex+"]");
				}
				estimateTotalHits[eIndex]++;
				runtimeTotalHits[rIndex]++;
			}
		}
		
		long delta = 0;
		
		for(int rIndex = numBins - 1; rIndex >= 0; rIndex--) {
			double runtimeHits = runtimeTotalHits[rIndex];
			System.out.println(new DecimalFormat("000000").format(runtimeHits));
			
			delta+= runtimeHits;
		}

		System.out.print("       ");
		
		for(int eIndex = 0; eIndex < numBins; eIndex++) {
			double estimateHits = estimateTotalHits[eIndex];
			System.out.print(new DecimalFormat("000000").format(estimateHits));
			System.out.print("  ");
			delta-= estimateHits;
		}
		System.out.println();

		System.out.println();
		System.out.println("delta: "+delta);
		System.out.println();

		for(int rIndex = numBins - 1; rIndex >= 0; rIndex--) {
			for(int eIndex = 0; eIndex < numBins; eIndex++) {
				double hitsOfRuntimeInEstimate = estimateToHitBins[eIndex][rIndex];
				System.out.print(new DecimalFormat("000000").format(hitsOfRuntimeInEstimate));
				System.out.print("  ");
			}
			System.out.println();
		}

		System.out.println();

		for(int rIndex = numBins - 1; rIndex >= 0; rIndex--) {
			for(int eIndex = 0; eIndex < numBins; eIndex++) {
				double hitsOfRuntimeInEstimate = estimateToHitBins[eIndex][rIndex];
				double totalHitsOfEstimate = estimateTotalHits[eIndex];
				System.out.print(new DecimalFormat("0.0000").format(hitsOfRuntimeInEstimate/totalHitsOfEstimate));
				System.out.print("  ");
			}
			System.out.println();
		}

		System.out.println();

		for(int rIndex = numBins - 1; rIndex >= 0; rIndex--) {
			for(int eIndex = 0; eIndex < numBins; eIndex++) {
				double hitsOfRuntimeInEstimate = estimateToHitBins[eIndex][rIndex];
				double totalHitsOfRuntime = runtimeTotalHits[rIndex];
				System.out.print(new DecimalFormat("0.0000").format(hitsOfRuntimeInEstimate/totalHitsOfRuntime));
				System.out.print("  ");
			}
			System.out.println();
		}

		System.out.println();
	}
	
	public int randomEstimateByRuntimeBin(int runtimeBin) {
		double rand = Math.random();
		double sumProbabilityOld = 0;
		double sumProbabilityNew = 0;
		
		//System.out.println("Random: "+rand);
		
		for (int i = runtimeBin; i < numBins; i++) {
			double hitsRelative = estimateToHitBins[i][runtimeBin];
			double hitsAbsolute = runtimeTotalHits[runtimeBin];

			sumProbabilityNew = (hitsRelative/hitsAbsolute) + sumProbabilityOld;

			//System.out.println("Probability added: "+(sumProbabilityNew - sumProbabilityOld));
			//System.out.println("Probability new: "+sumProbabilityNew);

			
			if(sumProbabilityOld <= rand && sumProbabilityNew >= rand) {
				return i;
			}
			
			sumProbabilityOld = sumProbabilityNew;
		}
		return numBins;
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
