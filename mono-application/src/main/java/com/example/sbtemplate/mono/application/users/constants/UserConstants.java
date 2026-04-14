package com.example.sbtemplate.mono.application.users.constants;

import java.util.regex.Pattern;

import com.example.sbtemplate.mono.application.shared.constants.PatternConstants;

public class UserConstants {

    // ==========================
    // Attributes
    // ==========================

    public static final String EMAIL_PATTERN = PatternConstants.EMAIL_PATTERN;
    public static final Pattern COMPILED_EMAIL_PATTERN = PatternConstants.COMPILED_EMAIL_PATTERN;
    public static final String NAME_PATTERN = PatternConstants.NAME_PATTERN;
    public static final Pattern COMPILED_NAME_PATTERN = PatternConstants.COMPILED_NAME_PATTERN;
    public static final String PHONE_PATTERN = PatternConstants.PHONE_PATTERN;
    public static final Pattern COMPILED_PHONE_PATTERN = PatternConstants.COMPILED_PHONE_PATTERN;
    public static final String PASSWORD_PATTERN = PatternConstants.PASSWORD_PATTERN;
    public static final Pattern COMPILED_PASSWORD_PATTERN = PatternConstants.COMPILED_PASSWORD_PATTERN;

    // ==========================
    // Constructors
    // ==========================

    /**
     * Constants class; no instantiation.
     */
    private UserConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
