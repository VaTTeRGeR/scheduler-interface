package de.dortmund.tu.wmsi.usermodel;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelMain {

	private static final boolean doAVG = false;
	private static final boolean doSWF = true;
	private static final int numSimAVG = 20;
	private static final int numSimSWF = 5;
	
	public static void main(String[] args) {
		if(doAVG) {
			runsim(numSimAVG,"mira_bpf_config/simulation_batch_priority_fair_estimate_avg.properties", "mira_bpf_config/simulation_easy_avg.properties", "mira_estimate_avg");
			runsim(numSimAVG,"lanl_bpf_config/simulation_batch_priority_fair_estimate_avg.properties", "lanl_bpf_config/simulation_easy_avg.properties", "lanl_estimate_avg");
			runsim(numSimAVG,"ctc_bpf_config/simulation_batch_priority_fair_estimate_avg.properties", "ctc_bpf_config/simulation_easy_avg.properties", "ctc_estimate_avg");
			runsim(numSimAVG,"kth_bpf_config/simulation_batch_priority_fair_estimate_avg.properties", "kth_bpf_config/simulation_easy_avg.properties", "kth_estimate_avg");
		}
		if(doSWF) {
			for (int i = 1; i <= numSimSWF; i++) {
				runsim(1,"mira_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "mira_bpf_config/simulation_easy_swf.properties", "mira_estimate_swf"+i);
				runsim(1,"lanl_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "lanl_bpf_config/simulation_easy_swf.properties", "lanl_estimate_swf"+i);
				runsim(1,"ctc_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "ctc_bpf_config/simulation_easy_swf.properties", "ctc_estimate_swf"+i);
				runsim(1,"kth_bpf_config/simulation_batch_priority_fair_estimate_swf.properties", "kth_bpf_config/simulation_easy_swf.properties", "kth_estimate_swf"+i);
			}
		}
	}
	
	private static void runsim(int n, String config0, String config1, String appendix) {
		//StatisticalMathHelper.setUserAccepteableWaitTimeParameters(c1, c2);

		SimulationInterface si = SimulationInterface.instance();

		AVGWTLogger.resetLog();

		for (int i = 0; i < n; i++) {
			si.configure(config0);
			si.simulate();
		}

		AVGWTLogger.resetLog();

		for (int i = 0; i < n; i++) {
			si.configure(config1);
			si.simulate();
		}
		
		try {
			new File("avg_output/").renameTo(new File("avg_output_"+appendix));
			new File("avg_output/").mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SimulationInterface.destroy();
		System.gc();
	}
}
