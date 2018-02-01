//UT-EID=am74874


import java.util.*;
import java.util.concurrent.*;

public class PSort {
	private static ForkJoinPool pool = new ForkJoinPool();

  private class Sorter extends RecursiveAction {
    private int[] array;
    private int begin;
    private int end;
    
    protected Sorter(int[] array, int begin, int end) {
      this.array = array;
      this.begin = begin;
      this.end = end;
    }
    
    public String toString() {
      String result = "Subarray: ";
      for(int i=begin; i<end; ++i){
        result += array[i] + ", ";
      }
      return result;
    }
    
    @Override
    protected void compute() {
      if(end - begin < 16) {
        insertSort(array, begin, end);
      }
      else {
        //quickSort(array, begin, end);
        
      if (edgeCase(array, begin, end)) {
        return;
      }
      
      int pivot = array[(end - begin) / 2];
      int index = partition(array, begin, end-1, pivot);

      Sorter left = new Sorter(array, begin, index);
      Sorter right = new Sorter(array, index, end);
      left.fork();
      right.fork();

      left.join();
      right.join();
      }
    }

    private void quickSort(int[] array, int begin, int end) {
      if (edgeCase(array, begin, end)) {
        return;
      }
      
      int pivot = array[(end - begin) / 2];
//      System.out.println("Pivot: " + pivot);
      int index = partition(array, begin, end-1, pivot);

      
      Sorter left = new Sorter(array, begin, index);
//      System.out.println("Subarray one: " + left);
      Sorter right = new Sorter(array, index+1, end);
//      System.out.println("Subarray two: " + right);

      left.fork();
      right.compute();
      left.join();
//      System.out.println("Active threads: " + pool.getActiveThreadCount());
    }
    
    private int partition(int[] array, int begin, int end, int pivot) {
//      System.out.println("Prior to partition: " + Arrays.toString(array));
      while (begin <= end) {
        while (array[begin] < pivot) {
          begin ++;
        }
        while (array[end] > pivot) {
          end --;
        }
        
        if (begin <= end) {
//          System.out.println("Swap begin idx: " + begin);
//          System.out.println("Swap end idx: " + end);
          swap(array, begin, end);
//          System.out.println("Post swap: " + Arrays.toString(array));
          begin++;
          end--;
        }
      }
//      System.out.println("After partition: " + Arrays.toString(array));
      return begin;
    }

    private void swap(int[] arr, int begin, int end) {
      int temp = arr[begin];
      arr[begin] = array[end];
      arr[end] = temp;
    }

    private void insertSort(int[] array, int begin, int end) {
//      System.out.println("Starting insertion sort for " + Arrays.toString(array));
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
//      System.out.println("Insertion sort result: " + Arrays.toString(array));
      
    }
  }
	
	private static boolean edgeCase(int[] arr, int begin, int end) { 
		if(arr.length == 0 || begin >= end)
			return true;
		return false;
	}

	public static void parallelSort(int[] A, int begin, int end) {
    pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		if(edgeCase(A, begin, end))
			return;

		pool.invoke(new PSort().new Sorter(A, begin, end));
	  pool.shutdown();	
	}
	
}
