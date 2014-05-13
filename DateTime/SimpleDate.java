package com.keatingfinance.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Lightweight date storage, year, month and day
 * are stored into a single 32 bit integer. Immutable and thread safe.
 * 
 * Unlike other "date" objects, it does not represent any time.
 * 
 * It has the benefit of unambiguously representing a date,
 * unlike Unix time whose interpretation will depend on the
 * timezone of the locale. 
 * 
 * Methods are provided to convert to and from "Unix" millisecond
 * time, and for this purpose, the GMT timezone is always used
 * to ensure consistency in interpretation. Please note this may
 * result in a different date being generated compared to the date
 * in the User's Locale, when obtaining the current date. 
 * 
 * This class uses a method similar to storing a date as plain 
 * integer, e.g. with the representation "20080521", except that 
 * the values are stored in binary rather than base 10, because extracting
 * the sub-parts will be very much more efficient. 
 * 
 * Possible range of year values:
 * Min year: 2^22 (= -4,194,304)
 * Max year: (2^22) - 1 (= 4,194,303)
 * 
 * 
 * The day, month, year are stored into the hash
 * in the following way: 
 * 
 * 	Day:   00000000000000000000000000011111
 * 	Month: 00000000000000000000000111100000
 *  Year:  11111111111111111111111000000000
 *  
 *  It is not necessary to create an instance of this class
 *  to hash a date, use the static "toHashCode" method, though
 *  unless performance is critical, it is recommended to create 
 *  an instance, so that the result is not confused with other 
 *  types.
 *  
 *  Handily, the representation of dates ensures that comparisons
 *  of the hash reveal which order comes first, giving a simple
 *  compareTo implementation, valid for the range quoted above. 
 *  
 *	Dates can be represented as a 16 bit integer, at the cost 
 * 	of a considerably reduced range:
 * 	Min year: YEAR_OFFSET = 1950
 * 	Max year: YEAR_OFFSET + 127 = 2077
 * 
 * 	The benefit of producing a 16bit date code is that it can be
 * 	aggregated with other information to provide reduced collision
 * 	or even collisionless hashCodes in objects that incorporate a date.
 *  
 * 
 * @author Oliver Keating
 *
 */
public class SimpleDate implements UniqueHash,Serializable, Comparable<SimpleDate>{

	private static final long serialVersionUID = 6633073981067266412L;
	public static int YEAR_OFFSET = 1950;
	private static int MONTH_MASK = 0b1111;
	private static int DAY_MASK =  0b11111;

	/**
	 * Value of the earliest date representable with the 16 bit compressed hash;
	 */
	public static final SimpleDate EARLIEST_SHORT = new SimpleDate(1950,1,1);
	/**
	 * Value of the latest (highest) date representable with the 16 bit compressed hash
	 */
	public static final SimpleDate LATEST_SHORT = new SimpleDate(2077,12,31);
	
	
	

	/**
	 * Returns a date representing todays date at UTC-0 based 
	 * on the system time.
	 * @return
	 */
	public static SimpleDate today(){
		return longToSimpleDate(System.currentTimeMillis());
	}
	


	public static SimpleDate longToSimpleDate(long date){
		return new SimpleDate(toHashCode(date));
	}

	
	public static int toHashCode(long date) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(date);
		int year = cal.get(Calendar.YEAR);
		int month = 1 + cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return toHashCode(year, month, day);
	}
	/**
	 * Creates a "hash" representation for a year, month and day. 
	 * Guaranteed to be collissionless for a the majority of dates of interest
	 * (see class comments).
	 * @param year
	 * @param month - please note, January is 1. This is different from java.util.Calender
	 * @param day
	 * @return hash
	 * @throws IllegalArgumentExcception if month and day are out of range, as defined by
	 * 				1 <= month <= 12
	 * 				1 <= day <= 31
	 * Please note: Does not perform additional checking that the date is a valid Calendar date,
	 * e.g. 1990, 02, 30, (Feb 30th) will NOT throw an exception. 
	 */
	public static int toHashCode(int year, int month, int day) {
		//check range. No check on year at present, as that will depend on application.
		if (month > 12 || day > 31 || month <=0 || day <=0){
			//this is likely to result from a coding mistake
			throw new IllegalArgumentException("Invalid date:" +year +"-" + month+ "-" +day);
		}
		//for negative values of year, the sign will be preserved
		int h=(year << 9);
		h |= (month << 5);
		h |= day;
		return h;
	}
	
	/*
	 * Instance variables
	 */
	
	private final int hash;
	
	/**
	 * Constructs a simple date with a given year, month and day.
	 * Note months start at 1.
	 * @param year - see doc for range information
	 * @param month - 1 (Jan) to 12 (Dec)
	 * @param day - 1 to 31
	 */
	public SimpleDate(int year, int month, int day){
		this.hash = toHashCode(year, month, day);
	}
	
	public SimpleDate(int hash){
		this.hash=hash;
	}
	
	public int getDay(){
		return hash & DAY_MASK;
	}
	
	public int getMonth(){
		return (hash >> 5) & MONTH_MASK;
	}
	
	public int getYear(){
		return (hash >> 9);
	}
	
	@Override public String toString(){
		return getYear() + "_"+ getMonth() + "_" + getDay();
	}
	
	public static String hashToString(int hash){
		return new SimpleDate(hash).toString();
	}
	
	/**
	 * Returns a 16 bit hash based on this date. Valid only between years
	 * of 1950-2078
	 * @return
	 */
	public short compressedHash(){
		return toCompressedHash(getYear(), getMonth(), getDay());
	}
	/**
	 * Provides a short (compressed) 16 bit hash. 
	 * 
	 * This enables other data to be combined with this hash and uniquely identify.
	 * 
	 * The trade-off of the compression is that the range of possible years is reduced,
	 * valid years are 1950-2077 (YEAR_OFFSET - (YEAR_OFFSET + 127)
	 * 
	 * This is potentially useful when we know in advance the range of years of interest
	 * is limited. It helps to fulfil the contract of UniqueHash by ensuring that other
	 * objects that contain a date can use 16 bits to store a date, and the other 16 bits
	 * to store additional details.
	 * 
	 * 
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static short toCompressedHash(int year, int month, int day){
		//the return is treated as unsigned, so we use a mask to avoid including sign bit
		return (short) (toHashCode(year-YEAR_OFFSET,month,day) & 0xffff);
	}
	/**
	 * Converts an SQL timestamp into a compressed hash.
	 * @param timeStamp
	 * @return
	 */
	public static short toCompressedHash(java.sql.Timestamp timeStamp){
		return toCompressedHash(timeStamp.getTime());
	}
	
	/**
	 * Takes in a 64 bit number and spits out a 16 bit number. 
	 * 
	 * Implicitly, information is lost, however, for every day
	 * the long value represents, the output is unique between
	 * 1950 and 2076
	 * 
	 * @param date
	 * @return
	 */
	public static short toCompressedHash(long date){
		return longToSimpleDate(date).compressedHash();
	}
	/**
	 * Provides a long representation of the short hash. 
	 * @param compressedHash
	 * @return
	 */
	public static long compressedHashToLong(short compressedHash){
		return compressedHashToDate( compressedHash).toLongValue();
	}
	
	/**
	 * Returns this simple date as a long value.
	 * @return
	 */
	public long toLongValue() {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.clear();
		cal.set(getYear(), getMonth()-1, getDay());
		return cal.getTimeInMillis();
	}

	public static SimpleDate compressedHashToDate(short compressedHash){
		return new SimpleDate(compressedHashToHash(compressedHash));
	}
	/**
	 * Provides a niave mask to the hash. Niave as no 
	 * checking is done. Method to complement unMaskHash
	 * @param hash
	 * @param mask
	 * @return hash & mask
	 */
	public static int maskHash(int hash, int mask){
		return hash & mask;
	}
	/**
	 * Provides a way to un-mask a hash. 
	 * The leftmost bit of the hash (within the mask) is treated
	 * as the sign bit. If the number is negative, it is padded
	 * with 1s. 
	 * @param hash
	 * @param mask
	 * @return an expanded 32bit hash, that is numerically the same
	 */
	public static int unMaskHash(int hash, int mask){
		
		//strictly this should not do anything. Just in case.
		hash = hash & mask;
		
		//make a copy, left shift by one
		int hashCopy = hash << 1;
		
		//check the sign bit
		if ((hashCopy & ~mask) != 0){
			//negative, means set bits above mask to 1
			return hash | ~mask;
		}
		//positive, nothing to do, leading bits are zero.
		return hash;
	}
	
	@Override public int hashCode(){
		return hash;
	}
	/**
	 * Returns a string in the form "yyyyMMdd"
	 * 
	 * Always exactly 8 characters long, padded with
	 * zeros where necessary.
	 * 
	 * Compatible with parseStandardString
	 * 
	 * Only well defined for years 0-9999 
	 * @return
	 */
	public String toStandardString(){
		/*
		 * Can not use String.valueOf(int) as
		 * this will not append leading zeros.
		 * 48 is the ASCII starting position for
		 * the character '0'
		 */
		char[] buf = new char[8];
		int year = getYear();
		buf[0]=(char) ((year/1000)+48);
		buf[1]=(char) (((year%1000)/100)+48);
		buf[2]=(char) (((year%100)/10)+48);
		buf[3]=(char) (((year%10)+48));
		
		int month = getMonth();
		buf[4]=(char) (((month/10)+48));
		buf[5]=(char) (((month%10)+48));
		
		int day = getDay();
		buf[6]=(char) (((day/10)+48));
		buf[7]=(char) (((day%10)+48));
		return new String(buf);
	}
	/**
	 * Takes a string in the form "yyyyMMdd" and turns it 
	 * into a simpledate.
	 * @param string
	 * @return
	 */
	public static SimpleDate parseStandardString(String string){
		char[] chars = string.toCharArray();
		check(chars);
		int year = 0;
		year +=(chars[0]-48)*1000;
		year +=(chars[1]-48)*100;
		year +=(chars[2]-48)*10;
		year +=(chars[3]-48);
		int month =0;
		month += (chars[4]-48)*10;
		month += (chars[5]-48);
		int day = 0;
		day +=(chars[6]-48)*10;
		day +=(chars[7]-48);
		return new SimpleDate(year, month, day);
	}
	/**
	 * better to throw an exception than have some
	 * undefined behaviour if the input is wrong.
	 * @param chars
	 */
	private static void check(char[] chars) {
		if (chars.length!=8){
			throw new IllegalArgumentException("For "+new String(chars)+": Must be exactly 8 characters long, in form \"yyyyMMdd\"");
		}
		for (char c: chars){
			if (c < 48 || c > 57){
				throw new IllegalArgumentException("For "+new String(chars)+": Must contain digits only, in form \"yyyyMMdd\"");
			}
		}
		
	}

	/**
	 * Returns a standardised string based on the millitime.
	 * This method always returns a string of exactly 8 characters
	 * in the form "YYYYMMDD".
	 * 
	 * Unlike java.util.Date.toString(), this always uses UTC time.
	 * 
	 * Only well defined for years 0-9999 
	 *  
	 * @param date
	 * @return
	 */
	public static String toStandardString(long date){
		return longToSimpleDate(date).toStandardString();
	}
	

	@Override public boolean equals(Object other){
		if (this==other){
			return true;
		}
		if (other instanceof SimpleDate){
			return this.hash == ((SimpleDate)other).hash;
		}
		return false;
	}
	/**
	 * Converts a 16 bit date hash into a 32 bit datehash
	 * @param compressedHash
	 * @return
	 */
	public static int compressedHashToHash(short compressedHash) {
		//expand to 32 bits
		int hash = compressedHash & 0xffff; //need the mask to treat as unsigned
		//get the stored year
		int year = hash >> 9;
		//clear out the year bits
		hash &= ((1<<9)-1);
		//add back the offset
		year+=YEAR_OFFSET;
		//store back the year bits
		hash |= (year<<9);
		return hash;
	}

	public SimpleDate subtractYears(int i) {
		int thisYear = this.getYear();
		int resultYear = thisYear-i;
		return new SimpleDate(resultYear, this.getMonth(), this.getDay());
	}

	@Override
	public int compareTo(SimpleDate o) {
		return (this.hash<o.hash ? -1 : (this.hash==o.hash ? 0 : 1));
	}
	
	

	public int compareToCompressedHash(short compressedHash){
		int ohash = compressedHashToHash(compressedHash);
		return (this.hash<ohash ? -1 : (this.hash==ohash ? 0 : 1));
	}


	public SimpleDate tomorrow() {
		long today = this.toLongValue();
		long tomorrow = today +  (24L * 60L * 60L * 1000L);
		return longToSimpleDate(tomorrow);
	}


	public SimpleDate yesterday() {
		long today = this.toLongValue();
		long yesterday = today -  (24L * 60L * 60L * 1000L);
		return longToSimpleDate(yesterday);
	}


	public boolean isGreaterThan(short dateHash) {
		int otherHash = compressedHashToHash(dateHash);
		return this.hash > otherHash;
	}



	public boolean isLessThan(short dateHash) {
		int otherHash = compressedHashToHash(dateHash);
		return this.hash < otherHash;
	}



	public boolean isGreaterThan(SimpleDate simpleDate) {
		return this.hash > simpleDate.hash;
	}
	public boolean isLessThan(SimpleDate simpleDate) {
		return this.hash < simpleDate.hash;
	}


	
}
