package com.example.sbtemplate.mono.application.shared.constants;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@SuppressWarnings({"java:S5998"})
public class PatternConstants {

    // ==========================
    // Attributes
    // ==========================

    public static final String EMAIL_PATTERN = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
    public static final Pattern COMPILED_EMAIL_PATTERN = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);

    public static final String NAME_PATTERN = "[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s'-]+";
    public static final Pattern COMPILED_NAME_PATTERN = Pattern.compile(NAME_PATTERN);

    public static final String PHONE_PATTERN = "^\\+?\\d(?:[\\s-]?\\d)+$";
    public static final Pattern COMPILED_PHONE_PATTERN = Pattern.compile(PHONE_PATTERN);

    public static final String LAST_NAME_PATTERN = NAME_PATTERN;
    public static final Pattern COMPILED_LAST_NAME_PATTERN = Pattern.compile(LAST_NAME_PATTERN);

    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$";
    public static final Pattern COMPILED_PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN);

    public static final DateTimeFormatter DATE_FORMAT_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_FORMAT_DDMMYYYY_HHMMSS = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");

    public static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    public static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator(); // UNIX = "\n" Windows = "\r\n"

    // ==========================
    // Constructors
    // ==========================

    /**
     * Constants class; no instantiation.
     */
    private PatternConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
