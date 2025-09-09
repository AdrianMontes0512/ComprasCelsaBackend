package com.example.demo.Solicitudes.dto;

import com.example.demo.Solicitudes.domain.SP;
import com.example.demo.Solicitudes.domain.Status;
import com.example.demo.Solicitudes.domain.UMedida;
import com.example.demo.Solicitudes.domain.Estado;
import com.example.demo.Solicitudes.domain.Moneda;
import com.example.demo.Solicitudes.domain.Prioridad;
import lombok.Data;

@Data
public class SolicitudByIdResponseDto {
    private Integer id;
    private Prioridad prioridad;
    private SP sp;
    private String descripcion;
    private String cantidad;
    private String precio;
    private UMedida umedida;
    private Moneda moneda;
    private Estado estado;
    private Integer usuarioId;
    private String ordenCompra;
    private String motivo;
    private String familia;
    private String subFamilia;
    private String comentarios;
    private String fecha;
    private String maquina;
    private String fechaOrden;
    private Status status;
}
