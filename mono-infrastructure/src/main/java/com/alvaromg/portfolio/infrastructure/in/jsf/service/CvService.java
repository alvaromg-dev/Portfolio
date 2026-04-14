package com.alvaromg.portfolio.infrastructure.in.jsf.service;

import com.alvaromg.portfolio.infrastructure.in.jsf.model.Basics;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.CvData;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.Education;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.LanguageOption;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.Profile;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.Project;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.Skill;
import com.alvaromg.portfolio.infrastructure.in.jsf.model.Work;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.LanguageEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioBasicsEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioEducationEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioProfileEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioProjectEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioSkillEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioWorkEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.LanguageJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.PortfolioBasicsJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.PortfolioEducationJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.PortfolioProjectJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.PortfolioSkillJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.PortfolioWorkJpaRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads and saves CV data in database tables, filtered by language.
 */
@Service
@RequiredArgsConstructor
public class CvService {

    private static final String DEFAULT_LANGUAGE_CODE = "es";

    private final LanguageJpaRepository languageJpaRepository;
    private final PortfolioBasicsJpaRepository portfolioBasicsJpaRepository;
    private final PortfolioWorkJpaRepository portfolioWorkJpaRepository;
    private final PortfolioProjectJpaRepository portfolioProjectJpaRepository;
    private final PortfolioSkillJpaRepository portfolioSkillJpaRepository;
    private final PortfolioEducationJpaRepository portfolioEducationJpaRepository;

    @Transactional(readOnly = true)
    public CvPayload getCvData(String requestedLanguageCode) {
        String normalizedCode = normalizeLanguageCode(requestedLanguageCode);
        String resolvedLanguageCode = resolveLanguageCode(normalizedCode);

        CvData cvData = new CvData();
        cvData.setBasics(readBasics(resolvedLanguageCode));
        cvData.setWork(portfolioWorkJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(resolvedLanguageCode)
            .stream()
            .map(entity -> {
                Work work = new Work();
                work.setName(entity.getName());
                work.setPosition(entity.getPosition());
                work.setUrl(entity.getUrl());
                work.setStartDate(entity.getStartDate());
                work.setEndDate(entity.getEndDate());
                work.setSummary(entity.getSummary());
                work.setHighlights(copyList(entity.getHighlights()));
                return work;
            })
            .toList());

        cvData.setProjects(portfolioProjectJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(resolvedLanguageCode)
            .stream()
            .map(entity -> {
                Project project = new Project();
                project.setName(entity.getName());
                project.setDescription(entity.getDescription());
                project.setUrl(entity.getUrl());
                project.setHighlights(copyList(entity.getHighlights()));
                return project;
            })
            .toList());

        cvData.setSkills(portfolioSkillJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(resolvedLanguageCode)
            .stream()
            .map(entity -> {
                Skill skill = new Skill();
                skill.setName(entity.getName());
                return skill;
            })
            .toList());

        cvData.setEducation(portfolioEducationJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(resolvedLanguageCode)
            .stream()
            .map(entity -> {
                Education education = new Education();
                education.setInstitution(entity.getInstitution());
                education.setArea(entity.getArea());
                education.setUrl(entity.getUrl());
                education.setStartDate(entity.getStartDate());
                education.setEndDate(entity.getEndDate());
                education.setCourses(copyList(entity.getCourses()));
                return education;
            })
            .toList());

        return new CvPayload(resolvedLanguageCode, cvData);
    }

    @Transactional
    public String saveCvData(String requestedLanguageCode, CvData cvData) {
        String normalizedCode = normalizeLanguageCode(requestedLanguageCode);
        String resolvedLanguageCode = resolveLanguageCode(normalizedCode);
        LanguageEntity language = languageJpaRepository.findByCodeIgnoreCase(resolvedLanguageCode)
            .orElseThrow(() -> new IllegalStateException("Language not found: " + resolvedLanguageCode));

        CvData safeCvData = cvData == null ? new CvData() : cvData;

        saveBasics(language, resolvedLanguageCode, safeCvData.getBasics());
        saveWork(language, resolvedLanguageCode, safeList(safeCvData.getWork()));
        saveProjects(language, resolvedLanguageCode, safeList(safeCvData.getProjects()));
        saveSkills(language, resolvedLanguageCode, safeList(safeCvData.getSkills()));
        saveEducation(language, resolvedLanguageCode, safeList(safeCvData.getEducation()));

        return resolvedLanguageCode;
    }

    public List<LanguageOption> getAvailableLanguages() {
        return languageJpaRepository.findAllByOrderByCodeAsc().stream()
            .map(language -> new LanguageOption(language.getCode(), language.getName()))
            .toList();
    }

    private Basics readBasics(String languageCode) {
        PortfolioBasicsEntity basicsEntity = portfolioBasicsJpaRepository
            .findByLanguage_Code(languageCode)
            .orElse(null);

        if (basicsEntity == null) {
            Basics basics = new Basics();
            basics.setProfiles(Collections.emptyList());
            return basics;
        }

        Basics basics = new Basics();
        basics.setName(basicsEntity.getName());
        basics.setLabel(basicsEntity.getLabel());
        basics.setImage(basicsEntity.getImage());
        basics.setEmail(basicsEntity.getEmail());
        basics.setSummary(basicsEntity.getSummary());
        basics.setStatus(basicsEntity.getStatus());
        basics.setProfiles(copyList(basicsEntity.getProfiles()).stream()
            .map(entity -> {
                Profile profile = new Profile();
                profile.setNetwork(entity.getNetwork());
                profile.setUrl(entity.getUrl());
                return profile;
            })
            .toList());
        return basics;
    }

    private void saveBasics(LanguageEntity language, String languageCode, Basics basics) {
        PortfolioBasicsEntity basicsEntity = portfolioBasicsJpaRepository
            .findByLanguage_Code(languageCode)
            .orElse(null);

        if (basics == null) {
            if (basicsEntity != null) {
                portfolioBasicsJpaRepository.delete(basicsEntity);
            }
            return;
        }

        List<Profile> profiles = safeList(basics.getProfiles()).stream()
            .filter(Objects::nonNull)
            .filter(profile -> !isBlank(profile.getNetwork()) || !isBlank(profile.getUrl()))
            .toList();

        boolean hasBasicsContent = !isBlank(basics.getName())
            || !isBlank(basics.getLabel())
            || !isBlank(basics.getImage())
            || !isBlank(basics.getEmail())
            || !isBlank(basics.getSummary())
            || !isBlank(basics.getStatus())
            || !profiles.isEmpty();

        if (!hasBasicsContent) {
            if (basicsEntity != null) {
                portfolioBasicsJpaRepository.delete(basicsEntity);
            }
            return;
        }

        PortfolioBasicsEntity target = basicsEntity;
        if (target == null) {
            target = PortfolioBasicsEntity.builder().build();
            target.setLanguage(language);
        }

        target.setName(orEmpty(basics.getName()));
        target.setLabel(orEmpty(basics.getLabel()));
        target.setImage(orEmpty(basics.getImage()));
        target.setEmail(orEmpty(basics.getEmail()));
        target.setSummary(orEmpty(basics.getSummary()));
        target.setStatus(orEmpty(basics.getStatus()));

        List<PortfolioProfileEntity> profileEntities = new ArrayList<>();
        int order = 1;
        for (Profile profile : profiles) {
            profileEntities.add(PortfolioProfileEntity.builder()
                .basics(target)
                .network(orEmpty(profile.getNetwork()))
                .url(orEmpty(profile.getUrl()))
                .sortOrder(order++)
                .build());
        }
        if (target.getProfiles() == null) {
            target.setProfiles(new ArrayList<>());
        } else {
            target.getProfiles().clear();
        }
        target.getProfiles().addAll(profileEntities);

        portfolioBasicsJpaRepository.save(target);
    }

    private void saveWork(LanguageEntity language, String languageCode, List<Work> workItems) {
        List<PortfolioWorkEntity> existing = portfolioWorkJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(languageCode);
        if (!existing.isEmpty()) {
            portfolioWorkJpaRepository.deleteAll(existing);
        }

        List<PortfolioWorkEntity> entities = new ArrayList<>();
        int order = 1;
        for (Work item : workItems) {
            if (item == null || isEmptyWork(item)) {
                continue;
            }
            entities.add(PortfolioWorkEntity.builder()
                .language(language)
                .name(orEmpty(item.getName()))
                .position(orEmpty(item.getPosition()))
                .url(nullIfBlank(item.getUrl()))
                .startDate(orEmpty(item.getStartDate()))
                .endDate(nullIfBlank(item.getEndDate()))
                .summary(orEmpty(item.getSummary()))
                .highlights(cleanStringList(item.getHighlights()))
                .sortOrder(order++)
                .build());
        }

        if (!entities.isEmpty()) {
            portfolioWorkJpaRepository.saveAll(entities);
        }
    }

    private void saveProjects(LanguageEntity language, String languageCode, List<Project> projectItems) {
        List<PortfolioProjectEntity> existing = portfolioProjectJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(languageCode);
        if (!existing.isEmpty()) {
            portfolioProjectJpaRepository.deleteAll(existing);
        }

        List<PortfolioProjectEntity> entities = new ArrayList<>();
        int order = 1;
        for (Project item : projectItems) {
            if (item == null || isEmptyProject(item)) {
                continue;
            }
            entities.add(PortfolioProjectEntity.builder()
                .language(language)
                .name(orEmpty(item.getName()))
                .description(orEmpty(item.getDescription()))
                .url(nullIfBlank(item.getUrl()))
                .highlights(cleanStringList(item.getHighlights()))
                .sortOrder(order++)
                .build());
        }

        if (!entities.isEmpty()) {
            portfolioProjectJpaRepository.saveAll(entities);
        }
    }

    private void saveSkills(LanguageEntity language, String languageCode, List<Skill> skillItems) {
        List<PortfolioSkillEntity> existing = portfolioSkillJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(languageCode);
        if (!existing.isEmpty()) {
            portfolioSkillJpaRepository.deleteAll(existing);
        }

        List<PortfolioSkillEntity> entities = new ArrayList<>();
        int order = 1;
        for (Skill item : skillItems) {
            if (item == null || isBlank(item.getName())) {
                continue;
            }
            entities.add(PortfolioSkillEntity.builder()
                .language(language)
                .name(orEmpty(item.getName()))
                .sortOrder(order++)
                .build());
        }

        if (!entities.isEmpty()) {
            portfolioSkillJpaRepository.saveAll(entities);
        }
    }

    private void saveEducation(LanguageEntity language, String languageCode, List<Education> educationItems) {
        List<PortfolioEducationEntity> existing = portfolioEducationJpaRepository.findAllByLanguage_CodeOrderBySortOrderAsc(languageCode);
        if (!existing.isEmpty()) {
            portfolioEducationJpaRepository.deleteAll(existing);
        }

        List<PortfolioEducationEntity> entities = new ArrayList<>();
        int order = 1;
        for (Education item : educationItems) {
            if (item == null || isEmptyEducation(item)) {
                continue;
            }
            entities.add(PortfolioEducationEntity.builder()
                .language(language)
                .institution(orEmpty(item.getInstitution()))
                .area(orEmpty(item.getArea()))
                .url(nullIfBlank(item.getUrl()))
                .startDate(orEmpty(item.getStartDate()))
                .endDate(nullIfBlank(item.getEndDate()))
                .courses(cleanStringList(item.getCourses()))
                .sortOrder(order++)
                .build());
        }

        if (!entities.isEmpty()) {
            portfolioEducationJpaRepository.saveAll(entities);
        }
    }

    private String resolveLanguageCode(String languageCode) {
        return languageJpaRepository.findByCodeIgnoreCase(languageCode)
            .map(language -> language.getCode().toLowerCase())
            .orElseGet(() -> languageJpaRepository.findByCodeIgnoreCase(DEFAULT_LANGUAGE_CODE)
                .map(language -> language.getCode().toLowerCase())
                .orElse(DEFAULT_LANGUAGE_CODE));
    }

    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return DEFAULT_LANGUAGE_CODE;
        }
        return languageCode.trim().toLowerCase();
    }

    private static boolean isEmptyWork(Work work) {
        return isBlank(work.getName())
            && isBlank(work.getPosition())
            && isBlank(work.getUrl())
            && isBlank(work.getStartDate())
            && isBlank(work.getEndDate())
            && isBlank(work.getSummary())
            && cleanStringList(work.getHighlights()).isEmpty();
    }

    private static boolean isEmptyProject(Project project) {
        return isBlank(project.getName())
            && isBlank(project.getDescription())
            && isBlank(project.getUrl())
            && cleanStringList(project.getHighlights()).isEmpty();
    }

    private static boolean isEmptyEducation(Education education) {
        return isBlank(education.getInstitution())
            && isBlank(education.getArea())
            && isBlank(education.getUrl())
            && isBlank(education.getStartDate())
            && isBlank(education.getEndDate())
            && cleanStringList(education.getCourses()).isEmpty();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nullIfBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static List<String> cleanStringList(List<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .toList();
    }

    private static <T> List<T> safeList(List<T> items) {
        return items == null ? Collections.emptyList() : items;
    }

    private static <T> List<T> copyList(List<T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(source);
    }

    public record CvPayload(String languageCode, CvData cvData) {
    }
}
