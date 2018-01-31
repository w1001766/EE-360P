//UT-EID=am74874


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{
	private int[] array;
	private int begin;
	private int end;
	
	private PSort(int[] array, int begin, int end) {
		this.array = array;
		this.begin = begin;
		this.end = end;
	}
	
	@Override
	protected void compute() {
		if(end - begin < 16) {
			insertSort(array, begin, end);
		}
		else {
			quickSort(array, begin, end);
		}
	}
	
	private void quickSort(int[] array, int begin, int end) {
		if (edgeCase(array, begin, end)) {
			return;
		}
		
		int pivot = array[(end - begin) / 2];
		int index = partition(array, begin, end-1, pivot);

    PSort left = new PSort(array, begin, index - 1);
    PSort right = new PSort(array, index, end);

    left.fork();
    right.compute();
    left.join();
	}
	
	private int partition(int[] array, int begin, int end, int pivot) {
		while (begin < end) {
			while (array[begin] < pivot) {
				begin ++;
			}
			while (array[end] > pivot) {
				end --;
			}
			
			if (begin < end) {
				swap(array, begin, end);
				begin++;
				end--;
			}
		}
		return begin;
	}

	private void insertSort(int[] array, int begin, int end) {
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
		
	}
	
	private void swap(int[] arr, int begin, int end) {
		int temp = arr[begin];
		arr[begin] = array[end];
		arr[end] = temp;
	}

	private static ForkJoinPool pool = new ForkJoinPool();
	
	private static boolean edgeCase(int[] arr, int begin, int end) { 
		if(arr.length == 0 || begin >= end)
			return true;
		return false;
	}

	public static void parallelSort(int[] A, int begin, int end){
		if(edgeCase(A, begin, end))
			return;

		pool.invoke(new PSort(A, begin, end));
		
	}
	
	
	
}
