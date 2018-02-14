public class FairReadWriteLock {
	int readers;
  int writers;

  public FairReadWriteLock() {
    readers = 0;
    writers = 0;
  }
                        
	public synchronized void beginRead() {
	  readers++;
  }
	
	public synchronized void endRead() {
	  readers--;
    notifyAll();
  }
	
	public synchronized void beginWrite() {
	  writers++;
  }
	
  public synchronized void endWrite() {
	  writers--;
    notifyAll();
  }
}
	
