package com.keatingfinance.imset;

import java.util.Iterator;

/**
 * The SymmetricDifference is the set of things 
 * in A or in B except the intersection of A and B.
 * 
 * It can be thought of as the exclusive OR, i.e. an
 * item can be in A OR B, but not both. 
 * 
 * This implementation represents a view rather than 
 * a hard implementation. 
 * 
 * @author Oliver Keating
 *
 */
public class SymmetricDifference<T> implements ImSet<T>{

	public static final char SYMMETRIC_DIFFERENCE = '\u2206';
	final private ImSet<T> setA;
	final private ImSet<T> setB;
	
	private Integer size = null;
	
	public SymmetricDifference(ImSet<T> setA, ImSet<T> setB) {
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
			size = count;
		}
		return size;
	}
	@Override
	public boolean contains(Object item) {
		if (setA.contains(item)){
			return ! setB.contains(item);
		}
		if (setB.contains(item)){
			return ! setA.contains(item);
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
	public boolean isEmpty() {
		return ! iterator().hasNext();
	}
	@Override
	public String description() {
		return setA.description()+SYMMETRIC_DIFFERENCE+setB.description();
	}
	
	private class Itr implements Iterator<T> {
		Iterator<T> itrA = setA.iterator();
		Iterator<T> itrB = setB.iterator();
		T next = setNext();
		@Override
		public boolean hasNext() {
			return next!=null;
		}
		/*
		 * Rule is any item in one set 
		 * must not be contained in the other
		 */
		private T setNext() {
			while(itrA.hasNext()){
				T next = itrA.next();
				if (! setB.contains(next)){
					return next;
				}
			}
			while(itrB.hasNext()){
				T next = itrB.next();
				if (! setA.contains(next)){
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
