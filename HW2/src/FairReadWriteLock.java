import java.util.*;

public class FairReadWriteLock {
  int readers = 0;
  int writers = 0;
  LinkedList<Long> rWait = new LinkedList<>();
  LinkedList<Long> wWait = new LinkedList<>();
                        
	public synchronized void beginRead() {
    readers++;
    
  }
	
	public synchronized void endRead() {
	  readers--;
  }
	
	public synchronized void beginWrite() {
	  writers++;
  }
	
  public synchronized void endWrite() {
	  writers--;
    notifyAll();
  }
}
	
