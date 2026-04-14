package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.portfolio.PortfolioEducationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioEducationJpaRepository extends JpaRepository<PortfolioEducationEntity, UUID> {

    List<PortfolioEducationEntity> findAllByLanguage_CodeOrderBySortOrderAsc(String languageCode);

    void deleteAllByLanguage_Code(String languageCode);
}
