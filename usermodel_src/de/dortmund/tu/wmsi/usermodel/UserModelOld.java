package de.dortmund.tu.wmsi.usermodel;

import java.io.File;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.model.BatchCreator;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelOld {

	public static void main(String[] args) {
		if(!new File("avg_output/").exists()) {
			new File("avg_output/").mkdir();
		}
		
		final int RUNS = 15;
		
		runsim(RUNS , "ctc_abs_overtime_easy",
				"ctc_bpf_config/simulation_overtime_easy_absolute.properties"
		);
		
		runsim(RUNS , "mira_abs_overtime_easy",
				"mira_bpf_config/simulation_easy_avg.properties",
				"mira_bpf_config/simulation_overtime_easy_absolute.properties"
		);
			
		runsim(RUNS , "kth_abs_overtime_easy",
				"kth_bpf_config/simulation_easy_avg.properties",
				"kth_bpf_config/simulation_overtime_easy_absolute.properties"
		);
	}
	
	private static void runsim(int n, String appendix, String ...configs) {
		StatisticalMathHelper.setUserAccepteableWaitTimeParameters(1.70, 29.5 * 60);
		//StatisticalMathHelper.setUserAccepteableWaitTimeParameters(2.28, 215.75 * 60); // .75 quantile setting
		//StatisticalMathHelper.setUserAccepteableWaitTimeParameters(1.14, -166.19 * 60);

		for (int i = 0; i < configs.length; i++) {
			SimulationInterface si = SimulationInterface.instance();
			
			AVGWTLogger.resetLog();
			
			for (int j = 0; j < n; j++) {
				BatchCreator.resetBatchStatistics();
				
				si.configure(configs[i]);
				si.simulate();
			}
			
			
			SimulationInterface.destroy();
			System.gc();
		}

		try {
			new File("avg_output/").renameTo(new File("avg_output_"+appendix));
			new File("avg_output/").mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
