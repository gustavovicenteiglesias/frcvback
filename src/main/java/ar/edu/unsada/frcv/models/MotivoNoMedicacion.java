package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "motivo_no_medicacion",
        indexes = {
                @Index(name = "idx_mnm_lm", columnList = "last_modified"),
                @Index(name = "idx_mnm_sd", columnList = "sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MotivoNoMedicacion extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 64)
    private Motivo motivo;

    @Column(name = "detalle")
    private String detalle;

    public enum Motivo {
        CONTRAINDICACION_FORMULACION,
        HTA_SEVERA,
        SIGUE_CON_SU_MEDICO,
        OTRO
    }
}
