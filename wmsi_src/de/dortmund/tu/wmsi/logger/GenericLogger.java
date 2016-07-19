package de.dortmund.tu.wmsi.logger;

import java.util.LinkedList;
import java.util.Queue;

import de.dortmund.tu.wmsi.SimulationInterface;
import de.dortmund.tu.wmsi.event.JobFinishedEvent;
import de.dortmund.tu.wmsi.event.JobStartedEvent;
import de.dortmund.tu.wmsi.routine.WorkloadModelRoutine;
import de.dortmund.tu.wmsi.routine.timing.RoutineTimingOnce;

public class GenericLogger implements Logger {

	Queue<String> printQueue = new LinkedList<String>();
	
	@Override
	public void jobFinished(JobFinishedEvent event) {
		if(event.getJob() == null)
			printQueue.add("Job finished at "+event.getTime());
		else
			printQueue.add("Job "+event.getJob().getJobId()+" finished at "+event.getTime());
	}

	@Override
	public void jobStarted(JobStartedEvent event) {
		if(event.getJob() == null)
			printQueue.add("Job started at "+event.getTime());
		else
			printQueue.add("Job "+event.getJob().getJobId()+" started at "+event.getTime());
	}

	@Override
	public void initialize() {
		SimulationInterface.instance().register(new WorkloadModelRoutine(new RoutineTimingOnce(SimulationInterface.instance().getSimulationEndTime()-1L)) {
			@Override
			protected void process(long t_now) {
				System.out.println();
				System.out.println("Printing generic Log:");
				while(!printQueue.isEmpty()) {
					System.out.println(printQueue.poll());
				}
				System.out.println();
			}
		});
	}

	@Override
	public void configure(String configPath) {}
}
