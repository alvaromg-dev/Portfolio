package com.alvaromg.portfolio.common.utils;

import java.io.File;
import java.io.IOException;

public class StaticPropertiesWrapperUtil {

    // ==========================
    // Attributes
    // ==========================

    private static PropertiesWrapperUtil pw = null;

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private StaticPropertiesWrapperUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    public static void load(File file) throws IOException {
        pw = new PropertiesWrapperUtil(file);
    }

    public static String getString(String key) {
        return pw.getString(key);
    }

    public static byte getByte(String key) {
        return pw.getByte(key);
    }

    public static short getShort(String key) {
        return pw.getShort(key);
    }

    public static int getInt(String key) {
        return pw.getInt(key);
    }

    public static long getLong(String key) {
        return pw.getLong(key);
    }

    public static float getFloat(String key) {
        return pw.getFloat(key);
    }

    public static double getDouble(String key) {
        return pw.getDouble(key);
    }

    public static boolean getBoolean(String key) {
        return pw.getBoolean(key);
    }
}
