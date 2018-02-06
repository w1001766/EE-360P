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
      int comparitiveRank = iterativeSearch(this.element, this.arrToMerge, 
                                            this.arrToMerge.length);
      int mergeArrIndex = comparitiveRank + this.elemArrIdx;
      if (this.arrToMerge.length == 1) {
        mergeArrIndex = comparitiveRank;
      }
      if (this.element > this.arrToMerge[this.arrToMerge.length-1]) {
        mergeArrIndex = comparitiveRank + this.arrToMerge.length;
      }
      while (!PMerge.usedIndices.add(mergeArrIndex)) {
        if (PMerge.debugMode) {
          System.out.println("Failed using index " + mergeArrIndex + " for element " + this.element);
        }
        mergeArrIndex = this.element >= this.arrToMerge[comparitiveRank] ?
                                        mergeArrIndex+1 : mergeArrIndex-1;
      }

      if (PMerge.debugMode) {
        System.out.println("comparitiveRank: " + comparitiveRank);
        System.out.println("Merge array index: " + mergeArrIndex);
      }

      return mergeArrIndex;
    }

    private int iterativeSearch(int elem, int[] arrToMerge, int arrLen) {
      if (PMerge.debugMode) {
        System.out.println("Ranking element " + elem);
      }
      for (int i=0; i < arrLen-1; ++i) {
        if (elem <= arrToMerge[i] && i == 0) {
          return 0;
        } else if (elem >= arrToMerge[i] && elem <= arrToMerge[i+1]) {
          return i+1;
        }
      }

      return this.elemArrIdx;
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
