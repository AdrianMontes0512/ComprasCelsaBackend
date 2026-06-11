package com.example.demo.User.application;

import com.example.demo.User.domain.User;
import com.example.demo.User.dto.ChangePasswordRequestDto;
import com.example.demo.User.dto.UserNameDto;
import com.example.demo.User.infraestructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class userController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<UserNameDto> getUserNameById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserNameDto(user.getFirstname(), user.getLastname())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Integer id,
                                            @RequestBody ChangePasswordRequestDto dto) {
        if (dto.getCurrentPassword() == null || dto.getNewPassword() == null
                || dto.getNewPassword().length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos inválidos. La nueva contraseña debe tener al menos 4 caracteres."));
        }
        return userRepository.findById(id)
                .map(user -> {
                    if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "La contraseña actual es incorrecta."));
                    }
                    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("ok", true));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado")));
    }
}
