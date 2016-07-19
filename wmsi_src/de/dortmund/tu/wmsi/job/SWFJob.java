package de.dortmund.tu.wmsi.job;

public class SWFJob extends Job {
	
	private long[] values = new long[18];
	
	public static final int JOB_ID = 0;
	public static final int SUBMIT_TIME = 1;
	public static final int WAIT_TIME = 2;
	public static final int RUN_TIME = 3;
	public static final int RESOURCES_ALLOCATED = 4;
	public static final int AVG_CPU_TIME = 5;
	public static final int USED_MEMORY = 6;
	public static final int RESOURCES_REQUESTED = 7;
	public static final int TIME_REQUESTED = 8;
	public static final int MEMORY_REQUESTED = 9;
	public static final int STATUS = 10;
	public static final int USER_ID = 11;
	public static final int GROUP_ID = 12;
	public static final int EXECUTABLE_NUMBER = 13;
	public static final int QUEUE_NUMBER = 14;
	public static final int PARTITION_NUMBER = 15;
	public static final int PRECEDING_JOB_NUMBER = 16;
	public static final int THINK_TIME_PRECEDING_JOB = 17;
	
	public SWFJob() {
		this(JobIDCounter.nextID());
	}
	
	public SWFJob(int id) {
		super(id);
		values[JOB_ID] = getJobId();
		for (int i = 1; i < values.length; i++) {
			values[i] = -1L;
		}
	}
	
	public SWFJob(long submitTime, long runDuration, long resources) {
		this(JobIDCounter.nextID(), submitTime, runDuration, resources);
	}
	
	public SWFJob(int id, long submitTime, long runDuration, long resources) {
		this(id);
		set(SUBMIT_TIME, submitTime);
		set(RUN_TIME, runDuration);
		set(RESOURCES_REQUESTED, resources);
		set(RESOURCES_ALLOCATED, resources);
	}
	
	@Override
	public long getSubmitTime() {
		return get(SUBMIT_TIME);
	}

	@Override
	public long getRunDuration() {
		return get(RUN_TIME);
	}

	@Override
	public long getResourcesRequested() {
		return get(RESOURCES_REQUESTED);
	}
	
	public long get(int selector) {
		if(selector < 0 || selector >= values.length) {
			throw new IllegalAccessError("SWFJob does not contain a field with ID "+selector);
		}
		return values[selector];
	}
	
	public SWFJob set(int selector, long value) {
		if(selector < 0 || selector >= values.length) {
			throw new IllegalAccessError("SWFJob does not contain a field with ID "+selector);
		}
		values[selector] = value;
		return this;
	}
	
}
