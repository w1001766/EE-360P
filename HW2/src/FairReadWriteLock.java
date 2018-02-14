public class FairReadWriteLock {
  private static int totalRWRequests = 0;
  private static int RWRequestsServed = 0;
	private static int readers = 0;
  private static int writers = 0;

	public synchronized void beginRead() {
    // This thread is served after preceeding threads
    int readThreadPriority = totalRWRequests++;
    
    // Wait for preceeding reader threads to be served
    while (readers > 0 || RWRequestsServed < totalRWRequests) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now reading
    ++readers;
    ++RWRequestsServed;
    notifyAll();
  }
	
	public synchronized void endRead() {
    if (--readers == 0) notifyAll();
  }
	
	public synchronized void beginWrite() {
    // This thread is served after preceeding threads
    int writeThreadPriority = totalRWRequests++;

    // Wait for preceeding reader and writer threads to be served
    while (RWRequestsServed < writeThreadPriority || readers > 0 || writers > 0) {
      try {
        wait();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    
    // Thread is now writing
    ++writers;
    ++RWRequestsServed;
    notifyAll();
  }
	
  public synchronized void endWrite() {
    if (--writers == 0) notifyAll();
  }
}
	
