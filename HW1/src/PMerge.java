//UT-EID=dpv292


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  private static final boolean debugMode = true;
  /**
   * Class that implements callable for parallel execution. Given an element
   * and the array it is to be merge with, determine its index in the final
   * array containing the two arrays to be merged.
   */
  private class Ranker implements Callable<Integer> {
    private int element;
    private int elemArrIdx;
    private int elemArrSize;
    private int[] arrToMerge = null;

    /**
     * Constructor for the Ranker class.
     * @param element     integer element to determine index of
     * @param elemArrIdx  the index of the element in its original array
     * @param elemArrSize the element's original array's size
     * @param arrToMerge  the array containing the elements to merge with
     */
    public Ranker(int element, int elemArrIdx, int elemArrSize, int[] arrToMerge) {
      this.element = element;
      this.elemArrIdx = elemArrIdx;
      this.elemArrSize = elemArrSize;
      this.arrToMerge = arrToMerge;
    }

    public Integer call() throws Exception {
      int comparitiveRank = binarySearch(this.element, 0, this.arrToMerge.length-1,
                                         this.arrToMerge);
      if (PMerge.debugMode) {
        System.out.println("comparitiveRank: " + comparitiveRank);
        System.out.println("Merge array index: " + (comparitiveRank + this.elemArrIdx));
      }

      return comparitiveRank + this.elemArrIdx;
    }

    private int binarySearch(int elem, int left, int right, int[] arrToMerge) {
      if (PMerge.debugMode) {
        System.out.println("elem: " + elem + "\n" + 
                           "left: " + left + "\n" + 
                           "right: " + right + "\n" +
                           "arrToMerge: " + Arrays.toString(arrToMerge));
      }
  
      Integer rank = null;
      Integer elemGreaterMidpoint = null;
      while (right > left && right != left) {
        int midpoint = (left + right)/2;
        if (PMerge.debugMode) {
          System.out.println("Left: " + left + "\nRight: " + right + 
                             "\nMidpoint: " + midpoint);
        }
        if (elem < arrToMerge[midpoint]) {
          if (PMerge.debugMode) System.out.println("elem < arrToMerge[midpoint]");
          rank = midpoint;
          right = midpoint;
        } else if (elem > arrToMerge[midpoint]) {
          if (PMerge.debugMode) System.out.println("elem > arrToMerge[midpoint]\n" + 
                                                   "midpoint: " + midpoint + "\n" + 
                                                   "elem: " + elem);
          left = midpoint;
          if (elemGreaterMidpoint == null 
              || elemGreaterMidpoint != (midpoint + right)/2) {
            elemGreaterMidpoint = (midpoint + right) / 2;
          } else if (elem == arrToMerge[midpoint]) {
            rank = midpoint + 1;
            break;
          } else { break; }
        }
      }
      return rank != null ? rank : this.elemArrIdx;
    }
  }

  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function
    final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
   
    // Determine A's elements' indices
    for (int i=0; i<A.length; ++i) {
      Future<Integer> idx = executorService.submit(new PMerge().
                                                   new Ranker(A[i],i,A.length,B));
      try {
        int elementRank = idx.get();
        System.out.println("elementRank: " + elementRank);
        C[elementRank] = A[i];
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    // Determine B's elements' indices
    for (int j=0; j<B.length; ++j) {
      Future<Integer> idx = executorService.submit(new PMerge().
                                                   new Ranker(B[j],j,B.length,A));
      try {
        C[idx.get()] = B[j];
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  
    // Tasks complete
    executorService.shutdown();
  }
}
