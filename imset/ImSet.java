package com.keatingfinance.imset;
/**
 * A ImSet is a reduced version of the java.util.Set,
 * it is meant to represent a Set, but the mutator methods
 * are not present.
 * 
 * In addition, it is useful to specify some metadata with the
 * set, such as the timestamp, the id, and a description.
 * 
 * @author Oliver Keating
 *
 * @param <T>
 */
public interface ImSet<T> extends Iterable<T> {
	int size();
	boolean contains(Object item);
	boolean isEmpty();
	/*
	 * Descriptors
	 */
	long timeStamp();
	int id();
	String description();
	/**
	 * Optional method - returns an object
	 * whose hashcode matches the hash specified,
	 * or null if there is no such object.
	 * @param hash
	 * @return
	 */
	T getFromHash(int hash);
}
