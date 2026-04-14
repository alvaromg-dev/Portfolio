package com.example.sbtemplate.mono.infrastructure.in.http.security;

import com.example.sbtemplate.mono.application.roles.constants.RolesConstants;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.RoleEntity;
import com.example.sbtemplate.mono.infrastructure.out.jpa.entities.UserEntity;
import com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.RoleJpaRepository;
import com.example.sbtemplate.mono.infrastructure.out.jpa.repository.jpaRepository.UserJpaRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityBootstrapDataInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        RoleEntity cvEditorRole = ensureRole(RolesConstants.CV_EDITOR, "Can edit portfolio and telemetry");
        RoleEntity adminRole = ensureRole(RolesConstants.ADMIN, "Can manage users");
        ensureAdminUser(cvEditorRole, adminRole);
    }

    private RoleEntity ensureRole(String code, String description) {
        Optional<RoleEntity> existing = roleJpaRepository.findByCode(code);
        if (existing.isPresent()) {
            RoleEntity role = existing.get();
            if (role.getDescription() == null || role.getDescription().isBlank()) {
                role.setDescription(description);
                return roleJpaRepository.save(role);
            }
            return role;
        }

        RoleEntity role = RoleEntity.builder()
            .id(UUID.randomUUID())
            .code(code)
            .description(description)
            .build();
        return roleJpaRepository.save(role);
    }

    private void ensureAdminUser(RoleEntity cvEditorRole, RoleEntity adminRole) {
        UserEntity admin = userJpaRepository.findByEmailWithRolesIgnoreCase(ADMIN_USERNAME).orElse(null);
        if (admin == null) {
            UserEntity created = UserEntity.builder()
                .id(UUID.randomUUID())
                .givenNames("Admin")
                .familyNames("System")
                .nif("00000000A")
                .email(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .roles(new LinkedHashSet<>())
                .build();
            created.getRoles().add(cvEditorRole);
            created.getRoles().add(adminRole);
            userJpaRepository.save(created);
            log.info("Created default admin user: {}", ADMIN_USERNAME);
            return;
        }

        boolean changed = false;
        if (admin.getDeletedAt() != null) {
            admin.setDeletedAt(null);
            changed = true;
        }
        if (!ADMIN_USERNAME.equalsIgnoreCase(admin.getEmail())) {
            admin.setEmail(ADMIN_USERNAME);
            changed = true;
        } else if (!ADMIN_USERNAME.equals(admin.getEmail())) {
            admin.setEmail(admin.getEmail().trim().toLowerCase(Locale.ROOT));
            changed = true;
        }
        if (admin.getGivenNames() == null || admin.getGivenNames().isBlank()) {
            admin.setGivenNames("Admin");
            changed = true;
        }
        if (admin.getFamilyNames() == null) {
            admin.setFamilyNames("System");
            changed = true;
        }
        if (admin.getNif() == null || admin.getNif().isBlank()) {
            admin.setNif("00000000A");
            changed = true;
        }
        if (admin.getPassword() == null || !passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            changed = true;
        }

        Map<String, RoleEntity> byCode = new LinkedHashMap<>();
        if (admin.getRoles() != null) {
            admin.getRoles().stream()
                .filter(role -> role != null && role.getCode() != null && !role.getCode().isBlank())
                .forEach(role -> byCode.put(role.getCode(), role));
        }
        byCode.put(RolesConstants.CV_EDITOR, cvEditorRole);
        byCode.put(RolesConstants.ADMIN, adminRole);
        LinkedHashSet<RoleEntity> normalizedRoles = new LinkedHashSet<>(byCode.values());
        if (admin.getRoles() == null || admin.getRoles().size() != normalizedRoles.size() || !admin.getRoles().containsAll(normalizedRoles)) {
            admin.setRoles(normalizedRoles);
            changed = true;
        }

        if (changed) {
            userJpaRepository.save(admin);
            log.info("Updated default admin user: {}", ADMIN_USERNAME);
        }
    }
}

