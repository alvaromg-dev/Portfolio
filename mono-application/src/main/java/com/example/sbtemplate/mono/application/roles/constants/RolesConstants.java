package com.example.sbtemplate.mono.application.roles.constants;

import java.util.Set;

public class RolesConstants {

    // ==========================
    // Attributes
    // ==========================

    // only the codes, not the IDs
    public static final String USER = "USER";
    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String DEVELOPER = "DEVELOPER";

    // Role groups
    public static final Set<String> ALL = Set.of(USER, ADMINISTRATOR, DEVELOPER);
    public static final Set<String> ADMINISTRATIVE = Set.of(ADMINISTRATOR, DEVELOPER);
    public static final Set<String> SUPER_ADMINISTRATIVE = Set.of(DEVELOPER);
    public static final Set<String> REQUIRED = Set.of(USER);

    // ==========================
    // Constructors
    // ==========================

    /**
     * Constants class; no instantiation.
     */
    private RolesConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
