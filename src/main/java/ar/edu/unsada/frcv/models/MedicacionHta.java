// src/main/java/ar/edu/unsada/frcv/models/MedicacionHta.java
package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicacion_hta",
        indexes = {
                @Index(name="idx_medhta_nombre", columnList="nombre"),
                @Index(name="idx_medhta_lm", columnList="last_modified"),
                @Index(name="idx_medhta_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicacionHta {
    @Id
    @Column(name="id", length = 36)
    private String id;

    @Column(name="nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name="last_modified", nullable = false)
    private Long lastModified; // en segundos

    @Column(name="sql_deleted", nullable = false)
    private Boolean sqlDeleted;

    @PrePersist @PreUpdate
    public void touch() {
        lastModified = System.currentTimeMillis()/1000L;
        if (sqlDeleted == null) sqlDeleted = false;
    }
}
