package com.example.demo.Auth;

import com.example.demo.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseLogin {
    private String token;
    private Integer id;
    private String firstname;
    private String lastname;
    private Role role;
}