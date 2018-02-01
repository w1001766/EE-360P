//UT-EID=am74874
//UT-EID=dpv292


import java.util.*;
import java.util.concurrent.*;

public class PSort {
  private static final boolean debugMode = false;
	private static ForkJoinPool pool = new ForkJoinPool();

  private class Sorter extends RecursiveAction {
    private int[] array;
    private int begin;
    private int end;
    
    /**
     * Paramatized constructor for the Sorter class
     * @param array the array to be sorted
     * @param begin the start index of the array
     * @param end the end index of the array
     */
    protected Sorter(int[] array, int begin, int end) {
      this.array = array;
      this.begin = begin;
      this.end = end;
    }
   
    /**
     * Stringifies the array or subarray the Sorter is working with
     * @return String
     */
    public String toString() {
      String result = "Subarray: ";
      for(int i=begin; i<end; ++i) {
        result += array[i] + ", ";
      }
      return result;
    }
    
    /**
     * Calls the Sorter to perform either insertionSort or quickSort depending
     * on the array size. Quicksort will execute recursively and request threads
     * for each recursive call
     */
    @Override
    protected void compute() {
      if(end - begin < 16) {
        insertSort(array, begin, end);
      } else {
        quickSort(array, begin, end);
      }
    }

    /**
     * Run of the mill quicksorting algorithm that uses the middle element
     * as the "pivot". Executes recursively using multiple threads.
     */
    private void quickSort(int[] array, int begin, int end) {
      if (edgeCase(array, begin, end)) {
        return;
      }
      
      int pivot = array[(end - begin) / 2];
      if (PSort.debugMode) {
        System.out.println("Pivot: " + pivot);
      }
      int index = partition(array, begin, end-1, pivot);

      
      Sorter left = new Sorter(array, begin, index);
      if (PSort.debugMode) {
        System.out.println("Subarray one: " + left);
      }
      Sorter right = new Sorter(array, index, end);
      if (PSort.debugMode) {
        System.out.println("Subarray two: " + right);
      }

      left.fork();
      right.compute();
      left.join();

      if (PSort.debugMode) {
        System.out.println("Active threads: " + pool.getActiveThreadCount());
      }
    }
    
    /**
     * Moves elements to their coresponding sides of the array depending on
     * whether or not their values are greater/less than the pivot.
     * @param array the array to be sorted
     * @param begin the start index of the array/subarray
     * @param end the end index of the array/subarray
     * @param pivot the quicksort pivoting element
     */
    private int partition(int[] array, int begin, int end, int pivot) {
      if (PSort.debugMode) {
        System.out.println("Prior to partition: " + Arrays.toString(array));
      }
      while (begin <= end) {
        while (array[begin] < pivot) {
          begin ++;
        }
        while (array[end] > pivot) {
          end --;
        }
        
        if (begin <= end) {
          if (PSort.debugMode) {
            System.out.println("Swap begin idx: " + begin);
            System.out.println("Swap end idx: " + end);
          }
          swap(array, begin, end);
          if (PSort.debugMode) {
            System.out.println("Post swap: " + Arrays.toString(array));
          }
          begin++;
          end--;
        }
      }
      if (PSort.debugMode) {
        System.out.println("After partition: " + Arrays.toString(array));
      }
      return begin;
    }

    /**
     * Wrapper function to swap two elements in an array.
     * @param arr array/subarray to be sorted
     * @param begin index of the beginning element of the array/subarray
     * @param end index of the ending element of the array/subarray
     */
    private void swap(int[] arr, int begin, int end) {
      int temp = arr[begin];
      arr[begin] = array[end];
      arr[end] = temp;
    }

    /**
     * Basic insertion sort algorithm.
     * @param array array/subarray to be sorted
     * @param begin index of starting element of array/subarray
     * @param end index of ending ekement of array/subarray
     */
    private void insertSort(int[] array, int begin, int end) {
      if (PSort.debugMode) {
        System.out.println("Starting insertion sort for " + Arrays.toString(array));
      }
      int i = begin;
      while(i < end) {
        int j = i;
        while(j > begin && array[j - 1] > array[j]) {
          //swap values
          swap(array, j, j-1);
          j--;
        }
        i++;
      }
      if (PSort.debugMode) {
        System.out.println("Insertion sort result: " + Arrays.toString(array));
      }
    }
  }
	
  /**
   * Filters out any given scenario that is invalid
   * @param arr array/subarray to be sorted
   * @param begin the starting index of the array/subarray
   * @param end the ending index of the array/subarray
   * @return boolean
   */
	private static boolean edgeCase(int[] arr, int begin, int end) { 
		return (arr.length == 0 || begin >= end);
	}

  /**
   * Static method to begin sorting an int array in parallel using ForkJoinPool.
   * ForkJoinPool uses the max amount of processors and works with a Sorter class
   * which extends RecursiveAction. At any given point if the array has 16 or
   * less elements, the program will perform insertion sort otherwise defaults
   * to quicksort
   * @param A array to be sorted
   * @param begin start index of the array
   * @param end ending index of the array
   */
	public static void parallelSort(int[] A, int begin, int end) {
    pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		if(edgeCase(A, begin, end))
			return;

		pool.invoke(new PSort().new Sorter(A, begin, end));
	  pool.shutdown();	
	}

}
