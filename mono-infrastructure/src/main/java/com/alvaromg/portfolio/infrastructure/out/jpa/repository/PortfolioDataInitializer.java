package com.alvaromg.portfolio.infrastructure.out.jpa.repository;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.LanguageEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio.LanguageJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioDataInitializer {

    private final PortfolioSchemaInitializer portfolioSchemaInitializer;
    private final LanguageJpaRepository languageJpaRepository;

    @PostConstruct
    public void initialize() {
        try {
            portfolioSchemaInitializer.ensureForeignKeys();
            upsertLanguage("es", "Español");
            upsertLanguage("en", "English");
        } catch (Exception ex) {
            log.error("Portfolio data initialization failed.", ex);
            throw ex;
        }
    }

    private LanguageEntity upsertLanguage(String code, String name) {
        return languageJpaRepository.findByCodeIgnoreCase(code)
            .map(existing -> {
                existing.setName(name);
                return languageJpaRepository.save(existing);
            })
            .orElseGet(() -> languageJpaRepository.save(LanguageEntity.builder()
                .code(code)
                .name(name)
                .build()));
    }
}
