package com.example.demo.Areas;

import com.example.demo.Areas.AreaRequestDto;
import com.example.demo.User.User;
import com.example.demo.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final UserRepository userRepository;

    @Transactional
    public Area createArea(AreaRequestDto dto) {
        Optional<User> jefeOpt = userRepository.findById(dto.getJefeId());
        if (jefeOpt.isEmpty()) {
            throw new IllegalArgumentException("Jefe no encontrado con ID: " + dto.getJefeId());
        }

        User jefe = jefeOpt.get();

        Area area = Area.builder()
                .NombreArea(dto.getNombreArea())
                .jefe(jefe)
                .build();

        Area savedArea = areaRepository.save(area);

        jefe.setArea(savedArea);
        userRepository.save(jefe);

        return savedArea;
    }

    @Transactional
    public Area updateArea(String nombreArea, AreaRequestDto dto) {
        Area area = areaRepository.findById(nombreArea)
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada: " + nombreArea));

        Optional<User> jefeOpt = userRepository.findById(dto.getJefeId().intValue());
        if (jefeOpt.isEmpty()) {
            throw new IllegalArgumentException("Jefe no encontrado con ID: " + dto.getJefeId());
        }

        area.setJefe(jefeOpt.get());
        return areaRepository.save(area);
    }
}
