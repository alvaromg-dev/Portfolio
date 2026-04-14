package com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.portfolio.PortfolioWorkEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioWorkJpaRepository extends JpaRepository<PortfolioWorkEntity, UUID> {

    List<PortfolioWorkEntity> findAllByLanguage_CodeOrderBySortOrderAsc(String languageCode);

    void deleteAllByLanguage_Code(String languageCode);
}
