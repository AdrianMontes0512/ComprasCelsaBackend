package com.example.demo.Solicitudes;

import com.example.demo.Auth.AuthService;
import com.example.demo.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Solicitudes", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
public class Solicitudes {
    @Id
    @GeneratedValue
    Integer id;

    @Column(nullable = true)
    String maquina;

    @Column(nullable = false)
    String CentroCosto;

    @Column(nullable = false)
    Prioridad prioridad;

    @Column(nullable = false)
    SP sp;

    @Column(nullable = false)
    String descripcion;

    @Column(nullable = false)
    String cantidad;

    @Column(nullable = false)
    String Precio;

    @Column(nullable = false)
    UMedida umedida;

    @Column(nullable = false)
    Moneda moneda;

    @Column(nullable = false)
    Estado estado;

    @Column(nullable = true)
    String OrdenCompra;

    @Column(nullable = false)
    String Motivo;

    @Column(nullable = false)
    String Familia;

    @Column(nullable = false)
    String SubFamilia;

    @Column(nullable = true)
    String Comentario;

    @Column(nullable = true)
    String Fecha;

    @Column(nullable = true)
    Status Status;

    @Column(nullable = true)
    String FechaOrden;

    @Lob
    @Column(nullable = true)
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;
}
