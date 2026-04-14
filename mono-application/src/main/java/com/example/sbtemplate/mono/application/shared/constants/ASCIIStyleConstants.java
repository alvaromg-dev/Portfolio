package com.example.sbtemplate.mono.application.shared.constants;

public class ASCIIStyleConstants {

    // ==========================
    // Attributes
    // ==========================

    public static final String ESC      = "\u001B[";
    public static final String RESET    = ESC + "0m";

    // Foreground
    public static final String FG_BLACK   = ESC + "30m";
    public static final String FG_RED     = ESC + "31m";
    public static final String FG_GREEN   = ESC + "32m";
    public static final String FG_YELLOW  = ESC + "33m";
    public static final String FG_BLUE    = ESC + "34m";
    public static final String FG_MAGENTA = ESC + "35m";
    public static final String FG_CYAN    = ESC + "36m";
    public static final String FG_WHITE   = ESC + "37m";
    public static final String FG_GRAY    = ESC + "90m";

    // Background
    public static final String BG_BLACK   = ESC + "40m";
    public static final String BG_RED     = ESC + "41m";
    public static final String BG_GREEN   = ESC + "42m";
    public static final String BG_YELLOW  = ESC + "43m";
    public static final String BG_BLUE    = ESC + "44m";
    public static final String BG_MAGENTA = ESC + "45m";
    public static final String BG_CYAN    = ESC + "46m";
    public static final String BG_WHITE   = ESC + "47m";

    // Styles
    public static final String BOLD       = ESC + "1m";
    public static final String FAINT      = ESC + "2m";
    public static final String ITALIC     = ESC + "3m";
    public static final String UNDERLINE  = ESC + "4m";
    public static final String BLINK      = ESC + "5m";
    public static final String REVERSE    = ESC + "7m";
    public static final String HIDDEN     = ESC + "8m";

    // ==========================
    // Constructors
    // ==========================

    /**
     * Constants class; no instantiation.
     */
    private ASCIIStyleConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
