package com.example.demo.Solicitudes.domain;

import com.example.demo.Areas.domain.Area;
import com.example.demo.Areas.infraestructure.AreaRepository;
import com.example.demo.Gmail.GmailService;
import com.example.demo.Solicitudes.infraestrucutre.SolicitudRepository;
import com.example.demo.Solicitudes.dto.SolicitudRequestDto;
import com.example.demo.Solicitudes.dto.SolicitudByIdResponseDto;
import com.example.demo.User.domain.User;
import com.example.demo.User.infraestructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SolicitudesService {

    private final SolicitudRepository solicitudRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final GmailService gmailService;


    public Solicitudes crearSolicitud(SolicitudRequestDto dto) {
        User usuario = userRepository.findById(dto.getUsuarioId().intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Solicitudes solicitud = Solicitudes.builder()
                .prioridad(dto.getPrioridad())
                .CentroCosto(dto.getCentrocosto())
                .sp(dto.getSp())
                .descripcion(dto.getDescripcion())
                .cantidad(dto.getCantidad())
                .Precio(dto.getPrecio())
                .umedida(dto.getUmedida())
                .moneda(dto.getMoneda())
                .estado(Estado.Pendiente)
                .usuario(usuario)
                .Motivo(dto.getMotivo())
                .Familia(dto.getFamilia())
                .SubFamilia(dto.getSubFamilia())
                .maquina(dto.getMaquina())
                .imageData(dto.getImageData())
                .imageMimeType(dto.getImageMimeType())
                .imageFilename(dto.getImageFilename())
                .createdAt(Instant.now())
                .build();
        solicitud = solicitudRepository.save(solicitud);

        Area areaDestino = determinarAreaDestino(solicitud);

        enviarNotificacionAUsuario(areaDestino.getJefe(), solicitud);

        return solicitud;
    }

    public SolicitudByIdResponseDto getSolicitudById(Integer id) {
        Solicitudes solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return mapToDto(solicitud);
    }

    public Page<SolicitudByIdResponseDto> getSolicitudesByUsuarioId(Integer usuarioId, Pageable pageable) {
        return solicitudRepository.findByUsuario_Id(usuarioId, pageable)
                .map(this::mapToDto);
    }

    public Page<SolicitudByIdResponseDto> getTodasLasSolicitudes(Pageable pageable) {
        return solicitudRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    private SolicitudByIdResponseDto mapToDto(Solicitudes solicitud) {
        SolicitudByIdResponseDto dto = new SolicitudByIdResponseDto();
        dto.setId(solicitud.getId());
        dto.setPrioridad(solicitud.getPrioridad());
        dto.setSp(solicitud.getSp());
        dto.setMaquina(String.valueOf(solicitud.getMaquina()));
        dto.setDescripcion(solicitud.getDescripcion());
        dto.setCantidad(solicitud.getCantidad());
        dto.setPrecio(solicitud.getPrecio());
        dto.setUmedida(solicitud.getUmedida());
        dto.setMoneda(solicitud.getMoneda());
        dto.setEstado(solicitud.getEstado());
        dto.setUsuarioId(solicitud.getUsuario().getId());
        dto.setMotivo(solicitud.getMotivo());
        dto.setFamilia(solicitud.getFamilia());
        dto.setSubFamilia(solicitud.getSubFamilia());
        dto.setComentarios(solicitud.getComentario());
        dto.setFecha(solicitud.getFecha());
        dto.setFechaOrden(solicitud.getFechaOrden());
        dto.setFechaAprobacion(solicitud.getFechaAprobacion());
        dto.setStatus(solicitud.getStatus());
        dto.setOrdenCompra(solicitud.getOrdenCompra());// esta linea no estaba y se agrego pq no se podia obtener desde la base de datos la informacion de orden de compra
        dto.setCreatedAt(solicitud.getCreatedAt());
        dto.setApprovedAt(solicitud.getApprovedAt());
        dto.setOcAssignedAt(solicitud.getOcAssignedAt());
        if (solicitud.getCreatedAt() != null && solicitud.getApprovedAt() != null) {
            dto.setTiempoAprobacionHoras(
                java.time.Duration.between(solicitud.getCreatedAt(), solicitud.getApprovedAt()).toHours()
            );
        }
        if (solicitud.getApprovedAt() != null && solicitud.getOcAssignedAt() != null) {
            dto.setTiempoOCHoras(
                java.time.Duration.between(solicitud.getApprovedAt(), solicitud.getOcAssignedAt()).toHours()
            );
        }
        return dto;
    }
    public Solicitudes actualizarSolicitud(Integer id, SolicitudRequestDto dto) {
        Solicitudes solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        String role = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
        if (dto.getEstado() != null) {
            if (!"JefeArea".equals(role) && !"ADMIN".equals(role)) {
                throw new org.springframework.security.access.AccessDeniedException(
                    "Solo JefeArea puede cambiar el estado de aprobación");
            }
        }
        if (dto.getOrdenCompra() != null && !dto.getOrdenCompra().isBlank()) {
            if (!"Compras".equals(role) && !"ADMIN".equals(role)) {
                throw new org.springframework.security.access.AccessDeniedException(
                    "Solo Compras puede asignar OC");
            }
        }

        if (dto.getOrdenCompra() != null && !dto.getOrdenCompra().isBlank()
                && !dto.getOrdenCompra().equals(solicitud.getOrdenCompra())) {
            solicitud.setOrdenCompra(dto.getOrdenCompra());
            if (solicitud.getOcAssignedAt() == null) {
                solicitud.setOcAssignedAt(Instant.now());
            }
        }
        if (dto.getMotivo() != null) solicitud.setMotivo(dto.getMotivo());
        if (dto.getFechaOrden() != null) solicitud.setFechaOrden(dto.getFechaOrden());
        if (dto.getStatus() != null) solicitud.setStatus(dto.getStatus());
        if (dto.getCentrocosto() != null) solicitud.setCentroCosto(dto.getCentrocosto());
        if (dto.getPrioridad() != null) solicitud.setPrioridad(dto.getPrioridad());
        if (dto.getSp() != null) solicitud.setSp(dto.getSp());
        if (dto.getDescripcion() != null) solicitud.setDescripcion(dto.getDescripcion());
        if (dto.getCantidad() != null) solicitud.setCantidad(dto.getCantidad());
        if (dto.getPrecio() != null) solicitud.setPrecio(dto.getPrecio());
        if (dto.getUmedida() != null) solicitud.setUmedida(dto.getUmedida());
        if (dto.getMoneda() != null) solicitud.setMoneda(dto.getMoneda());
        if (dto.getComentarios() != null) solicitud.setComentario(dto.getComentarios());
        if (dto.getFecha() != null) solicitud.setFecha(dto.getFecha());
        if (dto.getEstado() != null && dto.getEstado() != solicitud.getEstado()) {
            solicitud.setEstado(dto.getEstado());
            if (dto.getEstado() == Estado.Aprobado || dto.getEstado() == Estado.Rechazado) {
                Instant now = Instant.now();
                solicitud.setApprovedAt(now);
                solicitud.setFechaAprobacion(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
        if (dto.getImageData() != null) solicitud.setImageData(dto.getImageData());
        if (dto.getImageMimeType() != null) solicitud.setImageMimeType(dto.getImageMimeType());
        if (dto.getImageFilename() != null) solicitud.setImageFilename(dto.getImageFilename());
        if (dto.getFamilia() != null) solicitud.setFamilia(dto.getFamilia());
        if (dto.getSubFamilia() != null) solicitud.setSubFamilia(dto.getSubFamilia());
        if (dto.getMaquina() != null) solicitud.setMaquina(dto.getMaquina());
        if (dto.getUsuarioId() != null) {
            User usuario = userRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            solicitud.setUsuario(usuario);
        }

        return solicitudRepository.save(solicitud);
    }
    public Area determinarAreaDestino(Solicitudes solicitud) {
        // CentroCosto is the AreaId
        String areaId = solicitud.getCentroCosto();
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Área no encontrada para el Centro de Costo: " + areaId));
    }
    public void enviarNotificacionAUsuario(User jefeArea, Solicitudes solicitud) {
        String subject = "📝 Solicitud de compra requiere su aprobación";
        String body = String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #2c3e50;">Nueva Solicitud de Compra</h2>

            <p style="color: #34495e;">Estimad@ %s,</p>

            <p style="color: #34495e;">Se requiere su aprobación para la siguiente solicitud de compra:</p>

            <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                <p style="margin: 10px 0;"><strong>🔹 Solicitante:</strong> %s</p>
                <p style="margin: 10px 0;"><strong>🔹 Descripción:</strong> %s</p>
                <p style="margin: 10px 0;"><strong>🔹 Prioridad:</strong> %s 🔥</p>
                <p style="margin: 10px 0;"><strong>🔹 Cantidad:</strong> %s 🛒</p>
                <p style="margin: 10px 0;"><strong>🔹 Medición:</strong> %s 📏</p>
                <p style="margin: 10px 0;"><strong>🔹 Precio:</strong> %s 💵</p>
                <p style="margin: 10px 0;"><strong>🔹 Moneda:</strong> %s 💰</p>
            </div>

            <p style="color: #34495e;">Por favor, revisa y aprueba la solicitud lo antes posible.<br>
            Si tienes alguna pregunta, no dudes en contactarnos.</p>

            <p style="color: #34495e;">¡Gracias por tu atención y colaboración! 😊</p>

            <p style="color: #34495e;">Se adjunta imagen de referencia.</p>

            <hr style="border: 1px solid #eee; margin: 20px 0;">

            <p style="color: #7f8c8d;">Saludos cordiales,<br>
            <strong>Sistema de Compras</strong></p>
        </div>
        """,
                jefeArea.getFirstname(),
                solicitud.getUsuario().getFirstname() + " " + solicitud.getUsuario().getLastname(),
                solicitud.getDescripcion(),
                solicitud.getPrioridad(),
                solicitud.getCantidad(),
                solicitud.getUmedida(),
                solicitud.getPrecio(),
                solicitud.getMoneda()
        );

        try {
            gmailService.sendEmail(jefeArea.getUsername(), subject, body, solicitud.getImageData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SolicitudByIdResponseDto mapToDtoPublic(Solicitudes s) { return mapToDto(s); }

    public java.util.List<com.example.demo.Solicitudes.dto.ActividadRecienteDto> getActividadReciente(User user, int limit) {
        org.springframework.data.domain.PageRequest pr = org.springframework.data.domain.PageRequest.of(0, limit);
        Page<Solicitudes> page;
        switch (user.getRole().name()) {
            case "JefeArea" -> page = solicitudRepository.findRecentByJefe(user.getId(), pr);
            case "Compras", "ADMIN" -> page = solicitudRepository.findRecentForCompras(pr);
            default -> page = solicitudRepository.findRecentByUsuario(user.getId(), pr);
        }
        java.util.List<com.example.demo.Solicitudes.dto.ActividadRecienteDto> out = new java.util.ArrayList<>();
        for (Solicitudes s : page.getContent()) {
            String tipo;
            java.time.Instant when;
            if (s.getOcAssignedAt() != null) { tipo = "oc_asignada"; when = s.getOcAssignedAt(); }
            else if (s.getApprovedAt() != null && s.getEstado() == Estado.Aprobado) { tipo = "aprobada"; when = s.getApprovedAt(); }
            else if (s.getApprovedAt() != null && s.getEstado() == Estado.Rechazado) { tipo = "rechazada"; when = s.getApprovedAt(); }
            else { tipo = "creada"; when = s.getCreatedAt(); }
            String actor = s.getUsuario().getFirstname() + " " + s.getUsuario().getLastname();
            out.add(new com.example.demo.Solicitudes.dto.ActividadRecienteDto(
                tipo, s.getId(), s.getDescripcion(), actor, String.valueOf(s.getEstado()),
                String.valueOf(s.getPrioridad()), when));
        }
        return out;
    }

    public Page<SolicitudByIdResponseDto> searchSolicitudesByUsuario(
            Integer usuarioId,
            com.example.demo.Solicitudes.domain.Prioridad prioridad,
            com.example.demo.Solicitudes.domain.SP sp,
            com.example.demo.Solicitudes.domain.Estado estado,
            String idQuery,
            String descripcionQuery,
            Pageable pageable) {
        return solicitudRepository.searchByUsuario(
                usuarioId, prioridad, sp, estado,
                blankToNull(idQuery), blankToNull(descripcionQuery),
                pageable
        ).map(this::mapToDto);
    }

    public Page<SolicitudByIdResponseDto> searchSolicitudes(
            com.example.demo.Solicitudes.domain.Prioridad prioridad,
            com.example.demo.Solicitudes.domain.SP sp,
            com.example.demo.Solicitudes.domain.Estado estado,
            String idQuery,
            String usuarioQuery,
            String descripcionQuery,
            Pageable pageable) {
        return solicitudRepository.searchAll(
                prioridad, sp, estado,
                blankToNull(idQuery), blankToNull(usuarioQuery), blankToNull(descripcionQuery),
                pageable
        ).map(this::mapToDto);
    }

    public Page<SolicitudByIdResponseDto> searchSolicitudesByJefe(
            Integer jefeId,
            com.example.demo.Solicitudes.domain.Prioridad prioridad,
            com.example.demo.Solicitudes.domain.SP sp,
            com.example.demo.Solicitudes.domain.Estado estado,
            String idQuery,
            String usuarioQuery,
            String descripcionQuery,
            Pageable pageable) {
        return solicitudRepository.searchByJefe(
                jefeId, prioridad, sp, estado,
                blankToNull(idQuery), blankToNull(usuarioQuery), blankToNull(descripcionQuery),
                pageable
        ).map(this::mapToDto);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}