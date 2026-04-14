package com.example.sbtemplate.mono.infrastructure.in.jsf.controller;

import com.example.sbtemplate.mono.application.roles.constants.RolesConstants;
import com.example.sbtemplate.mono.infrastructure.in.http.constants.EndpointsConstants;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.CvData;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.TelemetryTrackRequest;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.AdminUserService;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.CvService;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.TelemetryService;
import com.example.sbtemplate.mono.infrastructure.in.jsf.view.PortfolioView;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    private final AdminUserService adminUserService;

    public JsfPageController(
        PortfolioView portfolioView,
        CvService cvService,
        TelemetryService telemetryService,
        AdminUserService adminUserService
    ) {
        this.portfolioView = portfolioView;
        this.cvService = cvService;
        this.telemetryService = telemetryService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/")
    public String index(
        @RequestParam(name = "lang", required = false) String languageCode,
        Model model,
        Authentication authentication
    ) {
        portfolioView.load(languageCode);
        String lang = portfolioView.getCurrentLanguageCode();
        model.addAttribute("cv", portfolioView);
        model.addAttribute("canEdit", hasAuthority(authentication, RolesConstants.CV_EDITOR));
        model.addAttribute("canManageUsers", hasAuthority(authentication, RolesConstants.ADMIN));
        model.addAttribute("isAuthenticated", isAuthenticated(authentication));
        model.addAttribute("loginButtonLabel", text(lang, "Login", "Login"));
        model.addAttribute("logoutButtonLabel", text(lang, "Salir", "Logout"));
        model.addAttribute("usersButtonLabel", text(lang, "Usuarios", "Users"));
        return "portfolio";
    }

    @GetMapping(EndpointsConstants.TELEMETRY_PAGE)
    @PreAuthorize("hasAuthority('" + RolesConstants.CV_EDITOR + "')")
    public String telemetry(
        @RequestParam(name = "lang", required = false) String languageCode,
        Model model,
        Authentication authentication
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
        model.addAttribute("canManageUsers", hasAuthority(authentication, RolesConstants.ADMIN));
        model.addAttribute("usersButtonLabel", text(lang, "Usuarios", "Users"));
        model.addAttribute("logoutButtonLabel", text(lang, "Salir", "Logout"));
        return "telemetry";
    }

    @GetMapping(EndpointsConstants.LOGIN_PAGE)
    public String login(
        @RequestParam(name = "lang", required = false) String languageCode,
        @RequestParam(name = "error", required = false) String error,
        @RequestParam(name = "logout", required = false) String logout,
        Model model,
        Authentication authentication
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        if (isAuthenticated(authentication)) {
            return "redirect:/?lang=" + lang;
        }

        model.addAttribute("pageTitle", "en".equals(lang) ? "Login" : "Acceso");
        model.addAttribute("loginLang", lang);
        model.addAttribute("loginTitle", "en".equals(lang) ? "Secure Access" : "Acceso seguro");
        model.addAttribute("loginSubtitle", "en".equals(lang) ? "Sign in to edit portfolio and view telemetry" : "Inicia sesion para editar portfolio y ver telemetria");
        model.addAttribute("loginUsernameLabel", "en".equals(lang) ? "Username" : "Usuario");
        model.addAttribute("loginPasswordLabel", "en".equals(lang) ? "Password" : "Contrasena");
        model.addAttribute("loginSubmitLabel", "en".equals(lang) ? "Sign in" : "Entrar");
        model.addAttribute("loginBackLabel", "en".equals(lang) ? "Back to portfolio" : "Volver al portfolio");
        model.addAttribute("loginErrorLabel", "en".equals(lang) ? "Invalid credentials" : "Credenciales invalidas");
        model.addAttribute("loginLogoutLabel", "en".equals(lang) ? "Session closed" : "Sesion cerrada");
        model.addAttribute("showLoginError", error != null);
        model.addAttribute("showLogoutMessage", logout != null);
        return "login";
    }

    @GetMapping(EndpointsConstants.USERS_PAGE)
    @PreAuthorize("hasAuthority('" + RolesConstants.ADMIN + "')")
    public String users(
        @RequestParam(name = "lang", required = false) String languageCode,
        Model model
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        model.addAttribute("pageTitle", "en".equals(lang) ? "Users" : "Usuarios");
        model.addAttribute("usersLang", lang);
        model.addAttribute("usersTitle", "en".equals(lang) ? "User management" : "Gestion de usuarios");
        model.addAttribute("usersBackLabel", "en".equals(lang) ? "Back to portfolio" : "Volver al portfolio");
        model.addAttribute("usersCreateTitle", "en".equals(lang) ? "Create user" : "Crear usuario");
        model.addAttribute("usersCreateButtonLabel", "en".equals(lang) ? "Create" : "Crear");
        model.addAttribute("usersUpdateButtonLabel", "en".equals(lang) ? "Update" : "Actualizar");
        model.addAttribute("usersDeleteButtonLabel", "en".equals(lang) ? "Delete" : "Eliminar");
        model.addAttribute("usersDeleteConfirm", "en".equals(lang) ? "Delete this user?" : "¿Eliminar este usuario?");
        model.addAttribute("usersUsernameLabel", "en".equals(lang) ? "Username" : "Usuario");
        model.addAttribute("usersGivenNamesLabel", "en".equals(lang) ? "Given names" : "Nombre");
        model.addAttribute("usersFamilyNamesLabel", "en".equals(lang) ? "Family names" : "Apellidos");
        model.addAttribute("usersPasswordLabel", "en".equals(lang) ? "Password" : "Contrasena");
        model.addAttribute("usersPasswordHint", "en".equals(lang) ? "Leave blank to keep current password" : "Vacio para mantener la contrasena");
        model.addAttribute("usersRolesLabel", "en".equals(lang) ? "Roles" : "Roles");
        model.addAttribute("usersLogoutLabel", "en".equals(lang) ? "Logout" : "Salir");
        model.addAttribute("usersRows", adminUserService.getUsers());
        return "users";
    }

    @PostMapping(EndpointsConstants.USERS_CREATE_PAGE)
    @PreAuthorize("hasAuthority('" + RolesConstants.ADMIN + "')")
    public String usersCreate(
        @RequestParam(name = "lang", required = false) String languageCode,
        @RequestParam(name = "username") String username,
        @RequestParam(name = "givenNames") String givenNames,
        @RequestParam(name = "familyNames", required = false) String familyNames,
        @RequestParam(name = "password") String password,
        RedirectAttributes redirectAttributes
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        try {
            adminUserService.createUser(username, givenNames, familyNames, password);
            redirectAttributes.addFlashAttribute("usersSuccess", "en".equals(lang) ? "User created" : "Usuario creado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("usersError", ex.getMessage());
        }
        return "redirect:/users?lang=" + lang;
    }

    @PostMapping(EndpointsConstants.USERS_UPDATE_PAGE)
    @PreAuthorize("hasAuthority('" + RolesConstants.ADMIN + "')")
    public String usersUpdate(
        @RequestParam(name = "lang", required = false) String languageCode,
        @RequestParam(name = "id") String id,
        @RequestParam(name = "username") String username,
        @RequestParam(name = "givenNames") String givenNames,
        @RequestParam(name = "familyNames", required = false) String familyNames,
        @RequestParam(name = "password", required = false) String password,
        RedirectAttributes redirectAttributes
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        try {
            adminUserService.updateUser(UUID.fromString(id), username, givenNames, familyNames, password);
            redirectAttributes.addFlashAttribute("usersSuccess", "en".equals(lang) ? "User updated" : "Usuario actualizado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("usersError", ex.getMessage());
        }
        return "redirect:/users?lang=" + lang;
    }

    @PostMapping(EndpointsConstants.USERS_DELETE_PAGE)
    @PreAuthorize("hasAuthority('" + RolesConstants.ADMIN + "')")
    public String usersDelete(
        @RequestParam(name = "lang", required = false) String languageCode,
        @RequestParam(name = "id") String id,
        RedirectAttributes redirectAttributes
    ) {
        String lang = Objects.equals("en", normalizeLanguageCode(languageCode)) ? "en" : "es";
        try {
            adminUserService.deleteUser(UUID.fromString(id));
            redirectAttributes.addFlashAttribute("usersSuccess", "en".equals(lang) ? "User deleted" : "Usuario eliminado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("usersError", ex.getMessage());
        }
        return "redirect:/users?lang=" + lang;
    }

    @PostMapping(EndpointsConstants.PORTFOLIO_SAVE)
    @PreAuthorize("hasAuthority('" + RolesConstants.CV_EDITOR + "')")
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
    @PreAuthorize("hasAuthority('" + RolesConstants.CV_EDITOR + "')")
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

    private static boolean hasAuthority(Authentication authentication, String authority) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private static String text(String lang, String spanish, String english) {
        return Objects.equals("en", lang) ? english : spanish;
    }
}
