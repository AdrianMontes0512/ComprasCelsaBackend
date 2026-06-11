package com.example.demo.Solicitudes.application;

import com.example.demo.Solicitudes.infraestrucutre.SolicitudRepository;
import com.example.demo.Solicitudes.dto.SolicitudRequestDto;
import com.example.demo.Solicitudes.domain.Solicitudes;
import com.example.demo.Solicitudes.domain.SolicitudesService;
import com.example.demo.Solicitudes.dto.SolicitudByIdResponseDto;
import com.example.demo.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudesService solicitudesService;
    private final SolicitudRepository solicitudRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('Empleado','TMLIMA')")
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudRequestDto dto) {
        Solicitudes solicitud = solicitudesService.crearSolicitud(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<SolicitudByIdResponseDto>> getSolicitudesByUsuarioId(
            @PathVariable Integer usuarioId,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Prioridad prioridad,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.SP sp,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Estado estado,
            @RequestParam(required = false) String idQuery,
            @RequestParam(required = false) String descripcionQuery,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(solicitudesService.searchSolicitudesByUsuario(
                usuarioId, prioridad, sp, estado, idQuery, descripcionQuery, pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getSolicitudById(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudesService.getSolicitudById(id));
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('Compras','ADMIN')")
    public ResponseEntity<Page<SolicitudByIdResponseDto>> obtenerTodasLasSolicitudes(
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Prioridad prioridad,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.SP sp,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Estado estado,
            @RequestParam(required = false) String idQuery,
            @RequestParam(required = false) String usuarioQuery,
            @RequestParam(required = false) String descripcionQuery,
            @PageableDefault(size = 14, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(solicitudesService.searchSolicitudes(
                prioridad, sp, estado, idQuery, usuarioQuery, descripcionQuery, pageable));
    }
    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> descargarImagen(@PathVariable Integer id) {
        Solicitudes solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        byte[] imageData = solicitud.getImageData();
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }

        String mime = solicitud.getImageMimeType();
        if (mime == null || mime.isBlank()) mime = "application/octet-stream";
        String filename = solicitud.getImageFilename();
        if (filename == null || filename.isBlank()) filename = "adjunto-" + id;
        // sanitize filename (avoid quotes / line breaks injection in header)
        filename = filename.replaceAll("[\\r\\n\"]", "_");

        return ResponseEntity.ok()
                .header("Content-Type", mime)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(imageData);
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('JefeArea','Compras','ADMIN')")
    public ResponseEntity<?> actualizarSolicitud(@PathVariable Integer id, @RequestBody SolicitudRequestDto dto) {
        return ResponseEntity.ok(solicitudesService.actualizarSolicitud(id, dto));
    }
    @GetMapping("/jefe")
    @PreAuthorize("hasAuthority('JefeArea')")
    public ResponseEntity<Page<SolicitudByIdResponseDto>> obtenerSolicitudesJefe(
            @AuthenticationPrincipal User jefe,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Prioridad prioridad,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.SP sp,
            @RequestParam(required = false) com.example.demo.Solicitudes.domain.Estado estado,
            @RequestParam(required = false) String idQuery,
            @RequestParam(required = false) String usuarioQuery,
            @RequestParam(required = false) String descripcionQuery,
            @PageableDefault(size = 14, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(solicitudesService.searchSolicitudesByJefe(
                jefe.getId(), prioridad, sp, estado, idQuery, usuarioQuery, descripcionQuery, pageable));
    }

    @GetMapping("/actividad-reciente")
    public ResponseEntity<java.util.List<com.example.demo.Solicitudes.dto.ActividadRecienteDto>> actividadReciente(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(solicitudesService.getActividadReciente(user, Math.min(limit, 50)));
    }

}
