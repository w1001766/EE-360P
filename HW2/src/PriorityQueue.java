import java.util.*;
import java.util.concurrent.*;
public class PriorityQueue {
  
  class Node {
      public String data;
      public int priority;
      public Node next;
      public ReentrantLock nodeLock;
  }
  
  ReentrantLock enq;
  ReentrantLock deq;
  Condition full;
  Condition empty;
  Node head;
  Node tail;
  AtomicInteger size;
  int capacity;


	public PriorityQueue(int maxSize) {
    // Creates a Priority queue with maximum allowed size as capacity
    enq = new ReentrantLock();
    deq = new ReentrantLock();
    full = lock.newCondition();
    empty = lock.newCondition();
    head = new Node(null);
    tail = head;
    size = new AtomicInteger(0);
    capacity = maxSize;
  }

	public int add(String name, int priority) {
    // Adds the name with its priority to this queue.
    // Returns the current position in the list where the name was inserted;
    // otherwise, returns -1 if the name is already present in the list.
    // This method blocks when the list is full.
    

    return -1;
	}

	public int search(String name) {
    // Returns the position of the name in the list;
    // otherwise, returns -1 if the name is not found.
    
    int index = -1;
    boolean found = false;
    Node node = queue.peek();
    
    while(!found){
      .lock();
      if(name.equals(head.data)) found = true;
      else{
        index++;
        node = node.next;
      }
      .unlock();
    }

    if(found) return index;
    else return -1;
	}

	public String getFirst() {
    // Retrieves and removes the name with the highest priority in the list,
    // or blocks the thread if the list is empty.
    if(size.get() == 0){
      //block the thread
    }
    

    return "";
	}
}

