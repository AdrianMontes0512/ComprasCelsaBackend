package com.example.demo.User.application;

import com.example.demo.User.dto.UserNameDto;
import com.example.demo.User.infraestructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class userController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserNameDto> getUserNameById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserNameDto(user.getFirstname(), user.getLastname())))
                .orElse(ResponseEntity.notFound().build());
    }
}