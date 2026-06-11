package com.example.demo.Solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ActividadRecienteDto {
    private String tipo;           // "creada" | "aprobada" | "rechazada" | "oc_asignada"
    private Integer solicitudId;
    private String descripcion;
    private String actor;          // nombre del solicitante / jefe / compras
    private String estado;
    private String prioridad;
    private Instant ocurrioEn;
}
