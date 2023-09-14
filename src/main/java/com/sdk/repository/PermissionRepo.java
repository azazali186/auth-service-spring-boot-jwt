package com.sdk.repository;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sdk.entity.PermissionEntity;

public interface PermissionRepo extends JpaRepository<PermissionEntity, UUID> {

    PermissionEntity findByName(String name);

}