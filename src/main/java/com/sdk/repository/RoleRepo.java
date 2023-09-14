package com.sdk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sdk.entity.RoleEntity;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepo  extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);
}
