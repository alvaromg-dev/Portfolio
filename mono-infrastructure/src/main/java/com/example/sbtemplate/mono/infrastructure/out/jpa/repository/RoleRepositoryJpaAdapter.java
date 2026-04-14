package com.example.sbtemplate.mono.infrastructure.out.jpa.repository;

import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.RoleEntity;
import com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.RoleJpaRepository;
import com.example.sbtemplate.mono.infrastructure.out.jpa.repository.mappers.RoleJpaEntityMapper;
import com.example.sbtemplate.mono.application.roles.ports.out.RoleRepository;
import com.example.sbtemplate.mono.domain.model.Role;
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
