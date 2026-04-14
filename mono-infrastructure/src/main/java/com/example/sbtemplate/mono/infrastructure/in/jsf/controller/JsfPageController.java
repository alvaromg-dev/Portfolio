package com.example.sbtemplate.mono.infrastructure.in.jsf.controller;

import com.example.sbtemplate.mono.infrastructure.in.http.constants.EndpointsConstants;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.CvData;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.TelemetryTrackRequest;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.CvService;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.TelemetryService;
import com.example.sbtemplate.mono.infrastructure.in.jsf.view.PortfolioView;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class JsfPageController {

    private final PortfolioView portfolioView;
    private final CvService cvService;
    private final TelemetryService telemetryService;

    public JsfPageController(PortfolioView portfolioView, CvService cvService, TelemetryService telemetryService) {
        this.portfolioView = portfolioView;
        this.cvService = cvService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/")
    public String index(
        @RequestParam(name = "lang", required = false) String languageCode,
        Model model
    ) {
        portfolioView.load(languageCode);
        model.addAttribute("cv", portfolioView);
        return "portfolio";
    }

    @GetMapping(EndpointsConstants.TELEMETRY_PAGE)
    public String telemetry(
        @RequestParam(name = "lang", required = false) String languageCode,
        Model model
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        model.addAttribute("pageTitle", "en".equals(lang) ? "Telemetry" : "Telemetria");
        model.addAttribute("telemetryTitle", "en".equals(lang) ? "Visitor Telemetry" : "Telemetria de visitas");
        model.addAttribute("telemetryDeviceNameLabel", "en".equals(lang) ? "Device name/model" : "Dispositivo (nombre/modelo)");
        model.addAttribute("telemetryFromLabel", "en".equals(lang) ? "From" : "Desde donde");
        model.addAttribute("telemetryBrowserLabel", "en".equals(lang) ? "Browser" : "Navegador");
        model.addAttribute("telemetryDeviceLabel", "en".equals(lang) ? "Device type" : "Tipo de dispositivo");
        model.addAttribute("telemetryTimeLabel", "en".equals(lang) ? "Time" : "Hora");
        model.addAttribute("telemetryActionsLabel", "en".equals(lang) ? "Actions" : "Acciones");
        model.addAttribute("telemetryDeleteLabel", "en".equals(lang) ? "Delete" : "Eliminar");
        model.addAttribute("telemetryDeleteConfirm", "en".equals(lang) ? "Delete this visit?" : "¿Eliminar esta visita?");
        model.addAttribute("telemetryBackLabel", "en".equals(lang) ? "Back to portfolio" : "Volver al portfolio");
        model.addAttribute("telemetryStatsDayLabel", "en".equals(lang) ? "Today" : "Hoy");
        model.addAttribute("telemetryStatsWeekLabel", "en".equals(lang) ? "This week" : "Esta semana");
        model.addAttribute("telemetryStatsMonthLabel", "en".equals(lang) ? "This month" : "Este mes");
        model.addAttribute("telemetryStatsYearLabel", "en".equals(lang) ? "This year" : "Este año");
        model.addAttribute("telemetryLang", lang);
        model.addAttribute("telemetryRows", telemetryService.getRecentVisits());
        model.addAttribute("telemetryStats", telemetryService.getVisitStats());
        return "telemetry";
    }

    @PostMapping(EndpointsConstants.PORTFOLIO_SAVE)
    @ResponseBody
    public Map<String, String> save(
        @RequestParam(name = "lang", required = false) String languageCode,
        @RequestBody(required = false) CvData cvData
    ) {
        String savedLanguage = cvService.saveCvData(languageCode, cvData);
        return Map.of("status", "ok", "language", savedLanguage);
    }

    @PostMapping(EndpointsConstants.TELEMETRY_TRACK)
    @ResponseBody
    public Map<String, String> telemetryTrack(
        @RequestBody(required = false) TelemetryTrackRequest trackRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String path = trackRequest == null ? "/" : trackRequest.path();
        String query = trackRequest == null ? null : trackRequest.query();
        String source = trackRequest == null ? null : trackRequest.source();
        telemetryService.trackVisit(request, response, path, query, source);
        return Map.of("status", "ok");
    }

    @PostMapping(EndpointsConstants.TELEMETRY_DELETE)
    @ResponseBody
    public Map<String, String> telemetryDelete(@RequestParam(name = "id") String id) {
        telemetryService.deleteVisit(id);
        return Map.of("status", "ok");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Error 400");
        detail.setDetail(ex.getMessage());
        return detail;
    }

    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null) {
            return "es";
        }
        return languageCode.trim().toLowerCase();
    }
}
