package de.dortmund.tu.wmsi_swf_example.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.job.SWFJob;
import de.dortmund.tu.wmsi.logger.Logger;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;
import de.dortmund.tu.wmsi.util.Util;

public class SWF_Logger implements Logger {

	private Queue<String> log = new LinkedList<String>();
	private static final String seperator = "\t";
	
	@Override
	public void init(final String configPath) {
		SimulationInterface.log("loading: "+configPath);
		Properties properties = Util.getProperties(configPath);

		final String swfPath = properties.getProperty("swf_output_file");
		final boolean printToConsole = Boolean.parseBoolean(properties.getProperty("print_to_console", "false"));

		SimulationInterface.instance().register(new WorkloadModelRoutine(new RoutineTimingOnce(Long.MAX_VALUE-1)) {
			@Override
			public void process(long time) {
				saveLog(swfPath);
				if(printToConsole) {
					printLog();
				}
			}
		});
	}

	@Override
	public void jobFinished(JobFinishedEvent event) {
		Job job = event.getJob();
		
		if(job instanceof SWFJob) {
			long t_finish = event.getTime();
			long t_wait = (t_finish - job.getRunDuration()) - job.getSubmitTime();

			SWFJob swfJob = (SWFJob)job;
			swfJob.set(SWFJob.WAIT_TIME, t_wait);
			
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 18; i++) {
				builder.append(swfJob.get(i));
				builder.append(seperator);
			}

			log.add(builder.toString());
		}
	}
	
	public String[] getLog() {
		return log.toArray(new String[log.size()]);
	}

	public void printLog() {
		String[] logArray = getLog();
		System.out.println("\n;SWF BEGIN");
		for (int i = 0; i < logArray.length; i++) {
			System.out.println(logArray[i]);
		}
		System.out.println(";END\n");
	}
	
	public void saveLog(String path) {
		try {
			PrintWriter writer = new PrintWriter(path);
			String[] logArray = getLog();
			writer.println(";SWF BEGIN");
			for (int i = 0; i < logArray.length; i++) {
				writer.println(logArray[i]);
			}
			writer.println(";END");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void clear() {
		log.clear();
	}

	@Override
	public void jobStarted(JobStartedEvent event) {}
}
