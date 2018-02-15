import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

public class PriorityQueue {
  
  private Node head;
  private Node tail;
  private int capacity;
  private AtomicInteger size;
  private Condition full; 
  private Condition empty;
  private ReentrantLock enq;
  private ReentrantLock deq;

  private class Node {
      public String data;
      public int priority;
      public Node next;
      public ReentrantLock nodeLock;

      public Node(){
        data = null;
        priority = -1;
        next = null;
        nodeLock = new ReentrantLock();
      }
      
      public Node(String name, int priority){
        data = name;
        this.priority = priority;
        next = null;
        nodeLock = new ReentrantLock();
      }


  }
  
	public PriorityQueue(int maxSize) {
    // Creates a Priority queue with maximum allowed size as capacity
    enq = new ReentrantLock();
    deq = new ReentrantLock();
    full = enq.newCondition();
    empty = deq.newCondition();
    head = new Node();
    tail = new Node();
    head.next = tail;
    size = new AtomicInteger(0);
    capacity = maxSize;
  }

	public int add(String name, int priority) {
    // Adds the name with its priority to this queue.
    // Returns the current position in the list where the name was inserted;
    // otherwise, returns -1 if the name is already present in the list.
    // This method blocks when the list is full.
    
    boolean wakeDeq = false;
    enq.lock();
    int index = 0;
    boolean found = false;
    Node first = head;
    Node second = head.next;
    Node n = new Node(name, priority);
    first.nodeLock.lock();
    second.nodeLock.lock();
    n.nodeLock.lock();
    try{
      while(size.get() == capacity){
        full.await();
      }

      //Search if the name exists in the pqueue
      if(search(name) == -1) return -1;

      //Search for the place to add the new node.

      while(!found){
        //Edge case: inserting in an empty queue
        if(first == null && second == null){
          first.next = n;
          n.next = second;
          found = true;
        }
        
        //Edge case: inserting at beginning of queue
        else if(first == null && (second.priority < n.priority)){
          first.next = n;
          n.next = second;
          found = true;
        }

        //Edge case: inserting at end of queue
        else if((first.priority > n.priority) && second == null){
          first.next = n;
          n.next = second;
          found = true;
        }
        
        //Found the right place to insert (in the general case)
        else if((first.priority <= n.priority) && (n.priority < second.priority)){
          first.next = n;
          n.next = second;
          found = true;
        }
        
        //Hand-over-hand method: swap locks and try again
        else {
          first.nodeLock.unlock();
          first = second;
          second = second.next;
          index++;
        }
      }
      if(size.getAndIncrement() == 0) wakeDeq = true;
    }catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    
    
    finally{
    	  first.nodeLock.unlock();
      second.nodeLock.unlock();
      n.nodeLock.unlock();
    	}
    
    if(wakeDeq) {
	    	deq.lock();
	    	try {
	    		empty.signalAll();
	    	}
	    	finally {
	    		deq.unlock();
	    	}
    }
    
    return index;
	}

	public int search(String name) {
    // Returns the position of the name in the list;
    // otherwise, returns -1 if the name is not found.
    
    int index = 0;
    boolean found = false;
    Node first = head;
    Node second = head.next;
    
    first.nodeLock.lock();
    second.nodeLock.lock();
    while(!found && first.next == null){
      if(name.equals(first.data)){
        found = true;
      }
      else{
        first.nodeLock.unlock();
        first = second;
        second = second.next;
        index++;
    }
    first.nodeLock.unlock();
    second.nodeLock.unlock();

	  }
    return found ? index : -1;
  }

	public String getFirst() {
    // Retrieves and removes the name with the highest priority in the list,
    // or blocks the thread if the list is empty.
    String result;
    boolean wakeEnq = false;
    deq.lock();
    try{
      while(size.get() == 0){
        //block the thread
        try {
          empty.await();
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      }
      //dequeue the first node's string
      result = head.next.data;
      head = head.next;

      //Set a flag to see if there's space in queue
      if(size.getAndDecrement() == 0) {
        wakeEnq = true;
      }
    }
    finally {
      deq.unlock();
    }

    //Notify all enqueue threads that there's space
    if (wakeEnq){
      enq.lock();
      try{
        full.signalAll();
      }
      finally{
        enq.unlock();
      }
    }
    return result;
	}
  
}

