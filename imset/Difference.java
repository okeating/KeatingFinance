package com.keatingfinance.imset;

import java.util.Iterator;

/**
 * The difference set is the set of things in A but not in B. 
 * 
 * It would be equivalent to writing 
 * 		setC.addAll(setA);
 * 		setC.removeAll(setB);
 * 
 * However, this implementation returns a view, rather than
 * a completing an operation as above. As a result, it saves 
 * considerably on space and execution time.
 * 
 * @author Oliver Keating
 *
 * @param <T>
 */
public class Difference<T> implements ImSet<T>{

	public static final char SET_MINUS = '\u2216';
	final private ImSet<T> setA;
	final private ImSet<T> setB;
	
	
	public Difference(ImSet<T> setA, ImSet<T> setB) {
		this.setA = setA;
		this.setB = setB;
	}

	private Integer size =null;
	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}
	@Override
	public int size() {
		if (size==null){
			int count = 0;
			for(@SuppressWarnings("unused") T t : this){
				count++;
			}
			size=count;
		}
		return size;
	}
	public boolean isEmpty(){
		return !iterator().hasNext();
	}
	
	@Override
	public boolean contains(Object item) {
		if(setA.contains(item)){
			if(! setB.contains(item)){
				return true;
			}
		}
		return false;
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
		return setA.description()+SET_MINUS+setB.description();
	}
	
	private class Itr implements Iterator<T>{
		Iterator<T> itr = setA.iterator();
		T next = setNext();
		@Override
		public boolean hasNext() {
			return next != null;
		}

		private T setNext() {
			while(itr.hasNext()){
				T next = itr.next();
				if (! setB.contains(next)){
					return next;
				}
			}
			return null;
		}

		@Override
		public T next() {
			T current = next;
			next = setNext();
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
