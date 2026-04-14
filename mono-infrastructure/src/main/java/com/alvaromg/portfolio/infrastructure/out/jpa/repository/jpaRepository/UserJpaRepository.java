package com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.UserEntity;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    // Read
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
    Optional<UserEntity> findById(UUID id);
    Optional<UserEntity> findByEmail(String email);
    @Query("select u.avatar from UserEntity u where u.id = :id")
    Optional<String> findAvatarById(UUID id);
    @EntityGraph(attributePaths = "roles")
    @Query("select u from UserEntity u where u.email = :email")
    Optional<UserEntity> findByEmailWithRoles(String email);
    @EntityGraph(attributePaths = "roles")
    @Query("select u from UserEntity u where lower(u.email) = lower(:email)")
    Optional<UserEntity> findByEmailWithRolesIgnoreCase(String email);
    @EntityGraph(attributePaths = "roles")
    List<UserEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    // Update

    @Modifying
    @Query("update UserEntity u set u.avatar = :avatar where u.id = :id")
    int updateAvatar(UUID id, String avatar);
}
