package com.example.sbtemplate.mono.infrastructure.in.jsf.service;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.telemetry.TelemetryVisitEntity;
import com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.telemetry.TelemetryVisitJpaRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private static final String VISITOR_COOKIE = "visitor_id";
    private static final int VISITOR_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365 * 2;
    private static final DateTimeFormatter DISPLAY_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Europe/Madrid"));
    private static final Pattern ANDROID_MODEL_PATTERN = Pattern.compile(";\\s*([^;\\)]+?)\\s+Build/");
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Madrid");

    private final TelemetryVisitJpaRepository telemetryVisitJpaRepository;

    @Transactional
    public void trackVisit(HttpServletRequest request, HttpServletResponse response, String pagePath, String pageQuery, String pageSource) {
        String visitorId = resolveVisitorId(request, response);
        String userAgent = truncate(header(request, "User-Agent"), 2000);
        String ipAddress = truncate(extractClientIp(request), 64);

        TelemetryVisitEntity entity = TelemetryVisitEntity.builder()
            .visitorId(visitorId)
            .ipAddress(ipAddress)
            .country(truncate(normalizeCountryName(firstHeader(request, "CF-IPCountry", "X-Vercel-IP-Country", "X-Country-Code", "X-AppEngine-Country")), 64))
            .region(truncate(firstHeader(request, "X-Vercel-IP-Country-Region", "X-AppEngine-Region"), 128))
            .city(truncate(firstHeader(request, "X-Vercel-IP-City", "X-AppEngine-City"), 128))
            .browser(truncate(detectBrowser(userAgent), 64))
            .deviceType(truncate(detectDevice(userAgent), 32))
            .operatingSystem(truncate(detectOs(userAgent), 64))
            .userAgent(userAgent)
            .source(truncate(nonEmpty(pageSource, header(request, "Referer")), 512))
            .path(truncate(nonEmpty(pagePath, "/"), 255))
            .query(truncate(pageQuery, 1000))
            .acceptLanguage(truncate(header(request, "Accept-Language"), 255))
            .build();

        telemetryVisitJpaRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<TelemetryVisitRow> getRecentVisits() {
        return telemetryVisitJpaRepository.findTop500ByOrderByVisitedAtDesc().stream()
            .map(entity -> new TelemetryVisitRow(
                entity.getId().toString(),
                detectDeviceName(entity.getUserAgent(), entity.getOperatingSystem(), entity.getDeviceType()),
                fromWhere(entity),
                nonEmpty(entity.getBrowser(), "-"),
                nonEmpty(entity.getDeviceType(), "-"),
                formatVisitedAt(entity.getVisitedAt())
            ))
            .toList();
    }

    @Transactional
    public void deleteVisit(String id) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Visit id is required");
        }
        UUID visitId;
        try {
            visitId = UUID.fromString(id.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid visit id");
        }
        telemetryVisitJpaRepository.deleteById(visitId);
    }

    @Transactional(readOnly = true)
    public TelemetryStats getVisitStats() {
        List<TelemetryVisitEntity> visits = telemetryVisitJpaRepository.findAll();
        ZonedDateTime now = ZonedDateTime.now(DEFAULT_ZONE);
        LocalDate today = now.toLocalDate();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        YearMonth currentMonth = YearMonth.from(today);
        int currentYear = today.getYear();

        int day = 0;
        int week = 0;
        int month = 0;
        int year = 0;

        for (TelemetryVisitEntity visit : visits) {
            LocalDate visitDate = parseVisitDate(visit.getVisitedAt());
            if (visitDate == null) {
                continue;
            }
            if (visitDate.equals(today)) {
                day++;
            }
            if (!visitDate.isBefore(weekStart) && !visitDate.isAfter(today)) {
                week++;
            }
            if (YearMonth.from(visitDate).equals(currentMonth)) {
                month++;
            }
            if (visitDate.getYear() == currentYear) {
                year++;
            }
        }

        return new TelemetryStats(day, week, month, year);
    }

    private static String fromWhere(TelemetryVisitEntity entity) {
        String country = nonEmpty(entity.getCountry(), "");
        String region = nonEmpty(entity.getRegion(), "");
        String city = nonEmpty(entity.getCity(), "");

        String geo = String.join(", ", List.of(country, region, city).stream()
            .filter(value -> !value.isBlank())
            .toList());
        if (!geo.isBlank()) {
            return geo;
        }

        String ip = nonEmpty(entity.getIpAddress(), "");
        if ("127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "Localhost";
        }

        return nonEmpty(entity.getIpAddress(), "-");
    }

    private static String formatVisitedAt(String visitedAt) {
        if (visitedAt == null || visitedAt.isBlank()) {
            return "-";
        }
        try {
            return DISPLAY_FORMATTER.format(Instant.parse(visitedAt));
        } catch (Exception ignored) {
            return visitedAt;
        }
    }

    private static LocalDate parseVisitDate(String visitedAt) {
        if (visitedAt == null || visitedAt.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(visitedAt).atZone(DEFAULT_ZONE).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveVisitorId(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (VISITOR_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        String newVisitorId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(VISITOR_COOKIE, newVisitorId);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(VISITOR_COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
        return newVisitorId;
    }

    private static String extractClientIp(HttpServletRequest request) {
        String forwardedFor = header(request, "X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isBlank()) {
                return first;
            }
        }
        String realIp = header(request, "X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static String detectBrowser(String userAgent) {
        if (isBlank(userAgent)) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("chrome/")) {
            return "Chrome";
        }
        if (ua.contains("safari/")) {
            return "Safari";
        }
        if (ua.contains("curl/")) {
            return "curl";
        }
        if (ua.contains("postmanruntime/")) {
            return "Postman";
        }
        return "Other";
    }

    private static String detectDevice(String userAgent) {
        if (isBlank(userAgent)) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("bot") || ua.contains("spider") || ua.contains("crawler")) {
            return "Bot";
        }
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "Tablet";
        }
        if (ua.contains("mobi") || ua.contains("iphone") || ua.contains("android")) {
            return "Mobile";
        }
        return "Desktop";
    }

    private static String detectOs(String userAgent) {
        if (isBlank(userAgent)) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    private static String detectDeviceName(String userAgent, String os, String deviceType) {
        if (isBlank(userAgent)) {
            return "Unknown device";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);

        if ("Bot".equalsIgnoreCase(deviceType)) {
            return "Bot";
        }
        if (ua.contains("iphone")) {
            return "iPhone";
        }
        if (ua.contains("ipad")) {
            return "iPad";
        }
        if (ua.contains("android")) {
            Matcher matcher = ANDROID_MODEL_PATTERN.matcher(userAgent);
            if (matcher.find()) {
                String model = matcher.group(1).trim();
                if (!model.isBlank() && !model.equalsIgnoreCase("Linux")) {
                    return model;
                }
            }
            return "Android device";
        }
        if (ua.contains("macintosh") || ua.contains("mac os x")) {
            return "Mac";
        }
        if (ua.contains("windows")) {
            return "Windows PC";
        }
        if (ua.contains("linux")) {
            return "Linux PC";
        }
        if (!isBlank(os)) {
            return os + " " + nonEmpty(deviceType, "").toLowerCase(Locale.ROOT);
        }
        return "Unknown device";
    }

    private static String normalizeCountryName(String countryRaw) {
        if (isBlank(countryRaw)) {
            return countryRaw;
        }
        String country = countryRaw.trim();
        if (country.length() == 2) {
            String display = new Locale("", country.toUpperCase(Locale.ROOT)).getDisplayCountry(new Locale("es", "ES"));
            if (!isBlank(display)) {
                return display;
            }
        }
        return country;
    }

    private static String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = header(request, name);
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    private static String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    public record TelemetryVisitRow(
        String id,
        String deviceName,
        String fromWhere,
        String browser,
        String device,
        String visitedAt
    ) {
    }

    public record TelemetryStats(
        int dayVisits,
        int weekVisits,
        int monthVisits,
        int yearVisits
    ) {
    }
}
