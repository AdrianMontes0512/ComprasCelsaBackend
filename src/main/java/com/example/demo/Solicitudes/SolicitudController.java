package com.example.demo.Solicitudes;

import com.example.demo.Areas.Area;
import com.example.demo.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "http://localhost:5173") // or "*" for all origins (not recommended for production)
@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudesService solicitudesService;
    private final SolicitudRepository solicitudRepository;

    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudRequestDto dto) {
        Solicitudes solicitud = solicitudesService.crearSolicitud(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerSolicitud(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudesService.getSolicitudById(id));
    }
    @GetMapping
    public ResponseEntity<Page<SolicitudByIdResponseDto>> obtenerTodasLasSolicitudes(
            @PageableDefault(size = 14) Pageable pageable) {
        return ResponseEntity.ok(solicitudesService.getTodasLasSolicitudes(pageable));
    }
    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> descargarImagen(@PathVariable Integer id) {
        Solicitudes solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        byte[] imageData = solicitud.getImageData();
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/png") // Change if needed
                .header("Content-Disposition", "attachment; filename=\"imagen.png\"")
                .body(imageData);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarSolicitud(@PathVariable Integer id, @RequestBody SolicitudRequestDto dto) {
        return ResponseEntity.ok(solicitudesService.actualizarSolicitud(id, dto));
    }
    @GetMapping("/jefe")
    public ResponseEntity<Page<SolicitudByIdResponseDto>> obtenerSolicitudesJefe(
            @AuthenticationPrincipal User jefe,
            @PageableDefault(size = 14) Pageable pageable) {
        Page<Solicitudes> todasLasSolicitudes = solicitudRepository.findAll(pageable);
        List<SolicitudByIdResponseDto> solicitudesFiltradas = new ArrayList<>();

        for (Solicitudes solicitud : todasLasSolicitudes.getContent()) {
            Double montoEnSoles = solicitudesService.convertirAMontoEnSoles(solicitud.getPrecio(), solicitud.getMoneda());
            Area areaDestino = solicitudesService.determinarAreaDestino(montoEnSoles, solicitud.getUsuario());

            if (areaDestino.equals(jefe.getArea())) {
                solicitudesFiltradas.add(solicitudesService.getSolicitudById(solicitud.getId()));
            }
        }
        Page<SolicitudByIdResponseDto> solicitudesPaginadas = new PageImpl<>(
                solicitudesFiltradas, pageable, todasLasSolicitudes.getTotalElements());
        return ResponseEntity.ok(solicitudesPaginadas);
    }

}
