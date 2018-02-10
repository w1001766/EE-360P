/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class ThreadSynch {
  private static final boolean debug = true;
  Semaphore countingSemaphore = null;
  Semaphore[] threadSemaphores = null;
  Semaphore blockingSemaphore = null;

	public ThreadSynch(int parties) {
    // "Gate" semaphore
    this.countingSemaphore = new Semaphore(parties);
    this.blockingSemaphore = new Semaphore(1);
    
    threadSemaphores = new Semaphore[parties];
    for (int i = 0; i < parties; ++i) {
      this.threadSemaphores[i] = new Semaphore(1);
    }

	}
	
	public int await() throws InterruptedException {
    
    if (this.countingSemaphore.availablePermits() == 1) {
      if (ThreadSynch.debug) {
        System.out.println("CountingSemaphore has no permits left");
      }
      boolean allAwaited = true;
      for (int i = 0; i < this.threadSemaphores.length; ++i) {
          if (ThreadSynch.debug) {
            System.out.println("Semaphore checking: " + this.threadSemaphores[i].toString());
          }
        allAwaited |= this.threadSemaphores[i].availablePermits() == 0;
      }
      if (allAwaited) {
        this.countingSemaphore.release();
        this.blockingSemaphore.acquire();
        for (int i = 0; i < this.threadSemaphores.length; ++i) {
          if (ThreadSynch.debug) {
            System.out.println("Semaphore relasing: " + this.threadSemaphores[i].toString());
          }
          this.threadSemaphores[i].release();
        }
        this.blockingSemaphore.release();
        return 0;
      }
    } else {
      int threadCount = this.countingSemaphore.availablePermits() - 1;
      if (threadSemaphores[threadCount].availablePermits() != 0) {
        if (ThreadSynch.debug) {
          System.out.println("Semaphore number: " + threadCount);
          System.out.println("Semaphore acquiring lock: " + this.threadSemaphores[threadCount].toString());
        }
        this.threadSemaphores[threadCount].acquire();
        if (ThreadSynch.debug) {
          System.out.println("Semaphore number: " + threadCount);
          System.out.println("Semaphore acquiring lock: " + this.threadSemaphores[threadCount].toString());
        }
        countingSemaphore.acquire();
        if (ThreadSynch.debug) {
          System.out.println("Counting sempaphore: " + this.countingSemaphore.toString());
        }
        return threadCount;
      }
    }

    return -1;
	}
}
