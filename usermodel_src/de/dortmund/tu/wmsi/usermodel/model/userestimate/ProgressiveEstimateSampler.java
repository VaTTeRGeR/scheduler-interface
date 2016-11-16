package de.dortmund.tu.wmsi.usermodel.model.userestimate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProgressiveEstimateSampler {
	private int								numBins;
	private double 							binSize;
	private long							biggestTimeSeconds;
	
	private long[][]						estimateToHitBins;			// [estimateIndex, runtimeIndex]
	private long[]							estimateTotalHits;			// [estimateIndex]
	private long[]							runtimeTotalHits;			// [estimateIndex]
	
	private Map<Long, ArrayList<Long>>		estimateToRuntimeSamples;	// [estimate] -> runtimes of that estimate

	
	public ProgressiveEstimateSampler(int numBins) {
		this.numBins = numBins;
		binSize = 0;
		biggestTimeSeconds = 0;
		estimateToRuntimeSamples = new HashMap<Long, ArrayList<Long>>();
	}
	
	public void addJobSample(long t_run, long t_estimate) {
	
		if(estimateToRuntimeSamples.containsKey(t_estimate)) {
			
			ArrayList<Long> runtimesList = estimateToRuntimeSamples.get(t_estimate);
			runtimesList.add(t_run);
			
		} else {
			
			ArrayList<Long> runtimesList = new ArrayList<Long>();
			runtimesList.add(t_run);
			
			estimateToRuntimeSamples.put(t_estimate, runtimesList);
		}
		
		if(Math.max(t_run, t_estimate) > biggestTimeSeconds) {
			
			biggestTimeSeconds = Math.max(t_run, t_estimate);
			
			build();
			
		} else {
			
			estimateToHitBins[getBinIndex(t_estimate)][getBinIndex(t_run)]++;
			
			estimateTotalHits[getBinIndex(t_estimate)]++;
			runtimeTotalHits[getBinIndex(t_run)]++;
		}
	}

	private void build() {
		binSize = biggestTimeSeconds / ((long)numBins);
		
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
				//System.out.println("Created bin ["+i+"|"+j+"] with "+estimateToHitBins[i][j]+" bins.");
			}
		}
	
		for (int i = 0; i < numBins; i++) {
			estimateTotalHits[i] = i+1;
			//System.out.println("Estimate "+i+" starts with a total of "+(i+1)+" hits.");
		}
		
		for (int i = 0; i < numBins; i++) {
			runtimeTotalHits[i] = numBins - i;
			//System.out.println("Runtime "+i+" starts with a total of "+(i+1)+" hits.");
		}
		
		for(Long estimate : estimateToRuntimeSamples.keySet()) {
			//System.out.println("Sorting in estimate "+estimate+".");
			ArrayList<Long> runtimesOfEstimate = estimateToRuntimeSamples.get(estimate);
			for (Long runtime : runtimesOfEstimate) {
				if(estimate <= biggestTimeSeconds && runtime <= biggestTimeSeconds) {
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
		}
		
		/*long delta = 0;
		
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

		System.out.println();*/
	}
	
	public long randomEstimateByRuntime(long seconds) {
		int runtimeBin = getBinIndex(seconds);
		
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
				return (long)(i*binSize + binSize/2);
			}
			
			sumProbabilityOld = sumProbabilityNew;
		}
		return biggestTimeSeconds;
	}
	
	public long averageRuntimeByEstimate(long seconds) {
		int estimateBinIndex = getBinIndex(seconds);
		
		if(estimateTotalHits == null || estimateTotalHits == null || runtimeTotalHits == null || estimateTotalHits[estimateBinIndex] <= 1) {
			return seconds;
		}
		
		double estimate = 0;
		
		for (int i = estimateBinIndex; i < numBins; i++) {
			double hitsRelative = estimateToHitBins[estimateBinIndex][i];
			double hitsAbsolute = estimateTotalHits[estimateBinIndex];

			estimate += (hitsRelative/hitsAbsolute) * binSize * (i + 0.5f);
		}
		return (long)estimate;
	}
	
	private int getBinIndex(long seconds) {
		int binId = (int)(seconds / binSize);
		
		binId = Math.max(0, binId);
		binId = Math.min(numBins - 1, binId);
		
		return binId;
	}
}
