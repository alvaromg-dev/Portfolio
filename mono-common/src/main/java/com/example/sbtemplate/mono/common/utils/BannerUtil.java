package com.example.sbtemplate.mono.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for generating and printing decorative text banners with
 * service information.
 */
public class BannerUtil {

    // ==========================
    // Attributes
    // ==========================

    private StringBuilder sb;
    private String banner = """
        ┏━┓ ┏┓    ╺┳╸ ┏━╸ ┏┳┓ ┏━┓ ╻   ┏━┓ ╺┳╸ ┏━╸
        ┗━┓ ┣┻┓    ┃  ┣╸  ┃┃┃ ┣━┛ ┃   ┣━┫  ┃  ┣╸
        ┗━┛ ┗━┛    ╹  ┗━╸ ╹ ╹ ╹   ┗━╸ ╹ ╹  ╹  ┗━╸
        """;
    private Map<String, Object> info = new LinkedHashMap<>();

    // ==========================
    // Constructors
    // ==========================

    // ==========================
    // Methods
    // ==========================

    /**
     * Static factory method to create a new instance of BannerUtil.
     * @return
     */
    public static BannerUtil create() {
        return new BannerUtil();
    }

    /**
     * Prints the banner along with the added information lines to the standard output.
     *
     * @return the complete banner string
     */
    public String print() {

        this.reset();
        this.addInfo(info);
        this.endBanner();

        String sbBanner = sb.toString();
        System.out.println(sbBanner);
        return sbBanner;
    }

    /**
     * Resets the internal StringBuilder to its initial state with ANSI color codes.
     */
    private void reset() {
        sb = new StringBuilder()
            .append("\n")
            .append("\u001B[32m")
            .append(banner)
            .append("\n");
    }

    /**
     * Adds multiple lines of information to the banner.
     *
     * @param info A map containing label-value pairs to be added to the banner.
     */
    private void addInfo(Map<String, Object> info) {
        info.forEach(this::addInfoLine);
    }

    /**
     * Adds a line of information to the banner if the value is not null or blank.
     *
     * @param label The label for the information line.
     * @param value The value to be displayed.
     */
    private void addInfoLine(String label, Object value) {
        if (value == null) return;
        sb.append(label).append(value.toString()).append("\n");
    }

    /**
     * Ends the banner with ANSI reset code.
     */
    private void endBanner() {
        sb.append("\u001B[0m");
    }

    // ==========================
    // Setters
    // ==========================

    public BannerUtil name(Object serviceName) {
        this.info.put("Service Name: ", serviceName);
        return this;
    }

    public BannerUtil port(Object servicePort) {
        this.info.put("Service port: ", servicePort);
        return this;
    }

    public BannerUtil time(Object startupTime) {
        this.info.put("Startup time: ", startupTime);
        return this;
    }
}

/*
Example letters for the banner:
 ┏
┏━┓   ┏┓    ┏━╸
┣━┫   ┣┻┓   ┃
╹ ╹   ┗━┛   ┗━╸
╺┳┓   ┏━╸   ┏━╸
 ┃┃   ┣╸    ┣╸
╺┻┛   ┗━╸   ╹
┏━╸   ╻ ╻   ╻
┃╺┓   ┣━┫   ┃
┗━┛   ╹ ╹   ╹
 ┏┓   ╻┏    ╻
  ┃   ┣┻┓   ┃
┗━┛   ╹ ╹   ┗━╸
            ┏━┛
┏┳┓   ┏┓╻   ┏┓╻
┃┃┃   ┃┗┫   ┃┗┫
╹ ╹   ╹ ╹   ╹ ╹
┏━┓   ┏━┓   ┏━┓
┃ ┃   ┣━┛   ┃┓┃
┗━┛   ╹     ┗┻┛
┏━┓   ┏━┓   ╺┳╸
┣┳┛   ┗━┓    ┃
╹┗╸   ┗━┛    ╹
╻ ╻   ╻ ╻   ╻ ╻
┃ ┃   ┃┏┛   ┃╻┃
┗━┛   ┗┛    ┗┻┛
╻ ╻   ╻ ╻   ╺━┓
┏╋┛   ┗┳┛   ┏━┛
╹ ╹    ╹    ┗━╸
━━━━━━━━━━━━━━━
┏━┓   ╺┓    ┏━┓
┃┃┃    ┃    ┏━┛
┗━┛   ╺┻╸   ┗━╸
┏━┓   ╻ ╻   ┏━╸
╺━┫   ┗━┫   ┗━┓
┗━┛     ╹   ┗━┛
┏━┓   ┏━┓   ┏━┓
┣━┓     ┃   ┣━┫
┗━┛     ╹   ┗━┛
┏━┓
┗━┫
┗━┛
*/
