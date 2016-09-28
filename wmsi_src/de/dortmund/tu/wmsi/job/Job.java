package de.dortmund.tu.wmsi.job;

public class Job {

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

	public static final long NOT_SET = -1;
	
	public Job(){
		this(JobIDCounter.nextID());
	}
	
	public Job(long id) {
		values[JOB_ID] = id;
		for (int i = 1; i < values.length; i++) {
			values[i] = -1L;
		}
	}
	
	public Job(long tsub, long trun, long res){
		this();
		set(SUBMIT_TIME, tsub);
		set(RUN_TIME, trun);
		set(TIME_REQUESTED, trun);
		set(RESOURCES_REQUESTED, res);
		set(RESOURCES_ALLOCATED, res);
	}
	
	public Job(long id, long tsub, long trun, long res){
		this(id);
		set(SUBMIT_TIME, tsub);
		set(RUN_TIME, trun);
		set(TIME_REQUESTED, trun);
		set(RESOURCES_REQUESTED, res);
		set(RESOURCES_ALLOCATED, res);
	}
	
	public long getJobId() {
		return values[JOB_ID];
	}

	public long getSubmitTime() {
		return get(SUBMIT_TIME);
	}

	public long getRunDuration() {
		return Math.max(get(RUN_TIME), get(TIME_REQUESTED));
	}

	public long getResourcesRequested() {
		return get(RESOURCES_REQUESTED);
	}
	
	public long get(int selector) {
		if(selector < 0 || selector >= values.length) {
			throw new IllegalAccessError("SWFJob does not contain a field with ID "+selector);
		}
		return values[selector];
	}
	
	public Job set(int selector, long value) {
		if(selector < 0 || selector >= values.length) {
			throw new IllegalAccessError("SWFJob does not contain a field with ID "+selector);
		}
		values[selector] = value;
		return this;
	}

	public boolean isValid() {
		return getResourcesRequested() > 0 && getRunDuration() >= 0 && getJobId() >= 0;
	};
	
	@Override
	public String toString() {
		return String.valueOf(get(JOB_ID));
	}

	public String printAll() {
		return	String.valueOf(get(JOB_ID))+" - "+
				String.valueOf(get(SUBMIT_TIME))+" - "+
				String.valueOf(get(WAIT_TIME))+" - "+
				String.valueOf(get(RUN_TIME))+" - "+
				String.valueOf(get(RESOURCES_REQUESTED));
	}
}
