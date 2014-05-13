package com.keatingfinance.datastruct;
/**
 * The contract for implementing this interface is hash code is unique.
 * 
 * The number returned by calling hashCode must be a unique primary key.
 * 
 * The number provided by a call to the object's hashCode() method must be
 * able to uniquely identify all the properties of that object, and guarantee
 * that if 	
 * 	a.hashCode() == b.hashCode()
 * then necessarily
 * 	a.equals(b) == true
 * 
 * It might also be possible to construct an instance of such an object simply
 * given the hash.
 * 
 * The purpose of this, data structures using hashCodes can be significantly sped
 * up by disregarding usual checking associated with resolving hash collisions, 
 * and avoids the need to run the .equals method. 
 * 
 * FastMap has an approximately factor 2 performance gain over conventional Java
 * hashMap. 
 * 
 * @author Oliver Keating
 *
 */
public interface UniqueHash {}
