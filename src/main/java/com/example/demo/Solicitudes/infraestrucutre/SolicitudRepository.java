package com.example.demo.Solicitudes.infraestrucutre;

import com.example.demo.Solicitudes.domain.Solicitudes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitudes, Integer> {
    Page<Solicitudes> findByUsuario_Id(Integer usuarioId, Pageable pageable);
}
