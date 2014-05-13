package com.keatingfinance.datastruct;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * Tests the FastMap implementation(s). Constructs a java.util.HashMap to compare lookup times.
 * 
 * Because of the effects of memoization and hot compiling, the second test may run
 * faster than the first, so the order is randomised with approx 50:50 split
 * 
 * @author Oliver Keating
 *
 */
public class FastMapTest {

	
	public static final int DEFAULT_SIZE = 5000;
	
	public static final int DEFAULT_REPEATS =5;
	/**
	 * The program can be run with no command arguments (uses defaults),
	 * or the size and repeats of the test can be specified,
	 * or only the size.
	 * First arg is the size
	 * Second arg is the repeats
	 * @param args 
	 */
	public static void main(String... args){
		int repeats;
		int size;
		
		if (args.length<2){
			repeats=DEFAULT_REPEATS;
		}  else {
			repeats = Integer.parseInt(args[1]);
		}
		if (args.length<1){
			size = DEFAULT_SIZE;
		} else {
			size = Integer.parseInt(args[0]);
		}
		
		
		for (int i = 0 ; i < repeats ; i++){
			runTest(size);
		}
		
	}

	
	
	public static void runTest(int size){
		int[] testValues = createRandomArray(size);
	
		Map<Integer,Integer> map = new HashMap<>();
		for (int i = 0; i < testValues.length ; i++){
			map.put(testValues[i],i);
		}
		
		IndexLookup indexLookup = new FastMapV5(testValues);
		
		if (Math.random() < 0.5){
			testHashMap(map,testValues);
			testIndexLookup(indexLookup,testValues);
		} else{
			testIndexLookup(indexLookup,testValues);
			testHashMap(map,testValues);
		}
		
	}
	
	
	private static void testHashMap(Map<Integer, Integer> map, int[] testValues) {
		long begin = System.nanoTime();
		for (int i = 0 ; i< testValues.length ; i++){
			int index = map.get(testValues[i]);
			if (index!=i){
				throw new AssertionError();
			}
		}
		long taken = System.nanoTime()-begin;
		System.out.println("HashMap test complete. Average lookup time= "+(taken/testValues.length)+" ns");
	}



	private static void testIndexLookup(IndexLookup indexLookup,
			int[] testValues) {
		long begin = System.nanoTime();
		for (int i = 0 ; i< testValues.length ; i++){
			int index = indexLookup.getIndex(testValues[i]);
			if (index!=i){
				throw new AssertionError();
			}
		}
		long taken = System.nanoTime()-begin;
		System.out.println("IndexLookup test complete. Average lookuptime= "+(taken/testValues.length)+" ns");
		
	}


	/**
	 * Creates a randomised array of the specified size, to 
	 * simulate primary keys it guarantees all values are unique
	 * @param size
	 * @return
	 */
	private static int[] createRandomArray(int size) {
		int[] arr = new int[size];
		Set<Integer> checkingSet = new HashSet<>();
		for (int i = 0 ; i<arr.length ; i++){
			int randomValue;
			do{
				randomValue = (int) (Math.random()*Integer.MAX_VALUE);
			}while(checkingSet.contains(randomValue));
			checkingSet.add(randomValue);
			arr[i]= randomValue;
		}
		return arr;
	}
	
}
