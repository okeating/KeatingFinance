package com.keatingfinance.datastruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.keatingfinance.datastruct.FastMapV5.Builder;

	/**
	 * FastMapV5 is a highly space-time optimised implementation of the
	 * IndexLookup. 
	 * 
	 * The constructor will take an array of  int[] PrimaryKeys and map then
	 * according to their index, in other words, for any given
	 * 		int primaryKey = primaryKeys[index];
	 * this provides a fast way to reverse that process, namely
	 * 		int index = fastMap.getIndex(primaryKey);
	 * 
	 * In tests, this provides the fastest lookup time, approximately a
	 * factor 2 better than using java.util.HashMap with Integers as keys
	 * and values. It is also space efficient by using primitive arrays rather
	 * than objects. 
	 * Immutability means FastMap is thread-safe.
	 * 
	 * Considering java.util.HashMap<Integer,Integer> as the starting point, this
	 * makes the following optimisations with the associated payoffs:
	 * 
	 * 1. Primitive Array values are used. This saves on both space 
	 * 		(even accounting for nulls) and lookup time.
	 * 2. Rather than allocating objects in a linked list, bucket collisions are
	 * resolved by looking up values in a "next" array. This has two important
	 * consequences:
	 * 		a) Collisions are resolved during construction by placing values 
	 * 		into empty buckets and linking them through the next array (pigeon 
	 * 		hole hashing). This makes the map inherently immutable, as attempting
	 * 		to add new elements later could collide with these buckets.
	 * 		b) The lack of linked lists means that each key-value pair is stored
	 * 		into its own bucket, and therefore the size of the backing arrays must 
	 * 		be at least as big as the number to be stored.
	 * 3. Because most of the time, the size of the underlying data structure is 
	 * likely to be less than 2^15, short integers are used for the value and
	 * next, which gives a useful memory saving. Attempting to construct a 
	 * larger map will throw an IllegalArgumentException.
	 * 
	 * If larger sizes are required an alternative implementation should be used.
	 * 
	 * The inner Builder class allows the FastMap to be constructed without
	 * specifying all of the data at once.
	 * 
	 * @author Oliver Keating
	 *
	 */
	public final class FastMapV5 implements IndexLookup, Serializable{

		


		private static final long serialVersionUID = -3722502401180559177L;
		final private static float MULTIPLIER = 1.0f;
		final private int length;
		final private int lengthMask;
		final private int size;
		
		final private int[] keys;
		final private short[] values;
		final private short[] next;
	
		/*
		 * this is what is returned if it does not exist,
		 * as with primitives, we cannot simply return null
		 * Can be changed to point somewhere else.
		 * 
		 * -1 is a standard convention, though 0 might be used
		 * if to point to an array index that contains the "null" value.
		 */
		private int nullIndex = -1;
		private static int nullKey = -1;

		
		public FastMapV5(UniqueHash[] orderedKeys){
			this(toIntArray(orderedKeys), MULTIPLIER);
		}
		public static int[] toIntArray(UniqueHash[] orderedKeys) {
			int[] arr = new int[orderedKeys.length];
			for (int i =0;i<arr.length;i++){
				arr[i]=orderedKeys[i].hashCode();
			}
			return arr;
		}
		/**
		 * Constructor, orderedKeys and multiplier. 
		 * 
		 * Multiplier allows some tuning in space-time tradeoff. Higher
		 * values reduce collisions, therefore lookup time, but will 
		 * result in a larger map. Due to the way this implementation
		 * works, the multiplier must be at least 1
		 * 
		 * @param orderedKeys - ordered in the sense that the index of 
		 * 			each key will become it's associated value. 
		 * 			Must have a length < Short.MAX_VALUE
		 * @param multiplier - must be at least 1. 
		 * 			Will be ignored if it would result in backing array
		 * 			sizes greater than Short.MAX_VALUE
		 */
		public FastMapV5(int[] orderedKeys, float multiplier){
			if (multiplier < 1){
				throw new IllegalArgumentException("Multiplier must be > 1 : "+multiplier);
			}
			if (orderedKeys.length >=Short.MAX_VALUE){
				throw new IllegalArgumentException("Too big: "+orderedKeys.length+" Maximum size is "+Short.MAX_VALUE);
			}
			length=calculateLength(orderedKeys.length, multiplier);
			lengthMask = length-1;
			keys = new int[length];
			values = new short[length];
			next = new short[length];
			Arrays.fill(keys,nullKey);
			Arrays.fill(values,(short)nullIndex);
			Arrays.fill(next,(short)nullIndex);
			size = orderedKeys.length;
			
			List<Integer> collissionList = getCollsionList(orderedKeys);
			
			for (Integer collission : collissionList){
				handleCollission(orderedKeys[collission],collission);
			}

			
		}
		public FastMapV5(int[] tickerIds) {
			this(tickerIds,1.0f);
		}
		/**
		 * For builder use only
		 * @param capacity
		 */
		private FastMapV5(int capacity){
			length=calculateLength(capacity, 1.0f);
			lengthMask = length-1;
			keys = new int[length];
			values = new short[length];
			next = new short[length];
			Arrays.fill(keys,nullKey);
			Arrays.fill(values,(short)nullIndex);
			Arrays.fill(next,(short)-1);
			size = capacity;
		}
		
		private List<Integer> getCollsionList(int[] orderedKeys) {
			List<Integer> collissionList = new ArrayList<Integer>();
			for (int i = 0 ; i < orderedKeys.length ; i++){
				boolean added = putIfBucketEmpty(orderedKeys[i],i);
				if (!added){
					collissionList.add(i);
				}
			}
			return collissionList;
		}
		
		public int size(){
			return size;
		}
		/**
		 * Returns the maximum value of keys
		 * in the same bucket. 
		 * @return
		 */
		public int deepestCollission() {
			int deepest=0;
			for (int i = 0 ; i < length ; i++){
				int depth = 0;
				if (next[i]!=-1){
					int j = i ;
					do {
						depth++;
						j = next[j];
					} while (next[j]!=nullKey);
				}
				deepest=Math.max(depth, deepest);
			}
			return deepest;
		}

		private void handleCollission(int key, int value) {
		
			int index = getEntryIndex(key);

			while(next[index] != -1){
				index=next[index];
			}
	
			int newIndex = index;
			
			while(keys[newIndex]!=nullIndex){
				newIndex++;
				if (newIndex == length){
					newIndex =0;
				}
			}
			
			next[index] = (short) newIndex;
			keys[newIndex]=key;
			values[newIndex]=(short) value;
			
		}
/**
 * Calculate length of the backing array. Must be a power of two.
 * @param tickerLenth
 * @param multiplier
 * @return
 */
		private int calculateLength(int tickerLenth, float multiplier) {
			int  value=  Math.round((tickerLenth)*multiplier);
			int capacity = 1;
			while (capacity < value){
				capacity <<= 1;
			} 
			return capacity < Short.MAX_VALUE ? capacity : Short.MAX_VALUE;
			
		}


		private boolean putIfBucketEmpty(int key, int value) {
			
			int index = getEntryIndex(key);
		
			if (keys[index]!=nullKey){
				if (keys[index]==key){
					throw new IllegalArgumentException("Duplicated key:"+key);
				}
				return false;
			}
			keys[index] = key;
			values[index] = (short) value;
			return true;
		}
			

		protected int getEntryIndex(int bitPattern){
			return bitPattern & lengthMask;
		}

		public boolean containsKey(int key){
			return getIndex(key)!=nullIndex;
		}
		
		public int getIndex(int key){
			int index = getEntryIndex(key);
		
			do{
				
				if (key == keys[index]){
					return values[index];
				}
				
				index = next[index];
		
			} while (index!=-1);
			
			return nullIndex;
		}


		/**
		 * Allows the caller to specify where "null" is contained
		 * @param nullIndex
		 */
		public void setNullIndex(int nullIndex){
			this.nullIndex=nullIndex;
		}
		
		@Override public boolean equals(Object other){
			if (this==other){
				return true;
			}
			if (other instanceof FastMapV5){
				FastMapV5 otherMap = ((FastMapV5)other);
				if (this.size!=otherMap.size || this.nullIndex!=otherMap.nullIndex){
					return false;
				}
				return
						Arrays.equals(this.keys, otherMap.keys) &&
						Arrays.equals(this.values, otherMap.values) &&
						Arrays.equals(this.next, otherMap.next);
			}
			return false;
		}
		
		@Override public int hashCode(){
			return size;
		}
		public Iterable<Integer> keyIterable(){
			return new Iterable<Integer>() {
				
				@Override
				public Iterator<Integer> iterator() {
					return keyIterator();
				}
			};
		}
		
		
		public Iterator<Integer> keyIterator(){
			return new Iterator<Integer>() {
				int index = nextIndex(-1);
				@Override
				public boolean hasNext() {
					return index < keys.length;
				}

				private int nextIndex(int index) {
					do{ 
						index++;
					} while((index<keys.length && (keys[index])==nullKey));
					return index;
				}

				@Override
				public Integer next() {
					int value = keys[index];
					index = nextIndex(index);
					return value;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		/**
		 * The builder allows the construction of the FastMap 
		 * inserting one key-value pair at a time. 
		 * 
		 * This will generally be more efficient than having to
		 * first construct an array of ids and then building the
		 * map in one go. 
		 * 
		 * Once build() has been called, the map is returned, and
		 * further attempts to call put() will result in an IllegalStateException,
		 * preserving the maps immutability. 
		 * 
		 * @param capacity - the maximum number of pairs to be stored.
		 * @return a builder
		 */
		public static Builder getBuilder(int capacity) {
			/*
			 * Note that although we instantiate an empty FastMap at this
			 * point, it is not available to the caller until Builder.build() 
			 * has been called. Thus under normal circumstances it should not
			 * be possible for a caller to view the map before it is built.
			 */
			return new FastMapV5(capacity).new Builder(capacity);
		}
		
		public class Builder {
			final private int[] collissionKeys;
			final private int[] collissionValues;
			int index=0;
			boolean complete=false;
			private Builder(int maxSize){
				collissionKeys=new int[maxSize];
				collissionValues= new int[maxSize];
			}
			
			public void put(int key, int value) {
				if (complete){
					throw new IllegalStateException("Map has already been built");
				}
				boolean added = putIfBucketEmpty(key, value);
				if (!added){
					collissionKeys[index]=key;
					collissionValues[index]=value;
					index++;
				}
			}

			public FastMapV5 build() {
				final int collissionCount = index;
				for (int i = 0 ; i< collissionCount; i++){
					int key = collissionKeys[i];
					int value = collissionValues[i];
					handleCollission(key, value);
				}
				
				complete= true;
				return FastMapV5.this;
			}

		}
	}

