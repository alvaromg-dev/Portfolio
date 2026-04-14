package com.example.sbtemplate.mono.application.roles.constants;

import java.util.Set;

public class RolesConstants {

    // ==========================
    // Attributes
    // ==========================

    // only the codes, not the IDs
    public static final String CV_EDITOR = "CV_EDITOR";
    public static final String ADMIN = "ADMIN";

    // Backward-compatible aliases for legacy references.
    @Deprecated
    public static final String USER = CV_EDITOR;
    @Deprecated
    public static final String ADMINISTRATOR = ADMIN;
    @Deprecated
    public static final String DEVELOPER = ADMIN;

    // Role groups
    public static final Set<String> ALL = Set.of(CV_EDITOR, ADMIN);
    public static final Set<String> ADMINISTRATIVE = Set.of(ADMIN);
    public static final Set<String> SUPER_ADMINISTRATIVE = Set.of(ADMIN);
    public static final Set<String> REQUIRED = Set.of(CV_EDITOR);

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
