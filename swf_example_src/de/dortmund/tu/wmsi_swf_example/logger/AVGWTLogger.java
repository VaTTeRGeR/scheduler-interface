package de.dortmund.tu.wmsi_swf_example.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
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

public class AVGWTLogger implements Logger {

	private HashMap	<Long, Long>	userToWaitTime;
	private HashMap	<Long, Long>	userToJobCount;
	private long globalWaitTime = 0, globalJobCount = 0;
	private long throughput = 0;
	private long t_last_submit = Long.MIN_VALUE;
	private long t_last_finish = Long.MIN_VALUE;
	private long max_resources = 0;
	private static Queue	<String> log = new LinkedList<String>();

	private PropertiesHandler properties = null;
	
	@Override
	public void initialize() {
		clear();

		StringBuilder builder = new StringBuilder();
		
		if(log.isEmpty()) {
			builder.append("%"+String.format("%15s", "AVGWT_U"));
			builder.append(String.format("%16s", "AVGWT_ALL"));
			builder.append(String.format("%16s", "THROUGHPUT"));
			builder.append(String.format("%16s", "LAST_SUBMIT"));
			builder.append(String.format("%16s", "LAST_FINISH"));
			log.add(builder.toString());
		}

		if(properties != null) {
			final String swfPath = properties.getString("swf_output_file", null);
			final boolean printToConsole = properties.getBoolean("print_to_console", false);
			max_resources = properties.getLong("resources", Long.MAX_VALUE);

			SimulationInterface.instance()
					.register(new WorkloadModelRoutine(new RoutineTimingOnce(SimulationInterface.instance().getSimulationEndTime() - 1L)) {
						@Override
						public void process(long time) {
							long sumUserAverageWaitTimes = 0;
							long userCount = userToWaitTime.size();
							for(Long wt : userToWaitTime.keySet()) {
								sumUserAverageWaitTimes += userToWaitTime.get(wt)/userToJobCount.get(wt);
							}
							long avgWaitTime = sumUserAverageWaitTimes/userCount;
							
							SimulationInterface si = SimulationInterface.instance();
							long t_simulated = si.getSimulationEndTime()-si.getSimulationBeginTime();
							
							double tp = ((double)(throughput/t_simulated))/(double)max_resources;
							DecimalFormatSymbols dfs = new DecimalFormatSymbols();
							dfs.setDecimalSeparator('.');
							log.add(String.format("%16s", avgWaitTime)+String.format("%16s", (globalWaitTime/globalJobCount))+String.format("%16s", new DecimalFormat("0.0000", dfs).format(tp))+String.format("%16s", t_last_submit)+String.format("%16s", t_last_finish));
							
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
		
		long user = job.get(Job.USER_ID);
		long wt = userToWaitTime.getOrDefault(user, 0L);
		long jc = userToJobCount.getOrDefault(user, 0L);
		
		wt += job.get(Job.WAIT_TIME);
		jc++;
		
		userToWaitTime.put(user, wt);
		userToJobCount.put(user, jc);
		
		globalWaitTime += job.get(Job.WAIT_TIME);
		globalJobCount++;
		
		throughput += job.get(Job.RUN_TIME)*job.get(Job.RESOURCES_REQUESTED);
		
		if(t_last_submit <= job.get(Job.SUBMIT_TIME)) {
			t_last_submit = job.get(Job.SUBMIT_TIME);
			t_last_finish = job.get(Job.SUBMIT_TIME)+job.get(Job.WAIT_TIME)+job.get(Job.RUN_TIME);
		}
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
		globalWaitTime = 0;
		globalJobCount = 0;
		throughput = 0;
		t_last_submit = Long.MIN_VALUE;
		t_last_finish = Long.MIN_VALUE;
		userToWaitTime = new HashMap<Long, Long>();
		userToJobCount = new HashMap<Long, Long>();
	}

	@Override
	public void jobStarted(JobStartedEvent event) {}

	public static void resetLog() {
		log.clear();
	}
}
