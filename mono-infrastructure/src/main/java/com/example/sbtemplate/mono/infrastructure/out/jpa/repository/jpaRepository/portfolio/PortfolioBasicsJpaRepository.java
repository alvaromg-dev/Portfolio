package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.portfolio.PortfolioBasicsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioBasicsJpaRepository extends JpaRepository<PortfolioBasicsEntity, UUID> {

    @EntityGraph(attributePaths = "profiles")
    Optional<PortfolioBasicsEntity> findByLanguage_Code(String languageCode);

    boolean existsByLanguage_Code(String languageCode);

    void deleteAllByLanguage_Code(String languageCode);
}
