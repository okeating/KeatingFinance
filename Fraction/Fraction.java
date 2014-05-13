package com.keatingfinance.util;

import java.io.Serializable;

/**
 * A fraction, represented by two ints, a numerator and a divisor.
 * 
 * Should be invoked using the valueOf(String) method, which will
 * parse a decimal string into a fraction. 
 * 
 * By default, attempting to parse a fraction from a decimal string
 * will attempt to look for and find recurring patterns. There is a tradeoff
 * between trying to reconstruct the "true" value and risking losing information
 * by incorrectly interpreting recurring values. This can be tuned by
 * feeding in an array of recursion thresholds, which determines how often
 * a sequence for a given length has to appear before it is considered to be
 * recurring. Any instance of "142857" is considered to be the result of
 * a division by seven.
 * 
 * @author Keating Finance
 *
 */
public class Fraction extends Number implements Serializable{

	private static int[] DEFAULT_RECURSION_THRESHOLD =  { 3, 2, 1, 1, 1 };
	
	final public static Fraction ZERO = new Fraction(0,1);
	final public static String ONE_OVER_SEVEN ="142857";
	private static final long serialVersionUID = 9043272863476093314L;
	protected final int numerator;
	final protected int divisor;
	/*
	 * Test code
	 */
	public static void main(String... args){
		String s = String.valueOf(22.0/7.0);
		System.out.println(s);
		System.out.println(Fraction.parseFraction(s));
		
	}

	
	protected Fraction(int numerator, int divisor){
		this.numerator=numerator;
		this.divisor=divisor;
		if (divisor == 0){
			throw new IllegalArgumentException("Cannot represent zero denominator: "+this.toString());
		}
	}
	/**
	 * Will construct a decimal fraction from the string, attempting to use
	 * the preferredDenominator. The resulting fraction may not use the
	 * preferredDenominator under two circumstances:
	 * 1. The numerator would overflow. The denominator is reduced in this case
	 * 2. The decimal contains more digits than can be represented with preferredDenominator.
	 * 
	 * 
	 * @param decimal
	 * @param preferredDenominator - must be power of ten
	 * @return
	 */
	public static Fraction parseDecimalFraction(String decimal, int preferredDenominator){
		if (!isPowerOfTen(preferredDenominator)){
			throw new IllegalArgumentException("denominator must be a power of ten"+preferredDenominator);
		}
		
		FractionParser parser = new FractionParser(decimal).parseFraction();
		long num = parser.interMediateNumerator;
		long denom = parser.interMediateDenomniator;
		boolean negative = parser.negative;
		
		while(denom < preferredDenominator){
			denom *= 10;
			num *= 10;
			if (overFlowsInt(denom) || overFlowsInt(num)){
				denom/=10;
				num/=10;
				break;
			}
		}
		return toFraction(negative? -num: num,denom);
		
	}
	
	public static Fraction valueOf(int numerator, int denominator){
		if ((numerator < 0) && (denominator < 0)){
			return new Fraction(-numerator, -denominator);
		}
		if (denominator < 0){
			denominator = -denominator;
			numerator =   -numerator;
		}
		return new Fraction(numerator, denominator);
	}
	
	public static Fraction valueOf(int value){
		return new Fraction(value, 1);
	}
	/**
	 * Returns a fraction representing a double value,
	 * subject the limitations of such a conversion.
	 * @param value
	 * @return
	 */
	public static Fraction valueOf(double value){
		return parseFraction((String.format("%.9f",value)));
	}
	
	public static Fraction parseSimplifiedFraction(String decimal){
		return new FractionParser(decimal).parseFraction().toSimplifiedFraction();
	}
	public static Fraction parseFraction(String decimal){
		return parseFraction(decimal, DEFAULT_RECURSION_THRESHOLD);
	}
	public static Fraction parseFraction(String decimal, int[] recursionThresholds){
		return new FractionParser(decimal, recursionThresholds).parseFraction().toSimplifiedFraction();
	}
	public static Fraction parseRawFraction(String decimal){
		return new FractionParser(decimal).parseFraction().toRawFraction();
	}
	
	private static boolean isPowerOfTen(int divisor){
		while(divisor > 1){
			if ((divisor % 10) != 0){
				return false;
			}
			divisor /=10;
		}
		return divisor==1;
	}
	public static Fraction simplify(long numerator, long divisor){
		boolean negative = false;
		if (numerator < 0){
			numerator = -numerator;
			negative = true;
		}
		if (divisor < 0 ) {
			divisor = -divisor;
			negative = !negative;
		}
		return simplify(numerator, divisor, negative);
	}
//	public static Fraction simplifiedFraction()
	/**
	 * Long values are used in the intermidiate steps.
	 * 
	 * Requires positive values of numerator and divisor
	 * to work properly. If the desired fraction is negative
	 * use the negative flag.
	 * 
	 * @param numerator
	 * @param divisor
	 * @param negative - if true the resulting fraction has a negative numerator;
	 * @return
	 */
	private static Fraction simplify(long numerator, long divisor, boolean negative){
		if (numerator==0){
			return ZERO;
		}
		
		long trialDivision = Math.min(numerator, divisor);
		
		boolean hasRemainder = false;
		do{
			hasRemainder = 
					(numerator % trialDivision !=0)
				|| (divisor % trialDivision != 0);
			
		} while (hasRemainder && --trialDivision > 1);
		
		long simplifiedNumerator = numerator/trialDivision;
		long simplifiedDenominator  = divisor/trialDivision;
		if (negative){
			simplifiedNumerator = -simplifiedNumerator;
		}
		return toFraction(simplifiedNumerator, simplifiedDenominator);
	}
	/**
	 * Long values to ints, checking for overflow
	 * @param numerator
	 * @param divisor
	 * @return
	 */
	protected static Fraction toFraction(long numerator, long divisor){
		if (overFlowsInt(numerator) || overFlowsInt(divisor)){
			throw new NumberFormatException("Cannot represent fraction as ints : "+numerator +" : "+divisor);
		}
		return new Fraction((int)numerator,(int) divisor);
	}
	
	protected static boolean overFlowsInt(long value) {
		if (value >= 0){
			return value > Integer.MAX_VALUE; 
		} else {
			return value < Integer.MIN_VALUE;
		}
	}


//	/**
//	 * Constructs a Fraction represnting a given String literal representation
//	 * of a decimal. 
//	 * @param decimal
//	 * @return
//	 * @throws NumberFormatException - if the value could not be parsed.
//	 */
//	public static Fraction valueOf(String decimal){
//		return valueOf(decimal,DEFAULT_RECURSION_THRESHOLD);
//	}
//	public static Fraction valueOf(double value){
//		return valueOf(Double.toString(value));
//	}
//	
//	public static Fraction valueOf(String decimal, int[] recusionThreshold){
//		
//		
//	
//		
//		return standardFraction(buf, decimalLocation);
//	}
	
	
	
	public Fraction simplyfy(){
		return simplify(numerator,divisor);
	}


	
	public static class FractionParser{
		long interMediateDenomniator;
		long interMediateNumerator;
		StringBuilder buf;
		int decimalLocation;
		int decimalOffset;
		final String input;
		int[] recursionThresholds;
		int leadingZeros;
		RecurringFractionParser recurringParser;
		boolean negative=false;
		public FractionParser(String input){
			this.input=input;
		}
		public FractionParser(String input, int[] recursionThresholds){
			this.input=input.trim();
			this.recursionThresholds=recursionThresholds;
		}
		public FractionParser parseFraction(){
			if (input.startsWith("-")){
				negative=true;
			}
			decimalLocation = input.indexOf('.');
			
			if (decimalLocation==-1){
				this.interMediateNumerator = Math.abs(Integer.parseInt(input));
				this.interMediateDenomniator= 1;
				return this;
			}
			buf = new StringBuilder(input);
			if (negative){
				buf.deleteCharAt(0);
				decimalLocation--;
			}
			
			deleteTrailingZeros();
			if (isRecurringFraction()){
				recurringParser.parseRecurringFraction();
				return this;
			}
			
			
			this.interMediateDenomniator=getDecimalDivisor();
			removeExcessCharsFromBuf();
			
			this.interMediateNumerator = Long.parseLong(buf.toString());
			return this;
		}
		
		private void removeExcessCharsFromBuf() {
			buf.deleteCharAt(decimalLocation);
			for (int i = 0 ; i < leadingZeros ; i++ ){
				buf.deleteCharAt(0);
			}
		}
		public Fraction toRawFraction(){
			return toFraction(negative? -interMediateNumerator : interMediateNumerator,interMediateDenomniator);
		}
		
		public Fraction toSimplifiedFraction(){
			return simplify(interMediateNumerator, interMediateDenomniator,negative);
		}
		
		/**
		 * raises 10 to the power of a given exponent.
		 * @param expnonent - max value is 19 (to avoid long overflow)
		 * @return
		 */
		protected static long tenToThePower(int exponent){
			if (exponent > 19){
				throw new NumberFormatException("Too big");
			}
			//don't use Math.pow as it uses doubles
			long value = 1; 
			while(exponent > 0){
				value *=10;
				exponent--;
			}
			return value;
			
		}
		private  void deleteTrailingZeros(){
			for (int i = buf.length()-1 ; i > decimalLocation ; i--){
				if (buf.charAt(i)=='0'){
					buf.deleteCharAt(i);
				} else {
					break;
				}
			}
		}
		
		protected  long getDecimalDivisor(){
			int offset = (buf.length()-1)-decimalLocation ;
			leadingZeros=0;
			for (int i = 0 ; i<buf.length() ; i++){
				if (buf.charAt(i)=='0'){
					leadingZeros++;
//					if (i > decimalLocation){
//						offset--; 
//					}
				} else if(i!=decimalLocation) {
					break;
				}
			}
			
			return tenToThePower(offset);
		}
		
		public boolean isRecurringFraction() {
			if(recursionThresholds!=null){
				recurringParser = new RecurringFractionParser();
				return recurringParser.isRecurring();
				
			}
			return false;
		}
		
		private class RecurringFractionParser{
			int blockSize;
			//initialised with nonsense values, to ensure not used accidentally
			int firstPatternIndex=-1;
			int lastPatternIndex=-1;
			private String leadingDigits;
			private String decimalDigits;
			private String pattern;
			private int numRepeats;
			private boolean oneOverSeven;
			private boolean isRecurring(){
				if (oneOverSeven){
					return true;
				}
				this.blockSize=minRepeatingBlockSize();
				return blockSize > 0 && isTerminatingSequence() ;
			}
			public RecurringFractionParser() {
				this.decimalDigits = buf.substring(decimalLocation+1, buf.length());
				this.leadingDigits = buf.substring(0,decimalLocation);
				this.oneOverSeven = decimalDigits.contains(ONE_OVER_SEVEN);
			}
			
		private void parseRecurringFraction(){
			if (oneOverSeven){
				firstPatternIndex = decimalDigits.indexOf(ONE_OVER_SEVEN);
			}

			int leadingValue = Integer.parseInt(leadingDigits);
			String prefixDigits = decimalDigits.substring(0,firstPatternIndex);
			if (oneOverSeven){
				interMediateNumerator = 1;
				interMediateDenomniator = 7 * tenToThePower(firstPatternIndex);
			} else { 
				simpleRecursiveFraction();
			}
			
			if (leadingValue==0 && isZero(prefixDigits)){
				return; //done
			}
			/*
			 * Works for now but could be improved
			 */
			FractionParser otherPart = new FractionParser(leadingDigits+"."+prefixDigits).parseFraction();
			if (interMediateDenomniator == otherPart.interMediateDenomniator){
				interMediateNumerator += otherPart.interMediateNumerator;
			} else {
				long lcd = lowestCommonDenominator(interMediateDenomniator, otherPart.interMediateDenomniator);
				long diff1 = lcd/interMediateDenomniator;
				long diff2 = lcd/otherPart.interMediateDenomniator;
				interMediateNumerator= (interMediateNumerator*diff1)+(otherPart.interMediateNumerator*diff2);
				interMediateDenomniator=lcd;
			}
			
		}
		
		private void simpleRecursiveFraction() {
			interMediateNumerator = Long.parseLong(pattern);
			interMediateDenomniator = recursiveDenominator();
		}
		
		private boolean isZero(String prefixDigits) {
			return prefixDigits.length()==0 || Integer.parseInt(prefixDigits)==0;
		}

		

		private long recursiveDenominator() {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < blockSize ; i++){
				buf.append('9');
			}
			for (int i = 0; i< firstPatternIndex ; i++){
				buf.append('0');
			}
			return Long.parseLong(buf.toString());
		}

		
	
		private int  minRepeatingBlockSize() {
			
			for (int i = 0; i< recursionThresholds.length ; i++){
				int blockSize= i+1;
				int repeats = repeats(decimalDigits, blockSize);
				if (repeats >= recursionThresholds[i]){
					this.numRepeats=repeats;
					return blockSize;
				}
			}
			return -1;
		}
		/**
		 * Requires input like "3333"
		 * @param input
		 * @param blockSizere
		 * @param threshold
		 * @return
		 */
		private  int repeats(String input, int blockSize){
			String bestPattern=null;
			int maxRepeats = 0 ;
			for (int i = 0 ; i < blockSize ; i++){
				int repeats = repeats(input, blockSize, i);
				if (repeats>maxRepeats){
					maxRepeats=repeats;
					bestPattern=pattern;
				}
			}
			this.pattern=bestPattern;
			return maxRepeats;
		}
		
		private int repeats(String input, int blockSize, int offset){
			String pattern = null;
			
			int recursionCount= 0;
			for (int i = input.length()-(offset+blockSize) ; i >= 0 ; i-=blockSize){
				String test= input.substring(i, i+blockSize);
				if (test.equals(pattern)){
					recursionCount++;
					this.pattern=pattern;
				}
				pattern=test;
			}
			return recursionCount;
		}
		/**
		 * By now we have established there is a repeating pattern,
		 * but we have to check if it terminates the string.
		 * @return
		 */
		private boolean isTerminatingSequence() {
			firstPatternIndex = decimalDigits.indexOf(pattern);
			lastPatternIndex = decimalDigits.lastIndexOf(pattern);
			int terminationIndex = lastPatternIndex + blockSize;
			int length = decimalDigits.length();
			if (terminationIndex == length){
				//simple case
				return true;
			}
			if (terminationIndex < (length-blockSize)){
				//not allowed
				return false;
			}
			assert terminationIndex==(length-blockSize);
			/*
			 * now the complicated case. This may occur because someone
			 * has rounded up the last digit E.g. 66667.
			 * Allowed through if the penultimate digit is greater than 5
			 * and the value of the final digit is one greater
			 */
			int penultimateDigit = Character.digit(decimalDigits.charAt(length-2), 10);
			if (penultimateDigit < 5){
				return false;
			}
			int finalDigit = Character.digit(decimalDigits.charAt(length-1), 10);
			return (finalDigit) == (penultimateDigit+1);
		}
		}
		
		
		
	}

	
	

	
	
	

	public int getNumerator(){
		return numerator;
	}
	public int getDivisor(){
		return divisor;
	}
	@Override public int hashCode(){
		return 31* numerator + 31 * divisor;
	}
	@Override public boolean equals(Object other){
		if (other instanceof Fraction){
			Fraction otherFrac = (Fraction)other;
			return this.numerator==otherFrac.numerator 
					&& this.divisor==otherFrac.divisor;
		}
		return false;
	}
	
	
	@Override public String toString(){
		return numerator + " / " + divisor; 
	}
	
	public int multiply(int value){
		value *= numerator;
		return value/divisor;
	}
	/*
	 * Uses long values for calculation
	 */
	public int multiplySafe(int value){
		long valueL = value;
		valueL *= (long)numerator;
		return (int) (valueL /(long)divisor);
	}
	
	public long multiplyLong(long value){
		value*= (long)numerator;
		return value /(long)divisor;
	}
	public long multiplyRoundHalfEven(long value){
		long numeratorL = (long) numerator;
		long divisorL = (long) divisor;
		value*= (long) numeratorL;
		long divisionResult = value / divisorL;
		long remainderTimesTwo = (value % divisorL) << 1;
		if (remainderTimesTwo > divisorL){
			divisionResult++;
		} else if (remainderTimesTwo==divisorL){
			if ((divisionResult & 1)==1){
				divisionResult++;
			}
		}
		return divisionResult;
		
	}
	public double multiplyDouble(double value){
		return value * this.asDouble();
	}
	
	public Fraction reciporacle(){
		return simplify(divisor, numerator);
	}
	
	public Fraction divide(Fraction other){
		return multiply(other.reciporacle());
	}
	
	public Fraction multiply(Fraction other){
		return simplify(this.numerator*other.numerator, this.divisor*other.divisor);
	}
	
	public Fraction subtract(Fraction other){
		if (this.divisor==other.divisor){
			return simplify(this.numerator-other.numerator, divisor);
		}
		int thisNum = this.numerator * other.divisor;
		int newDivisor = this.divisor * other.divisor;
		
		int otherNum = other.numerator * this.divisor;
	
		return simplify(thisNum-otherNum, newDivisor);
	}
	
	public Fraction add(Fraction other){
		if (this.divisor==other.divisor){
			return simplify(this.numerator+other.numerator, divisor);
		}
		int thisNum = this.numerator * other.divisor;
		int newDivisor = this.divisor * other.divisor;
		
		int otherNum = other.numerator * this.divisor;
	
		return simplify(thisNum+otherNum, newDivisor);
	}
	
	public double remainder(int value){
		value *= numerator;
		int remainder = value % divisor;
		return ((double)remainder)/(double)divisor;
	}
	
	public Fraction remainderAsFraction(int value){
		//upgrade to long to reduce chance of overflow
		long multiplication  = (long)numerator * (long) value;
		long remainder = multiplication % (long)divisor;
		return simplify(remainder,divisor);
	}
	
	public double asDouble(){
		return ((double)numerator) / ((double) divisor);
	}

		
	
	@Override
	public double doubleValue() {
		return asDouble();
	}

	@Override
	public float floatValue() {
		return (float)asDouble();
	}

	@Override
	public int intValue() {
		return (int) asDouble();
	}

	@Override
	public long longValue() {
		return (int) asDouble();
	}
	/**
	 * Returns true if the fraction can be represented with the 
	 * given denominator. 
	 * @param perferredMultiple
	 * @return
	 */
	public boolean isCompatibleDeonomiator(int testDenominator) {
		 
			if (testDenominator > divisor){
				return 	
						testDenominator / divisor > 1
						&&  testDenominator % divisor ==0;
			} else if (divisor > testDenominator){
				int diff = divisor / testDenominator;
				return 
						diff > 1
						&& divisor % testDenominator == 0
						&& diff % numerator == 0;
			} else {
				return true;
			}
	}
	
	public static long lowestCommonDenominator(long denom1, long denom2){
		if(denom1==denom2){
			return denom1;
		}
		if (denom1 == 0 || denom2 == 0){
			throw new ArithmeticException("Zero denominator:"+denom1+ " "+denom2);
		}
		long big = denom1 > denom2 ? denom1 : denom2;
		long small = denom1 < denom2 ? denom1 :denom2;
		for (int i = 1 ; i < Integer.MAX_VALUE ; i++){
			long trial = i*big;
			for (int j = 1 ; j < Integer.MAX_VALUE ; j++){
				if ((trial)==(j*small)){
					return trial;
				}
			}
		}
		throw new ArithmeticException("cannot find lcd "+denom1 + " " +denom2);
		
	}
	
	/**
	 * Returns a new Fraction representing this fraction, but with 
	 * a different denominator;
	 * Only call if the result of calling isCompatibleDeonomiator(..) is true,
	 * otherwise there risks a loss of information.
	 * @param newDenominator
	 * @return
	 */
	public Fraction setDenominator(int newDenominator) {
		if (newDenominator > divisor){
			int diff = newDenominator / divisor;
			int newNumerator = numerator * diff;
			return new Fraction(newNumerator,newDenominator);
		} else if (divisor > newDenominator){
			int diff = divisor / newDenominator;
			int newNumerator = numerator / diff;
			return new Fraction(newNumerator, newDenominator);
		} else {
			return this;
		}
	}
}
