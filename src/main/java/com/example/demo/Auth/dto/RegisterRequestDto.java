package com.example.demo.Auth.dto;

import com.example.demo.User.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    String username;
    String password;
    String firstname;
    String lastname;
    String country;
    Role role;
    String areaNombre;
}
