package de.dortmund.tu.wmsi_swf_example.logger;

import java.io.IOException;
import java.io.PrintWriter;
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
	private static Queue	<String> log = new LinkedList<String>();

	private PropertiesHandler properties = null;
	
	@Override
	public void initialize() {
		clear();

		StringBuilder builder = new StringBuilder();
		
		if(log.isEmpty()) {
			builder.append("%"+String.format("%11s", "AVG_WAITTIME"));
			log.add(builder.toString());
		}

		if(properties != null) {
			final String swfPath = properties.getString("swf_output_file", null);
			final boolean printToConsole = properties.getBoolean("print_to_console", false);

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
							log.add(avgWaitTime+"\t"+(globalWaitTime/globalJobCount)+"\t"+(throughput/t_simulated));
							
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
		userToWaitTime = new HashMap<Long, Long>();
		userToJobCount = new HashMap<Long, Long>();
	}

	@Override
	public void jobStarted(JobStartedEvent event) {}

	public static void resetLog() {
		log.clear();
	}
}
