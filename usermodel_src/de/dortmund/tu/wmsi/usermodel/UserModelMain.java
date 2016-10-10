package de.dortmund.tu.wmsi.usermodel;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelMain {

	public static void main(String[] args) {
		runsim("ctc_bpf_config/simulation_batch_priority_fair_avg.properties", "ctc_bpf_config/simulation_easy_avg.properties", "ctc");
		runsim("kth_bpf_config/simulation_batch_priority_fair_avg.properties", "kth_bpf_config/simulation_easy_avg.properties", "kth");
		runsim("lanl_bpf_config/simulation_batch_priority_fair_avg.properties", "lanl_bpf_config/simulation_easy_avg.properties", "lanl");
		runsim("mira_bpf_config/simulation_batch_priority_fair_avg.properties", "mira_bpf_config/simulation_easy_avg.properties", "mira");
	}
	
	private static void runsim(String config0, String config1, String appendix) {
		//StatisticalMathHelper.setUserAccepteableWaitTimeParameters(c1, c2);

		SimulationInterface si = SimulationInterface.instance();

		AVGWTLogger.resetLog();

		for (int i = 0; i < 30; i++) {
			si.configure(config0);
			si.simulate();
		}

		AVGWTLogger.resetLog();

		for (int i = 0; i < 30; i++) {
			si.configure(config1);
			si.simulate();
		}
		
		try {
			new File("avg_output/").renameTo(new File("avg_output_"+appendix));
			new File("avg_output/").mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
