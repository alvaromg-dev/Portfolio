package com.alvaromg.portfolio.infrastructure.in.jsf.service;

import com.alvaromg.portfolio.application.roles.constants.RolesConstants;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.RoleEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.UserEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.RoleJpaRepository;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String ADMIN_USERNAME = "admin";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,64}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{1,80}$");

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ManagedUserRow> getUsers() {
        return userJpaRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
            .map(this::toRow)
            .toList();
    }

    @Transactional
    public void createUser(String username, String givenNames, String familyNames, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (ADMIN_USERNAME.equals(normalizedUsername)) {
            throw new IllegalArgumentException("Username 'admin' is reserved");
        }
        validateName(givenNames, "Given names");
        validateOptionalName(familyNames, "Family names");
        validatePassword(password);

        if (userJpaRepository.findByEmailWithRolesIgnoreCase(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        RoleEntity cvEditorRole = requiredRole(RolesConstants.CV_EDITOR);

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .givenNames(givenNames.trim())
            .familyNames(normalizeOptionalName(familyNames))
            .nif("00000000A")
            .email(normalizedUsername)
            .password(passwordEncoder.encode(password))
            .roles(new LinkedHashSet<>(Set.of(cvEditorRole)))
            .build();

        userJpaRepository.save(entity);
    }

    @Transactional
    public void updateUser(UUID id, String username, String givenNames, String familyNames, String password) {
        UserEntity user = userJpaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean adminUser = isAdminUser(user);
        String normalizedUsername = adminUser ? ADMIN_USERNAME : normalizeUsername(username);
        if (!adminUser && ADMIN_USERNAME.equals(normalizedUsername)) {
            throw new IllegalArgumentException("Username 'admin' is reserved");
        }

        validateName(givenNames, "Given names");
        validateOptionalName(familyNames, "Family names");
        if (password != null && !password.isBlank()) {
            validatePassword(password);
        }

        UserEntity collision = userJpaRepository.findByEmailWithRolesIgnoreCase(normalizedUsername).orElse(null);
        if (collision != null && !collision.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username already exists");
        }

        RoleEntity cvEditorRole = requiredRole(RolesConstants.CV_EDITOR);
        RoleEntity adminRole = requiredRole(RolesConstants.ADMIN);

        user.setEmail(normalizedUsername);
        user.setGivenNames(givenNames.trim());
        user.setFamilyNames(normalizeOptionalName(familyNames));
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setDeletedAt(null);

        LinkedHashSet<RoleEntity> roles = new LinkedHashSet<>();
        roles.add(cvEditorRole);
        if (adminUser) {
            roles.add(adminRole);
        }
        user.setRoles(roles);

        userJpaRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        UserEntity user = userJpaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (isAdminUser(user)) {
            throw new IllegalArgumentException("Admin user cannot be deleted");
        }

        user.setDeletedAt(LocalDateTime.now());
        userJpaRepository.save(user);
    }

    private ManagedUserRow toRow(UserEntity user) {
        List<String> roleCodes = user.getRoles().stream()
            .map(RoleEntity::getCode)
            .filter(code -> code != null && !code.isBlank())
            .sorted(Comparator.naturalOrder())
            .toList();

        return new ManagedUserRow(
            user.getId().toString(),
            user.getEmail(),
            nullToEmpty(user.getGivenNames()),
            nullToEmpty(user.getFamilyNames()),
            roleCodes,
            roleCodes.contains(RolesConstants.ADMIN)
        );
    }

    private RoleEntity requiredRole(String code) {
        return roleJpaRepository.findByCode(code)
            .orElseThrow(() -> new IllegalStateException("Role not found: " + code));
    }

    private boolean isAdminUser(UserEntity user) {
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> RolesConstants.ADMIN.equals(role.getCode()))) {
            return true;
        }
        return ADMIN_USERNAME.equalsIgnoreCase(user.getEmail());
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Username must match [A-Za-z0-9._-] and be 3-64 chars");
        }
        return normalized;
    }

    private static void validateName(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " are required");
        }
        if (!NAME_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid " + label.toLowerCase(Locale.ROOT));
        }
    }

    private static void validateOptionalName(String value, String label) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!NAME_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid " + label.toLowerCase(Locale.ROOT));
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    private static String normalizeOptionalName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record ManagedUserRow(
        String id,
        String username,
        String givenNames,
        String familyNames,
        List<String> roles,
        boolean admin
    ) {
    }
}

