package com.keatingfinance.imset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * The Cached set creates an array based cache of the
 * items in the set, after iterating through the original 
 * set. 
 * 
 * This is useful in situations where there may be repeated 
 * iteration of the set, 
 * 
 * @author Oliver Keating
 *
 * @param <T>
 */
public class CachedSet<T> implements ImSet<T>,ImArraySet<T> {
	final private ImSet<T> set;
	final private T[] cache;
	final int size;
	@SuppressWarnings("unchecked")
	public CachedSet(ImSet<T> set){
		Object[] cache = new Object[512];
		int count = 0;
		for (T item : set){
			cache[count++]=item;
			if (count==cache.length){
				cache = Arrays.copyOf(cache, cache.length*2);
			}
		}
		this.cache=(T[]) cache;
		this.set=set;
		this.size=count;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}
	private class Itr implements Iterator<T>{
		int index = 0;
		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public T next() {
			if (index==size){
				throw new NoSuchElementException();
			}
			return cache[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(Object item) {
		return set.contains(item);
	}

	@Override
	public boolean isEmpty() {
		return size==0;
	}

	@Override
	public long timeStamp() {
		return set.timeStamp();
	}

	@Override
	public int id() {
		return set.id();
	}

	@Override
	public String description() {
		return set.description();
	}

	@Override
	public T getFromHash(int hash) {
		return set.getFromHash(hash);
	}

}
