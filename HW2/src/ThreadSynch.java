/*
 * EID's of group members
 * dpv292
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  private final int totalThreads;
  private int availablePermits;
  private Semaphore waitingRoom;
  private Semaphore entrance;

	public ThreadSynch(int parties) {
    this.totalThreads = parties;
    this.availablePermits = parties;
    this.waitingRoom = new Semaphore(0);

    // Ready to take first thread
    this.entrance = new Semaphore(1);
	}
	
	public int await() throws InterruptedException {
    // Don't let multiple threads try and enter holding at once
    try {
      this.entrance.acquire();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
    --this.availablePermits;
    this.entrance.release();
    
    /********* Critical Section **********/
    // Thread must wait for all threads to call await, i.e. all in waiting room
    if (this.availablePermits > 0) {
      try {
        this.waitingRoom.acquire();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    // No more permits left means all threads have called await() so release all
    else {
      this.availablePermits = this.totalThreads;
      this.waitingRoom.release(this.totalThreads -1);
    }

    return this.availablePermits;
  }
}
