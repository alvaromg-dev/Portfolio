package com.alvaromg.portfolio.infrastructure.out.jpa.repository;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.UserEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.UserJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.mappers.UserJpaEntityMapper;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.Role;
import com.alvaromg.portfolio.domain.model.User;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryJpaAdapter implements UserRepository {

    // UTILS
    @Autowired private PasswordEncoder passwordEncoder;

    // MAPPERS
    @Autowired private UserJpaEntityMapper userJpaEntityMapper;

    // REPOSITORIES
    @Autowired private UserJpaRepository userRepository;

    @Override
    public User create(User user) {
        Objects.requireNonNull(user);
        UserEntity entity = userJpaEntityMapper.toEntity(user);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        UserEntity persisted = userRepository.save(entity);
        return userJpaEntityMapper.toDomain(persisted);
    }

    @Override
    public boolean existsById(UUID id) {
        Objects.requireNonNull(id);
        return userRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id);
        return userRepository.findById(id).map(userJpaEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email);
        return userRepository.findByEmailWithRoles(email).map(userJpaEntityMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        Objects.requireNonNull(id);
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> update(User user) {
        UserEntity userE = userJpaEntityMapper.toEntity(user);
        Objects.requireNonNull(userE);
        userE = this.userRepository.save(userE);
        return Optional.of(userJpaEntityMapper.toDomain(userE));
    }

    @Override
    public boolean updateAvatar(UUID userId, String avatar) {

        if (avatar != null && avatar.length() > 255) {
            throw new IllegalArgumentException("Avatar URL length exceeds limit of 255 characters");
        }

        return userRepository.updateAvatar(userId, avatar) > 0;
    }

    @Override
    public Optional<User> updateName(UUID userId, String names, String surnames) {
        Optional<User> userOpt = this.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        user.setGivenNames(names);
        user.setFamilyNames(surnames);
        return this.update(user);
    }

    @Override
    public Optional<User> updateEmail(UUID userId, String email) {
        Optional<User> userOpt = this.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        user.setEmail(email);
        return this.update(user);
    }

    @Override
    public boolean updatePassword(UUID userId, String password) {
        Optional<User> userOpt = this.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        this.update(user);
        return true;
    }

    @Override
    public Optional<User> updateRoles(UUID userId, Set<Role> roles) {
        Optional<User> userOpt = this.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        user.setRoles(roles);
        return this.update(user);
    }

    @Override
    public Optional<String> findAvatarById(UUID id) {
        return userRepository.findAvatarById(id);
    }
}
