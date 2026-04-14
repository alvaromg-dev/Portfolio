package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.RoleEntity;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

    // READ
    boolean existsById(UUID id);
    Optional<RoleEntity> findByCode(String code);
    Optional<RoleEntity> findById(UUID id);
}
