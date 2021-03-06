//UT-EID=am74874
//UT-EID=dpv292


import java.util.*;
import java.util.concurrent.*;

public class PSort {
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
      if(end - begin < 16 && end-begin >0) {
        insertSort(array, begin, end);
      } else if(end-begin > 0) {
        quickSort(array, begin, end);
      } else {}
    }

    /**
     * Run of the mill quicksorting algorithm that uses the middle element
     * as the "pivot". Executes recursively using multiple threads.
     */
    private void quickSort(int[] array, int begin, int end) {
      if (edgeCase(array, begin, end)) {
        return;
      }
      
      int index = partition(array, begin, end-1);
      
      Sorter left = new Sorter(array, begin, index);
      Sorter right = new Sorter(array, index, end);

      left.fork();
      right.compute();
      left.join();
    }
    
    /**
     * Moves elements to their coresponding sides of the array depending on
     * whether or not their values are greater/less than the pivot.
     * @param array the array to be sorted
     * @param begin the start index of the array/subarray
     * @param end the end index of the array/subarray
     */
    private int partition(int[] array, int begin, int end) {
      int pivot = array[end];
      int i = begin - 1;
      for(int j = begin; j < end; j++){
        if (array[j] <= pivot){
          i++;
          swap(array, i, j);
        }
      }

      swap(array, i+1, end);
      return i+1;
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
      for (int i = begin; i < end; i++){
        for (int j = i; j > begin; j--){
          if (array[j] < array[j-1])
            swap(array, j, j-1);
        }
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
    final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		if(edgeCase(A, begin, end))
			return;

		pool.invoke(new PSort().new Sorter(A, begin, end));
	  pool.shutdown();	
	}

}
