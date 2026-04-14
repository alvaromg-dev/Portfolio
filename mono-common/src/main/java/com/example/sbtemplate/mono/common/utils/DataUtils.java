package com.example.sbtemplate.mono.common.utils;

import java.net.URI;

public class DataUtils {

    // ==========================
    // Attributes
    // ==========================

    public static final String DNI_NIE_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    public static final String NIE_PREFIXES = "XYZ";

    // ==========================
    // Constructors
    // ==========================

    /** Utility class; no instantiation. */
    private DataUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    // Valid

    /**
     * Validates a DNI using practical rules.
     * @param dni
     * @return true if valid, false otherwise
     */
    public static boolean isValidDNI(String dni) {
        return validateDNI(dni) == 0;
    }

    /**
     * Validates a NIE using practical rules.
     * @param nie
     * @return true if valid, false otherwise
     */
    public static boolean isValidNIE(String nie) {
        return validateNIE(nie) == 0;
    }

    /**
     * Validates a NIF (DNI or NIE) using practical rules.
     * @param nif
     * @return true if valid, false otherwise
     */
    public static boolean isValidNIF(String nif) {
        return validateNIF(nif) == 0;
    }

    /**
     * Validates an email address (practical rules, RFC-friendly but not exhaustive).
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return validateEmail(email) == 0;
    }

    /**
     * Validates a phone number using practical rules.
     * @param url
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        return validateUrl(url) == 0;
    }

    /**
     * Validates an IBAN (ISO 13616) using the mod-97 algorithm. Spaces are ignored.
     * @param iban the IBAN string to validate
     * @return true if the IBAN is valid, false otherwise
     */
    public static boolean isValidIBAN(String iban) {
        return validateIBAN(iban) == 0;
    }

    // Validations

    /**
     * Validates an IBAN (ISO 13616) using the mod-97 algorithm. Spaces are ignored.
     * Valid formats
     * - ES91 2100 0418 4502 0005 1332
     * - ES91-2100-0418-4502-0005-1332
     * - ES9121000418450200051332
     *
     * Error codes:
     * 0 = Valid
     * 1 = Missing IBAN
     * 2 = Too short
     * 3 = Too long
     * 4 = Invalid characters
     * 5 = Invalid checksum
     *
     * @param iban the IBAN string to validate
     * @return true if the IBAN is valid, false otherwise
     */
    public static int validateIBAN(String iban) {

        // Present
        if (iban == null) return 1;

        // Normalize
        iban = iban.replace("-", ""); // No hyphen
        iban = iban.replace(" ", ""); // No space
        iban = iban.toUpperCase();

        // Length
        if (iban.length() < 15) return 2;
        if (iban.length() > 34) return 3;

        // Rearrange: move first 4 chars to end
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder(rearranged.length() * 2);

        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else if (Character.isLetter(c)) {
                numeric.append(c - 55); // A=10 ... Z=35
            } else {
                return 4;
            }
        }

        // Compute mod-97
        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            mod = (mod * 10 + (numeric.charAt(i) - '0')) % 97;
        }

        // Valid IBAN if mod-97 is 1
        return mod == 1 ? 0 : 5;
    }

    /**
     * Validates an email address (practical rules, RFC-friendly but not exhaustive).
     *
     * Error codes:
     *  0 = Valid
     *  1 = Missing email (null/empty/blank)
     *  2 = Length out of bounds (email > 254 or local part > 64)
     *  3 = Contains whitespace or control characters
     *  4 = Must contain exactly one '@'
     *  5 = Invalid local part (leading/trailing dot, consecutive dots, or invalid characters)
     *  6 = Invalid domain format (empty label, consecutive dots, invalid characters, or label > 63)
     *  7 = Invalid TLD (last label < 2 chars or not alphabetic)
     *
     * Notes:
     *  - Allowed local-part chars: A-Z a-z 0-9 and ! # $ % & ' * + / = ? ^ _ ` { | } ~ -
     *    Dot (.) allowed but not at edges and not repeated.
     *  - Domain uses letters, digits and hyphens; labels can't start/end with hyphen.
     *  - This validator targets ASCII emails (no IDN). For IDN, convert domain to Punycode first.
     * @param email the email address to validate
     * @return error code
     */
    @SuppressWarnings("java:S3776")
     public static int validateEmail(String email) {

        // 1) Present
        if (email == null || email.isBlank()) return 1;
        email = email.trim();

        // 2) Lengths (common practical limits)
        if (email.length() > 254) return 2;

        // 3) No whitespace/control chars
        for (int i = 0; i < email.length(); i++) {
            char c = email.charAt(i);
            if (Character.isISOControl(c) || Character.isWhitespace(c)) {
                return 3;
            }
        }

        // 4) Exactly one '@'
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            return 4;
        }

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        if (local.length() > 64) return 2;

        // 5) Validate local part
        // Allowed: A-Z a-z 0-9 ! # $ % & ' * + / = ? ^ _ ` { | } ~ - and .
        // Dot cannot be first/last and cannot be consecutive.
        if (local.startsWith(".") || local.endsWith(".")) return 5;
        if (local.contains("..")) return 5;
        for (int i = 0; i < local.length(); i++) {
            char c = local.charAt(i);
            boolean ok =
                (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') ||
                "!#$%&'*+/=?^_`{|}~-.".indexOf(c) >= 0;
            if (!ok) return 5;
        }

        // 6) Validate domain
        // Must be labels separated by '.', each 1..63 chars, chars [A-Za-z0-9-], not start/end with '-'
        if (domain.startsWith(".") || domain.endsWith(".") || domain.contains("..")) return 6;

        String[] labels = domain.split("\\.");
        if (labels.length < 2) return 6; // require at least one dot (host.tld)

        for (String label : labels) {
            if (label.isEmpty()) return 6;
            if (label.length() > 63) return 6;
            if (label.startsWith("-") || label.endsWith("-")) return 6;
            for (int i = 0; i < label.length(); i++) {
                char c = label.charAt(i);
                boolean ok =
                    (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') ||
                    (c == '-');
                if (!ok) return 6;
            }
        }

        // 7) Validate TLD (last label): >= 2 and alphabetic
        String tld = labels[labels.length - 1];
        if (tld.length() < 2) return 7;
        for (int i = 0; i < tld.length(); i++) {
            char c = tld.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                return 7;
            }
        }

        return 0; // valid
    }

    /**
     * Calculates the DNI control letter for an 8-digit number string.
     * @param numbers the 8-digit number string
     * @return the control letter for the DNI
     * @throws IllegalArgumentException if the input is not a valid 8-digit number string
     */
    public static char calculateDNILetter(String numbers) {

        // Validate the input: must be a non-null 8-digit number string
        if (numbers == null || !numbers.matches("\\d{8}"))
            throw new IllegalArgumentException("Input must be an 8-digit number string.");

        // Parse the number and calculate the index for the letter
        int num = Integer.parseInt(numbers);

        // Return the letter corresponding to the remainder of num divided by 23
        return DNI_NIE_LETTERS.charAt(num % 23);
    }

    /**
     * Validates a Spanish DNI.
     *
     * Error codes:
     *  0 = Valid
     *  1 = Missing DNI
     *  2 = Incorrect length
     *  3 = The first 8 characters are not digits
     *  4 = The last character is not a letter
     *  5 = The DNI is not correct (wrong letter and/or number)
     *
     * @param dni DNI to validate (e.g. "99999999R")
     * @return error code
     */
    public static int validateDNI(String dni) {

        // Check DNI is present
        if (dni == null) return 1; // 1 = Missing DNI

        // Trim whitespace
        dni = dni.trim().toUpperCase();

        // Check string not empty
        if (dni.isEmpty()) return 1; // 1 = Missing DNI

        // Check correct length: 8 digits + 1 letter = 9
        if (dni.length() != 9) return 2; // 2 = Incorrect length

        // Numeric part of the DNI
        String numberPart = dni.substring(0, 8);

        // Ensure the first 8 characters are digits
        if (!numberPart.matches("\\d{8}")) return 3; // 3 = First 8 characters are not digits

        // Letter of the DNI
        char letter = dni.charAt(8);

        // Ensure the last character is a letter
        if (!Character.isLetter(letter)) return 4; // 4 = The last character is not a letter

        // Parse numeric part
        int num = Integer.parseInt(numberPart);

        // Compute the expected control letter
        char expectedLetter = DNI_NIE_LETTERS.charAt(num % 23);

        // Compare provided letter with expected one
        if (letter != expectedLetter) return 5; // 5 = Invalid letter

        // All checks passed
        return 0; // 0 = Valid
    }

    /**
     * Validates a Spanish NIE.
     *
     * Error codes:
     *  0 = Valid
     *  1 = Missing NIE
     *  2 = Incorrect length
     *  3 = The first character is not X, Y or Z
     *  4 = The 7 middle characters are not digits
     *  5 = The last character is not a letter
     *  6 = The NIE is not correct (wrong control letter)
     *
     * @param nie NIE to validate (e.g. "X1234567L")
     * @return error code
     */
    public static int validateNIE(String nie) {

        // Check NIE is present
        if (nie == null) return 1; // 1 = Missing NIE

        // Trim whitespace
        nie = nie.trim().toUpperCase();

        if (nie.isEmpty()) return 1; // 1 = Missing NIE

        // Check correct length: 1 letter + 7 digits + 1 letter = 9
        if (nie.length() != 9) return 2; // 2 = Incorrect length

        // First must be X/Y/Z
        char first = nie.charAt(0);
        if (NIE_PREFIXES.indexOf(first) == -1) return 3; // 3 = The first character is not X, Y or Z

        // Middle 7 must be digits
        String mid = nie.substring(1, 8);
        if (!mid.matches("\\d{7}")) return 4; // 4 = The 7 middle characters are not digits

        // Last must be a letter
        char last = nie.charAt(8);
        if (!Character.isLetter(last)) return 5; // 5 = The last character is not a letter

        // Compute control letter
        int prefix = NIE_PREFIXES.indexOf(first);
        int num = Integer.parseInt(prefix + mid);
        char expected = DNI_NIE_LETTERS.charAt(num % 23);

        // Compare provided letter with expected one
        if (last != expected) return 6; // 6 = The NIE is not correct (wrong control letter)

        // All checks passed
        return 0; // 0 = Valid
    }

    /**
     * Validates a Spanish NIF (DNI or NIE).
     *
     * Error codes:
     *  0 = Valid
     *  1 = Missing NIF
     *  2 = Incorrect length
     *  3 = The first 8 characters are not digits (for DNI) or the first character is not X, Y or Z (for NIE)
     *  4 = The last character is not a letter
     *  5 = The NIF is not correct (wrong letter and/or number)
     *
     * @param nif NIF to validate (e.g. "99999999R" or "X1234567L")
     * @return error code
     */
    public static int validateNIF(String nif) {

        // Check NIF is present
        if (nif == null) return 1; // 1 = Missing NIF

        // Trim whitespace
        nif = nif.trim().toUpperCase();

        // Check string not empty
        if (nif.isEmpty()) return 1; // 1 = Missing NIF

        // Check correct length: 8 digits + 1 letter = 9
        if (nif.length() != 9) return 2; // 2 = Incorrect length

        // Numeric part of the NIF
        String numberPart = nif.substring(0, 8);

        // Ensure the first 8 characters are digits
        if (!numberPart.matches("\\d{8}")) return 3; // 3 = First 8 characters are not digits

        // Letter of the NIF
        char letter = nif.charAt(8);

        // Ensure the last character is a letter
        if (!Character.isLetter(letter)) return 4; // 4 = The last character is not a letter

        // Parse numeric part
        int num = Integer.parseInt(numberPart);

        // Compute the expected control letter
        char expectedLetter = DNI_NIE_LETTERS.charAt(num % 23);

        // Compare provided letter with expected one
        if (letter != expectedLetter) return 5; // 5 = Invalid letter

        // All checks passed
        return 0; // 0 = Valid
    }

    /**
     * Validates a URL by attempting to parse it and requiring an http/https scheme and a host.
     * Error codes:
     *  0 = Valid
     *  1 = Missing URL
     *  2 = Malformed URL
     *  3 = Unsupported scheme or missing host
     * @param url the URL string to validate
     * @return true if the URL is valid, false otherwise
     */
    public static int validateUrl(String url) {

        // Check if the URL is null
        if (url == null) return 1;

        try {
            // Attempt to create a URI from the URL string
            URI uri = URI.create(url.trim());

            // Check if the scheme is http or https and that a host is present
            String scheme = uri.getScheme();

            // Validate the scheme and host
            if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) && uri.getHost() != null) {
                return 0;
            } else {
                return 3;
            }

        } catch (IllegalArgumentException ex) {
            return 2;
        }
    }
}
