/*
 * EID's of group members
 * dpv292
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  Semaphore countingSemaphore = null;
  Semaphore threadSemaphore = null;
  int parties = 0;

	public ThreadSynch(int parties) {
    this.countingSemaphore = new Semaphore(parties);
    this.threadSemaphore = new Semaphore(0);
    this.parties = parties;
	}
	
	public int await() throws InterruptedException {
    // Acquire a permit from the countingSemaphore
    this.countingSemaphore.acquire();  
    // Put the threads to bed until last thread arrives
    if(countingSemaphore.availablePermits() != 0){
      this.threadSemaphore.acquire();
      return countingSemaphore.availablePermits() - 1;
    }

    else{
    this.threadSemaphore.release(parties - 1);
    countingSemaphore.release(parties);
    return countingSemaphore.availablePermits() - 1;
    }
  }
}
