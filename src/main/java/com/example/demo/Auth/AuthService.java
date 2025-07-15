package com.example.demo.Auth;

import com.example.demo.Areas.Area;
import com.example.demo.Areas.AreaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Jwt.JwtService;
import com.example.demo.User.Role;
import com.example.demo.User.User;
import com.example.demo.User.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AreaRepository areaRepository; // Inject AreaRepository

    public AuthResponseLogin login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.getToken(user);
        return AuthResponseLogin.builder()
                .token(token)
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .role(user.getRole())
                .build();
    }

    // src/main/java/com/example/demo/Auth/AuthService.java

    public AuthResponse register(RegisterRequest request) {
        User.UserBuilder userBuilder = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .role(Role.valueOf(String.valueOf(request.getRole())));

        if (request.getAreaNombre() != null && !request.getAreaNombre().isEmpty()) {
            Area area = areaRepository.findById(request.getAreaNombre())
                    .orElseThrow(() -> new IllegalArgumentException("Area not found"));
            userBuilder.area(area);
        }

        User user = userBuilder.build();
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }

    public boolean isTokenValid(String token) {
        return jwtService.isTokenValid(token);
    }
}