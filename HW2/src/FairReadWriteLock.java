public class FairReadWriteLock {
  private static int totalRWRequests = 0;
  private static int RWRequestsServed = 0;
	private static int activeReaders = 0;
  private static int activeWriters = 0;

	public synchronized void beginRead() {
    // This thread is served after preceeding threads
    int readThreadPriority = totalRWRequests++;
    
    // Wait for preceeding reader threads to be served
    while (activeWriters > 0 || RWRequestsServed < totalRWRequests) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now reading
    ++activeReaders;
    ++RWRequestsServed;
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
          || RWRequestsServed < writeThreadPriority) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now writing
    ++activeWriters;
    ++RWRequestsServed;
    notifyAll();
  }
	
  public synchronized void endWrite() {
    if (--activeWriters == 0) notifyAll();
  }
}
