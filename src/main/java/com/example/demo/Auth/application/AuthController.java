package com.example.demo.Auth.application;

import com.example.demo.Auth.domain.AuthService;
import com.example.demo.Auth.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponseLoginDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto request)
    {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody tokenRequestDto request) {
        boolean isValid = authService.isTokenValid(request.getToken());
        return ResponseEntity.ok(isValid);
    }
}
