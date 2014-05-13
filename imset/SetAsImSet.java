package com.keatingfinance.imset;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class SetAsImSet<T> implements ImSet<T>{

	final Set<T> set;
	final long timeStamp;
	final int id;
	final String desc;
	public SetAsImSet(Set<T> set, long timeStamp, int id, String desc) {
		this.set = Objects.requireNonNull(set);
		this.timeStamp = timeStamp;
		this.id = id;
		this.desc = desc;
	}
	public SetAsImSet(Set<T> set) {
		this.set = Objects.requireNonNull(set);
		this.timeStamp=System.currentTimeMillis();
		this.id=set.hashCode();
		this.desc=set.toString();
	}
	@Override
	public Iterator<T> iterator() {
		return set.iterator();
	}
	@Override
	public int size() {
		return set.size();
	}
	@Override
	public boolean contains(Object item) {
		return set.contains(item);
	}
	@Override
	public long timeStamp() {
		return timeStamp;
	}
	@Override
	public int id() {
		return id;
	}
	@Override
	public String description() {
		return desc;
	}
	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}
	@Override
	public T getFromHash(int hash) {
		for (T item: set){
			if (item.hashCode()==hash){
				return item;
			}
		}
		return null;
	}
	
	
	
}
