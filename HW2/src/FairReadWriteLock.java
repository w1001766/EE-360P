import java.util.*;

public class FairReadWriteLock {
  private int totalRWRequests;
  private int rwRequestsServed;
	private int activeReaders;
  private int activeWriters;

  public FairReadWriteLock() {
    this.totalRWRequests = 0;
    this.rwRequestsServed = 0;
    this.activeReaders = 0;
    this.activeWriters = 0;
  }

	public synchronized void beginRead() {
    // This thread is served after preceeding threads
    int readThreadPriority = totalRWRequests++;
    // Wait for preceeding reader threads to be served
    while (activeWriters > 0 || rwRequestsServed < readThreadPriority) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now reading
    ++activeReaders;
    ++rwRequestsServed;
    notifyAll();
  }
	
	public synchronized void endRead() {
    if (--activeReaders == 0) notifyAll();
  }
	
	public synchronized void beginWrite() {
    // This thread is served after preceeding threads
    int writeThreadPriority = totalRWRequests++;
    // Wait for preceeding reader and writer threads to be served
    while (activeReaders > 0 || activeWriters > 0
          || rwRequestsServed < writeThreadPriority) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now writing
    ++activeWriters;
    ++rwRequestsServed;
    notifyAll();
  }
	
  public synchronized void endWrite() {
    if (--activeWriters == 0) notifyAll();
  }
}
