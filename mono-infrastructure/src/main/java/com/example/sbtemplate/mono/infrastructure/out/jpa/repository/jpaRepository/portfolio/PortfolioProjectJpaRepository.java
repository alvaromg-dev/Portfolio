package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.portfolio.PortfolioProjectEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioProjectJpaRepository extends JpaRepository<PortfolioProjectEntity, UUID> {

    List<PortfolioProjectEntity> findAllByLanguage_CodeOrderBySortOrderAsc(String languageCode);

    void deleteAllByLanguage_Code(String languageCode);
}
