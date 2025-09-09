package com.example.demo.Areas.infraestructure;

import com.example.demo.Areas.domain.Area;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, String> {
}
