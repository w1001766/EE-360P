/*
 * EID's of group members
 * dpv292
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  private final int totalThreads;
  private int availableSeats;
  private Semaphore waitingRoom;
  private Semaphore entrance;
  private Semaphore mutex;

	public ThreadSynch(int parties) {
    this.totalThreads = parties;
    this.availableSeats = parties;
    this.waitingRoom = new Semaphore(0);

    // Ready to take first thread
    this.entrance = new Semaphore(1);
	}
	
	public int await() throws InterruptedException {
    // Don't let multiple threads try and enter holding at once
    // also avoid multiple access to decrement
    try {
      this.entrance.acquire();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
    --this.availableSeats;
    this.entrance.release();
    
    /********* Critical Section **********/
    // Thread must wait for all threads to call await, i.e. all in waiting room
    if (this.availableSeats > 0) {
      try {
        this.waitingRoom.acquire();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    // No more permits left means all threads have called await() so release all
    else {
      this.availableSeats = this.totalThreads;
      this.waitingRoom.release(this.totalThreads -1); // Last thread not waiting
    }
   
    return this.availableSeats;
  }
}
