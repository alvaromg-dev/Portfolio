package com.example.sbtemplate.mono.infrastructure.out.jpa.repository.mappers;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.RoleEntity;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.UserEntity;
import com.example.sbtemplate.mono.domain.model.Role;
import com.example.sbtemplate.mono.domain.model.User;

@Component
public class UserJpaEntityMapper {

    @Autowired private RoleJpaEntityMapper roleJpaEntityMapper;

    public User toDomain(UserEntity user) {
        if (user == null) return null;
        Set<Role> roles = roleJpaEntityMapper.toDomainList(user.getRoles());
        return User.builder()
            .id(user.getId())
            .givenNames(user.getGivenNames())
            .familyNames(user.getFamilyNames())
            .nif(user.getNif())
            .avatar(user.getAvatar())
            .email(user.getEmail())
            .phone(user.getPhone())
            .password(user.getPassword())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .roles(roles)
            .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;
        Set<RoleEntity> roles = roleJpaEntityMapper.toEntityList(user.getRoles());
        return UserEntity.builder()
            .id(user.getId())
            .givenNames(user.getGivenNames())
            .familyNames(user.getFamilyNames())
            .nif(user.getNif())
            .avatar(user.getAvatar())
            .email(user.getEmail())
            .phone(user.getPhone())
            .password(user.getPassword())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .roles(roles)
            .build();
    }
}
