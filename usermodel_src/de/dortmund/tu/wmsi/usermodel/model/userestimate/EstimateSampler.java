package de.dortmund.tu.wmsi.usermodel.model.userestimate;

import java.util.ArrayList;

public class EstimateSampler {
	ArrayList<ArrayList<Long>> runtimes;
	
	public EstimateSampler(int bins) {
		runtimes = new ArrayList<ArrayList<Long>>(bins);
		for (int i = 0; i < bins; i++) {
			runtimes.set(i, new ArrayList<Long>());
		}
	}
}
