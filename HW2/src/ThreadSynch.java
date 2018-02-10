/*
 * EID's of group members
 * dpv292
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
        System.out.println("CountingSemaphore has 1 permit left");
      }
      this.threadSemaphores[0].acquire();
      this.countingSemaphore.acquire();
      boolean allAwaited = true;
      for (int i = 0; i < this.threadSemaphores.length; ++i) {
          if (ThreadSynch.debug) {
            System.out.println("Semaphore checking: " + this.threadSemaphores[i].toString());
          }
        allAwaited |= this.threadSemaphores[i].availablePermits() == 0;
      }
      if (allAwaited) {
        for (int i = 0; i < this.threadSemaphores.length; ++i) {
          this.threadSemaphores[i].release();
          if (ThreadSynch.debug) {
            System.out.println("Semaphore released: " + this.threadSemaphores[i].toString());
          }
        }
        this.countingSemaphore.release(this.threadSemaphores.length);
        if (ThreadSynch.debug) {
          System.out.println("Counting semaphore reset: " + this.countingSemaphore.toString());
        }
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
          System.out.println("Counting semaphore: " + this.countingSemaphore.toString());
        }
        return threadCount;
      }
    }

    return -1;
	}
}
