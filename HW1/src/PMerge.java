//UT-EID=dpv292


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  private static final boolean debugMode = false;
  private static Set usedIndices;
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
      // How does this element compare to the other array's elements?
      int comparitiveRank = binarySearch(this.element, 0, this.arrToMerge.length-1,
                                         this.arrToMerge);
      // Where do we insert this element in the merged array?
      int mergeArrIndex = comparitiveRank != this.elemArrIdx ? comparitiveRank + this.elemArrIdx :
                             this.element >= this.arrToMerge[this.arrToMerge.length - 1] ?
                                             comparitiveRank + this.arrToMerge.length :
                                             comparitiveRank + this.elemArrIdx;
      // Is the target index already in use?
      while (!PMerge.usedIndices.add(mergeArrIndex)) {
        mergeArrIndex = this.element >= this.arrToMerge[comparitiveRank] ?
                                        mergeArrIndex+1 : mergeArrIndex-1;
      }
      if (PMerge.debugMode) {
        System.out.println("comparitiveRank: " + comparitiveRank);
        System.out.println("Merge array index: " + mergeArrIndex);
      }
      return mergeArrIndex;
    }
    
    /**
     * Uses binary search to determine how an element ranks up compared to
     * another array's elements.
     * @param elem        element to be compared
     * @param left        starting index of compared array
     * @param right       ending index of compared array
     * @param arrToMerge  array element is compared to
     * @return int
     */
    private int binarySearch(int elem, int left, int right, int[] arrToMerge) {
      if (PMerge.debugMode) {
        System.out.println("elem: " + elem + "\n" + 
                           "left: " + left + "\n" + 
                           "right: " + right + "\n" +
                           "arrToMerge: " + Arrays.toString(arrToMerge));
      }
  
      Integer rank = null;
      Integer elemGreaterMidpoint = null;   // This is to track hitting the elem >
                                            // block twice - should be replaced...
      int midpoint = (left + right)/2;
      while (right > left && right != left) {
        midpoint = (left + right)/2;
        if (PMerge.debugMode) {
          System.out.println("Left: " + left + "\nRight: " + right + 
                             "\nMidpoint: " + midpoint);
        }
        // Search for element's rank in bottom half of array
        if (elem < arrToMerge[midpoint]) {
          if (PMerge.debugMode) System.out.println("elem < arrToMerge[midpoint]");
          rank = midpoint;
          right = midpoint;
        // Search for element's rank in top half of array
        } else if (elem > arrToMerge[midpoint]) {
          if (PMerge.debugMode) System.out.println("elem > arrToMerge[midpoint]\n" + 
                                                   "midpoint: " + midpoint + "\n" + 
                                                   "elem: " + elem);
          left = midpoint;
          if (elemGreaterMidpoint == null 
              || elemGreaterMidpoint != (midpoint + right)/2) {
            elemGreaterMidpoint = (midpoint + right) / 2;
          } else { break; }
        // Acounting for a dupe in the other array, @TODO: should either
        // increment or decrement that dupe's index based on some logic...
        } else if (elem == arrToMerge[midpoint]) {
          rank = midpoint + 1;
          break;
        } else { break; }
      }
      // Return element's rank
      return rank == null ? this.elemArrIdx : rank;
             //elem <= arrToMerge[midpoint] ? rank-1 : rank;
    }
  }

  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function
    final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    PMerge.usedIndices = Collections.synchronizedSet(new HashSet<Integer>());
   
    // Determine A's elements' indices
    for (int i=0; i<A.length; ++i) {
      Future<Integer> idx = executorService.submit(new PMerge().
                                                   new Ranker(A[i],i,A.length,B));
      try {
        int elementRank = idx.get();
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
