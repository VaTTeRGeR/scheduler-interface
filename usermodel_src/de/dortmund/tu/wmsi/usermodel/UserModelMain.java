package de.dortmund.tu.wmsi.usermodel;

import java.io.File;
import java.text.DecimalFormat;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi_swf_example.logger.AVGWTLogger;

public class UserModelMain {

	public static void main(String[] args) {
		runsim(1.7, 29.51 * 60.0);
		//runsim(1.7, 0.0);
		//runsim(2.28, 215.75 * 60);
		//runsim(2.28, 0.0);
	}
	
	private static void runsim(double c1, double c2) {
		StatisticalMathHelper.setUserAccepteableWaitTimeParameters(c1, c2);

		SimulationInterface si = SimulationInterface.instance();
		
		/*AVGWTLogger.resetLog();

		for (int i = 0; i < 1; i++) {
			si.configure("usermodel_config/simulation_absolute_overtime_spread_scaling.properties");
			si.simulate();
		}*/
		
		AVGWTLogger.resetLog();

		for (int i = 0; i < 3; i++) {
			si.configure("usermodel_config/simulation_overtime_easy_absolute.properties");
			si.simulate();
		}
		
		AVGWTLogger.resetLog();

		for (int i = 0; i < 3; i++) {
			si.configure("usermodel_config/simulation_backfill_grouped.properties");
			si.simulate();
		}

		AVGWTLogger.resetLog();

		for (int i = 0; i < 3; i++) {
			si.configure("usermodel_config/simulation_easy_avg.properties");
			si.simulate();
		}

		/*AVGWTLogger.resetLog();

		for (int i = 0; i < 1; i++) {
			si.configure("usermodel_config/simulation_overtime_easy_relative.properties");
			si.simulate();
		}*/
		
		try {
			new File("avg_output/").renameTo(new File("avg_output_"+c1+"_"+new DecimalFormat("#####.##").format(c2/60)+"/"));
			new File("avg_output/").mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
