// src/main/java/com/example/demo/User/UserNameDto.java
package com.example.demo.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserNameDto {
    private String firstname;
    private String lastname;
}