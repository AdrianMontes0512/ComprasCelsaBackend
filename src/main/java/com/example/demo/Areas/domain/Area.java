package com.example.demo.Areas.domain;

import com.example.demo.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Areas", uniqueConstraints = {@UniqueConstraint(columnNames = {"NombreArea", "jefe_id"})})
public class Area {
    @Id
    @Column(nullable = false)
    String NombreArea;


    @ManyToOne
    @JsonManagedReference
    private User jefe;

    @OneToMany(mappedBy = "area")
    private List<User> usuarios;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Area area = (Area) o;
        return java.util.Objects.equals(NombreArea, area.NombreArea);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(NombreArea);
    }
}
