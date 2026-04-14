package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.portfolio;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.portfolio.LanguageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageJpaRepository extends JpaRepository<LanguageEntity, UUID> {

    Optional<LanguageEntity> findByCodeIgnoreCase(String code);

    List<LanguageEntity> findAllByOrderByCodeAsc();
}
