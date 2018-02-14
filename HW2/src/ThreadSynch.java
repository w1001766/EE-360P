/*
 * EID's of group members
 * dpv292
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  Semaphore countingSemaphore = null;
  public Semaphore block = null;
  Semaphore mutex = null;
  Semaphore mutex2 = null;
  int parties = 0; int count = 0;

	public ThreadSynch(int parties) {
    this.countingSemaphore = new Semaphore(parties);
    this.block = new Semaphore(0);
    this.mutex = new Semaphore(1);
    this.mutex2 = new Semaphore(1);
    this.parties = parties;
    this.count = 0;
	}
	
	public int await() throws InterruptedException {
    
    mutex.acquire();
    count += 1;

    if(count == parties){
      System.out.println("Releasing threads...");
      count = 0;
      block.release(parties - 1);
      mutex.release();
      return count;
    }

    else{
      mutex.release();
      mutex2.acquire();  
      block.acquire();
      mutex2.release();
      return parties - count;
    }
  }
}
