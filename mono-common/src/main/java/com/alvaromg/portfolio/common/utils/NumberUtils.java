package com.alvaromg.portfolio.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {

    // ==========================
    // Attributes
    // ==========================

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private NumberUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Generates a random integer within [min, max], both inclusive.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer between min and max
     * @throws IllegalArgumentException if min is greater than max
     */
    public static int randomInt(int min, int max) {

        // Validate inputs
        if (min > max) throw new IllegalArgumentException("min must be <= max");

        // Handle the case where min and max are equal
        if (min == max) return min;

        // Calculate range
        long range = (long) max - (long) min + 1L;
        long offset = ThreadLocalRandom.current().nextLong(range);

        // Return the random int
        return (int) (min + offset);
    }

    /**
     * Generates a random double within [min, max], inclusive of both bounds.
     * NOTE: If max == Double.MAX_VALUE, the upper bound becomes effectively exclusive
     * because Math.nextUp(max) is Infinity.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random double between min and max
     * @throws IllegalArgumentException if min is greater than max
     */
    public static double randomDouble(double min, double max) {

        // Validate that min is less than or equal to max
        if (min > max) throw new IllegalArgumentException("min must be <= max");

        // Handle the case where min and max are equal
        if (min == max) return min;

        // Validate inputs
        if (!Double.isFinite(min) || !Double.isFinite(max))
            throw new IllegalArgumentException("Both min and max must be finite numbers");

        // Use Math.nextUp to ensure the upper bound is exclusive
        double upperExclusive = Math.nextUp(max);

        // Generate a random double in the range [min, upperExclusive)
        return ThreadLocalRandom.current().nextDouble(min, upperExclusive);
    }

    /**
     * Returns a random boolean
     * @return random boolean
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Rounds a double to the specified number of decimal places.
     * 
     * @param value  the double value to round
     * @param places the number of decimal places to round to
     * @return the rounded double value
     * @throws IllegalArgumentException if places is negative
     */
    public static double round(double value, int places) {

        // Validate the number of decimal places
        if (places < 0)
            throw new IllegalArgumentException("Number of decimal places must be non-negative");

        // preserve NaN/±Inf as is
        if (!Double.isFinite(value)) return value;

        // Round the value
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Rounds a float to the specified number of decimal places.
     *
     * @param value  the float value to round
     * @param places the number of decimal places to round to
     * @return the rounded float value
     * @throws IllegalArgumentException if places is negative
     */
    public static float round(float value, int places) {

        // Validate the number of decimal places
        if (places < 0)
            throw new IllegalArgumentException("Number of decimal places must be non-negative");

        // preserve NaN/±Inf as is
        if (!Float.isFinite(value)) return value;

        // Round the value
        return new BigDecimal(Float.toString(value)).setScale(places, RoundingMode.HALF_UP).floatValue();
    }

    /**
     * Rounds a BigDecimal to the specified number of decimal places.
     *
     * @param value  the BigDecimal value to round
     * @param places the number of decimal places to round to
     * @return the rounded BigDecimal value
     * @throws IllegalArgumentException if places is negative
     */
    public static BigDecimal round(BigDecimal value, int places) {

        // Validate inputs
        Objects.requireNonNull(value, "BigDecimal must not be null");

        // Validate the number of decimal places
        if (places < 0)
            throw new IllegalArgumentException("Number of decimal places must be non-negative");

        // Use BigDecimal.setScale to round the value
        return value.setScale(places, RoundingMode.HALF_UP);
    }

    /**
     * Validates if the given string is a valid byte.
     *
     * @param number the string to validate
     * @return true if the string is a valid byte, false otherwise
     */
    public static boolean isValidByte(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid byte
            Byte.parseByte(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid short.
     *
     * @param number the string to validate
     * @return true if the string is a valid short, false otherwise
     */
    public static boolean isValidShort(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid short
            Short.parseShort(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid integer.
     *
     * @param number the string to validate
     * @return true if the string is a valid integer, false otherwise
     */
    public static boolean isValidInteger(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid integer
            Integer.parseInt(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid long.
     *
     * @param number the string to validate
     * @return true if the string is a valid long, false otherwise
     */
    public static boolean isValidLong(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid long
            Long.parseLong(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid BigInteger.
     *
     * @param number the string to validate
     * @return true if the string is a valid BigInteger, false otherwise
     */
    public static boolean isValidBigInteger(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid BigInteger
            new BigInteger(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid float.
     *
     * @param number the string to validate
     * @return true if the string is a valid float, false otherwise
     */
    public static boolean isValidFloat(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid float
            float f = Float.parseFloat(number.trim());
            return Float.isFinite(f); // reject NaN/Infinity
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid double.
     *
     * @param number the string to validate
     * @return true if the string is a valid double, false otherwise
     */
    public static boolean isValidDouble(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid double
            double d = Double.parseDouble(number.trim());
            return Double.isFinite(d); // reject NaN/Infinity
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the given string is a valid BigDecimal.
     *
     * @param number the string to validate
     * @return true if the string is a valid BigDecimal, false otherwise
     */
    public static boolean isValidBigDecimal(String number) {
        if (ObjectUtils.isNullOrEmpty(number)) return false; // Handle null case
        try {
            // Validate that the input is not null and is a valid BigDecimal
            new BigDecimal(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
