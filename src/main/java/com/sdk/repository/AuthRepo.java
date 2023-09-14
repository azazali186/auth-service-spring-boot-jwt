package com.sdk.repository;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sdk.entity.UserEntity;

public interface AuthRepo  extends JpaRepository<UserEntity, UUID> {
    UserEntity findByEmail(String email);    
    UserEntity findByUsername(String username);

}
