package com.keatingfinance.datastruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * ShortFastMap is an immutable map that maps short values to short keys. It is considerably more time and space efficient
 * than using a HashMap<Short,Short> for the same purpose, mainly because of the avoidance of the use of wrappers,
 * and because allocating an array of shorts does take up less space than ints. This is not true of short fields.
 * 
 * This is designed to be immutable, and the size of the map is made big enough to have a next nearest power of two to the size
 * of the data. 
 *  
 * @author Oliver Keating
 *
 */
public class ShortFastMap {

		
		final private int lengthMask;
		final private int size;
		
		final private short[] keys;
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
		final private static short nullKey = Short.MIN_VALUE;

	
		/**
		 * Constructor, orderedKeys and multiplier. 
		 * 
		 * Multiplier allows some tuning in space-time tradeoff. Higher values reduce collisions, therefore lookup time, but will 
		 * result in a larger map. Due to the way this implementation works, the multiplier must be at least 1
		 * 
		 * @param orderedKeys - ordered in the sense that the index of each key will become it's associated value. Must have a length < Short.MAX_VALUE
		 */
		public ShortFastMap(short[] orderedKeys){
			
			if (orderedKeys.length >=Short.MAX_VALUE){
				throw new IllegalArgumentException("Too big: "+orderedKeys.length+" Maximum size is "+Short.MAX_VALUE);
			}
			int length=calculateLength(orderedKeys.length);
			lengthMask =length -1;
			keys = new short[length];
			values = new short[length];
			next = new short[length];
			Arrays.fill(keys,nullKey);
			Arrays.fill(values,(short)nullIndex);
			Arrays.fill(next,(short)nullIndex);
			size = orderedKeys.length;

			
			List<Short> collissionList = getCollsionList(orderedKeys);
		
//			System.out.println("collisions "+collissionList.size());
			
			for (Short collission : collissionList){
				handleCollission(orderedKeys[collission],collission);
			}

//			System.out.println("deepest collission="+deepestCollission());
			
		}
		
		private List<Short> getCollsionList(short[] orderedKeys) {
			List<Short> collissionList = new ArrayList<Short>();
			for (int i = 0 ; i < orderedKeys.length ; i++){
				boolean isCollision = put(orderedKeys[i],i);
				if (isCollision){
					collissionList.add((short) i);
				}
			}
			return collissionList;
		}
		
		public ShortFastMap(short[] inputKeys, short[] inputValues){
			if (inputKeys.length >=Short.MAX_VALUE){
				throw new IllegalArgumentException("Too big: "+inputKeys.length+" Maximum size is "+Short.MAX_VALUE);
			}
			int length=calculateLength(inputKeys.length);
			lengthMask =length -1;
			keys = new short[length];
			values = new short[length];
			next = new short[length];
			Arrays.fill(keys,nullKey);
			Arrays.fill(values,(short)nullIndex);
			Arrays.fill(next,(short)nullIndex);
			size = keys.length;
			List<Integer>  collissionMap = getCollissionMap(inputKeys,inputValues);
			for (Integer keyValue : collissionMap){
				short key = (short) ((keyValue>>16));
				short value = (short)(keyValue & 0xffff);
				handleCollission(key, value);
			}
		}
	
		private List<Integer> getCollissionMap(short[] keys,
				short[] values) {
			List<Integer> collissions = new ArrayList<>();
			for (int i = 0 ; i < keys.length ; i++){
				boolean isCollision = put(keys[i],values[i]);
				if (isCollision){
					collissions.add( ( ( ((int)keys[i]) <<16 ) |   values[i]));
				}
			}
			return collissions;
		}

		public int size(){
			return size;
		}



		private void handleCollission(short key, short value) {
		
			int index = getEntryIndex(key);

			while(next[index] != nullIndex){
				index=next[index];
			}
	
			int newIndex = index;
			
			while(keys[newIndex]!=nullKey){
				newIndex++;
				if (newIndex == lengthMask){
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
		private int calculateLength(int length) {
			int capacity = 1;
			while (capacity < length){
				capacity <<= 1;
			} 
			return capacity < Short.MAX_VALUE ? capacity : Short.MAX_VALUE;
			
		}


		private boolean put(short key, int value) {
			
			int index = getEntryIndex(key);
		
			if (keys[index]!=nullKey){
				if (keys[index]==key){
					throw new IllegalArgumentException("Duplicated key:"+key);
				}
				return true;
			}
			keys[index] = key;
			values[index] = (short) value;
			return false;
		}
			

		private int getEntryIndex(short bitPattern){
			return bitPattern & lengthMask;
		}

		public boolean containsKey(short key){
			return getIndex(key)!=nullIndex;
		}
		
		public int getIndex(short bitPattern){
			int index = getEntryIndex(bitPattern);
		
			do{
				
				if (bitPattern == keys[index]){
					return values[index];
				}
				
				index = next[index];
		
			} while (index!=nullIndex);
			
			return nullIndex;
		}
}
