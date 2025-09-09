package com.example.demo.Solicitudes.dto;

import com.example.demo.Solicitudes.domain.*;
import lombok.Data;

@Data
public class SolicitudRequestDto {
    private Prioridad prioridad;
    private SP sp;
    private String centrocosto;
    private String descripcion;
    private String cantidad;
    private String precio;
    private UMedida umedida;
    private Moneda moneda;
    private Estado estado;
    private Integer usuarioId;
    private byte[] imageData;
    private String ordenCompra="";
    private String motivo;
    private String familia;
    private String subFamilia;
    private String Comentarios;
    private String Fecha;
    private String maquina;
    private String fechaOrden;
    private Status status;
}
