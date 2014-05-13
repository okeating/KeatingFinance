package com.keatingfinance.util;

import java.io.Serializable;

/**
 * Simple Time. Represents a time of day, without reference to the date or timezone.
 * 
 * The concept is useful because the standard implementation of Date
 * always consider the time zone, which can lead to confusion, e.g.
 * Querying a database about an event on a particular time may produce
 * different results depending on the locale of the client computer. 
 * @author Oliver Keating
 *
 */
public class SimpleTime implements Serializable, Comparable<SimpleTime> {
	/*
	 * Test code
	 */
	public static void main(String... args){
		SimpleTime now = (getUTCFromMillis(System.currentTimeMillis()));
		System.out.println(now);
		long milliOff = now.absoluteMilliDifference();
		System.out.println(milliOff);
		SimpleTime aTime = new SimpleTime(11, 57, 0);
		System.out.println(aTime.absoluteMilliDifference());
	}
	
	private static final long serialVersionUID = 6009451543049054310L;
	final private static int SIX_BITS  = (1<<6)-1;
	final private static int FIVE_BITS = (1<<5)-1;
	
	final private static int MILLI_MASK = 	(1<<10)-1;
	final private static int SECONDS_MASK = (SIX_BITS<<16);
	final private static int MINUTES_MASK = (SIX_BITS<<22);
	final private static int HOURS_MASK=	(FIVE_BITS<<27);
	
	public static SimpleTime getUTCFromMillis(long millis){
		int milliSeconds = (int) (millis % 1000);
		int seconds = (int) ((millis/1000) % 60);
		int minutes = (int) ((millis/(1000*60)) % 60);
		int hours = (int) ((millis/(1000*60*60)) % 24);
		return new SimpleTime(hours, minutes, seconds, milliSeconds);
		
	}
	
	
	
	final private int theTime;
	
	
	public SimpleTime(int hours, int minutes){
		this(hours,minutes,0);
	}
	
	public SimpleTime(int hours, int minutes, int seconds){
		this(hours,minutes,seconds,0);
	}
	
	public SimpleTime(int hours,int minutes, int seconds, int milliSeconds){
		this(encode(hours,minutes,seconds,milliSeconds));
	}
	
	private static int encode(int hours, int minutes, int seconds,
			int milliSeconds) {
		checkArgs(hours,minutes,seconds,milliSeconds);
		int result = (MILLI_MASK & milliSeconds);
		result |= (SECONDS_MASK & (seconds << 16));
		result |= (MINUTES_MASK & (minutes << 22));
		result |= (HOURS_MASK & (hours<< 27));
		return result;
	}

	private static void checkArgs(int hours, int minutes, int seconds,
			int milliSeconds) {
		rangeCheck(milliSeconds,1000,"milliSeconds");
		rangeCheck(seconds,60,"seconds");
		rangeCheck(minutes,60,"minutes");
		rangeCheck(hours,24,"hours");
	}

	private static void rangeCheck(int variable, int upperBound, String variableName) {
		if (variable < 0 || upperBound <= variable){
			throw new IllegalArgumentException(variableName + " out of range: "+variable);
		}
	}

	private SimpleTime(int theTime){
		this.theTime=theTime;
	}
	
	public int getHours(){
		return (theTime & HOURS_MASK) >> 27;
	}
	
	public int getMinutes(){
		return (theTime & MINUTES_MASK) >> 22;
	}
	
	public int getSeconds(){
		return (theTime & SECONDS_MASK) >> 16;
	}
	public int getMilliSeconds(){
		return theTime & MILLI_MASK;
	}
	
	public long getMilliSecondsFromMidnight(){
		long total = getMilliSeconds();
		total += getSeconds() * 1000L;
		total += getMinutes() * 1000L * 60L;
		total += getHours() * 1000L * 60L * 60L;
		return total;
	}
	
	@Override public String toString(){
		return getHours()+":"+getMinutes()+":"+getSeconds();
	}
	@Override public int hashCode(){
		return theTime;
	}
	@Override public boolean equals(Object oth){
		return (oth instanceof SimpleTime)
				&& (this.theTime==((SimpleTime)oth).theTime);
	}

	@Override
	public int compareTo(SimpleTime oth) {
		return this.theTime < oth.theTime ? -1 :
			this.theTime==oth.theTime? 0 : 1;
	}
	/**
	 * Returns the absolute (always positive)
	 * difference between the time this represents
	 * and the time obtained from System.currentTimeMillis();
	 * 
	 * This is useful for determining how long to wait 
	 * for a particular time.
	 * @return
	 */
	public long absoluteMilliDifference() {
		long milliTime = System.currentTimeMillis();
		long millisSinceMidNight = milliTime % (24L * 60L * 60L* 1000L);
		return Math.abs(this.getMilliSecondsFromMidnight()-millisSinceMidNight);
	}
	
	
}
