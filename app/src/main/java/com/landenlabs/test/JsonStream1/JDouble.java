package com.landenlabs.test.JsonStream1;



//Region [Custom JDouble class] ----------------------------------------------------------------

/**
 * Custom Json JDouble class to replace Double to avoid reallocating everytime Double needed.
 * Assumes recipient will immediately extract the double and discard the JDouble.
 */
public class JDouble  extends Number {

    public final static JDouble JDOUBLE = new JDouble();

    public double dValue;

    public JDouble set(String str) {
        dValue = stringToDouble(str);
        return this;
    }

    @Override
    public int intValue() {
        assert (false);
        return 0;
    }

    @Override
    public long longValue() {
        assert (false);
        return 0;
    }

    @Override
    public float floatValue() {
        assert (false);
        return 0;
    }

    @Override
    public double doubleValue() {
        return dValue;
    }

    public static double stringToDouble(String str) {
        // Handle negative numbers
        int sign = 1;
        int startAt = 0;
        if (str.charAt(0) == '-') {
            sign = -1;
            startAt = 1;
        }

        double integerPart = 0;
        double decimalPart = 0;
        boolean isDecimalPointFound = false;
        double decimalDivisor = 1;

        // Iterate through each character in the string
        for (int i = startAt; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '.') {
                isDecimalPointFound = true;
            } else /* if (currentChar >= '0' && currentChar <= '9') */ {
                if (isDecimalPointFound) {
                    decimalPart = decimalPart * 10 + (currentChar - '0');
                    decimalDivisor *= 10;
                } else {
                    integerPart = integerPart * 10 + (currentChar - '0');
                }
            }
        }

        double finalResult = integerPart + (decimalPart / decimalDivisor);
        return sign * finalResult;
    }

}
