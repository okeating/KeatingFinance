package com.keatingfinance.imset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ImSetAsSet<T> implements Set<T>{

	final ImSet<T> set;

	public ImSetAsSet(ImSet<T> set) {
		this.set = set;
	}

	@Override
	public boolean add(T arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0) {
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object each: arg0){
			if (set.contains(each)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return set.size()==0;
	}

	@Override
	public Iterator<T> iterator() {
		return set.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[size()];
		int index = 0;
		for (T t : set){
			array[index++]=t;
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] array) {
		int size = size();
		if (array.length < size){
			array = Arrays.copyOf(array, size);
		}
		int index = 0;
		for (T t : set){
			array[index++]=(U) t;
		}
		return array;
	}
	
}
