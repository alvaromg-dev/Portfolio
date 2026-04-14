package com.alvaromg.portfolio.infrastructure.in.http.controller.mappers;

import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.create.CreateUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.delete.DeleteUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.read.ReadUserAvatarRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.read.ReadUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserAvatarRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserEmailRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserNameRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserPasswordRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.out.RoleResponse;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.out.UserResponse;
import com.alvaromg.portfolio.application.users.ports.in.CreateUserUseCase.CreateUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.DeleteUserUseCase.DeleteUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserAvatarUseCase.ReadUserAvatarCommand;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserUseCase.ReadUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserAvatarUseCase.UpdateUserAvatarCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserEmailUseCase.UpdateUserEmailCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserNameUseCase.UpdateUserNameCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserPasswordUseCase.UpdateUserPasswordCommand;
import com.alvaromg.portfolio.domain.model.User;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired private RoleMapper roleMapper;

    public CreateUserCommand createUserCommand(CreateUserRequest request) {
        return CreateUserCommand.builder()
            .givenNames(request.givenNames())
            .familyNames(request.familyNames())
            .email(request.email())
            .password(request.password())
            .build();
    }

    public ReadUserCommand readUserCommand(UUID clientId, ReadUserRequest request) {
        return ReadUserCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .build();
    }

    public DeleteUserCommand deleteUserCommand(UUID clientId, DeleteUserRequest request) {
        return DeleteUserCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .build();
    }

    public UserResponse toResponse(User user) {
        Set<RoleResponse> roles = roleMapper.toResponseSet(user.getRoles());
        return UserResponse.builder()
            .id(user.getId())
            .givenNames(user.getGivenNames())
            .familyNames(user.getFamilyNames())
            .nif(user.getNif())
            .email(user.getEmail())
            .phone(user.getPhone())
            .roles(roles)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .build();
    }

    public UpdateUserAvatarCommand updateUserAvatarCommand(UUID clientId, UpdateUserAvatarRequest request) {
        return UpdateUserAvatarCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .avatar(request.avatar())
            .build();
    }

    public UpdateUserNameCommand updateUserNameCommand(UUID clientId, UpdateUserNameRequest request) {
        return UpdateUserNameCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .givenNames(request.givenNames())
            .familyNames(request.familyNames())
            .build();
    }

    public UpdateUserEmailCommand updateUserEmailCommand(UUID clientId, UpdateUserEmailRequest request) {
        return UpdateUserEmailCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .email(request.email())
            .build();
    }

    public UpdateUserPasswordCommand updateUserPasswordCommand(UUID clientId, UpdateUserPasswordRequest request) {
        return UpdateUserPasswordCommand.builder()
            .clientId(clientId)
            .userId(request.userId())
            .oldPassword(request.oldPassword())
            .newPassword(request.newPassword())
            .build();
    }

    public ReadUserAvatarCommand readUserAvatarCommand(ReadUserAvatarRequest request) {
        return ReadUserAvatarCommand.builder()
            .userId(request.userId())
            .build();
    }
}
