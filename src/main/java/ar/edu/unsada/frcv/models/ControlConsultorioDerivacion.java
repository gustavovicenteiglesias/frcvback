package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "control_consultorio_derivacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_cc_derivacion", columnNames = {"control_consultorio_id", "derivacion_id"}),
        indexes = {
                @Index(name = "idx_ccd_cc", columnList = "control_consultorio_id"),
                @Index(name = "idx_ccd_deriv", columnList = "derivacion_id"),
                @Index(name = "idx_ccd_lm", columnList = "last_modified"),
                @Index(name = "idx_ccd_sd", columnList = "sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlConsultorioDerivacion extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "control_consultorio_id", nullable = false)
    private ControlConsultorio controlConsultorio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "derivacion_id", nullable = false)
    private DerivacionConsultorio derivacion;

    @Column(name = "detalle")
    private String detalle;
}
