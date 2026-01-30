package com.neocommercepay.user.controller;

import com.neocommercepay.common.dto.user.UserResponse;
import com.neocommercepay.user.entity.User;
import com.neocommercepay.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = userService.mapToResponse(user, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestParam String fullName) {
        User user = userService.updateUser(id, fullName);
        UserResponse response = userService.mapToResponse(user, null);
        return ResponseEntity.ok(response);
    }
}
