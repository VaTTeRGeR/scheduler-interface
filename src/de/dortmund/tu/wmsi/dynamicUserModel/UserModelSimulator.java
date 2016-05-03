package de.dortmund.tu.wmsi.dynamicUserModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;

import de.irf.it.rmg.research.workload.util.ConfigReader;


public class UserModelSimulator {

	
	
	public static void main(String[] args) throws Exception {
		ConfigReader reader = new ConfigReader();
		
		//Folder with all config files
		final File configfolder = new File(reader.getConfigFolderPath());
		
		//Run simulation for each configuration
		for (final File fileEntry : configfolder.listFiles()) {
			
			String extension 		= FilenameUtils.getExtension(fileEntry.getName());

			//Check if file is property-file
			if (extension.equals("properties") && !fileEntry.isDirectory()) {
				int repeatitions 	= 1; //TODO: save to different files if value > 1
				String name 		= FilenameUtils.removeExtension(fileEntry.getName());
				String configPath 	= configfolder + "/" + name + ".properties";				
				String resultPath 	= configfolder + "_results/"; 
				
				runSimulation(repeatitions, name, configPath, resultPath);
		    }
		}	
	}



	public static void runSimulation(int repeatitions, String name,
			String propertiesPath, String resultPath) throws Exception {
		final int NUMBER_OF_SIMULATIONS = repeatitions;
		final String NAME_OF_SIMULATION = name;
		final String PROPERTIES_PATH = propertiesPath;

		ConfigReader reader = new ConfigReader();

		String[] command = reader.getCommand(PROPERTIES_PATH);

		for ( int currentRun = 1; currentRun <= NUMBER_OF_SIMULATIONS; currentRun++) {
		
			Process pro = Runtime.getRuntime().exec(command);
			System.out.println("[Simulation -" + NAME_OF_SIMULATION + "- "
					+ currentRun + " gestartet]");
			printLines(command + " stdout:", pro.getInputStream());
			printLines(command + " stderr:", pro.getErrorStream());
			pro.waitFor();
			System.out.println(command + " exitValue() " + pro.exitValue());
			
			Files.copy(Paths.get(reader.getTracePath()), 
					Paths.get(resultPath + name + ".csv"), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	

	private static void printLines(String name, InputStream ins)
			throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			System.out.println(name + " " + line);
		}
	}
	
	


}
