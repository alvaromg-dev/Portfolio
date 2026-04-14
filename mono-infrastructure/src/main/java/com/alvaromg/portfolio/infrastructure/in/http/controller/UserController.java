package com.alvaromg.portfolio.infrastructure.in.http.controller;

import com.alvaromg.portfolio.infrastructure.in.http.constants.EndpointsConstants;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.create.CreateUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.delete.DeleteUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.read.ReadUserAvatarRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.read.ReadUserRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserAvatarRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserEmailRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserNameRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update.UpdateUserPasswordRequest;
import com.alvaromg.portfolio.infrastructure.in.http.controller.dto.out.UserResponse;
import com.alvaromg.portfolio.infrastructure.in.http.controller.mappers.UserMapper;
import com.alvaromg.portfolio.infrastructure.in.http.security.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import com.alvaromg.portfolio.application.roles.constants.RolesConstants;
import com.alvaromg.portfolio.application.users.ports.in.CreateUserUseCase;
import com.alvaromg.portfolio.application.users.ports.in.DeleteUserUseCase;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserAvatarUseCase;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserUseCase;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserAvatarUseCase;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserEmailUseCase;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserNameUseCase;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserPasswordUseCase;
import com.alvaromg.portfolio.application.users.ports.in.CreateUserUseCase.CreateUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.DeleteUserUseCase.DeleteUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserAvatarUseCase.ReadUserAvatarCommand;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserUseCase.ReadUserCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserAvatarUseCase.UpdateUserAvatarCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserEmailUseCase.UpdateUserEmailCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserNameUseCase.UpdateUserNameCommand;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserPasswordUseCase.UpdateUserPasswordCommand;
import com.alvaromg.portfolio.domain.model.User;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Slf4j
public class UserController {

    // MAPPERS
    @Autowired private UserMapper userMapper;

    // USE CASES
    @Autowired private CreateUserUseCase createUserUseCase;
    @Autowired private DeleteUserUseCase deleteUserUseCase;
    @Autowired private ReadUserUseCase readUserUseCase;
    @Autowired private UpdateUserAvatarUseCase updateUserAvatarUseCase;
    @Autowired private UpdateUserNameUseCase updateUserNameUseCase;
    @Autowired private UpdateUserEmailUseCase updateUserEmailUseCase;
    @Autowired private UpdateUserPasswordUseCase updateUserPasswordUseCase;
    @Autowired private ReadUserAvatarUseCase readUserAvatarUseCase;

    // ################
    // # CREATE
    // ################

    /**
     * Create User Endpoint
     * 201 CREATED
     * 400 BAD REQUEST - if email is already in use or invalid format
     * 400 BAD REQUEST - if password is invalid format
     * 400 BAD REQUEST - if given names is invalid format
     * @param request
     * @return
     */
    @PostMapping(EndpointsConstants.USERS_CREATE)
    @PreAuthorize("hasAuthority('" + RolesConstants.ADMIN + "')")
    public UserResponse create(
        @RequestBody CreateUserRequest request
    ) {
        log.info(EndpointsConstants.USERS_CREATE);
        CreateUserCommand command = userMapper.createUserCommand(request);
        User created = createUserUseCase.createUser(command);
        return userMapper.toResponse(created);
    }

    // ################
    // # READ
    // ################

    /**
     * Read User Endpoint - by authenticated user's id
     * 200 OK
     * 404 NOT FOUND - if user not found
     * @param principal
     * @return
     */
    @PostMapping(EndpointsConstants.USERS_READ)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public UserResponse readById(
        @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        log.info(EndpointsConstants.USERS_READ);
        ReadUserRequest request = new ReadUserRequest(principal.getId());
        ReadUserCommand command = userMapper.readUserCommand(principal.getId(), request);
        User user = readUserUseCase.readUser(command);
        return userMapper.toResponse(user);
    }

    /**
     * Read User Avatar Endpoint
     * returns the base 64 image string
     * 200 OK
     * 404 NOT FOUND - if user or avatar not found
     * @param principal
     * @param userId
     * @return
     */
    @GetMapping(EndpointsConstants.USERS_READ_AVATAR)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public String getUserAvatar(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestParam String userId
    ) {
        log.info(EndpointsConstants.USERS_READ_AVATAR);
        ReadUserAvatarRequest request = new ReadUserAvatarRequest(UUID.fromString(userId));
        ReadUserAvatarCommand command = userMapper.readUserAvatarCommand(request);
        return readUserAvatarUseCase.readUserAvatar(command);
    }

    // ################
    // # UPDATE
    // ################

    /**
     * Update User Avatar Endpoint
     * 200 OK
     * 404 NOT FOUND - if user not found
     * @param principal
     * @param request
     * @return
     */
    @PatchMapping(EndpointsConstants.USERS_UPDATE_AVATAR)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public boolean updateAvatar(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestBody UpdateUserAvatarRequest request
    ) {
        log.info(EndpointsConstants.USERS_UPDATE_AVATAR);
        UpdateUserAvatarCommand command = userMapper.updateUserAvatarCommand(principal.getId(), request);
        return updateUserAvatarUseCase.updateUserAvatar(command);
    }

    @PatchMapping(EndpointsConstants.USERS_UPDATE_NAME)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public UserResponse updateName(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestBody UpdateUserNameRequest request
    ) {
        UpdateUserNameCommand command = userMapper.updateUserNameCommand(principal.getId(), request);
        User user = updateUserNameUseCase.updateUserName(command);
        return userMapper.toResponse(user);
    }

    @PatchMapping(EndpointsConstants.USERS_UPDATE_EMAIL)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public UserResponse updateEmail(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestBody UpdateUserEmailRequest request
    ) {
        log.info(EndpointsConstants.USERS_UPDATE_EMAIL);
        UpdateUserEmailCommand command = userMapper.updateUserEmailCommand(principal.getId(), request);
        User user = updateUserEmailUseCase.updateUserEmail(command);
        return userMapper.toResponse(user);
    }

    @PatchMapping(EndpointsConstants.USERS_UPDATE_PASSWORD)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public Boolean updatePassword(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestBody UpdateUserPasswordRequest request
    ) {
        log.info(EndpointsConstants.USERS_UPDATE_PASSWORD);
        UpdateUserPasswordCommand command = userMapper.updateUserPasswordCommand(principal.getId(), request);
        return updateUserPasswordUseCase.updateUserPassword(command);
    }

    // ################
    // # DELETE
    // ################

    @DeleteMapping(EndpointsConstants.USERS_DELETE)
    @PreAuthorize("hasAuthority('" + RolesConstants.USER + "')")
    public boolean delete(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestBody DeleteUserRequest request
    ) {
        log.info(EndpointsConstants.USERS_DELETE);
        DeleteUserCommand command = userMapper.deleteUserCommand(principal.getId(), request);
        return deleteUserUseCase.delete(command);
    }
}
