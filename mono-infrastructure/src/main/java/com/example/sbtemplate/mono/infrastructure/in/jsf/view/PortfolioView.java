package com.example.sbtemplate.mono.infrastructure.in.jsf.view;

import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Basics;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.CvData;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Education;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.LanguageOption;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Profile;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Project;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Skill;
import com.example.sbtemplate.mono.infrastructure.in.jsf.model.Work;
import com.example.sbtemplate.mono.infrastructure.in.jsf.service.CvService;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * JSF view model for the portfolio page.
 */
@Component("portfolioView")
@RequestScope
@RequiredArgsConstructor
@Getter
public class PortfolioView {

    private static final Pattern YEAR_PATTERN = Pattern.compile("^(\\d{4})");

    private final CvService cvService;

    private Basics basics = new Basics();
    private List<Work> work = Collections.emptyList();
    private List<Project> projects = Collections.emptyList();
    private List<Skill> skills = Collections.emptyList();
    private List<Education> education = Collections.emptyList();
    private List<LanguageOption> languages = Collections.emptyList();
    private String currentLanguageCode = "es";

    public void load(String languageCode) {
        CvService.CvPayload payload = cvService.getCvData(languageCode);
        CvData data = payload.cvData();

        currentLanguageCode = payload.languageCode();
        basics = data.getBasics() == null ? new Basics() : data.getBasics();
        if (basics.getProfiles() == null) {
            basics.setProfiles(Collections.emptyList());
        }

        work = safeList(data.getWork());
        projects = safeList(data.getProjects());
        skills = safeList(data.getSkills());
        education = safeList(data.getEducation());
        languages = safeList(cvService.getAvailableLanguages());
    }

    public String getPageTitle() {
        return "Alvaro Martin Granados - Portfolio";
    }

    public String getLanguageSelectorLabel() {
        return text("Idioma", "Language");
    }

    public String getEditButtonLabel() {
        return text("Editar", "Edit");
    }

    public String getSaveButtonLabel() {
        return text("Guardar", "Save");
    }

    public String getAddButtonLabel() {
        return text("Añadir", "Add");
    }

    public String getTelemetryButtonLabel() {
        return text("Telemetria", "Telemetry");
    }

    public String getAboutTitle() {
        return text("Sobre mi", "About me");
    }

    public String getExperienceTitle() {
        return text("Experiencia laboral", "Work experience");
    }

    public String getProjectsTitle() {
        return text("Proyectos", "Projects");
    }

    public String getSkillsTitle() {
        return text("Habilidades", "Skills");
    }

    public String getEducationTitle() {
        return text("Educacion", "Education");
    }

    public String getEmailAriaLabel() {
        return text("Enviar correo electronico", "Send email");
    }

    public String getLinkedinAriaLabel() {
        return text("Ver perfil de LinkedIn", "View LinkedIn profile");
    }

    public String getGithubAriaLabel() {
        return text("Ver perfil de GitHub", "View GitHub profile");
    }

    public String getLinkedinUrl() {
        return profileUrl("LinkedIn");
    }

    public String getGithubUrl() {
        return profileUrl("GitHub");
    }

    public List<String> getSkillNames() {
        return skills.stream()
            .map(Skill::getName)
            .filter(Objects::nonNull)
            .toList();
    }

    public String yearRange(String startDate, String endDate) {
        Integer startYear = yearFrom(startDate);
        if (startYear == null) {
            return "";
        }
        Integer endYear = yearFrom(endDate);
        if (endYear == null) {
            return startYear + " - " + text("Actual", "Present");
        }
        return startYear + " - " + endYear;
    }

    public boolean isCurrentLanguage(String code) {
        if (code == null || currentLanguageCode == null) {
            return false;
        }
        return currentLanguageCode.equalsIgnoreCase(code);
    }

    public boolean hasHeroData() {
        if (basics == null) {
            return false;
        }
        return hasText(basics.getName())
            || hasText(basics.getLabel())
            || hasText(basics.getStatus())
            || hasText(basics.getEmail())
            || hasText(basics.getImage())
            || hasText(getLinkedinUrl())
            || hasText(getGithubUrl());
    }

    public boolean hasAboutData() {
        return basics != null && hasText(basics.getSummary());
    }

    public boolean hasExperienceData() {
        return !work.isEmpty();
    }

    public boolean hasProjectsData() {
        return !projects.isEmpty();
    }

    public boolean hasSkillsData() {
        return !getSkillNames().isEmpty();
    }

    public boolean hasEducationData() {
        return !education.isEmpty();
    }

    private String text(String spanish, String english) {
        return isEnglish() ? english : spanish;
    }

    private boolean isEnglish() {
        return "en".equalsIgnoreCase(currentLanguageCode);
    }

    private String profileUrl(String network) {
        if (basics == null || basics.getProfiles() == null) {
            return "";
        }
        String normalized = network.toLowerCase(Locale.ROOT);
        return basics.getProfiles().stream()
            .filter(profile -> profile.getNetwork() != null)
            .filter(profile -> profile.getNetwork().toLowerCase(Locale.ROOT).equals(normalized))
            .map(Profile::getUrl)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse("");
    }

    private Integer yearFrom(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(date.trim());
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }
        return null;
    }

    private static <T> List<T> safeList(List<T> items) {
        return items == null ? Collections.emptyList() : items;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
