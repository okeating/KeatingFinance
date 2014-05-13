package com.keatingfinance.util;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
/**
 * SimpleDateFormatter formats a SimpleDate, with day month and year. 
 * It can date a regex in the form of 
 * y (lowercase) for year, must be between 2-4 consecutive characters
 * M (uppercase) for month, MM will return numeric month, MMM is the string code
 * d (lowercase) for day, must be of the form dd
 * seperation characters are ignored during parsing. If more than one seperation character is
 * used, only the last one will appear in date formatting.
 * 
 * The constructor has a var-args secondary String argument. When parsing, if the first parse fails,
 * then it will attempt to parse using the next regex.  
 * 
 * @author Oliver Keating
 *
 */
public class SimpleDateFormatter {
	public static void main(String...args) throws ParseException{
		SimpleDateFormatter df = new SimpleDateFormatter("yyyy-MMM-dd","yyyy-MM-dd");
		SimpleDate date = df.parseSimpleDate("2003-04-22");
		SimpleDateFormatter df2 = new SimpleDateFormatter("dd-MMM-yyyy");
		System.out.println(df2.format(date));
	}
	public static final String  BRITISH_DATE = "dd-MM-yyyy";
	public static final String AMERICAN_DATE = "MM-dd-yyyy";

	public enum DMY {
		DAY('d',2,2),MONTH('M',3,2),YEAR('y',4,2); 
		final private char c;
		final private int min;
		final private int max;
		DMY(char c,int max, int min){
		this.c=c;
		this.max=max;
		this.min=min;
		}
		boolean inRange(int size){
			return min <=size && size<=max;
		}
		
	};
		
	private final String primayRegex;
	private final SimpleDateFormatter[] secondaryFormatters ;
	private boolean monthFlag = false;
	private char seperator;
	private TimePart day;
	private TimePart month;
	private TimePart year;

	
	public SimpleDateFormatter(String primaryRegex, String...secondaryRegex){
		this.primayRegex=primaryRegex;
		secondaryFormatters= new SimpleDateFormatter[secondaryRegex.length];
		for (int i = 0 ; i < secondaryRegex.length ; i++){
			secondaryFormatters[i]=new SimpleDateFormatter(secondaryRegex[i]);
		}
		compile();
	}


	private void compile() {
		for (int i = 0 ; i < primayRegex.length() ; i++){
			char c = primayRegex.charAt(i);
			DMY type = getDMYForChar(c);
			if (type!=null){
				characterFound(type,i);
			} else {
				seperator=c;
			}
		}
		runChecks();
	}

	
	private void runChecks() {
		checkNotNull(day,"day");
		checkNotNull(month,"month");
		checkNotNull(year,"year");
		checkRange(day,DMY.DAY);
		checkRange(month,DMY.MONTH);
		checkRange(year,DMY.YEAR);
	}


	private void checkRange(TimePart part, DMY type) {
		boolean inRange = type.inRange(part.size());
		if (!inRange){
			throw new IllegalArgumentException("For type "+type+" not well defined "+primayRegex+" "+part.size());
		}
	}


	private void checkNotNull(TimePart part, String name) {
		if (part==null){
			throw new IllegalArgumentException("Cannot compile "+primayRegex +" no reference of "+name);
		}
	}


	private DMY getDMYForChar(char c) {
		for (DMY each: DMY.values()){
			if (each.c==c){
				return each;
			}
		}
		return null;
	}

	public String format(SimpleDate simpleDate){
		int y = simpleDate.getYear();
		int m = simpleDate.getMonth();
		int d = simpleDate.getDay();
		char[] buf = new char[primayRegex.length()];
		Arrays.fill(buf, seperator);
		year.fill(buf,y);
		day.fill(buf,d);
		if (monthFlag){
			month.fillMonthCode(buf,m);
		} else {
			month.fill(buf,m);
		}
		return new String(buf);
	}

	private void characterFound(DMY type, int index) {
		boolean correctlyIncremented = false;
		switch(type){
		case DAY:
			if (day==null){
				day = new TimePart(index);
				correctlyIncremented=true;
			} else {
				correctlyIncremented = day.incrementAndCheck(index);
			}
			break;
		case MONTH:
			if (month==null){
				month = new TimePart(index);
				correctlyIncremented=true;
			} else {
				correctlyIncremented = month.incrementAndCheck(index);
				if (month.size() == 3){
					monthFlag=true;
				}
			}
			break;
		case YEAR:
			if (year==null){
				year = new TimePart(index);
				correctlyIncremented=true;
			} else {
				correctlyIncremented = year.incrementAndCheck(index);
			}
			break;
		default:
			throw new AssertionError();
		
		}
		if (!correctlyIncremented){
			throw new IllegalArgumentException("Cannot parse "+primayRegex + " for "+type +" at "+index);
		}
	}


	public SimpleDate parseSimpleDate(String input) throws ParseException{
		if (input == null){
			throw new ParseException("null", 0);
		}
		
		try{
			return internalParseSimpleDate(input);
		} catch (ParseException firstException){
			for (SimpleDateFormatter next : secondaryFormatters ){
				try{
					return next.internalParseSimpleDate(input);
				} catch (ParseException ignored){}
			}
			throw firstException;
		}
	}
	
	
	private SimpleDate internalParseSimpleDate(String input) throws ParseException {
		int y = year.parseString(input);
		int m = monthFlag ? month.parseMonthCode(input) : month.parseString(input);
		int d = day.parseString(input);
		return new SimpleDate(y, m, d);
	}


	private static class TimePart{
		final static int[] modulo = { 10,100,1000,10000};
		final static int[] divisor = { 1, 10, 100, 1000};
		final static int CHAR_ZERO = 48; // == '0'
		int start;
		int end;
		TimePart(int start){
			this.start=start;
			this.end=start;
		}
		/**
		 * m starts from 1, so subtract 1 and use it to directly 
		 * reference the values() array. 
		 * @param buf
		 * @param m
		 */
		public void fillMonthCode(char[] buf, int m) {
			MonthCodes monthCode = MonthCodes.values()[m-1];
			String code = monthCode.name();
			buf[start]=code.charAt(0);
			buf[start+1]=code.charAt(1);
			buf[start+2]=code.charAt(2);
		}
		/**
		 * Do this manually rather than String.valueOf 
		 * because of the set number of characters to fill
		 * @param buf
		 * @param value
		 */
		public void fill(char[] buf, int value) {
			for (int i = start; i<=end; i++){
				int off = end-i;
				int digit = (value % modulo[off]) / divisor[off];
				buf[i]=(char) (digit+CHAR_ZERO);
			}
		}

		boolean incrementAndCheck(int index){
			end++;
			return end==index;
		}
		
		int size(){
			return 1+end-start;
		}
		
		int parseString(String theString) throws ParseException{
			String substring = theString.substring(start,end+1);
			try{
				return Integer.parseInt(substring);
			} catch (NumberFormatException n){
				throw new ParseException("Needs to be an integer "+substring, start);
			}
		}
		int parseMonthCode(String theString) throws ParseException{
			return MonthCodes.getValueOf(theString,start,end);
		}
		
	}
	
	enum MonthCodes{
		Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec;

		public static int getValueOf(String string, int start, int end) throws ParseException {
			String substring  = string.substring(start,end+1);
			MonthCodes[] values = values();
			for (int i = 0 ; i < values.length ; i++){
				if (substring.equalsIgnoreCase(values[i].name())){
					int monthvalue = i+1;
					return monthvalue;
				}
			}
			throw new ParseException("Not recognised: "+substring,start);
		}
	}

	public String format(long time) {
		return format(SimpleDate.longToSimpleDate(time));
	}
	
	public Iterable<String> supportedFormats(){
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					int index =0;
					@Override
					public boolean hasNext() {
						return index < (1 + secondaryFormatters.length);
					}

					@Override
					public String next() {
						String next = index == 0 ? primayRegex : secondaryFormatters[index-1].primayRegex;
						index++;
						return next;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
