package com.sdk.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sdk.entity.RoleEntity;
import com.sdk.entity.UserEntity;
import com.sdk.entity.UserWithToken;
import com.sdk.exception.InvalidCredentialsException;
import com.sdk.exception.MissingFieldException;
import com.sdk.exception.UserAlreadyExistsException;
import com.sdk.exception.UserNotFoundException;
import com.sdk.repository.AuthRepo;
import com.sdk.repository.RoleRepo;
import com.sdk.request.LoginRequest;
import com.sdk.request.RegisterRequest;
import com.sdk.response.ApiResponse;
import com.sdk.utils.JwtUtil;

@Service
public class AuthService {

    // @Autowired User users;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthRepo authRepo;

    @Autowired
    private RoleRepo roleRepo;

    public ApiResponse registerHandler(RegisterRequest req) throws UserAlreadyExistsException{
        if (req.getEmail() == null || req.getUsername() == null || req.getPassword() == null) {
            throw new MissingFieldException("Email, Username, and Password are required fields.");
        }

        UserEntity existingUser = authRepo.findByEmail(req.getEmail());

        if (existingUser != null) {
            throw new UserAlreadyExistsException("User with the same email already exists.");
        }

        existingUser = authRepo.findByUsername(req.getUsername());

        if (existingUser != null) {
            throw new UserAlreadyExistsException("User with the same username already exists.");
        }

        Optional<RoleEntity> existRole = roleRepo.findByName("MEMBER");

        RoleEntity role = new RoleEntity();

        if (existRole.isPresent()) {
            role = existRole.get();
        }else {
            role.setName("MEMBER");
            role.setDesc("MEMBER ROLE");
            roleRepo.save(role);
            role = roleRepo.findByName("MEMBER").get();
            
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(req.getUsername());
        String hashedPassword = hashPassword(req.getPassword());
        newUser.setPassword(hashedPassword);
        newUser.setEmail(req.getEmail());
        newUser.setCreatedAt(new Date());
        newUser.setRoleId(role);
        authRepo.save(newUser);

        newUser.setPassword(null);

        LOGGER.info("User created: {}", newUser);

        ApiResponse<UserEntity> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "User registered successfully",
                newUser);

        return response;
    }

    private String hashPassword(String plainTextPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(plainTextPassword);
    }

    public ApiResponse loginHandler(LoginRequest req) throws UserNotFoundException, InvalidCredentialsException {
        UserEntity userEntity = authRepo.findByEmail(req.getEmail());
    
        // If user not found, throw exception
        if (userEntity == null) {
            throw new UserNotFoundException("User not found with email: " + req.getEmail());
        }
    
        // Assuming you have a utility method to check the password
        if (!isPasswordValid(req.getPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
    
        // For security reasons, you may want to remove or nullify sensitive data
        // like the password hash before sending the user object in the response
        userEntity.setPassword(null);

        String jwtToken = JwtUtil.getInstance().generateToken(userEntity.getEmail());

        UserWithToken userWithToken = new UserWithToken(userEntity, jwtToken);

        ApiResponse<UserWithToken> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "Login successful",
            userWithToken
        );
    
        return response;
    }
    

    private boolean isPasswordValid(String plainTextPassword, String hashedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(plainTextPassword, hashedPassword);
    }

}
