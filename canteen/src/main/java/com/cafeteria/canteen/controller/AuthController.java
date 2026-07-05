package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.dto.ChangePasswordRequest;
import com.cafeteria.canteen.dto.LoginRequest;
import com.cafeteria.canteen.dto.LoginResponse;
import com.cafeteria.canteen.entity.AdminUser;
import com.cafeteria.canteen.repository.AdminUserRepository;
import com.cafeteria.canteen.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Username and password are required"));
        }

        AdminUser user = adminUserRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid username or password"));
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Not authenticated"));
        }
        if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Current and new password are required"));
        }
        if (request.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "New password must be at least " + MIN_PASSWORD_LENGTH + " characters"));
        }

        AdminUser user = adminUserRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not found"));
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Current password is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully"));
    }
}
