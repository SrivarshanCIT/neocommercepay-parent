package com.neocommercepay.user.service;

import com.neocommercepay.common.dto.user.UserRegisterRequest;
import com.neocommercepay.common.dto.user.UserResponse;
import com.neocommercepay.common.exception.BusinessException;
import com.neocommercepay.common.exception.NotFoundException;
import com.neocommercepay.user.entity.Role;
import com.neocommercepay.user.entity.User;
import com.neocommercepay.user.event.UserEventProducer;
import com.neocommercepay.user.repository.RoleRepository;
import com.neocommercepay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final UserEventProducer userEventProducer;

    @Transactional
    public User registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .description("Default user role")
                            .permissions(new HashSet<>())
                            .build();
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordService.encodePassword(request.getPassword()))
                .fullName(request.getFullName())
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        userEventProducer.publishUserCreated(savedUser);

        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateUser(Long id, String fullName) {
        User user = getUserById(id);
        user.setFullName(fullName);
        User updatedUser = userRepository.save(user);

        userEventProducer.publishUserUpdated(updatedUser);

        return updatedUser;
    }

    public UserResponse mapToResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .token(token)
                .build();
    }
}
