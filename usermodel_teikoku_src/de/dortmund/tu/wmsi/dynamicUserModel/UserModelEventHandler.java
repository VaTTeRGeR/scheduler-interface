package de.dortmund.tu.wmsi.dynamicUserModel;

import de.irf.it.rmg.core.teikoku.Bootstrap;
import de.irf.it.rmg.core.teikoku.common.Reservation;
import de.irf.it.rmg.core.teikoku.exceptions.OccupationException;
import de.irf.it.rmg.core.teikoku.exceptions.SubmissionException;
import de.irf.it.rmg.core.teikoku.grid.resource.ResourceBroker;
import de.irf.it.rmg.core.teikoku.job.Job;
import de.irf.it.rmg.core.teikoku.job.State;
import de.irf.it.rmg.core.teikoku.runtime.events.JobAbortedEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.JobCompletedEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.JobQueuedEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.JobReleasedEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.JobStartedEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.ReservationEndEvent;
import de.irf.it.rmg.core.teikoku.runtime.events.ReturnOfLentResourcesEvent;
import de.irf.it.rmg.core.teikoku.scheduler.Partition;
import de.irf.it.rmg.core.teikoku.scheduler.PartitionScheduler;
import de.irf.it.rmg.core.teikoku.workload.swf.SWFJob;
import de.irf.it.rmg.sim.kuiga.Event;
import de.irf.it.rmg.sim.kuiga.Kernel;
import de.irf.it.rmg.sim.kuiga.annotations.AcceptedEventType;
import de.irf.it.rmg.sim.kuiga.annotations.EventSink;
import de.irf.it.rmg.sim.kuiga.annotations.InvalidAnnotationException;
import de.irf.it.rmg.sim.kuiga.annotations.MomentOfNotification;
import de.irf.it.rmg.sim.kuiga.annotations.NotificationTime;
import de.irf.it.rmg.sim.kuiga.listeners.TimeChangeListener;
import de.irf.it.rmg.sim.kuiga.listeners.TypeChangeListener;
import de.irf.it.rmg.util.time.Instant;
import de.irf.it.rmg.util.time.Period;


@EventSink
public class UserModelEventHandler 
	implements TimeChangeListener, TypeChangeListener {
	
	private final UserModel usermodel;

	/**
	 * TODO: not yet commented
	 * 
	 * @param site
	 * @throws InvalidAnnotationException
	 */
	public UserModelEventHandler(UserModel usermodel)
			throws InvalidAnnotationException {
		this.usermodel = usermodel;
		/*
		 * Register the compute site event handler as eventsink to be notified
		 * for handling events in the main handling phase and for changes in
		 * time or eventtype
		 */
		Kernel.getInstance().registerEventSink(this);
		Kernel.getInstance().registerTypeChangeListener(this);
		Kernel.getInstance().registerTimeChangeListener(this);
	}
	

	@Override
	public void notifyTypeChange(Event fromEvent, Event toEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyTimeChange(Instant fromTime, Instant toTime) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = JobReleasedEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverReleasedEvent(JobReleasedEvent event) {
		//System.out.println("job: " + event.toString());
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = JobQueuedEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverQueuedEvent(JobQueuedEvent event) {

	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = JobStartedEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverStartedEvent(JobStartedEvent event) {
		usermodel.receiveStartedEvent(event);
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = JobCompletedEvent.class)
	@MomentOfNotification(value = NotificationTime.BEFORE)
	public void deliverCompletedEvent(JobCompletedEvent event) {
		
		//SWFJob j = (SWFJob)this.usermodel.getJobQueue().element();
		
		//Partition p = ( Partition )event.getTags().get("partition");
		
		//usermodel.receiveCompletedEvent(event);
		
		//try {
			//Kernel.getInstance().dispatch(new Event());

			//this.usermodel.getExecutor().submit(j, p);
		//} catch (SubmissionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
		
		//System.out.println("Job in Queue "+j.toString());
		//System.out.println("Completed Event: "+event.toString());
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = ReservationEndEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverReservationEndEvent(ReservationEndEvent event) {
		
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 * @throws Exception
	 */
	@AcceptedEventType(value = ReturnOfLentResourcesEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverReturnOfLentResourcesEvent(ReturnOfLentResourcesEvent event) {
	
	}

	/**
	 * TODO: not yet commented
	 * 
	 * @param event
	 */
	@AcceptedEventType(value = JobAbortedEvent.class)
	@MomentOfNotification(value = NotificationTime.HANDLE)
	public void deliverAbortedEvent(JobAbortedEvent event) {
		
	}

}



