package de.dortmund.tu.wmsi.usermodel;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.model.BatchCreator;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelMain {

	private static final boolean doAVG = true;
	private static final boolean doSWF = false;
	private static final int numSimAVG = 15;
	private static final int numSimSWF = 5;
	
	public static void main(String[] args) {
		if(!new File("avg_output/").exists()) {
			new File("avg_output/").mkdir();
		}
		
		if(doAVG) {
			runsim(numSimAVG, "hpc2n_avg",
					"hpc2n_bpf_config/simulation_batch_priority_fair_estimate_bfo_avg.properties",
					"hpc2n_bpf_config/simulation_easy_es_avg.properties",
					"hpc2n_bpf_config/simulation_overtime_easy_absolute.properties"
					);
			
			runsim(numSimAVG, "lanl_avg",
					"lanl_bpf_config/simulation_batch_priority_fair_estimate_bfo_avg.properties",
					"lanl_bpf_config/simulation_easy_es_avg.properties",
					"lanl_bpf_config/simulation_overtime_easy_absolute.properties"
					);
			
			runsim(numSimAVG, "ctc_avg",
					"ctc_bpf_config/simulation_batch_priority_fair_estimate_bfo_avg.properties",
					"ctc_bpf_config/simulation_easy_es_avg.properties",
					"ctc_bpf_config/simulation_overtime_easy_absolute.properties"
					);
					
			runsim(numSimAVG, "kth_avg",
					"kth_bpf_config/simulation_batch_priority_fair_estimate_bfo_avg.properties",
					"kth_bpf_config/simulation_easy_es_avg.properties",
					"kth_bpf_config/simulation_overtime_easy_absolute.properties"
					);

			runsim(numSimAVG, "sdsc_avg",
					"sdsc_bpf_config/simulation_batch_priority_fair_estimate_bfo_avg.properties",
					"sdsc_bpf_config/simulation_easy_es_avg.properties",
					"sdsc_bpf_config/simulation_overtime_easy_absolute.properties"
					);
		}
		
		if(doSWF) {
			for (int i = 1; i <= numSimSWF; i++) {
				runsim(1, "mira_bpf_estimate_swf"+i, "mira_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "mira_bpf_config/simulation_easy_swf.properties");
				runsim(1, "lanl_bpf_estimate_swf"+i, "lanl_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "lanl_bpf_config/simulation_easy_swf.properties");
				runsim(1, "ctc_bpf_estimate_swf"+i, "ctc_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "ctc_bpf_config/simulation_easy_swf.properties");
				runsim(1, "kth_bpf_estimate_swf"+i, "kth_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "kth_bpf_config/simulation_easy_swf.properties");
			}
		}
	}
	
	private static void runsim(int n, String appendix, String ...configs) {
		StatisticalMathHelper.setUserAccepteableWaitTimeParameters(1.70, 29.5 * 60);

		for (int i = 0; i < configs.length; i++) {
			SimulationInterface si = SimulationInterface.instance();
			
			AVGWTLogger.resetLog();
			
			for (int j = 0; j < n; j++) {
				BatchCreator.resetBatchStatistics();
				
				si.configure(configs[i]);
				si.simulate();

				//EstimateSampler.saveEstimateMatrix();
				//ProgressiveEstimateSampler.saveEstimateMatrix();
			}
			
			
			SimulationInterface.destroy();
			System.gc();
		}

		try {
			if(new File("avg_output_"+appendix).exists()) {
				new File("avg_output_"+appendix).delete();
			}
				
			new File("avg_output/").renameTo(new File("avg_output_"+appendix));
			new File("avg_output/").mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
