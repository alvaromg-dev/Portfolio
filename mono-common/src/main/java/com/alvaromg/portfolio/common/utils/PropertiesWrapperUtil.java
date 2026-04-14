package com.alvaromg.portfolio.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class PropertiesWrapperUtil extends Properties {

    private static final long serialVersionUID = 1892981;

    // ==========================
    // Constructors
    // ==========================

    /**
     * Constructs a PropertiesWrapper and loads properties from the provided file.
     *
     * @param file the properties file to read.
     * @throws IOException if an error occurs while reading the properties file.
     * @throws IllegalArgumentException if the file is null.
     */
    public PropertiesWrapperUtil(File file) throws IOException {

        // Validate that the file is not null
        Objects.requireNonNull(file, "file must not be null");

        // Read properties from the file
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            load(reader);
        } catch (IOException e) {
            throw new IOException("Error loading properties from file: " + file, e);
        }
    }

    // ==========================
    // Methods
    // ==========================

    public String getString(String key) {
        return getRequiredValue(key);
    }

    public byte getByte(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("byte", key, value), e);
        }
    }

    public short getShort(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("short", key, value), e);
        }
    }

    public int getInt(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("int", key, value), e);
        }
    }

    public long getLong(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("long", key, value), e);
        }
    }

    public float getFloat(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("float", key, value), e);
        }
    }

    public double getDouble(String key) {
        String value = getRequiredValue(key).trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMsg("double", key, value), e);
        }
    }

    public boolean getBoolean(String key) {
        String value = getRequiredValue(key).trim();
        if ("true".equalsIgnoreCase(value))  return true;
        if ("false".equalsIgnoreCase(value)) return false;
        if ("1".equalsIgnoreCase(value))     return true;
        if ("0".equalsIgnoreCase(value))     return false;
        if ("on".equalsIgnoreCase(value))    return true;
        if ("off".equalsIgnoreCase(value))   return false;
        if ("yes".equalsIgnoreCase(value))   return true;
        if ("no".equalsIgnoreCase(value))    return false;
        throw new IllegalArgumentException(invalidMsg("boolean", key, value));
    }

    // ==========================
    // Private helpers
    // ==========================

    private String getRequiredValue(String key) {
        Objects.requireNonNull(key, "key must not be null");
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return value;
    }

    private static String invalidMsg(String expectedType, String key, String actual) {
        return "Invalid " + expectedType + " for key '" + key + "': '" + actual + "'";
    }
}
