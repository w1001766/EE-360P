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
    
    if (this.countingSemaphore.availablePermits() == 0) {
      boolean allAwaited = true;
      for (int i = 0; i < this.threadSemaphores.length; ++i) {
        allAwaited |= this.threadSemaphores[i].availablePermits() == 0;
      }
      if (allAwaited) {
        this.countingSemaphore.release();
        this.blockingSemaphore.acquire();
        for (int i = 0; i < this.threadSemaphores.length; ++i) {
          this.threadSemaphores[i].release();
        }
        this.blockingSemaphore.release();
        return 0;
      }
    } else {
      int threadCount = this.countingSemaphore.availablePermits() - 1;
      if (threadSemaphores[threadCount].availablePermits() != 0) {
        System.out.println("Releasing thread: " + threadCount);
        this.threadSemaphores[threadCount].acquire();
        countingSemaphore.acquire();
        return threadCount;
      }
    }

    return -1;
	}
}
