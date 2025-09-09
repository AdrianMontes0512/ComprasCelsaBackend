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
public class AuthResponseLoginDto {
    private String token;
    private Integer id;
    private String firstname;
    private String lastname;
    private Role role;
}