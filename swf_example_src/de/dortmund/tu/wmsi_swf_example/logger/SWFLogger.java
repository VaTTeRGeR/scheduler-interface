package de.dortmund.tu.wmsi_swf_example.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.logger.Logger;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class SWFLogger implements Logger {

	private Queue<String> log = new LinkedList<String>();
	private PropertiesHandler properties = null;
	
	@Override
	public void initialize() {
		log.clear();
		StringBuilder builder = new StringBuilder();
		
		builder.append(";"+String.format("%11s", "JOB_ID"));
		builder.append(String.format("%12s", "T_SUB"));
		builder.append(String.format("%12s", "T_WAIT"));
		builder.append(String.format("%12s", "T_RUN"));
		builder.append(String.format("%12s", "RES_USED"));
		builder.append(String.format("%12s", "AVG_CPU"));
		builder.append(String.format("%12s", "MEM_USED"));
		builder.append(String.format("%12s", "RES_REQ"));
		builder.append(String.format("%12s", "T_RUN_REQ"));
		builder.append(String.format("%12s", "MEM_REQ"));
		builder.append(String.format("%12s", "STATUS"));
		builder.append(String.format("%12s", "USER_ID"));
		builder.append(String.format("%12s", "GROUP_ID"));
		builder.append(String.format("%12s", "EXE_ID"));
		builder.append(String.format("%12s", "QUEUE_ID"));
		builder.append(String.format("%12s", "PART_ID"));
		builder.append(String.format("%12s", "PRECEDING"));
		builder.append(String.format("%12s", "THINKTIME"));
		
		log.add(builder.toString());

		if(properties != null) {
			final String swfPath = properties.getString("swf_output_file", null);
			final boolean printToConsole = properties.getBoolean("print_to_console", false);

			SimulationInterface.instance()
					.register(new WorkloadModelRoutine(new RoutineTimingOnce(SimulationInterface.instance().getSimulationEndTime() - 1L)) {
						@Override
						public void process(long time) {
							saveLog(swfPath);
							if (printToConsole) {
								printLog();
							}
						}
					});
		} else {
			throw new IllegalStateException("SWFLogger was not configured");
		}
	}

	@Override
	public void configure(final String configPath) {
		SimulationInterface.log("loading: "+configPath);
		properties = new PropertiesHandler(configPath);
	}

	@Override
	public void jobFinished(JobFinishedEvent event) {
		Job job = event.getJob();
		
		long t_finish = event.getTime();
		long t_wait = (t_finish - job.getRunDuration()) - job.getSubmitTime();

		Job swfJob = job;
		swfJob.set(Job.WAIT_TIME, t_wait);
			
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 18; i++) {
			builder.append(String.format("%12d", swfJob.get(i)));
		}

		log.add(builder.toString());
	}
	
	public String[] getLog() {
		return log.toArray(new String[log.size()]);
	}

	public void printLog() {
		String[] logArray = getLog();
		
		for (int i = 0; i < logArray.length; i++) {
			System.out.println(logArray[i]);
		}
		System.out.println("\n");
	}
	
	public void saveLog(String path) {
		try {
			PrintWriter writer = new PrintWriter(path);
			String[] logArray = getLog();
			for (int i = 0; i < logArray.length; i++) {
				writer.println(logArray[i]);
			}
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
