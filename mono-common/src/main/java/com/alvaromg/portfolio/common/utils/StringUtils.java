package com.alvaromg.portfolio.common.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility methods for working with strings.
 * <p>
 * This class provides various static methods for string manipulation, including:
 * - String capitalization
 * - Whitespace cleaning
 * - Joining strings with a separator
 * ...
 * </p>
 */
public class StringUtils {

    // ==========================
    // Attributes
    // ==========================

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private StringUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Capitalizes the first character of the input string.
     * If the string is null or empty, it is returned as-is.
     *
     * @param text the string to capitalize
     * @return the capitalized string, or the original value if null/empty
     */
    public static String capitalize(String text) {

        // Check if the input is null or empty
        if (ObjectUtils.isNullOrEmpty(text)) return text;

        // Convert the first character to uppercase
        int firstCp = text.codePointAt(0);
        int titleCp = Character.toTitleCase(firstCp);
        int len = Character.charCount(firstCp);

        // Capitalize the first character and return the modified string
        return new StringBuilder()
            .appendCodePoint(titleCp)
            .append(text.substring(len))
            .toString();
    }

    /**
     * Cleans whitespace from the input string by trimming leading/trailing spaces
     * and replacing multiple spaces with a single space.
     * Returns {@code null} if the input is {@code null}.
     *
     * @param text the string to clean
     * @return the cleaned string, or {@code null} if input is {@code null}
     */
    public static String cleanWhitespaces(String text) {

        // Check if the input is null
        if (text == null) return null;

        // Convert the input to a string
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Joins a list of objects with a separator, optionally cleaning whitespace of each element via {@link #cleanWhitespaces(Object)}.
     * {@code null} elements are skipped. If separator is {@code null}, an empty string is used.
     * @param separator the separator to use between elements, or {@code null} for no separator
     * @param cleanWhitespaces whether to clean whitespace from each element
     * @param strings the list of objects to join
     * @return the joined string, or an empty string if the input list is {@code null}
     */
    public static String concat(String separator, boolean cleanWhitespaces, List<?> strings) {

        // Holds the parts to join
        List<String> parts = new ArrayList<>();

        // If input is null, return empty string
        if (ObjectUtils.isNullOrEmpty(strings)) return "";

        // Iterate over the input list, cleaning whitespace if needed
        for (Object obj : strings) {
            if (obj == null) continue;
            String str = obj.toString();
            parts.add(cleanWhitespaces ? cleanWhitespaces(str) : str);
        }

        // Join the parts with the specified separator
        separator = (separator == null ? "" : separator);

        // Join all parts into a single string
        return String.join(separator, parts);
    }

    /**
     * Removes diacritical marks (accents) from the given text using Unicode normal form.
     * Returns {@code null} if input is {@code null}.
     * @param text the input string
     * @return the accent-free string, or {@code null} if input is {@code null}
     * <p>
     * Example: "café" becomes "cafe", "jalapeño" becomes "jalapeno".
     * </p>
     */
    public static String removeAccents(String text) {

        // Check for null input
        if (text == null) return null;

        // normalize the string to decompose characters with diacritics
        text = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Remove all diacritical marks (Unicode category Mn)
        text = Pattern.compile("\\p{M}+").matcher(text).replaceAll("");

        // Remove all combining diacritical marks (Unicode category Mn)
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Normalizes a text by: removing accents, cleaning white spaces and lowercasing.
     * Returns {@code null} if input is {@code null}.
     * @param text the input string to normalize
     * @return the normalized string
     */
    public static String normalize(String text) {

        // Remove accents
        text = removeAccents(text);

        // Remove extra spaces
        text = cleanWhitespaces(text);

        // If the text is null return null
        if (text == null) return null;

        // Convert to lowercase and return
        return text.toLowerCase(Locale.ROOT);
    }

    /**
     * Counts the number of Unicode letters in the given string.
     * Non-letter characters are ignored. Returns 0 for null or empty strings.
     * @param text the string to analyze
     * @return the number of letters
     */
    public static int countLetters(String text) {

        // Return 0 if the text is null or empty
        if (ObjectUtils.isNullOrEmpty(text)) return 0;

        // Counter for letters
        int count = 0;

        // Iterate over each character in the string
        for (char c : text.toCharArray())
            // Increment count if the character is a letter
            if (Character.isLetter(c)) count++;

        // Return total letter count
        return count;
    }

    /**
     * Counts the number of words in the given string.
     * Words are separated by whitespace. Returns 0 for null or empty strings.
     * @param text the string to analyze
     * @return the number of words
     */
    public static int countWords(String text) {

        // Return 0 if the text is null or empty
        if (ObjectUtils.isNullOrEmpty(text)) return 0;

        // Remove leading/trailing spaces and split by one or more spaces
        String[] words = text.trim().split("\\s+");

        // Return the number of words found
        return words.length;
    }

    /**
     * Counts the number of lines in the given string.
     * Lines are separated by \n or \r\n. Returns 0 for null or empty strings.
     * @param text the string to analyze
     * @return the number of lines
     */
    public static int countLines(String text) {

        // Return 0 if the text is null or empty
        if (ObjectUtils.isNullOrEmpty(text)) return 0;

        // Split the string using \R (matches any line break)
        String[] lines = text.split("\\R");

        // Return the number of lines found
        return lines.length;
    }
}
