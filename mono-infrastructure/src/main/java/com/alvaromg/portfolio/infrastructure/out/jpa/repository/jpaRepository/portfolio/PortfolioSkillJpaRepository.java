package com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioSkillEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioSkillJpaRepository extends JpaRepository<PortfolioSkillEntity, UUID> {

    List<PortfolioSkillEntity> findAllByLanguage_CodeOrderBySortOrderAsc(String languageCode);

    void deleteAllByLanguage_Code(String languageCode);
}
