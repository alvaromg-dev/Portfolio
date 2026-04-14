package com.alvaromg.portfolio.infrastructure.out.jpa.repository;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.RoleEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.RoleJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.mappers.RoleJpaEntityMapper;
import com.alvaromg.portfolio.application.roles.ports.out.RoleRepository;
import com.alvaromg.portfolio.domain.model.Role;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryJpaAdapter implements RoleRepository {

    // MAPPERS
    @Autowired private RoleJpaEntityMapper roleJpaEntityMapper;

    // REPOSITORIES
    @Autowired private RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<Role> findByCode(String code) {
        RoleEntity role = roleJpaRepository.findByCode(code).orElse(null);
        return Optional.of(roleJpaEntityMapper.toDomain(role));
    }
}
