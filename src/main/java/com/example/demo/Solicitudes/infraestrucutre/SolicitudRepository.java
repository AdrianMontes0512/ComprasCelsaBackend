package com.example.demo.Solicitudes.infraestrucutre;

import com.example.demo.Solicitudes.domain.Estado;
import com.example.demo.Solicitudes.domain.Prioridad;
import com.example.demo.Solicitudes.domain.SP;
import com.example.demo.Solicitudes.domain.Solicitudes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudRepository extends JpaRepository<Solicitudes, Integer> {

    // ---- LISTADO POR USUARIO con filtros ----
    @Query(
        value = "SELECT s FROM Solicitudes s " +
                "WHERE s.usuario.id = :usuarioId " +
                "AND (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))",
        countQuery = "SELECT COUNT(s) FROM Solicitudes s " +
                "WHERE s.usuario.id = :usuarioId " +
                "AND (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))"
    )
    Page<Solicitudes> searchByUsuario(
        @Param("usuarioId") Integer usuarioId,
        @Param("prioridad") Prioridad prioridad,
        @Param("sp") SP sp,
        @Param("estado") Estado estado,
        @Param("idQuery") String idQuery,
        @Param("descripcionQuery") String descripcionQuery,
        Pageable pageable
    );

    // ---- LISTADO GLOBAL (Compras / ADMIN) con filtros ----
    @Query(
        value = "SELECT s FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.User.domain.User u ON u.id = s.usuario.id " +
                "WHERE (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:usuarioQuery IS NULL OR LOWER(CONCAT(u.firstname,' ',u.lastname)) LIKE LOWER(CONCAT('%', CAST(:usuarioQuery AS string), '%'))) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))",
        countQuery = "SELECT COUNT(s) FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.User.domain.User u ON u.id = s.usuario.id " +
                "WHERE (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:usuarioQuery IS NULL OR LOWER(CONCAT(u.firstname,' ',u.lastname)) LIKE LOWER(CONCAT('%', CAST(:usuarioQuery AS string), '%'))) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))"
    )
    Page<Solicitudes> searchAll(
        @Param("prioridad") Prioridad prioridad,
        @Param("sp") SP sp,
        @Param("estado") Estado estado,
        @Param("idQuery") String idQuery,
        @Param("usuarioQuery") String usuarioQuery,
        @Param("descripcionQuery") String descripcionQuery,
        Pageable pageable
    );

    // ---- LISTADO POR JEFE (área de la solicitud) con filtros ----
    @Query(
        value = "SELECT s FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.Areas.domain.Area a ON a.NombreArea = s.CentroCosto " +
                "LEFT JOIN com.example.demo.User.domain.User u ON u.id = s.usuario.id " +
                "WHERE a.jefe.id = :jefeId " +
                "AND (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:usuarioQuery IS NULL OR LOWER(CONCAT(u.firstname,' ',u.lastname)) LIKE LOWER(CONCAT('%', CAST(:usuarioQuery AS string), '%'))) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))",
        countQuery = "SELECT COUNT(s) FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.Areas.domain.Area a ON a.NombreArea = s.CentroCosto " +
                "LEFT JOIN com.example.demo.User.domain.User u ON u.id = s.usuario.id " +
                "WHERE a.jefe.id = :jefeId " +
                "AND (:prioridad IS NULL OR s.prioridad = :prioridad) " +
                "AND (:sp IS NULL OR s.sp = :sp) " +
                "AND (:estado IS NULL OR s.estado = :estado) " +
                "AND (:idQuery IS NULL OR CAST(s.id AS string) LIKE CONCAT('%', CAST(:idQuery AS string), '%')) " +
                "AND (:usuarioQuery IS NULL OR LOWER(CONCAT(u.firstname,' ',u.lastname)) LIKE LOWER(CONCAT('%', CAST(:usuarioQuery AS string), '%'))) " +
                "AND (:descripcionQuery IS NULL OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:descripcionQuery AS string), '%')))"
    )
    Page<Solicitudes> searchByJefe(
        @Param("jefeId") Integer jefeId,
        @Param("prioridad") Prioridad prioridad,
        @Param("sp") SP sp,
        @Param("estado") Estado estado,
        @Param("idQuery") String idQuery,
        @Param("usuarioQuery") String usuarioQuery,
        @Param("descripcionQuery") String descripcionQuery,
        Pageable pageable
    );

    // Compatibilidad: lo siguen usando MisSolicitudesPage (sin filtros) y la "actividad reciente".
    Page<Solicitudes> findByUsuario_Id(Integer usuarioId, Pageable pageable);

    @Query("SELECT s FROM Solicitudes s WHERE s.usuario.id = :uid ORDER BY " +
           "COALESCE(s.ocAssignedAt, s.approvedAt, s.createdAt) DESC")
    Page<Solicitudes> findRecentByUsuario(@Param("uid") Integer uid, Pageable pageable);

    @Query("SELECT s FROM Solicitudes s " +
           "LEFT JOIN com.example.demo.Areas.domain.Area a ON a.NombreArea = s.CentroCosto " +
           "WHERE a.jefe.id = :jefeId ORDER BY " +
           "COALESCE(s.approvedAt, s.createdAt) DESC")
    Page<Solicitudes> findRecentByJefe(@Param("jefeId") Integer jefeId, Pageable pageable);

    @Query("SELECT s FROM Solicitudes s WHERE s.estado = com.example.demo.Solicitudes.domain.Estado.Aprobado " +
           "ORDER BY COALESCE(s.ocAssignedAt, s.approvedAt) DESC")
    Page<Solicitudes> findRecentForCompras(Pageable pageable);

    // Mantengo findByJefeId por si algo lo consume; sirve de fallback ahora que existe searchByJefe.
    @Query(
        value = "SELECT s FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.Areas.domain.Area a ON a.NombreArea = s.CentroCosto " +
                "WHERE a.jefe.id = :jefeId",
        countQuery = "SELECT COUNT(s) FROM Solicitudes s " +
                "LEFT JOIN com.example.demo.Areas.domain.Area a ON a.NombreArea = s.CentroCosto " +
                "WHERE a.jefe.id = :jefeId"
    )
    Page<Solicitudes> findByJefeId(@Param("jefeId") Integer jefeId, Pageable pageable);
}
