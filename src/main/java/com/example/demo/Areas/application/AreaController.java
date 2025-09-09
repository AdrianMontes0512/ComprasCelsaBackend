package com.example.demo.Areas.application;

import com.example.demo.Areas.dto.AreaRequestDto;
import com.example.demo.Areas.domain.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @PostMapping
    public ResponseEntity<?> createArea(@RequestBody AreaRequestDto dto) {
        return ResponseEntity.ok(areaService.createArea(dto));
    }

    @PutMapping("/{nombreArea}")
    public ResponseEntity<?> updateArea(@PathVariable String nombreArea,
                                        @RequestBody AreaRequestDto dto) {
        return ResponseEntity.ok(areaService.updateArea(nombreArea, dto));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAreasNombre() {
        return ResponseEntity.ok(areaService.getAllAreasNombre());
    }
}
