import java.util.*;

public class PriorityQueueTester {
  PriorityQueue pq;

  PriorityQueueTester() {
    this.pq = new PriorityQueue(5);
  }


  public static void main(String[] args) {
    PriorityQueueTester pqt = new PriorityQueueTester();
    try {
      tester1 t1 = new tester1(pqt.pq);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
class tester1 implements Runnable{
  PriorityQueue pq;
  String[] names = new String[] {"Federico", "TImberlon", "Troy", "Julian", "Troy"};
  int[] priorities = new int[] {1, 5, 6, 2, 7};

  public tester1(PriorityQueue pq) {
    this.pq = pq;
    new Thread(this).start();
  }

  public void run() {
    try {
      for (int i=0; i<5; ++i) {
        pq.add(names[i], priorities[i]);
      }

      for (int i=0; i<4; ++i) {
        System.out.println(pq.getFirst());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
