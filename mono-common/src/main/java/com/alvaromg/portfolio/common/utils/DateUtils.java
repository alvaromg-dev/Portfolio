package com.alvaromg.portfolio.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * Utility methods for working with dates and times using {@link LocalDateTime}.
 * <p>
 * This class favors the modern Java Time API (java.time.*). For
 * interoperability with legacy
 * {@link java.util.Date}, use {@link #toLocalDateTime(Date)} and
 * {@link #toDate(LocalDateTime)}.
 * All methods are static and stateless (thread-safe). Unless specified
 * otherwise, the system
 * default time zone is used when converting between Date and LocalDateTime.
 * </p>
 */
public class DateUtils {

    // ==========================
    // Attributes
    // ==========================

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private DateUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Calculates age in full years from a birth date to today.
     *
     * @param birthDate the birth date (not null)
     * @param today today
     * @return the age in full years
     * @throws IllegalArgumentException if {@code birthDate} or {@code birthDate} are null
     */
    public static int calculateAge(LocalDate birthDate, LocalDate today) {

        // Validate variables
        Objects.requireNonNull(birthDate, "birthDate cannot be null");
        Objects.requireNonNull(today, "today cannot be null");

        // Calculate and return the age
        return Period.between(birthDate, today).getYears();
    }

    /**
     * Calculates the percentage of elapsed time between two instants (start inclusive, end exclusive)
     * based on the provided current instant. Rounded to two decimals.
     *
     * @param startDateTime start of interval (not null)
     * @param current current time (not null)
     * @param endDateTime end of interval (not null)
     * @return percentage in range [0.0, 100.0]
     * @throws IllegalArgumentException if any argument is null or start is not strictly before end
     */
    public static double percentageElapsed(
        LocalDateTime startDateTime,
        LocalDateTime current,
        LocalDateTime endDateTime
    ) {
        // Validate inputs
        Objects.requireNonNull(startDateTime, "startDateTime cannot be null");
        Objects.requireNonNull(current, "current cannot be null");
        Objects.requireNonNull(endDateTime, "endDateTime cannot be null");

        // Validate interval
        if (!startDateTime.isBefore(endDateTime))
            throw new IllegalArgumentException("startDateTime must be strictly before endDateTime");

        // Ensure start is before end
        if (current.isBefore(startDateTime)) return 0.0; // now <= start
        if (!current.isBefore(endDateTime)) return 100.0; // now >= end

        // Calculate elapsed percentage
        double totalMillis = Duration.between(startDateTime, endDateTime).toMillis();
        double elapsedMillis = Duration.between(startDateTime, current).toMillis();
        double pct = (elapsedMillis * 100.0) / totalMillis;

        // Round to two decimal places
        return BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Formats a {@link LocalDateTime} as "dd/MM/yyyy".
     * If you have a {@link Date}, first convert with
     * {@link #toLocalDateTime(Date)}.
     *
     * @param dateTime the date-time to format (not null)
     * @return formatted string in pattern "dd/MM/yyyy"
     * @throws IllegalArgumentException if {@code dateTime} is null
     */
    public static String formatDateToString(LocalDateTime dateTime) {

        // Validate variables
        Objects.requireNonNull(dateTime, "dateTime cannot be null");

        // Format and return the date
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Formats a {@link LocalDateTime} using a custom pattern.
     * If you have a {@link Date}, first convert with
     * {@link #toLocalDateTime(Date)}.
     *
     * @param dateTime the date-time to format (not null)
     * @param pattern  the {@link DateTimeFormatter} pattern to use (not null/empty)
     * @return formatted string according to the pattern
     * @throws IllegalArgumentException if {@code dateTime} is null, or pattern is
     *                                  null/empty
     */
    public static String formatLocalDateTimeToString(LocalDateTime dateTime, String pattern) {

        // Validate variables
        Objects.requireNonNull(dateTime, "dateTime cannot be null");
        Objects.requireNonNull(pattern, "pattern cannot be null");

        // Format date to string
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Converts a {@link LocalDateTime} to a legacy {@link Date} using the system
     * default zone.
     *
     * @param dateTime the value to convert (not null)
     * @return the corresponding {@link Date}
     * @throws IllegalArgumentException if {@code dateTime} is null
     */
    public static Date toDate(LocalDateTime dateTime) {

        // Validate inputs
        Objects.requireNonNull(dateTime, "dateTime cannot be null");

        // LocalDateTime to Instant
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Instant to Date
        return Date.from(instant);
    }

    /**
     * Converts a legacy {@link Date} to {@link LocalDateTime} using the system
     * default zone.
     *
     * @param date the value to convert (not null)
     * @return the corresponding {@link LocalDateTime}
     * @throws IllegalArgumentException if {@code date} is null
     */
    public static LocalDateTime toLocalDateTime(Date date) {

        // Validate inputs
        Objects.requireNonNull(date, "date cannot be null");

        // Date to LocalDateTime
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
