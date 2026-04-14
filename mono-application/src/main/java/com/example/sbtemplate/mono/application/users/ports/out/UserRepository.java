package com.example.sbtemplate.mono.application.users.ports.out;

import com.example.sbtemplate.mono.domain.model.Role;
import com.example.sbtemplate.mono.domain.model.User;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository {

    // CREATE
    User create(User user);

    // READ
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<String> findAvatarById(UUID id);

    // UPDATE
    Optional<User> update(User user);
    Optional<User> updateRoles(UUID userId, Set<Role> roles);
    boolean updateAvatar(UUID userId, String avatar);
    Optional<User> updateName(UUID userId, String name, String surnames);
    Optional<User> updateEmail(UUID userId, String email);
    boolean updatePassword(UUID userId, String password);

    // DELETE
    void deleteById(UUID id);
}
