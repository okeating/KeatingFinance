package com.keatingfinance.imset;

import java.util.Collections;
import java.util.Iterator;
/**
 * A singleton enumerated object representing the empty set.
 * Static method emptySet() is provided also to provide a 
 * Generic compatible way of obtaining the empty set, similar
 * to Collections.emptySet() etc.
 * @author Oliver Keating
 *
 */
public enum EmptySet implements ImSet<Object>{
	EMPTY_SET;
	final public static char EMPTY_SET_CHAR = '\u2205';
	@SuppressWarnings("unchecked")
	public static <T> ImSet<T> emptySet(){
		//this is a typesafe conversion because it never returns anything.
		return (ImSet<T>) EMPTY_SET;
	}
	@Override
	public Iterator<Object> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean contains(Object item) {
		return false;
	}

	@Override
	public long timeStamp() {
		return 0;
	}

	@Override
	public int id() {
		return 0;
	}

	@Override
	public String description() {
		return String.valueOf(EMPTY_SET_CHAR);
	}
	@Override
	public boolean isEmpty() {
		return true;
	}
	@Override
	public Object getFromHash(int hash) {
		return null;
	}
}
