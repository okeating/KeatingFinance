package com.keatingfinance.imset;

import java.util.Iterator;

public class PredicateSet<T> implements ImSet<T>{

	final private ImSet<T> set;
	final private Predicate<T> predicate;
	final String desc;
	private Integer size;
	
	public PredicateSet(ImSet<T> set, Predicate<T> predicate) {
		this.set = set;
		this.predicate = predicate;
		this.desc=set.description()+predicate.toString();
	}
	public PredicateSet(ImSet<T> set, Predicate<T> predicate, String desc) {
		this.set = set;
		this.predicate = predicate;
		this.desc=desc;
	}
	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}
	@Override
	public int size() {
		if (size==null){
			int count = 0;
			for (@SuppressWarnings("unused") T t : this){
				count++;
			}
			this.size = count;
		}
		return size;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object item) {
		if (set.contains(item)){
			//at this point we know the item is instanceof T
			return predicate.test((T) item);
		}
		return false;
	}
	@Override
	public boolean isEmpty() {
		return ! iterator().hasNext();
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
		return desc;
	}
	
	private class Itr implements Iterator<T>{
		Iterator<T> itr=  set.iterator();
		T next = setNext();
		@Override
		public boolean hasNext() {
			return next!=null;
		}

		private T setNext() {
			while(itr.hasNext()){
				T next = itr.next();
				if (predicate.test(next)){
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
		return  set.getFromHash(hash);
	}
}
