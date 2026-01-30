package com.neocommercepay.user.service;

import com.neocommercepay.common.dto.user.UserLoginRequest;
import com.neocommercepay.common.dto.user.UserResponse;
import com.neocommercepay.common.exception.UnauthorizedException;
import com.neocommercepay.common.security.JwtUtil;
import com.neocommercepay.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtUtil jwtUtil;

    public UserResponse login(UserLoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        log.info("User logged in successfully: {}", user.getEmail());

        return userService.mapToResponse(user, token);
    }
}
