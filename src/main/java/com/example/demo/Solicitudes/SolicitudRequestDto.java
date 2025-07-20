package com.example.demo.Solicitudes;

import com.example.demo.Solicitudes.Moneda;
import com.example.demo.Solicitudes.Prioridad;
import com.example.demo.Solicitudes.SP;
import com.example.demo.Solicitudes.UMedida;

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
}
