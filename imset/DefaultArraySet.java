package com.keatingfinance.imset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
/**
 * An ImSet backed by an array. The array is to be specifed on
 * construction.
 * 
 * A method is also provided for creating a subset of the set. 
 * this returns a view of the backing array.
 * 
 * @author Oliver Keating
 *
 * @param <T>
 */
public class DefaultArraySet<T> implements ImSet<T>,ImArraySet<T> {

	final private T[] array;
	final long timeStamp;
	final int id;
	final String desc;
	public DefaultArraySet(T[] array) {
		this.array = Objects.requireNonNull(array);
		this.timeStamp=System.currentTimeMillis();
		this.id = array.hashCode();
		this.desc=Arrays.toString(array);
	}
	public DefaultArraySet(T[] array, long timeStamp, int id, String desc){
		this.array=Objects.requireNonNull(array);
		this.timeStamp=timeStamp;
		this.id=id;
		this.desc=desc;
	}
	
	
	
	public ImSet<T> getSubSet(int fromIndex, int toIndex){
		return new SubSet(fromIndex, toIndex);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator(0, array.length);
	}

	@Override
	public int size() {
		return array.length;
	}

	@Override
	public boolean contains(Object item) {
		return contains(item,0,array.length);
	}

	private boolean contains(Object item, int fromIndex, int toIndex){
		for (int i =fromIndex; i< toIndex; i++){
			T t = array[i];
			if (t!=null){
				if (t.equals(item)){
					return true;
				}
			} else if (item ==null){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public long timeStamp() {
		return timeStamp;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String description() {
		return desc;
	}
	
	private class ArrayIterator implements Iterator<T>{
		int index;
		final int toIndex;
		
		ArrayIterator(int fromIndex, int toIndex) {
			this.index = fromIndex;
			this.toIndex = toIndex;
		}
		@Override
		public boolean hasNext() {
			return index < toIndex;
		}
		@Override
		public T next() {
			if (index == toIndex){
				throw new NoSuchElementException();
			}
			return array[index++];
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	@Override
	public boolean isEmpty() {
		return array.length==0;
	}
	
	private class SubSet implements ImSet<T>{
		final int fromIndex;
		final int toIndex;
		
		
		SubSet(int fromIndex, int toIndex) {
			this.fromIndex = fromIndex;
			this.toIndex = toIndex;
		}

		@Override
		public Iterator<T> iterator() {
			return new ArrayIterator(fromIndex, toIndex);
		}

		@Override
		public int size() {
			return toIndex-fromIndex;
		}

		@Override
		public boolean contains(Object item) {
			return DefaultArraySet.this.contains(item, fromIndex, toIndex);
		}

		@Override
		public long timeStamp() {
			return timeStamp;
		}

		@Override
		public int id() {
			return id;
		}

		@Override
		public String description() {
			return DefaultArraySet.this.description()+":SubSet("+fromIndex+":"+toIndex+")";
		}

		@Override
		public boolean isEmpty() {
			return size()==0;
		}

		@Override
		public T getFromHash(int hash) {
			return DefaultArraySet.this.getFromHash(hash);
		}
		
	}
	@Override
	public T getFromHash(int hash) {
		for (T t : array){
			if (t.hashCode()==hash){
				return t;
			}
		}
		return null;
	}

	
	
}
