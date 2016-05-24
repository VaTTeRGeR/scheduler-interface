package de.dortmund.tu.wmsi.logger;

import java.util.LinkedList;
import java.util.Queue;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.job.Job;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;

public class SWF_Logger implements Logger {

	private Queue<String> log = new LinkedList<String>();
	private static final String seperator = "\t";
	
	@Override
	public void init() {
		SimulationInterface.instance().register(new WorkloadModelRoutine(new RoutineTimingOnce(Long.MAX_VALUE-1)) {
			@Override
			public void process(long time) {
				printLog();
			}
		});
	}

	@Override
	public void jobFinished(JobFinishedEvent event) {
		Job job = event.getJob();
		
		long t_finish = event.getTime();
		long t_wait = (t_finish-job.getRunDuration())-job.getSubmitTime();

		StringBuilder builder = new StringBuilder();
		builder.append(job.getJobId()).append(seperator); // id
		builder.append(job.getSubmitTime()).append(seperator); // t_submit
		builder.append(t_wait).append(seperator); // t_wait
		builder.append(job.getRunDuration()).append(seperator); // t_run
		builder.append(job.getResourcesRequested()).append(seperator); // res_alloc
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append(job.getResourcesRequested()).append(seperator); // res_requested
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped
		builder.append("-1").append(seperator); // skipped

		log.add(builder.toString());
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
	
	public void clear() {
		log.clear();
	}
}
