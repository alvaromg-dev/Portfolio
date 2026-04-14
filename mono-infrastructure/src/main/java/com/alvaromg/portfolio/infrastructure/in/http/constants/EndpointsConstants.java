package com.alvaromg.portfolio.infrastructure.in.http.constants;

public class EndpointsConstants {

    private EndpointsConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BASE = "/api/mono";

    public static final String SWAGGER = BASE + "/swagger";
    public static final String SWAGGER_INDEX = SWAGGER + "/index.html";
    public static final String SWAGGER_DOCS = SWAGGER + "/api-docs";

    public static final String USERS = BASE + "/users";
    public static final String USERS_CREATE = USERS + "/create";
    public static final String USERS_READ = USERS + "/read";
    public static final String USERS_READ_AVATAR = USERS + "/read/avatar";
    public static final String USERS_UPDATE_AVATAR = USERS + "/update/avatar";
    public static final String USERS_UPDATE_NAME = USERS + "/update/name";
    public static final String USERS_UPDATE_EMAIL = USERS + "/update/email";
    public static final String USERS_UPDATE_PASSWORD = USERS + "/update/password";
    public static final String USERS_DELETE = USERS + "/delete";

    public static final String AUTH = BASE + "/auth";

    public static final String AUTH_EMAIL = AUTH + "/email";
    public static final String AUTH_REFRESH = AUTH + "/refresh";
    public static final String AUTH_INVALIDATE = AUTH + "/invalidate";
    public static final String AUTH_VALIDATE = AUTH + "/validate";
    public static final String PORTFOLIO_SAVE = BASE + "/portfolio";
    public static final String TELEMETRY_TRACK = BASE + "/telemetry/track";
    public static final String TELEMETRY_DELETE = BASE + "/telemetry/delete";
    public static final String TELEMETRY_PAGE = "/telemetry";
    public static final String LOGIN_PAGE = "/login";
    public static final String USERS_PAGE = "/users";
    public static final String USERS_CREATE_PAGE = USERS_PAGE + "/create";
    public static final String USERS_UPDATE_PAGE = USERS_PAGE + "/update";
    public static final String USERS_DELETE_PAGE = USERS_PAGE + "/delete";

    private static final String[] PUBLIC_PATHS = {
        SWAGGER + "/**",
        AUTH_EMAIL,
        AUTH_REFRESH,
        AUTH_VALIDATE,
        TELEMETRY_TRACK,
        "/",
        LOGIN_PAGE,
        "/portfolio.css",
        "/portfolio.js",
        "/telemetry.js",
        "/favicon.svg",
        "/Onest-VariableFont_wght.woff2",
        "/Alvaro_Martin_Granados.webp"
    };

    public static String[] getPublicPaths() {
        return PUBLIC_PATHS.clone();
    }
}
