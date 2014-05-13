package com.keatingfinance.imset;

import java.util.Iterator;

/**
 * Represents the intersection of two sets, A and B
 * This can be thought of as the AND operator, an
 * item has to be in both A AND B to be in the intersection.
 * 
 * As with the other implementations, this represents a view.
 * 
 * @author Oliver Keating
 *
 * @param <T>
 */
public class Intersection<T> implements ImSet<T>{

	public static final char INTERSECTION = '\u2229';
	
	final private ImSet<T> setA;
	final private ImSet<T> setB;
	
	private Integer size = null;
	
	public Intersection(ImSet<T> setA, ImSet<T> setB) {
		this.setA = setA;
		this.setB = setB;
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}

	@Override
	public int size() {
		if (size==null){
			int count =0;
			for (@SuppressWarnings("unused") T t: this){
				count++;
			}
			this.size=count;
		}
		return size;
	}
	@Override
	public boolean isEmpty() {
		return ! iterator().hasNext();
	}
	@Override
	public boolean contains(Object item) {
		return setA.contains(item) && setB.contains(item);
	}

	@Override
	public long timeStamp() {
		return setA.timeStamp();
	}

	@Override
	public int id() {
		return setA.id();
	}

	@Override
	public String description() {
		return setA.description()+INTERSECTION+setB.description();
	}
	
	private class Itr implements Iterator<T>{
		Iterator<T> itr = setA.iterator();
		T next = setNext();
		@Override
		public boolean hasNext() {
			return next!=null;
		}

		private T setNext() {
			while(itr.hasNext()){
				T next = itr.next();
				if (setB.contains(next)){
					return next;
				}
			}
			return null;
		}

		@Override
		public T next() {
			T current = next;
			this.next=setNext();
			return current;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	@Override
	public T getFromHash(int hash) {
		T value = setA.getFromHash(hash);
		return value != null ? value: setB.getFromHash(hash);
	}
}
