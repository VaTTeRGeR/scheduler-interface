package de.dortmund.tu.wmsi;

public class MainCommandLine {

	public static void main(String[] args) {
		SimulationInterface simface = SimulationInterface.instance();
		if(args.length > 0) {
			String configPath = null;
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
				case "-config":
				case "-f":
				case "-file":
					try {
						configPath = args[i+1];
					} catch (NullPointerException ne) {
						ne.printStackTrace();
					}
					break;

				default:
					break;
				}
			}
			if(configPath != null)
				simface.simulate(configPath);
			else 
				System.err.println("config path cannot be null!");
		}
	}
}
