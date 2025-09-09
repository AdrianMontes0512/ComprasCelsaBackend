package com.example.demo.User.infraestructure;

import java.util.Optional;

import com.example.demo.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(Integer jefeId);
}