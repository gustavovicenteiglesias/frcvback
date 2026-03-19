package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "control_consultorio_evento",
        uniqueConstraints = @UniqueConstraint(name = "uk_cc_evento", columnNames = {"control_consultorio_id", "evento_id"}),
        indexes = {
                @Index(name = "idx_cce_cc", columnList = "control_consultorio_id"),
                @Index(name = "idx_cce_evento", columnList = "evento_id"),
                @Index(name = "idx_cce_lm", columnList = "last_modified"),
                @Index(name = "idx_cce_sd", columnList = "sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlConsultorioEvento extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "control_consultorio_id", nullable = false)
    private ControlConsultorio controlConsultorio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoConsultorio evento;

    // Campo libre para “otro” u observación puntual
    @Column(name = "detalle")
    private String detalle;
}
