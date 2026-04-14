package com.alvaromg.portfolio.infrastructure.in.http.controller.mappers;

import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.out.RoleResponse;
import com.alvaromg.portfolio.domain.model.Role;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role request) {
        return RoleResponse.builder()
            .id(request.getId())
            .code(request.getCode())
            .description(request.getDescription())
            .build();
    }

    public Role toModel(RoleResponse response) {
        return Role.builder()
            .id(response.id())
            .code(response.code())
            .description(response.description())
            .build();
    }

    public Set<RoleResponse> toResponseSet(Set<Role> roles) {
        return roles.stream()
            .map(this::toResponse)
            .collect(Collectors.toSet());
    }

    public Set<Role> toModelSet(Set<RoleResponse> responses) {
        return responses.stream()
            .map(this::toModel)
            .collect(Collectors.toSet());
    }
}
