package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "derivacion_catalogo",
        uniqueConstraints = @UniqueConstraint(name = "uk_derivacion_nombre", columnNames = "nombre"),
        indexes = {
                @Index(name = "idx_derivacion_lm", columnList = "last_modified"),
                @Index(name = "idx_derivacion_sd", columnList = "sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DerivacionConsultorio extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "nombre", nullable = false, length = 64)
    private String nombre;

    @Column(name = "activo", columnDefinition = "TINYINT(1)")
    private Boolean activo = true;
}
