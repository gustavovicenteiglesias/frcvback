package ar.edu.unsada.frcv.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="roles")
@Getter
@Setter
public class RoleEntity {
    @Id
    @Column(name="id", length = 36)   // ahora Hibernate espera VARCHAR(36)
    private String id;

    @Column(unique = true, nullable = false)
    private String code; // ADMIN, MEDICO, ENFERMERO

    private String descripcion;
    private Long lastModified;
    private Boolean sqlDeleted = false;
    // getters/setters
}
