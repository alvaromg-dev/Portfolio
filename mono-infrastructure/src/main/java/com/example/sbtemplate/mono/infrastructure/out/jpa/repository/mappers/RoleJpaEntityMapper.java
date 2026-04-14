package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.RoleEntity;
import com.example.sbtemplate.mono.domain.model.Role;

@Component
public class RoleJpaEntityMapper {

    public RoleEntity toEntity(Role role) {
        if (role == null) return null;
        return RoleEntity.builder()
            .id(role.getId())
            .code(role.getCode())
            .description(role.getDescription())
            .build();
    }

    public Role toDomain(RoleEntity role) {
        if (role == null) return null;
        return Role.builder()
            .id(role.getId())
            .code(role.getCode())
            .description(role.getDescription())
            .build();
    }

    public Set<Role> toDomainList(Set<RoleEntity> roles) {
        if (roles == null) return new HashSet<>();
        return roles.stream().map(this::toDomain).collect(Collectors.toSet());
    }

    public Set<RoleEntity> toEntityList(Set<Role> roles) {
        if (roles == null) return new HashSet<>();
        return roles.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
