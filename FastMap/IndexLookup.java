package com.keatingfinance.datastruct;
import java.io.Serializable;
import java.util.Iterator;


/**
 * The index lookup provides a 1-1 link from a given primary
 * key, to an index. 
 * 
 * In a database context, this provides a value for a row for
 * a given integer primary key. 
 * 
 * In a programming context, this may, for example, provide
 * the appropriate array index for a given primary key.
 * 
 * In general, the constructor will take an array of  int[] PrimaryKeys 
 * and map them according to their index,
 * in other words, for any given
 * 		int primaryKey = primaryKeys[index];
 * this provides a fast way to reverse that process, namely
 * 		int index = fastMap.getIndex(primaryKey);
 * 
 * Please note unlike standard maps, no standard methods are provided
 * to modify the map after construction. This allows for a more efficient
 * implementation.
 * 
 * @author Keating Finance
 *
 */
public interface IndexLookup extends Serializable{
	int getIndex(int primaryKey);
	boolean containsKey(int primaryKey);
	Iterable<Integer> keyIterable();
	int size();
}
