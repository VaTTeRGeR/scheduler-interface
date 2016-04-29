package speinterface.util;

public class Job {
	private static int idcounter = 0;
	private int id = -1;
	
	public Job() {
		id = idcounter++;
	}
	
	public int getJobId() {
		return id;
	}
}
