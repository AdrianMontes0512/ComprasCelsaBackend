package com.example.demo.Solicitudes;

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
}
