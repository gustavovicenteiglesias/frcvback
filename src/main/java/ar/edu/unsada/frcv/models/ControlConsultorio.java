package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "control_consultorio",
        uniqueConstraints = @UniqueConstraint(name="uk_cc_visita", columnNames = "visita_id"),
        indexes = {
                @Index(name="idx_cc_lm", columnList="last_modified"),
                @Index(name="idx_cc_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlConsultorio extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @OneToOne(optional = false)
    @JoinColumn(name = "visita_id", nullable = false, unique = true)
    private Visita visita;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    @Column(name="ta_sistolica")
    private Integer taSistolica;

    @Column(name="ta_diastolica")
    private Integer taDiastolica;

    @Column(name = "medicacion_json", columnDefinition = "json")
    private String medicacionJson;

    @Lob
    @Column(name="conducta")
    private String conducta;
}
