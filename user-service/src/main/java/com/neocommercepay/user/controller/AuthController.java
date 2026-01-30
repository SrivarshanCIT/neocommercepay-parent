package com.neocommercepay.user.controller;

import com.neocommercepay.common.dto.user.UserLoginRequest;
import com.neocommercepay.common.dto.user.UserRegisterRequest;
import com.neocommercepay.common.dto.user.UserResponse;
import com.neocommercepay.common.security.JwtUtil;
import com.neocommercepay.user.entity.User;
import com.neocommercepay.user.service.AuthService;
import com.neocommercepay.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = userService.registerUser(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        UserResponse response = userService.mapToResponse(user, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
