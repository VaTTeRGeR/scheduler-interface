package de.dortmund.tu.wmsi_swf_example.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.logger.Logger;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;
import de.dortmund.tu.wmsi.usermodel.util.StatisticalMathHelper;
import de.dortmund.tu.wmsi.util.JobSubmitComparator;
import de.dortmund.tu.wmsi.util.PropertiesHandler;

public class AVGWTLogger implements Logger {

	private HashMap	<Long, Long>	userToWaitTime_real;
	private HashMap	<Long, Long>	userToJobCount;
	private HashMap	<Long, Long>	userToWaitTime_accwt;
	private HashMap	<Long, ArrayList<Job>>	userToJobs;
	private	ArrayList<Long>			userids;
	private	ArrayList<Long>			waitTimes;

	private long globalWaitTime = 0, globalAccWaitTime = 0, globalJobCount = 0;
	private long throughput = 0;
	private long t_last_submit = Long.MIN_VALUE;
	private long t_last_finish = Long.MIN_VALUE;
	private long max_resources = 0;
	private long t_threshold = 20*60;
	
	private static LinkedList<String> log = new LinkedList<String>();

	private PropertiesHandler properties = null;
	
	public void clear() {
		globalWaitTime = 0;
		globalAccWaitTime = 0;
		globalJobCount = 0;
		throughput = 0;
		t_last_submit = Long.MIN_VALUE;
		t_last_finish = Long.MIN_VALUE;
		userToWaitTime_real = new HashMap<Long, Long>();
		userToJobCount = new HashMap<Long, Long>();
		userToWaitTime_accwt = new HashMap<Long, Long>();
		userToJobs = new HashMap<Long, ArrayList<Job>>();
		userids = new ArrayList<Long>(512);
		waitTimes = new ArrayList<Long>(512000);
	}
	
	@Override
	public void initialize() {
		clear();

		StringBuilder builder = new StringBuilder();
		
		if(log.isEmpty()) {
			builder.append("%"+String.format("%19s", "AVGWT_U"));
			builder.append(String.format("%20s", "AVGACCWT_U"));
			
			builder.append(String.format("%20s", "AVGWT_ALL"));
			builder.append(String.format("%20s", "AVGACCWT_ALL"));
			
			builder.append(String.format("%20s", "THROUGHPUT"));
			
			builder.append(String.format("%20s", "LAST_SUBMIT"));
			builder.append(String.format("%20s", "LAST_FINISH"));
			
			builder.append(String.format("%20s", "JOBCOUNT"));
			
			builder.append(String.format("%20s", "ACTIVEUSERS"));
			
			builder.append(String.format("%20s", "JOBS/USER"));
			
			builder.append(String.format("%20s", "AWTA_LW"));
			builder.append(String.format("%20s", "AWTA_LQ"));
			builder.append(String.format("%20s", "AWTA_M"));
			builder.append(String.format("%20s", "AWTA_UQ"));
			builder.append(String.format("%20s", "AWTA_UW"));
			
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
							long userCount = userToWaitTime_real.size();
							for(Long wt : userToWaitTime_real.keySet()) {
								sumUserAverageWaitTimes += userToWaitTime_real.get(wt)/userToJobCount.get(wt);
							}

							long avgWaitTime;
							if(userCount>0)
								avgWaitTime = sumUserAverageWaitTimes/userCount;
							else
								avgWaitTime = 0;
							
							long sumUserAverageAccWaitTimes = 0;
							for(Long wt : userToWaitTime_accwt.keySet()) {
								sumUserAverageAccWaitTimes += userToWaitTime_accwt.get(wt)/userToJobCount.get(wt);
							}
							long avgAccWaitTime;
							if(userCount>0)
								avgAccWaitTime = sumUserAverageAccWaitTimes/userCount;
							else
								avgAccWaitTime = 0;
							
							SimulationInterface si = SimulationInterface.instance();
							long t_simulated = si.getSimulationEndTime()-si.getSimulationBeginTime();
							
							long [] waitTimesPrimitive = new long[waitTimes.size()];
							for (int i = 0; i < waitTimes.size(); i++) {
								waitTimesPrimitive[i] = waitTimes.get(i);
							}
							long[] boxPlotValues = StatisticalMathHelper.getBoxPlotValues(waitTimesPrimitive);
							
							JobSubmitComparator jsc = new JobSubmitComparator();
							for(ArrayList<Job> jobs : userToJobs.values()) {
								jobs.sort(jsc);
								long avgb = 0;
								long prevSubmitTime = Long.MIN_VALUE;
								for(Job job : jobs) {
									long newSubmitTime = job.get(Job.SUBMIT_TIME);
									if(newSubmitTime prevSubmitTime)
								}
							}
							
							double tp = ((double)(throughput/t_simulated))/(double)max_resources;
							DecimalFormatSymbols dfs = new DecimalFormatSymbols();
							dfs.setDecimalSeparator('.');
							log.add(String.format("%20s", avgWaitTime)+
									String.format("%20s", avgAccWaitTime)+
									String.format("%20s", (globalWaitTime/globalJobCount))+
									String.format("%20s", (globalAccWaitTime/globalJobCount))+
									String.format("%20s", new DecimalFormat("0.0000", dfs).format(tp))+
									String.format("%20s", t_last_submit)+
									String.format("%20s", t_last_finish)+
									String.format("%20s", globalJobCount)+
									String.format("%20s", userids.size())+
									String.format("%20s", globalJobCount/userids.size())+
									String.format("%20s", boxPlotValues[0])+
									String.format("%20s", boxPlotValues[1])+
									String.format("%20s", boxPlotValues[2])+
									String.format("%20s", boxPlotValues[3])+
									String.format("%20s", boxPlotValues[4]));
							
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
		
		if(!userids.contains(user)){
			userids.add(user);
		}

		long wt = userToWaitTime_real.getOrDefault(user, 0L);
		long accwt = userToWaitTime_accwt.getOrDefault(user, 0L);
		long jc = userToJobCount.getOrDefault(user, 0L);
		
		ArrayList<Job> jobsOfUser = userToJobs.getOrDefault(user, new ArrayList<Job>());
		jobsOfUser.add(job);
		userToJobs.put(user, jobsOfUser);
		
		wt += job.get(Job.WAIT_TIME);
		accwt += Math.max(0, job.get(Job.WAIT_TIME) - StatisticalMathHelper.userAccepteableWaitTime(job.get(Job.TIME_REQUESTED)));
		jc++;
		
		userToWaitTime_real.put(user, wt);
		userToWaitTime_accwt.put(user, accwt);
		userToJobCount.put(user, jc);
		
		globalWaitTime += job.get(Job.WAIT_TIME);
		globalAccWaitTime += Math.max(0, job.get(Job.WAIT_TIME) - StatisticalMathHelper.userAccepteableWaitTime(job.get(Job.TIME_REQUESTED)));
		globalJobCount++;
		
		waitTimes.add(job.get(Job.WAIT_TIME));
		
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

	@Override
	public void jobStarted(JobStartedEvent event) {}

	public static void resetLog() {
		log.clear();
	}
}
