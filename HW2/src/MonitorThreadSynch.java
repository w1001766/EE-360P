/*
 * EID's of group members
 * dpv292
 * am74874
 */

public class MonitorThreadSynch {
  private int totalThreads;
  private int availableSeats;
	
	public MonitorThreadSynch(int parties) {
    totalThreads = parties;
    availableSeats = parties;
	}
	
	public synchronized int await() throws InterruptedException {
    // Hold all threads until they all call wait()
		if (--availableSeats > 0) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    // Awaken all held threads and reset counter
    } else {
      notifyAll();
      this.availableSeats = this.totalThreads;
    }
	  return this.availableSeats;
	}
}
