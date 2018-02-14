/*
 * EID's of group members
 * dpv292
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  Semaphore countingSemaphore = null;
  Semaphore threadSemaphore = null;
  Semaphore releaseSemaphore = null;
  int parties = 0;

	public ThreadSynch(int parties) {
    this.countingSemaphore = new Semaphore(parties);
    this.threadSemaphore = new Semaphore(0);
    this.releaseSemaphore = new Semaphore(parties);
    this.parties = parties;
	}
	
	public int await() throws InterruptedException {
    // Acquire a permit from the countingSemaphore
    this.countingSemaphore.acquire();  
    // Put the threads to bed until last thread arrives
    if(countingSemaphore.availablePermits() != 0){
      this.threadSemaphore.acquire();
      //TODO: Find a way to let the threads go out at once.
      //Also, the return values are completely wrong, but i'm not sure what the
      // values are used for???
      return countingSemaphore.availablePermits() - 1;
    }

    else{
      System.out.println("Releasing threads...");
      countingSemaphore.release(parties);
      this.threadSemaphore.release(parties - 1);
      return 0;
    }
  }
}
