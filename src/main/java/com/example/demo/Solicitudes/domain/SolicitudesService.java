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
import org.springframework.stereotype.Service;

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
        dto.setStatus(solicitud.getStatus());
        dto.setOrdenCompra(solicitud.getOrdenCompra());// esta linea no estaba y se agrego pq no se podia obtener desde la base de datos la informacion de orden de compra
        return dto;
    }
    public Solicitudes actualizarSolicitud(Integer id, SolicitudRequestDto dto) {
        Solicitudes solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (dto.getOrdenCompra() != null) solicitud.setOrdenCompra(dto.getOrdenCompra());
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
        if (dto.getEstado() != null) solicitud.setEstado(dto.getEstado());
        if (dto.getImageData() != null) solicitud.setImageData(dto.getImageData());
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
}