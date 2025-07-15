package com.example.demo.Auth;

import com.example.demo.Auth.tokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponseLogin> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody tokenRequest request) {
        boolean isValid = authService.isTokenValid(request.getToken());
        return ResponseEntity.ok(isValid);
    }
}
