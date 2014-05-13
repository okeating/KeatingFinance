package com.keatingfinance.imset;
/**
 * Marker interface to indicate the ImSet is backed by
 * an array. This is important to know because if 
 * repeated iteration over the ImSet is required, it may 
 * be more efficient to wrap the set into an ImArraySet
 * first such as CachedSet, because the iteration over
 * an array will achieve the faster rate of iteration, 
 * particularly when the ImSet contains a deep structure of
 * Predicates, Unions etc. 
 * @author Oliver Keating
 *
 * @param <T>
 */
public interface ImArraySet<T> extends ImSet<T>{
	//no additional methods defined.
}
