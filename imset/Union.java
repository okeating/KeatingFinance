package com.keatingfinance.imset;

import java.util.Iterator;
/**
 * Represents the Union of two sets. Can be considered
 * much like the OR operator, contained in Set A, OR Set B.
 * @author Oliver Keating
 *
 * @param <T>
 */
public class Union<T> implements ImSet<T>{
	
	public static final char UNION = '\u222a';

	final private ImSet<T> setA;
	final private ImSet<T> setB;
	
	private Integer size =null;
	
	public Union(ImSet<T> setA, ImSet<T> setB) {
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
			int count = 0; 
			for (@SuppressWarnings("unused") T t :this){
				count++;
			}
			this.size=count;
		}
		return size;
	}

	@Override
	public boolean contains(Object item) {
		return setA.contains(item) || setB.contains(item);
	}

	@Override
	public long timeStamp() {
		return setA.timeStamp();
	}

	@Override
	public int id() {
		return setB.id();
	}
	@Override
	public boolean isEmpty() {
		return ! iterator().hasNext();
	}
	@Override
	public String description() {
		return setA.description()+UNION+setB.description();
	}
	private class Itr implements Iterator<T>{
		Iterator<T> itrA = setA.iterator();
		Iterator<T> itrB = setB.iterator();
		T next = setNext();
		@Override
		public boolean hasNext() {
			return next!=null;
		}

		private T setNext() {
			if (itrA.hasNext()){
				return itrA.next();
			}
			while(itrB.hasNext()){
				T next = itrB.next();
				if (!setA.contains(next)){
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
